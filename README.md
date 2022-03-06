# Distributed-file-system

The Distributed-file-system is built on using Java RMI, multithreading and sockets.

Here Master java file serves as primary server. The replica java file used for creating replicas of the primary server. The client file will be used to do certain operations on server, which triggers its replicas. ReplicaFuture file is used for async communication in the threading.

Tcp ports are being used for socket connections. master and client uses the same tcp-port and replica is different.

To run the Master.java 

java Master [own ip] [rmi-port] [tcp-port]

to run the Replica.java

java Replica [own-ip] [rmi-port] [Master-ip] [master-RMI-port] [tcp-port]

To run the client.java

Client is capable to do certain operations on server based on the operation type we choose. at a time please choose one type and run.

[type] = put/mkdir/ ...

java Client [type] filepath [master-ip] [master-RMI-port] [tcp-port] [own-ip]


