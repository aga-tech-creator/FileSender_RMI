import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileTransferServer extends Remote {
    void send(File f, byte[] buffer, int bytes) throws RemoteException, IOException;
}
