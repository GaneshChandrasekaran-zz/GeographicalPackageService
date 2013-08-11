Overview

Write a distributed system in Java for a package tracking system called Geographic Package Service (GPS). The GPS system will include distributed objects and client programs, residing in multiple processes, running on different host computers, and communicating with each other using Java remote method invocation (RMI). 

Geographic Package System

The GPS Company provides customers with a package delivery service. GPS delivers a package to a destination specified by geographic coordinates (longitude and latitude, or X and Y), not postal addresses. GPS uses an experimental version of Star Trek's transporter technology to beam a package to its destination. Because the transporter has a limited range (it's still experimental, after all), GPS cannot necessarily beam a package directly to its final destination. Instead, the service works like this:

    GPS has a number of offices in different cities at certain (X,Y) locations.

    Each GPS office is able to beam a package to any of its neighboring GPS offices. The neighbors are defined as follows:

        If there is one GPS office in the system, then that GPS office has no neighbors.

        If there are two GPS offices in the system, then each GPS office has one neighbor, which is the other GPS office.

        If there are three GPS offices in the system, then each GPS office has two neighbors, which are the other two GPS offices.

        If there are four GPS offices in the system, then each GPS office has three neighbors, which are the other three GPS offices.

        If there are five or more GPS offices in the system, then each GPS office has three neighbors, which are the other three GPS offices closest to itself.

        Closeness is measured using the usual Euclidean distance: The distance between office A at location (X1,Y1) and office B at location (X2,Y2) is ((X1 - X2)2 +         (Y1 - Y2)2)1/2.

        New GPS offices can open and existing GPS offices can close at any time. If a GPS office opens or closes, the other GPS offices' neighbor sets might change. 

    A customer goes to a GPS office with a package to be sent to a certain (X,Y) destination. The GPS office assigns a tracking number to the package. The package then     commences its journey through the GPS system.

    When a package arrives at a GPS office (including the originating office):

        The package undergoes an examination to make sure it is intact. This examination takes 3 seconds.

        If the package's final destination is closer to this GPS office than to any of this GPS office's neighbors, the package is instantaneously beamed to its final     destination.

        Otherwise, the package is instantaneously beamed to one of this GPS office's neighbors, namely the neighbor that is closest to the package's final destination. 

    For tracking purposes, each GPS office informs the customer when the package arrives and when the package departs.

    Each GPS office also informs GPS corporate headquarters when any package arrives and when any package departs. 

    The GPS distributed system consists of the following components:

    A distributed object to represent one GPS office.
    A client program to represent a customer sending a package.
    A client program to represent GPS corporate headquarters.
    The Registry Server from the Computer Science Course Library. 

Any number of GPS office objects and any number of clients may be running simultaneously. Only one Registry Server is running. It is assumed that the Registry Server does not fail. 

