import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.*;
import java.util.*;

/**
 * @author Ganesh Chandrasekaran
 * @version 04-20-2013
 */

/**
 * The class GPSOffice creates distributed nodes which represent a GPSOffice and
 * allows routing of packages from source to destination
 * 
 * Usage: java Start GPSOffice <I>host</I> <I>port</I> <I>officeName</I>
 * <I>X-coordinate</I> <I>Y-coordinate</I><BR>
 * 
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * <I>officeName</I> = name of this node itself <BR>
 * <I>X-coordinate</I> = X-coordinate of the GPSOffice <BR>
 * <I>Y-coordinate</I> = Y-coordinate of the GPSOffice <BR>
 * 
 */
public class GPSOffice implements GPSOfficeRef {
	private String hostName;
	private int portNumber;
	private String officeName;
	private double X;
	private double Y;

	private RegistryProxy registry;

	HashMap<String, Double> tempDistance;

	HashMap<String, Double[]> neighborsTable;

	private ScheduledExecutorService threadPool;

	private RemoteEventGenerator<TrackPackage> eventGenerator;

	/**
	 * Call this method to access the X coordinate of a GPSOffice object
	 * 
	 * @return X-coordinate of the GPSOffice node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double getX() throws RemoteException {
		return X;
	}

	/**
	 * Call this method to access the Y coordinate of a GPSOffice object
	 * 
	 * @return Y-coordinate of the GPSOffice node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double getY() throws RemoteException {
		return Y;
	}

	/**
	 * Call this method to access the name of a GPSOffice object
	 * 
	 * @return Name of the GPSOffice object if it exists
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public String getOfficeName() throws RemoteException {

		return officeName;
	}

	/**
	 * Construct a new GPSOffice object.
	 * <P>
	 * The command line arguments are: <BR>
	 * <TT>args[0]</TT> = Registry Server's host <BR>
	 * <TT>args[1]</TT> = Registry Server's port <BR>
	 * <TT>args[2]</TT> = name of this node itself <BR>
	 * <TT>args[3]</TT> = X-coordinate of this node <BR>
	 * <TT>args[4]</TT> = Y-coordinate of this node
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 * @exception IOException
	 *                Thrown if an I/O error or a remote error occurred
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if there was a problem with
	 *                the command line arguments
	 * 
	 */
	public GPSOffice(String[] args) throws IOException, NotBoundException {
		// Parse command line arguments
		if (args.length != 5) {
			throw new IllegalArgumentException(
					"Usage: java Start GPSOffice <hostName> <portNumber> <officeName> <X> <Y>");
		}
		hostName = args[0];
		portNumber = parseInt(args[1], "portNumber");
		officeName = args[2];
		X = parseDouble(args[3], "X-Coordinate");
		Y = parseDouble(args[4], "Y-Coordinate");

		// Get a proxy for the Registry Server.
		registry = new RegistryProxy(hostName, portNumber);

		threadPool = Executors.newSingleThreadScheduledExecutor();

		// Prepare to generate remote events.
		eventGenerator = new RemoteEventGenerator<TrackPackage>();

		// Export this GPSOffice node.
		UnicastRemoteObject.exportObject(this, 0);

		// Bind this GPSOffice node into the Registry Server.
		try {
			registry.bind(officeName, this);

			// Fetches the list of nodes bound in the registry and updates the
			// neighbor of each GPSOffice.
			List<String> listOfOffices1 = registry.list();
			Iterator<String> iter1 = listOfOffices1.iterator();

			while (iter1.hasNext()) {

				GPSOfficeRef neighbor1 = (GPSOfficeRef) registry.lookup(iter1
						.next());
				neighbor1.updateNeighbors();
			}

		} catch (AlreadyBoundException e) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException e1) {

			}
			throw new IllegalArgumentException("GPSOffice(): <officeName> = "
					+ officeName + " already exists");
		} catch (RemoteException e) {

			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException e1) {

			}
			throw e;
		}
	}

	/**
	 * Call this method to calculate Euclidean distance between the object that
	 * invokes this method and the argument passed in the method
	 * 
	 * @param obj
	 *            GPSOfficeRef
	 * 
	 * @return Distance between the 2 GPSOffice nodes
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double calculateDistance(GPSOfficeRef obj) throws RemoteException {
		if (this.getOfficeName() == obj.getOfficeName())
			return 0;
		else {
			double x = obj.getX();
			double y = obj.getY();
			// Calculates the Euclidean distance.
			double distance = Math.sqrt(((this.X - x) * (this.X - x))
					+ ((this.Y - y) * (this.Y - y)));

			return distance;
		}
	}

	/**
	 * Call this method to update the neighbor list of a node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public void updateNeighbors() throws RemoteException {

		neighborsTable = new HashMap<String, Double[]>();

		double distValue;

		List<String> listOfOffices = registry.list();
		try {
			Double maxDistance = 0.0;
			Object removeNodeName = null;

			Iterator<String> iter = listOfOffices.iterator();

			while (iter.hasNext()) {
				GPSOfficeRef neighbor = (GPSOfficeRef) registry.lookup(iter
						.next());

				distValue = this.calculateDistance(neighbor);

				// Adds the first 3 the neighbors.
				if ((distValue != 0) && (neighborsTable.size() < 3)) {

					Double[] tempXY = new Double[3];
					tempXY[0] = neighbor.getX();
					tempXY[1] = neighbor.getY();
					tempXY[2] = distValue;

					neighborsTable.put(neighbor.getOfficeName(), tempXY);

				}
				// Removes the neighbor that has maximum distance
				else if ((distValue != 0) && (neighborsTable.size() == 3)) {

					maxDistance = 0.0;
					Set<String> nodes = neighborsTable.keySet();

					Iterator<String> nodeIterator = nodes.iterator();

					while (nodeIterator.hasNext()) {

						Object nodeName = nodeIterator.next();

						Double[] nodeValues = neighborsTable.get(nodeName);
						if (nodeValues[2] > maxDistance) {
							maxDistance = nodeValues[2];
							removeNodeName = nodeName;
						}
					}
					// Replaces the neighbor node having maximum distance
					if (distValue < maxDistance) {

						Double[] tempXY = new Double[3];
						tempXY[0] = neighbor.getX();
						tempXY[1] = neighbor.getY();
						tempXY[2] = distValue;

						neighborsTable.put(neighbor.getOfficeName(), tempXY);
						neighborsTable.remove(removeNodeName);
					}
				}
			}
		
		} catch (NotBoundException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method is called by a GPSOffice node to calculate the distance
	 * between itself and the destination
	 * 
	 * @param destX1
	 *            double
	 * 
	 * @param destY1
	 *            double
	 * 
	 * @return The distance between the current node and the destination
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double destDistance(double destX1, double destY1)
			throws RemoteException {
		double x = this.getX();
		double y = this.getY();
		String myName = this.getOfficeName();

		double distance = Math.sqrt(((destX1 - x) * (destX1 - x))
				+ ((destY1 - y) * (destY1 - y)));

		return distance;
	}

	/**
	 * This method is called by thread pools to route the package to the next
	 * nearest neighbor node in the GPSOffice system
	 * 
	 * @param packageNum
	 *            long
	 * 
	 * @param destX
	 *            double
	 * 
	 * @param destX
	 *            double
	 * 
	 * @param nodeListener
	 *            RemoteEventListener<TrackPackage>
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public void route(long packageNum, double destX, double destY,
			RemoteEventListener<TrackPackage> nodeListener)
			throws RemoteException {

		double nearestXY = 0.0;
		String tempString;
		GPSOfficeRef nodeTemp = null;
		GPSOfficeRef nodeTempNearest;
		String nearestNeighbor = null;
		String finalNearestNeighbour = null;

		final double destinationX = destX;
		final double destinationY = destY;
		final long currentPackageNumber = packageNum;
		final String currentOfficeName = this.getOfficeName();
		// Report a TrackPackage to Headquarters remote event listeners.
		eventGenerator.reportEvent(new TrackPackage("Package number "
				+ packageNum + " arrived at " + this.getOfficeName()
				+ " office", packageNum, 0));
		// Report a TrackPackage event to Customer remote event listeners.
		nodeListener.report(0, new TrackPackage("Package number "
				+ currentPackageNumber + " arrived at " + this.getOfficeName()
				+ " office", currentPackageNumber, 0));

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e2) {

		}

		double dist1 = this.destDistance(destX, destY);
		nearestXY = dist1;
		Set<String> myNeighbors = this.neighborsTable.keySet();

		Iterator<String> myNeighborsIterator = myNeighbors.iterator();

		// Finds the neighbor that is closest to the destination
		while (myNeighborsIterator.hasNext()) {
			tempString = (String) myNeighborsIterator.next();

			try {
				nodeTemp = (GPSOfficeRef) registry.lookup(tempString);
			} catch (NotBoundException e) {
			}

			double newDist = nodeTemp.destDistance(destX, destY);

			if (nearestXY > newDist) {
				nearestXY = newDist;
				nodeTempNearest = nodeTemp;
				nearestNeighbor = nodeTempNearest.getOfficeName();

			}
			finalNearestNeighbour = nearestNeighbor;
		}

		if (nearestXY == dist1) {

			// Report a TrackPackage to Headquarters remote event listeners.
			eventGenerator
					.reportEvent(new TrackPackage("Package number "
							+ packageNum + " delivered from "
							+ this.getOfficeName() + " office to (" + destX
							+ "," + destY + ")", packageNum, 2));
			// Report a TrackPackage event to Customer remote event listeners.
			try {
				nodeListener.report(0,
						new TrackPackage("Package number "
								+ currentPackageNumber + " delivered from "
								+ this.getOfficeName() + " office to (" + destX
								+ "," + destY + ")", currentPackageNumber, 2));
			} catch (Exception e) {

			}

		} else {

			// Report a TrackPackage to Headquarters remote event listeners.
			eventGenerator.reportEvent(new TrackPackage("Package number "
					+ packageNum + " departed from " + this.getOfficeName()
					+ " office", packageNum, 1));
			// Report a TrackPackage event to Customer remote event listeners.
			nodeListener.report(0,
					new TrackPackage("Package number " + currentPackageNumber
							+ " departed from " + this.getOfficeName()
							+ " office", currentPackageNumber, 1));

			try {
				final GPSOfficeRef nextNode = (GPSOfficeRef) registry
						.lookup(finalNearestNeighbour);
				final String nextNodeName = nextNode.getOfficeName();
				final RemoteEventListener<TrackPackage> nodeListener1 = nodeListener;

				// Assigns the task of forwarding the package to a new thread
				threadPool.execute(new Runnable() {
					public void run() {

						// Forwards the package to nearest neighbor node or
						// destination whichever is closest
						try {
							nextNode.route(currentPackageNumber, destinationX,
									destinationY, nodeListener1);
						} catch (RemoteException e) {

							try {

								// Report a TrackPackage to Headquarters remote
								// event listeners.
								eventGenerator.reportEvent(new TrackPackage(
										"Package number "
												+ currentPackageNumber
												+ " departed from "
												+ nextNodeName + " office",
										currentPackageNumber, 1));
								// Report a TrackPackage event to Customer
								// remote event listeners.
								nodeListener1.report(0, new TrackPackage(
										"Package number "
												+ currentPackageNumber
												+ " departed from "
												+ nextNodeName + " office",
										currentPackageNumber, 1));

								// Report a TrackPackage to Headquarters remote
								// event listeners.
								eventGenerator.reportEvent(new TrackPackage(
										"Package number "
												+ currentPackageNumber
												+ " lost by " + nextNodeName
												+ " office",
										currentPackageNumber, 2));
								try {
									// Report a TrackPackage event to Customer
									// remote event listeners.
									nodeListener1.report(0, new TrackPackage(
											"Package number "
													+ currentPackageNumber
													+ " lost by "
													+ nextNodeName + " office",
											currentPackageNumber, 2));
								} catch (Exception e4) {

									try {
										Thread.sleep(45000);
									} catch (InterruptedException e1) {
									}

									List<String> listOfOffices1 = registry
											.list();
									Iterator<String> iter1 = listOfOffices1
											.iterator();

									while (iter1.hasNext()) {
								
										GPSOfficeRef neighbor1;
										try {
											neighbor1 = (GPSOfficeRef) registry
													.lookup(iter1.next());
											neighbor1.updateNeighbors();
										} catch (NotBoundException e1) {
										}
									}
								}

							} catch (RemoteException e1) {
							}
						}
					}
				});
			} catch (NotBoundException e1) {

			}
		}
	}

	/**
	 * Assigns a tracking number to a new package that arrives at the
	 * originating node
	 * 
	 * @return Package number to track the package
	 * 
	 */
	public long assignPackageNumber() throws RemoteException {
		long packageNumber = System.currentTimeMillis();
		return packageNumber;
	}

	/**
	 * This methods initiates the routing of package towards its destination
	 * 
	 * @param packNumber
	 *            long
	 * 
	 * @param x
	 *            double
	 * 
	 * @param y
	 *            double
	 * 
	 * @param nodeListener
	 *            RemoteEventListener<TrackPackage>
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public void sendPackage(long packNumber, double x, double y,
			RemoteEventListener<TrackPackage> nodeListener)
			throws RemoteException {

		final long getPackNum = packNumber;
		try {

			this.route(getPackNum, x, y, nodeListener);
		} catch (Exception e) {
			// Report a TrackPackage to Headquarters remote event listeners.
			eventGenerator.reportEvent(new TrackPackage("Package number "
					+ packNumber + " departed from " + this.getOfficeName()
					+ " office", packNumber, 1));
			// Report a TrackPackage event to Customer remote event listeners.
			nodeListener.report(0, new TrackPackage("Package number "
					+ packNumber + " departed from " + this.getOfficeName()
					+ " office", packNumber, 1));

			try {
				Thread.sleep(100);
			} catch (InterruptedException e2) {
			}

			// Report a TrackPackage to any remote event listeners.
			eventGenerator.reportEvent(new TrackPackage("Package number "
					+ packNumber + " lost by " + this.getOfficeName()
					+ " office", packNumber, 2));
			try {
				// Report a TrackPackage event to Customer remote event
				// listeners.
				nodeListener.report(0, new TrackPackage("Package number "
						+ packNumber + " lost by " + this.getOfficeName()
						+ " office", packNumber, 2));
			} catch (Exception e3) {
				try {
					Thread.sleep(45000);
				} catch (InterruptedException e1) {
				}

				List<String> listOfOffices1 = registry.list();
				Iterator<String> iter1 = listOfOffices1.iterator();

				while (iter1.hasNext()) {
			
					GPSOfficeRef neighbor1;
					try {
						neighbor1 = (GPSOfficeRef) registry
								.lookup(iter1.next());
						neighbor1.updateNeighbors();
					} catch (NotBoundException e1) {
					}
				}
			}

		}
	}

	/**
	 * Add the given remote event listener to this node. Whenever a query is
	 * forwarded to this node, this node will report a NodeEvent to the given
	 * listener.
	 * 
	 * @param listener
	 *            Remote event listener.
	 */
	public Lease addListener(RemoteEventListener<TrackPackage> listener)
			throws RemoteException {
		return eventGenerator.addListener(listener);
	}

	/**
	 * Parse an integer command line argument.
	 * 
	 * @param arg
	 *            Command line argument.
	 * @param name
	 *            Argument name.
	 * 
	 * @return Integer value of <TT>arg</TT>.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>arg</TT> cannot be
	 *                parsed as an integer.
	 */
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			System.err.printf("GPSOffice: Invalid <%s>: \"%s\"", name, arg);
			return 0;
		}
	}

	/**
	 * Parse a double command line argument.
	 * 
	 * @param arg
	 *            Command line argument.
	 * @param name
	 *            Argument name.
	 * 
	 * @return Double value of <TT>arg</TT>.
	 * 
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>arg</TT> cannot be
	 *                parsed as an integer.
	 */
	private static double parseDouble(String arg, String name) {
		try {
			return Double.parseDouble(arg);
		} catch (NumberFormatException exc) {
			System.err.printf("GPSOffice: Invalid <%s>: \"%s\"", name, arg);
			return 0;
		}
	}

}
