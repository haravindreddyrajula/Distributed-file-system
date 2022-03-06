
// import java.io.InputStream;
import java.io.OutputStream;
// import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

public class ReplicaFuture implements Callable {

    private Socket socket;
    private byte[] contents;
    private Storage stub;
    private String ipAddress;
    private String port;
    private String path;

    public ReplicaFuture(Socket socket, byte[] contents, Storage stub, String ipAddress, String port, String path) {
        this.socket = socket;
        this.contents = contents;
        this.stub = stub;
        this.ipAddress = ipAddress;
        this.port = port;
        this.path = path;
    }

    @Override
    public Integer call() throws Exception {
        try {
            // System.out.println("Hello from Callable");
            // System.out.println(ipAddress);
            // System.out.println(port);
            // System.out.println(path);
            OutputStream os = socket.getOutputStream();
            os.write(contents);
            // os.write(contents);
            boolean flag = stub.write(ipAddress, port, path);
            os.flush();
            os.close();
            socket.close();
            // System.out.println("after stub completed");
            if (flag) {
                return 1;
            }
        } catch (Exception e) {
            System.out.println("Error in sending information to replica");
            e.printStackTrace();
        }

        return 0;
    }

}