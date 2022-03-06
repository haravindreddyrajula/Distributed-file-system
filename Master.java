import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Master extends UnicastRemoteObject implements Storage {

    private Map<String, List<Storage>> replicaips;
    private Map<Storage, List<String>> replicanames;
    private Set<Storage> storageServers;
    // private String[] files;
    // String environment;

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

    //
    // public boolean put(String IP, String PORT, String path) throws Exception {
    // System.out.println("Sending file to server and its replicas");

    // // if (Replicaloc.get(path) == null){
    // // System.out.println("File " + path + "not exist" + " storing file ");

    // for (Storage stub : storageServers)
    // stub.write(IP, PORT, path);

    // return true;
    // // }
    // // else
    // // return false;

    // }

    // Writing the file
    public boolean write(String IP, String PORT, String path) throws UnknownHostException, IOException {
        System.out.println("Sending file to server and its replicas");
        System.out.println("Writing " + path + "in Master Server ");

        String addr = new String(IP); // ip o
        int port = Integer.parseInt(PORT);// Tcp port listening on sender (put)

        Socket tempsocket = new Socket(InetAddress.getByName(addr), port);// create socket

        byte[] contents = new byte[10000];
        FileOutputStream fos = new FileOutputStream(path);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        InputStream is = tempsocket.getInputStream();
        int bytesRead = 0;
        while ((bytesRead = is.read(contents)) != -1)
            bos.write(contents, 0, bytesRead);

        // Writing for replicas
        for (Storage stub : storageServers)
            stub.write(IP, PORT, path);

        bos.flush();
        bos.close();
        tempsocket.close();
        System.out.println("File saved successfully! at Primary server");

        return true;
    }

    public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files, Storage command_stub)
            throws RemoteException, NotBoundException {

        storageServers.add(command_stub); // check if server is active

        // System.out.println("Storage server : " + IP_STORAGE_SERVER + " " +
        // PORT_STORAGE_SERVER + " connected");
        // for (String file : files) {
        // if (replicaips.get(file) == null) {
        // List<Storage> temp = new ArrayList<Storage>();
        // temp.add(command_stub);
        // replicaips.put(file, temp);
        // } else
        // replicaips.get(file).add(command_stub);
        // }
        // if (replicanames.get(command_stub) == null) {
        // List<String> temp = new ArrayList<String>();
        // temp.add(new String(IP_STORAGE_SERVER));
        // temp.add(new String(PORT_STORAGE_SERVER + ""));
        // replicanames.put(command_stub, temp);
        // }
        // System.out.println(replicaips.keySet());
        // System.out.println(replicaips.values());
        // System.out.println();

        // System.out.println(replicanames.keySet());
        // System.out.println(replicanames.values());
        return new String[2];
    }

    public List<String> list() throws Exception {
        return new ArrayList<>(replicaips.keySet());
    }

    // private void getAllFiles() throws Exception {

    // File curFolder = new File(".");
    // File[] filesList = curFolder.listFiles();
    // ArrayList<String> list = new ArrayList<String>();
    // for (File file : filesList) {
    // if (file.isFile())
    // list.add(file.getName());
    // }
    // files = list.toArray(new String[list.size()]);
    // }

    // main function
    public static void main(String args[]) throws RemoteException {

        // Checking the INPUTS...
        if (args.length < 2) {
            System.err.println("Incorrect arguments length.. Please give IP address and port number");
            System.exit(1);
        }

        // if (System.getSecurityManager() != null) {
        // // System.out.println("Security manager");
        // System.setProperty("java.rmi.server.hostname", args[0]);
        // } else {
        // System.setSecurityManager(new SecurityManager());
        // System.out.println("error 2");
        // String value = System.setProperty("java.rmi.server.hostname", args[0]);
        // System.out.println("error 3");
        // if (value == null) {
        // System.out.println("error");
        // }
        // System.err.println("security manager is null");
        // System.exit(1);
        // }

        System.setProperty("java.rmi.server.hostname", args[0]);
        String value = System.getProperty("java.rmi.server.hostname");
        if (value == null) {
            System.err.println("error in configuring RMI");
            System.exit(1);
        }
        if (System.getSecurityManager() != null) {
            System.out.println("Security manager error");
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

}