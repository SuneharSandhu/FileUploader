import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Client extends JFrame {

	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JTextField textField;
	private JTextArea textArea;
	private JButton btnConnectAndUpload;
	
	private File file;
	
	private final int PORT = 5520;
	
	private Socket sock = null;         
    private DataOutputStream writeSock;    
    private BufferedReader readSock;    

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Client() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frame.getContentPane().setLayout(springLayout);
		
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MSWord", "doc", "docx");
		chooser.setFileFilter(filter);
		
		springLayout.putConstraint(SpringLayout.NORTH, chooser, 10, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, chooser, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, chooser, 290, SpringLayout.NORTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, chooser, 740, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(chooser);
		
		JLabel serverAddress = new JLabel("Server Address:");
		springLayout.putConstraint(SpringLayout.NORTH, serverAddress, 19, SpringLayout.SOUTH, chooser);
		springLayout.putConstraint(SpringLayout.WEST, serverAddress, 116, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, serverAddress, -527, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(serverAddress);
		
		textField = new JTextField("constance.cs.rutgers.edu");
		springLayout.putConstraint(SpringLayout.NORTH, textField, -5, SpringLayout.NORTH, serverAddress);
		springLayout.putConstraint(SpringLayout.WEST, textField, 6, SpringLayout.EAST, serverAddress);
		springLayout.putConstraint(SpringLayout.EAST, textField, -286, SpringLayout.EAST, frame.getContentPane());
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		btnConnectAndUpload = new JButton("Connect and Upload");
		springLayout.putConstraint(SpringLayout.NORTH, btnConnectAndUpload, -5, SpringLayout.NORTH, serverAddress);
		springLayout.putConstraint(SpringLayout.WEST, btnConnectAndUpload, 6, SpringLayout.EAST, textField);
		frame.getContentPane().add(btnConnectAndUpload);
		
		textArea = new JTextArea();
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 35, SpringLayout.SOUTH, btnConnectAndUpload);
		springLayout.putConstraint(SpringLayout.WEST, textArea, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, textArea, -10, SpringLayout.SOUTH, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, textArea, 740, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(textArea);
		
		JLabel lblErrorMessages = new JLabel("Error Messages:");
		springLayout.putConstraint(SpringLayout.WEST, lblErrorMessages, 10, SpringLayout.WEST, frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, lblErrorMessages, -6, SpringLayout.NORTH, textArea);
		springLayout.putConstraint(SpringLayout.EAST, lblErrorMessages, 117, SpringLayout.WEST, frame.getContentPane());
		frame.getContentPane().add(lblErrorMessages);
		
		btnConnectAndUpload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnConnectAndUpload.getText().equals("Connect and Upload")) {
					openSocket();
					if (sock.isConnected()) {
						textArea.append("Connected to Server!\n");
						
						file = chooser.getSelectedFile();
						String fileName = file.getName();
						String fileLength = Long.toString(file.length()); 
						String fullPathFileName = file.getAbsolutePath();
						
						sendNullTerminatedString(fileName); 
						sendNullTerminatedString(fileLength); 
						textArea.append("Sent file name: " + fileName + "\n"); 
						textArea.append("Sent file length: " + fileLength + "\n"); 
						
						sendFile(fullPathFileName);
			
					}
					btnConnectAndUpload.setText("Disconnect");
				}
				else {
					closeSocket();
				}
			}
		});
	}
	
	private void openSocket() {
		try {
			String hostAddr = textField.getText();
			sock = new Socket(hostAddr, PORT);
			writeSock = new DataOutputStream(sock.getOutputStream());  
			readSock = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} 
		catch (Exception e) {
			textArea.append("Error: " + e.getMessage() + "\n");
            sock = null;   
		}
	}
	
	private void closeSocket() {
		try {
			readSock.close();
			writeSock.close();
			sock.close();
			textArea.append("Disconnected!\n");
			btnConnectAndUpload.setText("Connect and Upload");
		} 
		catch (Exception e) {
			textArea.append("Error: " + e.getMessage() + "\n");
            sock = null;    
		}
	}
	
	private void sendNullTerminatedString(String s) {
		try {
			byte[] b = s.getBytes();
			writeSock.write(b);
			writeSock.write('\0');
		}
		catch (Exception e) {
			textArea.append("Error: " + e.getMessage() + "\n");
		}
	}
	
	private void sendFile(String fullPathFileName) {
		byte[] buffer = new byte[1024];
		int bytesLeft = (int) file.length();
		textArea.append("Sending file...\n");
		
		try {
			FileInputStream fileIn = new FileInputStream(fullPathFileName);
			
			while (bytesLeft > 0) {
				int dataRead = fileIn.read(buffer, 0, Math.min(bytesLeft, buffer.length));
				writeSock.write(buffer);
				bytesLeft -= dataRead;
			}
			writeSock.write('\0');
			textArea.append("File sent. Waiting for the server...\n");
			int read = readSock.read();
			if (read == 64) {
				textArea.append("Upload O.K.\n");
				closeSocket();
			}
			fileIn.close();
			writeSock.close();
		}
		catch (Exception e) {
			textArea.append("Error: " + e.getMessage() + "\n");
			closeSocket();
		}
	}
}
