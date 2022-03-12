import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private Registry master;
    private Storage service_stub;
    private String masterIP;
    private int masterPort;
    private Map<String, List<String>> fileLocation;

    // constructor
    public Client(String IP, String PORT, String tcp) throws Exception {
        this.masterIP = IP;
        this.masterPort = Integer.parseInt(PORT);

        master = LocateRegistry.getRegistry(masterIP, masterPort);
        service_stub = (Storage) master.lookup("Master");
        fileLocation = service_stub.getFileMap();
    }

    // Transfering file (reading and exporting)
    private void transfer(String args[], String fileDetail) throws Exception {
        String path = args[1];

        new Thread(new Runnable() {
            public void run() {
                System.out.println("File: " + path);
                try {
                    Socket socket = new Socket(InetAddress.getByName(args[2]), Integer.parseInt(args[4]));
                    String anim = "|/-\\";
                    File file = new File(path);
                    FileInputStream fis = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    OutputStream os = socket.getOutputStream();
                    byte[] contents;
                    long fileLength = file.length();
                    long current = 0;
                    // long start = System.nanoTime();
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
                        os.write(contents);
                        int x = (int) ((current * 100) / fileLength);

                        String data = "\r" + anim.charAt(x % anim.length()) + " " + x + "%" + "Sent";
                        System.out.write(data.getBytes());

                    }
                    os.flush();
                    os.close();
                    bis.close();
                    socket.close();
                    System.out.println("File sent succesfully!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        service_stub.write(args[5], args[4], args[1], fileDetail); // client ip, tcp port, filepath, detial=[new, old]
    }

    // client only method
    private void directories(String args[], String fileDetail) throws Exception {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("Folder: " + args[1]);
                try {
                    // clientIp, tcpport, filepath, operation, detail
                    service_stub.directoryimpl(args[5], args[4], args[1], args[0], fileDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();

        // service_stub.put(args[5], args[4], args[1]); // client ip, tcp port, filepath
    }

    public void run() throws Exception {
        List<String> files = service_stub.list(); // get storge server hosting "path" file
        for (String file : files)
            System.out.println(file);
    }

    // client only method
    public String authorizeCheck(String file, String clientIP) {
        if (!fileLocation.containsKey(file)) {
            return "new"; // new file
        } else {
            if (fileLocation.get(file).contains(clientIP)) {
                return "existing"; // rewriting file
            } else {
                return "denied";
            }
        }
    }

    // CLI : java Client
    public static void main(String args[]) throws Exception {
        // java client
        // args[0] -> operation [put/list/mkdir....]
        // args[1] -> file path
        // args[2] -> master server
        // args[3] -> master port
        // args[4] -> tcp port
        // args[5] -> client ip

        if (args.length < 6) {
            System.err.println("Bad usage. plz provide operation, filepath, masterip, master port, tcp port, your ip");
            System.exit(1);
        }
        Client object = new Client(args[2], args[3], args[4]);

        // inserting or rewriting
        if (args[0].equalsIgnoreCase("put")) {
            String detail = object.authorizeCheck(args[1], args[5]); // authorization check call
            if (detail.equals("denied")) {
                System.err
                        .println("Permission Denied: Because you don't have authorization to the " + args[5]);
                System.exit(1);
            } else {
                object.transfer(args, detail);
            }
        }

        if (args[0].equalsIgnoreCase("mkdir")) {
            String detail = object.authorizeCheck(args[1], args[5]);
            if (detail.equals("denied")) {
                System.err
                        .println("Permission Denied: Because you don't have authorization to the " + args[5]);
                System.exit(1);
            } else {
                object.directories(args, detail);
            }
        }

    }
}
