import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileTransferClient extends Remote {
    void send(File f, byte[] buffer, int bytes) throws RemoteException, FileNotFoundException, IOException;
}
