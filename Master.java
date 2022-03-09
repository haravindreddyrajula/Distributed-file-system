import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
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
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class Master extends UnicastRemoteObject implements Storage {

    private Map<String, List<Storage>> fileLocation;
    private Map<Storage, List<String>> replicaDetails;
    private Set<Storage> replicaInstances;

    private static ServerSocket ssock;

    // private String[] files;
    // String environment;

    // Constructor
    public Master() throws RemoteException {
        super();
        fileLocation = new HashMap<String, List<Storage>>();
        replicaDetails = new HashMap<Storage, List<String>>();
        replicaInstances = new HashSet<Storage>();

    }

    // binding remote object with given name in registry
    public synchronized void start(String port, String tcp) throws NumberFormatException, IOException {
        // Creates and exports a Registry instance on the local host that accepts
        // requests on the specified port.
        Registry registry = LocateRegistry.createRegistry(Integer.parseInt(port));

        /**
         * Replaces the binding for the specified <code>name</code> in this registry
         * with the supplied remote reference. If there is an existing binding for the
         * specified <code>name</code>, it is discarded.
         **/
        registry.rebind("Master", new Master());

        ssock = new ServerSocket(Integer.parseInt(tcp));
    }

    // Writing the file
    public boolean write(String IP, String PORT, String path) throws UnknownHostException, IOException {

        new Thread(new Runnable() {
            public void run() {
                System.out.println("Given File: " + path);
                try {
                    Socket socket = ssock.accept();

                    FileOutputStream fos = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = socket.getInputStream();

                    // TODO : Implement writing on server after replica writing is successful
                    int bytesRead = 0;
                    byte[] contents = new byte[10000];
                    while ((bytesRead = is.read(contents)) != -1) {
                        // bos.write(contents); //file size: contents size 10000
                        bos.write(contents, 0, bytesRead); // size: actual size of file
                    }

                    System.out.println("master server write done");
                    // Writing for replicas
                    FutureTask<Integer>[] futurePhaseOne = new FutureTask[replicaInstances.size()];
                    FutureTask<Integer>[] futurePhaseTwo = new FutureTask[replicaInstances.size()];
                    List<Socket> replicaSockets = new ArrayList<>();

                    for (Storage stub : replicaInstances) {
                        List<String> s = replicaDetails.get(stub);
                        Socket socketre = new Socket(InetAddress.getByName(s.get(0)), Integer.parseInt(s.get(1)));
                        replicaSockets.add(socketre);
                        // Spwaning phase 1 threads
                        Callable replicaFuture = new ReplicaFuturePhaseOne(socketre, contents, stub, s.get(0), s.get(1),
                                path);
                        futurePhaseOne[replicaSockets.size() - 1] = new FutureTask<>(replicaFuture);
                        Thread t = new Thread(futurePhaseOne[replicaSockets.size() - 1]);
                        t.start();
                    }

                    int voteCount = 0;
                    for (int i = 0; i < replicaSockets.size(); i++) {
                        voteCount = voteCount + (Integer) (futurePhaseOne[i].get());
                    }

                    System.out.println("phase one complete");

                    int i = 0;
                    for (Storage stub : replicaInstances) {
                        String message = null;
                        if (voteCount == replicaSockets.size()) { // all agreed on commit
                            message = "COMMIT"; // convert string to byte array
                        } else {
                            message = "ABORT";
                        }
                        List<String> s = replicaDetails.get(stub);
                        Callable replicaFuturePhaseTwo = new ReplicaFuturePhaseTwo(replicaSockets.get(i),
                                message.getBytes(),
                                stub,
                                s.get(0),
                                s.get(1), path);
                        futurePhaseTwo[i] = new FutureTask<>(replicaFuturePhaseTwo);
                        Thread t = new Thread(futurePhaseTwo[i]);
                        t.start();
                        i++;
                    }

                    //
                    // for (Storage stub : storageServers) {
                    // List<String> s = replicanames.get(stub);
                    // // stub.write(s.get(0), s.get(1), path);
                    // // System.out.println("before stub completed");
                    // Socket socketre = new Socket(InetAddress.getByName(s.get(0)),
                    // Integer.parseInt(s.get(1)));

                    // Callable replicaFuture = new ReplicaFuturePhaseOne(socketre, contents, stub,
                    // s.get(0), s.get(1),
                    // path);
                    // futures[0] = new FutureTask<>(replicaFuture);
                    // Thread t = new Thread(futures[0]);
                    // t.start();

                    // // OutputStream os = socketre.getOutputStream();
                    // // os.write(contents);
                    // // stub.write(s.get(0), s.get(1), path);
                    // // os.flush();
                    // // os.close();
                    // // socketre.close();
                    // System.out.println("after stub completed");
                    // }

                    // if ((Integer) (futures[0].get()) == 1) {
                    // System.out.println("stub write completed");
                    // }

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
        return true;

    }

    public boolean directoryimpl(String IP, String PORT, String path, String type)
            throws UnknownHostException, IOException {

        new Thread(new Runnable() {
            public void run() {
                System.out.println("Folder: " + path);
                try {

                    File serverpathdir = new File(path);
                    if (type.equals("mkdir"))
                        serverpathdir.mkdir();
                    else if (type.equals("delete"))
                        serverpathdir.delete();

                    // FutureTask[] futures = new FutureTask[1];
                    for (Storage stub : replicaInstances) {
                        // List<String> s = replicanames.get(stub);
                        // stub.write(s.get(0), s.get(1), path);
                        System.out.println("before stub completed");
                        stub.directoryimpl(IP, PORT, path, type);
                        // Socket socketre = new Socket(InetAddress.getByName(s.get(0)),
                        // Integer.parseInt(s.get(1)));

                        // Callable replicaFuture = new ReplicaFuture(socketre, contents, stub,
                        // s.get(0), s.get(1),
                        // path);
                        // futures[0] = new FutureTask<>(replicaFuture);
                        // Thread t = new Thread(futures[0]);
                        // t.start();

                        // OutputStream os = socketre.getOutputStream();
                        // os.write(contents);
                        // stub.write(s.get(0), s.get(1), path);
                        // os.flush();
                        // os.close();
                        // socketre.close();
                        System.out.println("after stub completed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        return true;

    }

    public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files, Storage command_stub)
            throws RemoteException, NotBoundException {
        System.out.println(command_stub.toString());
        replicaInstances.add(command_stub); // check if server is active
        System.out.println("Replica: " + IP_STORAGE_SERVER + " got connected to Master");

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
        if (replicaDetails.get(command_stub) == null) {
            List<String> temp = new ArrayList<String>();
            temp.add(new String(IP_STORAGE_SERVER));
            temp.add(new String(PORT_STORAGE_SERVER + ""));
            replicaDetails.put(command_stub, temp);
        }
        return new String[2];
    }

    public List<String> list() throws Exception {
        return new ArrayList<>(fileLocation.keySet());
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
    public static void main(String args[]) throws NumberFormatException, IOException {

        // Checking the INPUTS...
        /**
         * java Master
         * args[0] -> master ip
         * args[1] -> master port
         * args[2] -> tcp
         */
        if (args.length < 3) {
            System.err.println("Incorrect arguments length.. Please give IP address, port number and tcp");
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

        new Master().start(args[1], args[2]);
        System.out.println("\n Master/Primary Server is listening on port = " + args[1]); // args[1] is port number
        System.out.println("\n Socket listening on port : " + args[2]);

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
    public boolean remove(String file) throws RemoteException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean writePhaseTwo(String IP, String PORT, String path, String userName)
            throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean writePhaseone(String IP, String PORT, String path, String userName)
            throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

}