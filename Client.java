import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
// import java.io.LineNumberInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private Registry master;
    private Storage service_stub;
    private String masterIP;
    private int masterPort;
    private Map<String, List<String>> fileLocation;
    private static Map<String, String> regClients;
    private static Map<String, String> fileKeyMap;
    private static String fileStatus;

    // Security variables
    private static SecretKeySpec secretKey;
    private static byte[] key;
    private String enKey = "secretkey";

    // 5 argument constructor
    public Client(String filePath, String IP, String PORT, String tcp, String clientIP) throws Exception {
        this.masterIP = IP;
        this.masterPort = Integer.parseInt(PORT);

        master = LocateRegistry.getRegistry(masterIP, masterPort);
        service_stub = (Storage) master.lookup("Master");

        regClients = service_stub.getRegisteredClients(); // getting <clients,secretkeys> mapping
        fileLocation = service_stub.getFileMap(); // getting <filenames, list<clients>> mapping
        fileKeyMap = service_stub.getFileKeys(); // getting <filepath, secretKey> mapping

        fileStatus = authorizeCheck_(filePath, clientIP);

    }

    // Registering as a new user with master server
    public void newClient(String clientIP) throws Exception {
        System.out.println(
                "\nWe see you are a new client. You may need to enter your secretKey for encryption process.");
        System.out.print("Client: " + clientIP + " Type your secretkey: ");

        Scanner in = new Scanner(System.in);
        enKey = in.nextLine();
        boolean authorized = service_stub.getValidate(clientIP, enKey);
        if (!authorized) {
            System.err.println("\nMaster server denied Access");
            System.exit(1);
        } else {
            System.out.println("\nMaster server granted Access");
        }
    }

    // Authorization Block
    public String authorizeCheck_(String file, String clientIP) throws Exception {

        if (!fileLocation.containsKey(file)) { // if file is new
            if (regClients.containsKey(clientIP)) { // if client is not new
                enKey = regClients.get(clientIP);
            } else
                newClient(clientIP);

            return "new";
        } else {
            if (fileLocation.get(file).contains(clientIP)) {
                if (!regClients.containsKey(clientIP))
                    newClient(clientIP);
                enKey = fileKeyMap.get(file);
            } else {
                System.err.println("Permission denied, You are not authorized to access this filepath: " + file);
                System.exit(1);
            }
            return "exists";
        }
    }

    // Key setter
    public static void setKey(final String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // encrypt block
    public static String encrypt(final String strToEncrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // AES/GCM/NoPadding AES/ECB/PKCS5Padding
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    // decrypt block
    public static String decrypt(final String strToDecrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder()
                    .decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    private void getFile(String args[]) throws Exception {
        String path = args[1];
        // service_stub.read(path, args[2], args[4]);
        // Socket socket = ssock_client.accept();
        new Thread(new Runnable() {
            public void run() {
                System.out.println("Writing File: " + path);
                try {
                    service_stub.read(path);
                    Socket socket = new Socket(InetAddress.getByName(args[2]), Integer.parseInt(args[4]));
                    // OutputStream os = socket.getOutputStream();
                    // os.write(new String("Hello").getBytes());
                    // os.flush();
                    // os.close();
                    InputStream is = socket.getInputStream();

                    FileOutputStream fos = new FileOutputStream(path);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    int bytesRead = 0;
                    byte[] contents = new byte[10000];
                    while ((bytesRead = is.read(contents)) != -1) {
                        // bos.write(decrypt(contents, enKey), 0, bytesRead);
                        bos.write(contents, 0, bytesRead);
                    }
                    // System.out.println(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                    // bos.write(is.readAllBytes());
                    bos.flush();
                    // is.close();
                    bos.close();
                    // fos.flush();
                    // fos.close();
                    socket.close();
                    System.out.println("File:" + path + " read succesfully!");

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("exception");
                } finally {

                }
            }

        }).start();

    }

    // Transfering file (reading and exporting)
    private void transfer(String args[], String fileDetail) throws Exception {
        String path = args[1];

        new Thread(new Runnable() {
            public void run() {
                System.out.println("Transfering File: " + path);
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

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
        // client ip, tcp port, filepath, detial=[new, existing]
        if (!service_stub.write(args[5], args[4], args[1], fileDetail)) {
            System.out.println("\nError in writing file...");
        } else {
            System.out.println("File sent succesfully!");
        }
    }

    // client only method
    private void directories(String args[], String fileDetail) throws Exception {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("Folder: " + args[1]); // file path
                try {
                    // clientIp, tcpport, filepath, operation, detail
                    service_stub.directoryimpl(args[5], args[4], args[1], args[0], fileDetail);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void authShare(String args[]) throws Exception {
        new Thread(new Runnable() {
            public void run() {
                System.out.println("Trying to share Folder: " + args[1] + " with " + args[6]); // file path
                try {
                    // clientIp, tcpport, filepath, operation, detail
                    // service_stub.directoryimpl(args[5], args[4], args[1], args[0], fileDetail);
                    List<String> iplist = new ArrayList<>(Arrays.asList(args[6].split(",")));
                    service_stub.authShare(iplist, args[1], args[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void run() throws Exception {
        List<String> files = service_stub.list(); // get storge server hosting "path" file
        for (String file : files)
            System.out.println(file);
    }

    // client only method
    public String authorizeCheck(String file, String clientIP) {
        if (!fileLocation.containsKey(file)) {
            return "new";
        } else {
            if (fileLocation.get(file).contains(clientIP)) {
                return "existing";
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
        // args[0] -> operation [put]
        if (args[0].equalsIgnoreCase("put")) {
            String detail = object.authorizeCheck(args[1], args[5]); // filepath, client ip
            if (detail.equals("denied")) {
                System.err
                        .println("Permission Denied: Because you don't have authorization to the " + args[1]);
                System.exit(1);
            } else {
                object.transfer(args, detail);
            }
        }

        // args[0] -> mkdir, create, rmdir, remove
        if (args[0].equalsIgnoreCase("mkdir") || args[0].equalsIgnoreCase("create") || args[0]
                .equalsIgnoreCase("rmdir") || args[0].equalsIgnoreCase("remove")) {
            String detail = object.authorizeCheck(args[1], args[5]); // filepath, client ip
            if (detail.equals("denied")) {
                System.err
                        .println("Permission Denied: Because you don't have authorization to the " + args[1]);
                System.exit(1);
            } else {
                object.directories(args, detail);
            }
        }

        // args[0] -> rename
        // args[1] -> newfile.txt,oldfile.txt
        if (args[0].equalsIgnoreCase("rename")) {
            if (args[1].contains(",")) {
                String detail = object.authorizeCheck(args[1], args[5]); // file path, client ip
                if (detail.equals("denied")) {
                    System.err
                            .println("Permission Denied: Because you don't have authorization to the " + args[1]);
                    System.exit(1);
                } else {
                    object.directories(args, detail);
                }
            } else {
                System.err.println("Incorrect args: plz give the newfile and oldfile");
                System.exit(1);
            }
        }

        // args[0] -> read
        if (args[0].equalsIgnoreCase("read")) {
            object.getFile(args);
            // if (object.authorizeCheck(args[1], args[5]).equals("denied")) {
            // System.err
            // .println("Permission Denied: Because you don't have authorization to the " +
            // args[1]);
            // System.exit(1);
            // } else {
            // object.getFile(args);
            // }
        }

        // args[0] -> share
        // args[6] -> ip1,ip2,ip3
        // if (args.length < 7) {
        // System.err.println(
        // "Bad usage. plz provide operation, filepath, masterip, master port, tcp port,
        // your ip and ips which you want to share your file with..");
        // System.exit(1);
        // }
        if (args[0].equalsIgnoreCase("share")) {
            if (args.length < 7) {
                System.err.println(
                        "Bad usage. plz provide operation, filepath, masterip, master port, tcp port, your ip and ips which you want to share your file with..");
                System.exit(1);
            }
            if (object.authorizeCheck(args[1], args[5]).equals("denied")) {
                System.err
                        .println("Permission Denied: Because you don't have authorization to the " + args[1]);
                System.exit(1);
            } else {
                object.authShare(args);
            }
        }

    }
}
