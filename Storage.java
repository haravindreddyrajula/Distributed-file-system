import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;

public interface Storage extends Remote {

    // command
    public boolean create(String file) throws RemoteException, IOException;

    // registration
    public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files, Storage command_stub)
            throws RemoteException, NotBoundException;

    // Service
    public boolean createFile(String file) throws RemoteException, FileNotFoundException;

    public List<String> getStorage(String file) throws RemoteException, FileNotFoundException, IOException;

    public boolean put(String IP, String PORT, String path) throws Exception;

    public List<String> list() throws Exception;

    // Storage
    public byte[] read() throws RemoteException;

    public void read(String path) throws IOException, RemoteException;

    // public boolean create (String file) throws RemoteException, IOException;
    public void write(String IP, String PORT, String path) throws UnknownHostException, IOException;
}
