import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Master extends UnicastRemoteObject implements Storage {

    private Map<String, List<Storage>> replicaips;
    private Map<Storage, List<String>> replicanames;
    private Set<Storage> storageServers;

    // Constructor
    public Master() throws RemoteException {
        super();
        replicaips = new HashMap<String, List<Storage>>();
        replicanames = new HashMap<Storage, List<String>>();
        storageServers = new HashSet<Storage>();
    }

    // This block used to bind remote object with given name in registry
    public synchronized void start(String port) throws RemoteException {
        // Creates and exports a Registry instance on the local host that accepts
        // requests on the specified port.
        Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));
        /**
         * Replaces the binding for the specified <code>name</code> in this registry
         * with the supplied remote reference. If there is an existing binding for the
         * specified <code>name</code>, it is discarded.
         **/
        registry.rebind("Master", new Master());
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
        } else {
            // TODO : Setting up the security manager
            // System.set
            System.err.println("security manager is null");
            System.exit(1);
        }
        new Master().start(args[1]);
        System.out.println("\n Master/Primary Server is listening on port = " + args[1]); // args[1] is port number

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

    @Override
    public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files, Storage command_stub)
            throws RemoteException, NotBoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean createFile(String file) throws RemoteException, FileNotFoundException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getStorage(String file) throws RemoteException, FileNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean put(String IP, String PORT, String path) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> list() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}