import edu.rit.ds.registry.RegistryProxy;
import java.io.Serializable;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Ganesh Chandrasekaran
 * @version 04-20-2013
 */

/**
 * Class Customer encapsulates a Customer in the GPS system. A customer goes to
 * a GPS office with a package to be sent to a certain (X,Y) destination. The
 * GPS office assigns a tracking number to the package. The package then
 * commences its journey through the GPS system.
 * 
 * Usage: java Customer <I>host</I> <I>port</I> <I>originNode</I>
 * <I>X</I><I>Y</I> <BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * <I>originNode</I> = name of originating node <BR>
 * <TT>X</TT> = X-coordinate of this node <BR>
 * <TT>Y</TT> = Y-coordinate of this node
 */
public class Customer implements Serializable {

	private static RegistryProxy registry;
	private long customerPackageNumber;
	private double xCord;
	private double yCord;
	private static RemoteEventListener<TrackPackage> nodeListener;

	/**
	 * The Customer consturctor is used to initialize each customer objects
	 * package number, X and Y coordinates
	 * 
	 * @param n
	 *            long
	 * @param x
	 *            double
	 * @param y
	 *            double
	 */
	public Customer(long n, double x, double y) {
		this.customerPackageNumber = n;
		this.xCord = x;
		this.yCord = y;
	}

	/**
	 * Customer main program.
	 */
	public static void main(String[] args) throws Exception {
		// Parse command line arguments.
		if (args.length != 5)
			usage();
		String host = args[0];
		int port = parseInt(args[1], "port");
		String originNode = args[2];
		double x = parseDouble(args[3], "X - coordinate");
		double y = parseDouble(args[4], "Y - coordinate");
		long myPackageNumber = 0;
		GPSOfficeRef node = null;
		registry = new RegistryProxy(host, port);
		// Look up GPSOffice node name in the Registry Server
		try {
			node = (GPSOfficeRef) registry.lookup(originNode);
		} catch (NotBoundException e) {
			System.out.println("The GPSOffice named " + originNode
					+ " does not exist!");
			System.exit(1);
		}
		// Assign tracking number to the package
		myPackageNumber = node.assignPackageNumber();

		final Customer customer = new Customer(myPackageNumber, x, y);

		// Export a remote event listener object for receiving notifications
		// from GPSOffice objects.
		nodeListener = new RemoteEventListener<TrackPackage>() {
			public void report(long seqnum, TrackPackage event) {
				if (event.status == 2) {
					System.out.println(event.message);
					System.exit(0);
				}
				if (event.status == 1)
					System.out.println(event.message);
				if (event.status == 0)
					System.out.println(event.message);

			}
		};
		UnicastRemoteObject.exportObject(nodeListener, 0);

		// Sends the package to the originating GPSOffice node
		node.sendPackage(myPackageNumber, x, y, nodeListener);
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err
				.println("Usage: java Customer <host> <port> <originNode> <X> <Y>");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
		System.err.println("<originNode> = Name of originating node");
		System.err.println("<X> = X cordinate of destination");
		System.err.println("<Y> = Y cordinate of destination");
		System.exit(1);
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
			System.err.printf("Customer: Invalid <%s>: \"%s\"", name, arg);
			usage();
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
			System.err.printf("Customer: Invalid <%s>: \"%s\"", name, arg);
			usage();
			return 0;
		}
	}
}