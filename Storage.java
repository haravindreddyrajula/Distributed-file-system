import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;

public interface Storage extends Remote {

        public String[] register(String IP_STORAGE_SERVER, int PORT_STORAGE_SERVER, String[] files,
                        Storage command_stub)
                        throws RemoteException, NotBoundException;

        public boolean directoryimpl(String IP, String PORT, String path, String type)
                        throws UnknownHostException, IOException;

        public boolean create(String file) throws RemoteException, IOException;

        public boolean remove(String file) throws RemoteException, IOException;

        public boolean put(String IP, String PORT, String path) throws Exception;

        public byte[] read() throws RemoteException;

        public void read(String path) throws IOException, RemoteException;

        public boolean write(String IP, String PORT, String path) throws UnknownHostException, IOException;

        public boolean writePhaseTwo(String IP, String PORT, String path, String userName)
                        throws UnknownHostException, IOException;

        public boolean writePhaseone(String IP, String PORT, String path, String userName)
                        throws UnknownHostException, IOException;

        public List<String> getStorage(String file) throws RemoteException, FileNotFoundException, IOException;

        public List<String> list() throws Exception;
}
