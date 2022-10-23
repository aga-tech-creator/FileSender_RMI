import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FileTransferImplClient extends UnicastRemoteObject implements IFileTransferClient {

    protected FileTransferImplClient() throws RemoteException {
        super();
    }

    @Override
    public void send(File f, byte[] buffer, int bytes) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(f, true);
        outputStream.write(buffer, 0, bytes);
        outputStream.close();
    }

}
