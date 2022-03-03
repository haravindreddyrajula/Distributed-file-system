import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Master implements Storage {

    private Map<String, List<Storage>> Replicaloc;
    private Map<Storage, List<String>> Replica;
    private Set<Storage> StorageServers;

    // Constructor
    public Master() throws RemoteException {
        Replicaloc = new HashMap<String, List<Storage>>();
        Replica = new HashMap<Storage, List<String>>();
        StorageServers = new HashSet<Storage>();
    }

    public synchronized void start(String PORT) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(Integer.parseInt(PORT));
        registry.rebind("Master", new Master()); // bind remote obj with name
    }

    // main function
    public static void main(String args[]) throws RemoteException {

        // Checking the INPUTS...
        if (args.length < 2) {
            System.err.println("Incorrect arguments length.. Please give IP address and port number");
            System.exit(1);
        }

        if (System.getSecurityManager() != null) {
            System.setProperty("java.rmi.server.hostname", args[0]); // args[0] is ip address
            new Master().start(args[1]); // throws remoteexception
            System.out.println("\nListening Incoming  Connections on the port " + args[1]);
        } else {
            // TODO : Setting up the security manager
            // System.set
        }

    }

    @Override
    public byte[] read() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void read(String path) throws IOException, RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean create(String file) throws RemoteException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void write(String IP, String PORT, String path) throws UnknownHostException, IOException {
        // TODO Auto-generated method stub

    }
}