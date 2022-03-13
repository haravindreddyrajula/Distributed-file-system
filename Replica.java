import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.registry.LocateRegistry;

public class Replica extends UnicastRemoteObject implements Storage {

    private static String masterIP;
    private static int masterPort;
    private static int tcpPort;
    private static String replicaIP;
    private static int replicaPort;
    private String[] files;
    public Storage storage;
    private ConcurrentHashMap<String, byte[]> isolatedStorage;
    private ConcurrentHashMap<String, byte[]> fileContentStorage;
    private static ServerSocket ssock;
    // private static Socket socket;

    // Constructor -> No args
    public Replica() throws RemoteException {
        // super();
        // TODO Auto-generated constructor stub
    }

    // Constructor -> Single arg
    public Replica(File root) throws RemoteException {
    }

    // Constructor -> arg array
    public Replica(String[] args) throws Exception {

        replicaIP = args[0]; // IP
        replicaPort = Integer.parseInt(args[1]);
        masterIP = args[2];
        masterPort = Integer.parseInt(args[3]);
        tcpPort = Integer.parseInt(args[4]);
        isolatedStorage = new ConcurrentHashMap<String, byte[]>();
        fileContentStorage = new ConcurrentHashMap<String, byte[]>();

        ssock = new ServerSocket(tcpPort);

        createServer(replicaIP, replicaPort);
        registerMaster(masterIP, masterPort);
    }

    // Registring with RMI
    private void createServer(String IP, int PORT) throws Exception, RemoteException {
        System.setProperty("java.rmi.server.hostname", IP); // success

        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind("Service_" + IP, new Replica());

        Registry storageserver = LocateRegistry.getRegistry("localhost", PORT);
        storage = (Storage) storageserver.lookup("Service_" + IP);

    }

    // Getting Master RMI ref
    private void registerMaster(String IP, int PORT) throws Exception {
        Registry masterServer = LocateRegistry.getRegistry(IP, PORT);
        Storage reg_stub = (Storage) masterServer.lookup("Master");
        reg_stub.register(replicaIP, tcpPort, files, storage); // tcpport check?

        System.out.println("Connected to Master successfully");
    }

    //
    public boolean directoryimpl(String IP, String PORT, String path, String type) // not used: ip, port
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        return true;
    }

    // Renaming dir/file
    public boolean rename(String newFile, String oldFile) throws RemoteException, IOException {
        File oldDir = new File(oldFile);
        File newDir = new File(newFile);

        if (newDir.isDirectory() || newDir.isFile()) {
            System.err.println("The new path: " + newFile + " already exists");
            return false;
        }

        if (oldDir.isDirectory() || oldDir.isFile()) {
            return oldDir.renameTo(newDir);
        } else {
            System.err.println("the old path: " + oldFile + " doesnt exist");
            return false;
        }
    }

    // Creating dir/file
    public boolean create(String path, String type) throws RemoteException, IOException {
        File dir = new File(path);

        if (dir.isDirectory() || dir.isFile()) {
            System.err.println("The path: " + path + " already exists");
            return false;
        }

        if (type.equals("mkdir")) {
            return dir.createNewFile();
        } else {
            return dir.mkdir();
        }
    }

    // removing dir/file
    public boolean remove(String path) throws RemoteException, IOException {

        File dir = new File(path);
        if (dir.isDirectory() && dir.list().length != 0) {
            System.err.println("The folder can't be deleted because it has the files/subdir in it");
            return false;
        }
        read(path); // before deleting it store the contents
        return dir.delete();
    }

    // reading block used before deleting file
    public void read(String path) throws RemoteException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    File file = new File(path);
                    if (file.isFile()) {
                        FileInputStream fis = new FileInputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        byte[] contents;
                        long fileLength = file.length();
                        long current = 0;
                        while (current != fileLength) {
                            int size = 10000;
                            if (fileLength - current >= size)
                                current += size;
                            else {
                                size = (int) (fileLength - current);
                                current = fileLength;
                            }
                            contents = new byte[size];
                            bis.read(contents, 0, size);
                            fileContentStorage.put(path, contents); // Assuming max file size is 10kb
                        }
                        bis.close();
                    } else {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Writing file if deleting file failed
    public boolean write(String path) throws UnknownHostException, IOException {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("File: " + path);
                try {
                    FileOutputStream fos = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(fileContentStorage.get(path));
                    bos.flush();
                    bos.close();
                    fos.flush();
                    fos.close();
                    fileContentStorage.remove(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    // Write : 2pc (phase1)
    public boolean writePhaseone(String IP, String PORT, String path, String client)
            throws UnknownHostException, IOException {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("File: " + path + " is in phase one & time: " + System.nanoTime());
                try {
                    Socket socket = ssock.accept();
                    InputStream is = socket.getInputStream();
                    isolatedStorage.put(client, is.readAllBytes());
                    is.close();
                    socket.close();
                    System.out.println("File success at phase one& time: " + System.nanoTime());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        return true;
    }

    // Write : 2pc (phase2)
    public boolean writePhaseTwo(String IP, String path) throws UnknownHostException, IOException {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("File: " + path + " is in phase two & time: " + System.nanoTime());
                try {
                    FileOutputStream fos = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bos.write(isolatedStorage.get(IP));
                    bos.flush();
                    bos.close();
                    fos.flush();
                    fos.close();
                    isolatedStorage.remove(IP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    // Abort Write 2PC
    public boolean writeAbort(String IP) throws UnknownHostException, IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    isolatedStorage.remove(IP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    public static void main(String args[]) throws RemoteException, NotBoundException, UnknownHostException, Exception {
        if (args.length < 5) {
            System.err.println(
                    "Incorrect arguments. Please give IP addresses and port numbers of your and master and tcp port");
            System.exit(1);
        }

        new Replica(args);
    }

    @Override
    public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files, Storage command_stub)
            throws RemoteException, NotBoundException {
        // TODO Auto-generated method stub
        return null;
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
    public Map<String, List<String>> getFileMap() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean write(String IP, String PORT, String path, String fileDetail)
            throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean directoryimpl(String clientIP, String PORT, String path, String type, String fileDetail)
            throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean directoryimplReplica(String path, String type, String fileDetail)
            throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public byte[] read() throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

}
