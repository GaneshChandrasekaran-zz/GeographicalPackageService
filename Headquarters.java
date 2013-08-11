import edu.rit.ds.registry.RegistryProxy;
import java.io.Serializable;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Ganesh Chandrasekaran
 * @version 04-20-2013
 */

/**
 * Class Headquarters encapsulates a Headquarters in the GPSOffice system. Each
 * GPS office also informs GPS corporate headquarters when any package arrives
 * and when any package departs.
 * <P>
 * Class Headquarters also has the main program for the GPSOffice system.
 * <P>
 * Usage: java Headquarters <I>host</I> <I>port</I>" <BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 */
public class Headquarters implements Serializable {

	private static RegistryProxy registry;
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;
	private static RemoteEventListener<TrackPackage> nodeListener;

	/**
	 * Headquarters main program.
	 */
	public static void main(String[] args) throws Exception {
		// Parse command line arguments.
		if (args.length != 2)
			usage();
		String host = args[0];
		int port = parseInt(args[1], "port");

		// Get a proxy for the Registry Server.
		registry = new RegistryProxy(host, port);

		// Export a remote event listener object for receiving notifications
		// from the Registry Server.
		registryListener = new RegistryEventListener() {
			public void report(long seqnum, RegistryEvent event) {
				listenToGPSOffice(event.objectName());

			}
		};
		UnicastRemoteObject.exportObject(registryListener, 0);

		// Export a remote event listener object for receiving notifications
		// from GPSOffice objects.
		nodeListener = new RemoteEventListener<TrackPackage>() {
			public void report(long seqnum, TrackPackage event) {

				System.out.println(event.message);

			}
		};
		UnicastRemoteObject.exportObject(nodeListener, 0);

		// Tell the Registry Server to notify us when a new GPSOffice object is
		// bound.
		registryFilter = new RegistryEventFilter().reportType("GPSOffice")
				.reportBound();
		registry.addEventListener(registryListener, registryFilter);

		// Tell all existing GPSOffice objects to notify us of notifications.
		for (String objectName : registry.list("GPSOffice")) {
			listenToGPSOffice(objectName);
		}

	}

	/**
	 * Tell the given GPSOffice object to notify us of the status.
	 * 
	 * @param objectName
	 *            Node object's name.
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	private static void listenToGPSOffice(String objectName) {
		try {
			GPSOfficeRef node = (GPSOfficeRef) registry.lookup(objectName);
			node.addListener(nodeListener);
		} catch (NotBoundException exc) {
		} catch (RemoteException exc) {
		}
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err.println("Usage: java Headquarters <host> <port>");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
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
			System.err.printf("Headquarters: Invalid <%s>: \"%s\"", name, arg);
			usage();
			return 0;
		}
	}
}