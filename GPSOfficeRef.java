import java.rmi.Remote;
import java.rmi.RemoteException;
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;

/**
 * @author Ganesh Chandrasekaran
 * @version 04-20-2013
 */

/**
 * Interface GPSOfficeRef specifies the Java RMI remote interface for a
 * distributed node object in the GPSOffice system.
 */
public interface GPSOfficeRef extends Remote {
	/**
	 * Call this method to access the X coordinate of a GPSOffice object
	 * 
	 * @return X-coordinate of the GPSOffice node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double getX() throws RemoteException;

	/**
	 * Call this method to access the Y coordinate of a GPSOffice object
	 * 
	 * @return Y-coordinate of the GPSOffice node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public double getY() throws RemoteException;

	/**
	 * Call this method to access the name of a GPSOffice object
	 * 
	 * @return Name of the GPSOffice object if it exists
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public String getOfficeName() throws RemoteException;

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
	public double calculateDistance(GPSOfficeRef obj) throws RemoteException;

	/**
	 * Call this method to update the neighbor list of a node
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred
	 */
	public void updateNeighbors() throws RemoteException;

	/**
	 * This method is called by thread pools to route the package to the next
	 * nearest neighbor node in the GPSOffice system
	 * 
	 * @param n
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
	public void route(long n, double x, double y,
			RemoteEventListener<TrackPackage> nodeListener)
			throws RemoteException;

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
			throws RemoteException;

	/**
	 * This methods initiates the routing of package towards its destination
	 * 
	 * @param n
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
	public void sendPackage(long n, double x, double y,
			RemoteEventListener<TrackPackage> nodeListener)
			throws RemoteException;

	/**
	 * Add the given remote event listener to this node. Whenever a package
	 * arrives or departs from this node, this node will report a TrackPackage
	 * event to the given listener.
	 * 
	 * @param listener
	 *            Remote event listener.
	 */
	public Lease addListener(RemoteEventListener<TrackPackage> listener)
			throws RemoteException;

	/**
	 * Assigns a tracking number to a new package that arrives at the
	 * originating node
	 * 
	 * @return Package number to track the package
	 * 
	 */

	public long assignPackageNumber() throws RemoteException;

}
