import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.LocateRegistry.*;

public class Replica extends UnicastRemoteObject implements Storage, Serializable {

    private static String masterIP;
    private static int masterPort;
    private static int tcpPort;
    private static String replicaIP;
    private static int replicaPort;
    private String[] files;
    public Storage storage;
    // public Storage registration;

    private static ServerSocket ssock;
    private static Socket socket;

    protected Replica() throws RemoteException {
        super();
        // TODO Auto-generated constructor stub
    }

    public Replica(String[] args) throws Exception {

        replicaIP = args[0]; // IP
        replicaPort = Integer.parseInt(args[1]);
        masterIP = args[2];
        masterPort = Integer.parseInt(args[3]);
        tcpPort = Integer.parseInt(args[4]);

        // create server function: while rebinding change the service to service[1],
        // service[2].... to distinguish

        createServer(replicaIP, replicaPort);
        getAllFiles();
        registerMaster(masterIP, masterPort);
    }

    private void createServer(String IP, int PORT) throws Exception, RemoteException {
        System.setProperty("java.rmi.server.hostname", IP);

        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("Service_" + IP, new Replica());
        Registry storageserver = LocateRegistry.getRegistry("localhost", PORT);

        storage = (Storage) storageserver.lookup("Service_" + IP);
    }

    private void getAllFiles() throws Exception {
        File curDir = new File(".");
        File[] filesList = curDir.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        for (File f : filesList) {
            if (f.isFile())
                list.add(f.getName());
        }
        files = list.toArray(new String[list.size()]);
    }

    private void registerMaster(String IP, int PORT) throws Exception {

        Registry masterServer = LocateRegistry.getRegistry(IP, PORT);
        Storage registration_stub = (Storage) masterServer.lookup("Master");

        // implement this in master server
        registration_stub.register(replicaIP, tcpPort, files, storage); // tcpport check?
    }

    public static void main(String args[]) throws RemoteException, NotBoundException, UnknownHostException, Exception {
        if (args.length < 5) {
            System.err.println(
                    "Incorrect arguments length.. Please give IP addresses and port numbers of master and slave nodes and tcp port");
            System.exit(1);
        }
        // try {
        new Replica(args);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // System.out.println(IP_RMI);

    }

    @Override
    public boolean create(String file) throws RemoteException, IOException {
        // TODO Auto-generated method stub
        return false;
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
    public void write(String IP, String PORT, String path) throws UnknownHostException, IOException {
        // TODO Auto-generated method stub

    }

}
