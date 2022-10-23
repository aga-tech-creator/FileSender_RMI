import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class FileSenderScreen {
    boolean clientMode = true, connected = false, isClientBinded = false;
    int width, height;
    String title, ipName, portName;
    JFrame mainScreen;
    JButton hostButton, connectButton;
    JRadioButton tcp, udp;
    ButtonGroup radioGroup;
    JLabel protocolText, ipText, ipPortText;
    JTextField ipField, fileField, ipPortField;
    JFileChooser fChooser;
    JProgressBar progressBar;
    IFileTransferServer stubServer;
    IFileTransferClient stubClient;

    public FileSenderScreen(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void start() {
        this.mainScreen = new JFrame();
        this.mainScreen.setLayout(null);
        this.mainScreen.setTitle(this.title);
        this.mainScreen.setSize(this.width, this.height);
        this.mainScreen.setLocation(500, 300);
        this.mainScreen.setResizable(false);
        this.mainScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.progressBarConfig();
        this.layoutIpConfig();
        this.layoutAppConfig();
        this.mainScreen.setVisible(true);
    }

    private void layoutIpConfig() {
        ipText = new JLabel("IP address");
        ipPortText = new JLabel("Port number");
        ipPortField = new JTextField();
        ipField = new JTextField();
        hostButton = new JButton("Host");
        connectButton = new JButton("Connect");

        hostButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!ipField.getText().equals("") && !ipPortField.getText().equals("")) {
                    ipName = ipField.getText();
                    portName = ipPortField.getText();

                    try {
                        stubServer = new FileTransferImplServer();
                        Registry registry = LocateRegistry.createRegistry(Integer.parseInt(portName));
                        Naming.rebind("rmi://" + ipName + ":" + portName + "/FTServer", stubServer);

                        clientMode = false;
                        connected = true;
                        hostButton.setEnabled(false);
                        connectButton.setEnabled(false);

                        JOptionPane.showMessageDialog(null, "Hosting.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    } catch (NumberFormatException | RemoteException | MalformedURLException evnt) {
                        JOptionPane.showMessageDialog(null, "Error was ocurred hosting connection.", "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Blank IP/Port field.", "Input/Output Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                if (!ipField.getText().equals("") && !ipPortField.getText().equals("")) {
                    ipName = ipField.getText();
                    portName = ipPortField.getText();

                    try {
                        stubClient = new FileTransferImplClient();
                        Naming.rebind("rmi://" + ipName + ":" + portName + "/FTClient", stubClient);
                        stubServer = (IFileTransferServer) Naming
                                .lookup("rmi://" + ipField.getText() + ":" + ipPortField.getText() + "/FTServer");

                        clientMode = true;
                        connected = true;
                        hostButton.setEnabled(false);
                        connectButton.setEnabled(false);

                        JOptionPane.showMessageDialog(null, "Connected.", "Success", JOptionPane.INFORMATION_MESSAGE);

                    } catch (MalformedURLException | RemoteException | NotBoundException evnt) {
                        JOptionPane.showMessageDialog(null, "Error was ocurred in connection.", "Connection Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Blank IP/Port field.", "Input/Output Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        this.mainScreen.add(ipPortField);
        this.mainScreen.add(ipPortText);
        this.mainScreen.add(ipText);
        this.mainScreen.add(ipField);
        this.mainScreen.add(hostButton);
        this.mainScreen.add(connectButton);

        ipText.setBounds(285, 30, 100, 14);
        ipField.setBounds(285, 60, 130, 16);
        ipPortText.setBounds(435, 30, 100, 14);
        ipPortField.setBounds(435, 60, 50, 16);
        hostButton.setBounds(410, 90, 80, 24);
        connectButton.setBounds(300, 90, 90, 24);
    }

    private void progressBarConfig() {
        progressBar = new JProgressBar();
        progressBar.setForeground(Color.GREEN);
        progressBar.setStringPainted(true);

        this.mainScreen.add(progressBar);

        progressBar.setBounds(50, 505, 700, 30);
    }

    private void layoutAppConfig() {
        fChooser = new JFileChooser();

        // Change the name "Cancel" to "Delete".
        UIManager.put("FileChooser.cancelButtonText", "Clear");
        SwingUtilities.updateComponentTreeUI(fChooser);

        // Change the name "Send" to "Delete".
        fChooser.setApproveButtonText("Send");
        fChooser.setBorder(BorderFactory.createLineBorder(Color.darkGray));

        this.mainScreen.add(fChooser);

        fChooser.setBounds(50, 135, 700, 350);

        fChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

                    if (connected) {
                        File f = fChooser.getSelectedFile();

                        try {
                            if (clientMode) {
                                progressBar.setMinimum(0);
                                progressBar.setMaximum(
                                        ((f.length() % 60000) == 0) ? (int) (f.length() / 60000)
                                                : (int) ((f.length() / 60000) + 1));

                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            int bytes = 0;
                                            byte[] buffer = new byte[60000];
                                            FileInputStream inputStream = new FileInputStream(f);
                                            File fOutput = new File(f.getName());

                                            while ((bytes = inputStream.read(buffer)) != -1) {
                                                stubServer.send(fOutput, buffer, bytes);
                                                progressBar.setValue(progressBar.getValue() + 1);
                                            }

                                            inputStream.close();
                                        } catch (IOException e) {
                                            JOptionPane.showMessageDialog(null, "Erro was ocurred with archive value.",
                                                    "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }

                                        progressBar.setValue(0);
                                    }
                                }.start();

                            } else {
                                progressBar.setMinimum(0);
                                progressBar.setMaximum(
                                        ((f.length() % 60000) == 0) ? (int) (f.length() / 60000)
                                                : (int) ((f.length() / 60000) + 1));

                                if (!isClientBinded) {
                                    stubClient = (IFileTransferClient) Naming
                                            .lookup("rmi://" + ipName + ":" + portName + "/FTClient");
                                }

                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            int bytes = 0;
                                            byte[] buffer = new byte[60000];
                                            FileInputStream inputStream = new FileInputStream(f);
                                            File fOutput = new File(f.getName());

                                            while ((bytes = inputStream.read(buffer)) != -1) {
                                                stubClient.send(fOutput, buffer, bytes);
                                                progressBar.setValue(progressBar.getValue() + 1);
                                            }

                                            inputStream.close();
                                        } catch (IOException e) {
                                            JOptionPane.showMessageDialog(null, "Erro was ocurred with archive value.",
                                                    "Error",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }

                                        progressBar.setValue(0);
                                    }
                                }.start();
                            }
                        } catch (MalformedURLException | RemoteException | NotBoundException e1) {
                            JOptionPane.showMessageDialog(null, "Failed to bind client.",
                                    "Server error.",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
                    fChooser.setSelectedFile(new File(""));
                }
            }
        });
    }
}