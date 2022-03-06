import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import java.rmi.registry.LocateRegistry;

public class Replica extends UnicastRemoteObject implements Storage, Serializable {

    private static String masterIP;
    private static int masterPort;
    private static int tcpPort;
    private static String replicaIP;
    private static int replicaPort;
    private String[] files;
    public Storage storage;

    private static ServerSocket ssock;
    private static Socket socket;

    public Replica() throws RemoteException {
        // super();
        // TODO Auto-generated constructor stub
    }

    public Replica(File root) throws RemoteException {
    }

    public Replica(String[] args) throws Exception {

        replicaIP = args[0]; // IP
        replicaPort = Integer.parseInt(args[1]);
        masterIP = args[2];
        masterPort = Integer.parseInt(args[3]);
        tcpPort = Integer.parseInt(args[4]);

        ssock = new ServerSocket(tcpPort);

        createServer(replicaIP, replicaPort);
        // getAllFiles(); commented out for creating replica
        registerMaster(masterIP, masterPort);
    }

    private void createServer(String IP, int PORT) throws Exception, RemoteException {
        System.setProperty("java.rmi.server.hostname", IP); // success

        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("Service_" + IP, new Replica());

        Registry storageserver = LocateRegistry.getRegistry("localhost", PORT);
        storage = (Storage) storageserver.lookup("Service_" + IP);

    }

    private void registerMaster(String IP, int PORT) throws Exception {
        Registry masterServer = LocateRegistry.getRegistry(IP, PORT);
        Storage reg_stub = (Storage) masterServer.lookup("Master");
        reg_stub.register(replicaIP, tcpPort, files, storage); // tcpport check?

        System.out.println("Connected to Master successfully");
    }

    private void getAllFiles() throws Exception {

        // current working directory using Path
        String path = Paths.get("").toAbsolutePath().toString();
        // System.out.println("Working Directory = " + path);

        // with "." refers to the current directory
        File curFolder = new File(".");

        /**
         * The list() method of the Java File class is used to list all the files and
         * subdirectories present inside a directory. It returns all the files and
         * directories as a string array.
         * return type : Project 1, Put.java, Put.class
         **/
        // String[] fileList = curFolder.list();
        /**  */

        File[] filesList = curFolder.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        for (File file : filesList) {
            // Comment file.isfile condition to get the files and directories
            // using "path" will get absolute path of the files
            if (file.isFile())
                /**
                 * file.getName() -> abc.txt
                 * file -> .\abc.txt
                 */
                list.add(file.getName());
        }
        files = list.toArray(new String[list.size()]);
    }

    // Writing file
    public boolean write(String IP, String PORT, String path) throws UnknownHostException, IOException {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("File: " + path);
                try {
                    Socket socket = ssock.accept();

                    byte[] contents = new byte[10000];
                    FileOutputStream fos = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = socket.getInputStream();

                    int bytesRead = 0;
                    while ((bytesRead = is.read(contents)) != -1) {
                        System.out.println(bytesRead);
                        // For(i=0;i<noOfReplicas;++){}

                        bos.write(contents, 0, bytesRead);

                        // bos.write(contents);
                        System.out.println("haravind");
                    }

                    System.out.println("stub write completed");
                    bos.flush();
                    bos.close();
                    fos.flush();
                    fos.close();
                    socket.close();
                    System.out.println("File saved successfully! at Primary server");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        // System.out.println("Writing " + path + " in your system/replica ");
        // String addr = new String(IP); // ip
        // int port = Integer.parseInt(PORT);// Tcp port listening on sender (put)

        // Socket socket = new Socket(InetAddress.getByName(addr), port);// crate socket

        // byte[] contents = new byte[10000];
        // FileOutputStream fos = new FileOutputStream(path);
        // BufferedOutputStream bos = new BufferedOutputStream(fos);
        // InputStream is = socket.getInputStream();
        // int bytesRead = 0;
        // System.out.println("tempsocket input");
        // System.out.println(contents.length);
        // System.out.println(contents.toString());
        // while ((bytesRead = is.read(contents)) != -1) {
        // System.out.println("agh");
        // // bos.write(contents, 0, bytesRead);
        // bos.write(contents);
        // System.out.println("haravind");
        // }
        // System.out.println("bos write completed");
        // bos.flush();
        // bos.close();
        // socket.close();
        // System.out.println("File saved successfully at replica");
        return true;
    }

    public static void main(String args[]) throws RemoteException, NotBoundException, UnknownHostException, Exception {
        if (args.length < 5) {
            System.err.println(
                    "Incorrect arguments. Please give IP addresses and port numbers of your and master and tcp port");
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

    // @Override
    // public void write(String IP, String PORT, String path) throws
    // UnknownHostException, IOException {
    // // TODO Auto-generated method stub

    // }

}
