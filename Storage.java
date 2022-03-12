import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;
import java.util.Map;

public interface Storage extends Remote {

        // master 1
        public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files,
                        Storage command_stub)
                        throws RemoteException, NotBoundException;

        // Dealing with directories
        public boolean directoryimpl(String clientIP, String PORT, String path, String type, String fileDetail)
                        throws UnknownHostException, IOException;

        public boolean directoryimplReplica(String path, String type, String fileDetail)
                        throws UnknownHostException, IOException;

        public boolean create(String path, String type) throws RemoteException, IOException;

        public boolean remove(String path) throws RemoteException, IOException;

        public boolean rename(String newFile, String oldFile) throws RemoteException, IOException;

        public boolean put(String IP, String PORT, String path) throws Exception;

        public byte[] read() throws RemoteException;

        public void read(String path) throws IOException, RemoteException;

        public boolean write(String IP, String PORT, String path, String fileDetail)
                        throws UnknownHostException, IOException;

        public boolean writePhaseTwo(String IP, String path) throws UnknownHostException, IOException;

        public boolean writePhaseone(String IP, String PORT, String path, String client)
                        throws UnknownHostException, IOException;

        public List<String> getStorage(String file) throws RemoteException, FileNotFoundException, IOException;

        public List<String> list() throws Exception;

        public Map<String, List<String>> getFileMap() throws Exception;
}