Software Requirements

    GPS Office Object

    The system must have a Java RMI remote object class for a GPS office, of which any number of instances may be running.

    An instance of the GPS office object must be run by typing this command line:

    java Start GPSOffice <host> <port> <name> <X> <Y>

        <host> is the name of the host computer where the Registry Server is running.
        <port> is the port number to which the Registry Server is listening.
        <name> is the name of the city where the GPS office is located.
        <X> is the GPS office's X coordinate (type double).
        <Y> is the GPS office's Y coordinate (type double). 

    Note: This means that the GPS office object's class must be named GPSOffice, this class must not be in a package, and this class must define the appropriate     constructor for the Start program.

    Note: The Registry Server is an instance of class edu.rit.ds.registry.RegistryServer.

    The GPS office object's constructor must throw an exception if there are any of the following problems with the command line arguments. The exception's detail     message must be a meaningful explanation of the problem.
        Any required argument is missing.
        There are extra arguments.
        The port argument cannot be parsed as an integer.
        The X or Y argument cannot be parsed as a double.
        There is no Registry Server running at the given host and port.
        Another GPS office with the same city name is already in existence.
        Any other error condition is encountered while starting up the GPS office object. 

    The GPS office object must behave as described under Geographic Package System above.

    When a package first enters the system, the originating GPS office must assign the package's tracking number; the tracking number must be the value returned by     System.currentTimeMillis().

    The GPS office object must not print anything.

    The GPS office object must continue running until killed externally.

    Customer Client Program

    The system must have a client program for a customer sending a package, of which any number of instances may be running.

    An instance of the client program must be run by typing this command line:

    java Customer <host> <port> <name> <X> <Y>

        <host> is the name of the host computer where the Registry Server is running.
        <port> is the port number to which the Registry Server is listening.
        <name> is the name of the city where the originating GPS office is located.
        <X> is the package's destination's X coordinate (type double).
        <Y> is the package's destination's Y coordinate (type double). 

    Note: This means that the client program's class must be named Customer, and this class must not be in a package.

    Note: The Customer program is a client program, not a distributed object.

    Note: The Registry Server is an instance of class edu.rit.ds.registry.RegistryServer.

    The client program must print an error message on the console and must terminate if there are any of the following problems with the command line arguments. The     error message must be a meaningful explanation of the problem. The error message may include an exception stack trace.
        Any required argument is missing.
        There are extra arguments.
        The port argument cannot be parsed as an integer.
        The X or Y argument cannot be parsed as a double.
        There is no Registry Server running at the given host and port.
        There is no GPS office object for the given city name. 

    Whenever the customer's package arrives at a GPS office (including the originating office), the client program must print the following message on the console:

    Package number <tracknum> arrived at <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office at which the package arrived.

    Whenever the customer's package departs from a GPS office (including the originating office) en route to another GPS office and the GPS office encounters no errors     while forwarding the package, the client program must print the following message on the console:

    Package number <tracknum> departed from <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office from which the package departed.

    Whenever the customer's package departs from a GPS office (including the originating office) en route to another GPS office and the GPS office encounters an error     while forwarding the package, the client program must print the following message on the console and must terminate:

    Package number <tracknum> lost by <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office from which the package departed.

    When the customer's package is delivered from a GPS office (including the originating office) to the package's final destination, the client program must print the     following message on the console and must terminate:

    Package number <tracknum> delivered from <name> office to (<X>,<Y>)

    where <tracknum> is replaced by the package's tracking number, <name> is replaced by the city name of the GPS office that delivered the package, <X> is replaced by     the X coordinate of the package's destination, and <Y> is replaced by the Y coordinate of the package's destination.

    If the client program encounters an error condition not mentioned above, the client program must print an error message on the console and must terminate. The     error message must be a meaningful explanation of the problem. The error message may include an exception stack trace.

    The client program must not print anything other than specified above.

    Headquarters Client Program

    The system must have a client program for GPS corporate headquarters, of which any number of instances may be running.

    An instance of the client program must be run by typing this command line:

    java Headquarters <host> <port>

        <host> is the name of the host computer where the Registry Server is running.
        <port> is the port number to which the Registry Server is listening. 

    Note: This means that the client program's class must be named Headquarters, and this class must not be in a package.

    Note: The Headquarters program is a client program, not a distributed object.

    Note: The Registry Server is an instance of class edu.rit.ds.registry.RegistryServer.

    The client program must print an error message on the console and must terminate if there are any of the following problems with the command line arguments. The     error message must be a meaningful explanation of the problem. The error message may include an exception stack trace.
        Any required argument is missing.
        There are extra arguments.
        The port argument cannot be parsed as an integer.
        There is no Registry Server running at the given host and port. 

    Whenever a package arrives at a GPS office (including the originating office), the client program must print the following message on the console:

    Package number <tracknum> arrived at <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office at which the package arrived.

    Whenever a package departs from a GPS office (including the originating office) en route to another GPS office and the GPS office encounters no errors while     forwarding the package, the client program must print the following message on the console:

    Package number <tracknum> departed from <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office from which the package departed.

    Whenever a package departs from a GPS office (including the originating office) en route to another GPS office and the GPS office encounters an error while     forwarding the package, the client program must print the following message on the console:

    Package number <tracknum> lost by <name> office

    where <tracknum> is replaced by the package's tracking number and <name> is replaced by the city name of the GPS office from which the package departed.

    Whenever a package is delivered from a GPS office (including the originating office) to the package's final destination, the client program must print the     following message on the console:

    Package number <tracknum> delivered from <name> office to (<X>,<Y>)

    where <tracknum> is replaced by the package's tracking number, <name> is replaced by the city name of the GPS office that delivered the package, <X> is replaced by     the X coordinate of the package's destination, and <Y> is replaced by the Y coordinate of the package's destination.

If the client program encounters an error condition not mentioned above, the client program must print an error message on the console and must terminate. The error message must be a meaningful explanation of the problem. The error message may include an exception stack trace.

    The client program must not print anything other than specified above.

    The client program must continue running until killed externally.

    General Requirements

    The distributed objects and client programs must operate in the manner described under Geographic Package System above.

    Design Restrictions

    The GPS office objects must be the only objects bound into the Registry Server.

    A GPS office object method called by a client program must not call methods on any other GPS office object.

    Hint: Consider utilizing a thread pool from package java.util.concurrent.

The customer client program must call methods only on the GPS office object specified on the command line. The customer client program must not call methods on any other GPS office object. 