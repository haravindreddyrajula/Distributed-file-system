import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;
import java.util.Map;

public interface Storage extends Remote {

        public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, Storage command_stub)
                        throws RemoteException, NotBoundException;

        // master
        public boolean directoryimpl(String clientIP, String PORT, String path, String type, String fileDetail)
                        throws UnknownHostException, IOException;

        // used by both
        public boolean create(String path, String type) throws RemoteException, IOException;

        // used by both
        public boolean remove(String path) throws RemoteException, IOException;

        // used by both
        public boolean rename(String newFile, String oldFile) throws RemoteException, IOException;

        // public boolean put(String IP, String PORT, String path) throws Exception;

        // used by both
        public void read(String path) throws IOException, RemoteException;

        // public void read(String path, String ip, String port) throws IOException,
        // RemoteException;

        public boolean write(String path) throws UnknownHostException, IOException;

        public boolean write(String clientIP, String PORT, String path, String fileDetail)
                        throws UnknownHostException, IOException;

        public boolean writePhaseTwo(String IP, String path) throws UnknownHostException, IOException;

        public boolean writePhaseone(String IP, String PORT, String path, String client)
                        throws UnknownHostException, IOException;

        public boolean writeAbort(String IP) throws UnknownHostException, IOException;

        // public List<String> getStorage(String file) throws RemoteException,
        // FileNotFoundException, IOException;

        public List<String> list() throws Exception;

        public Map<String, List<String>> getFileMap() throws Exception;

        public void authShare(List<String> iplist, String path, String operation) throws Exception;
}
