import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
	
	private final int PORT = 5520;
	
	private Socket sock;
	private ServerSocket servSock; 
	private PrintStream ps = null;
	private FileOutputStream out = null;
	private DataInputStream in = null;
	
	public static void main(String[] args) {
		
		try {
			Server server = new Server();
			server.run();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
	
	public void run() {
		try {
			servSock = new ServerSocket(PORT);
			System.out.println("Server running..."); 
			System.out.println("Waiting for connection...");
						
			while(true) {
				sock = servSock.accept();
				System.out.println("Got a connection: " + new Date().toString());
				System.out.println("Connected to: " + sock.getInetAddress() + " Port: " + PORT);
				
				in = new DataInputStream(new BufferedInputStream(sock.getInputStream()));
				
				String fileName = getNullTerminatedString();
				System.out.println("Got file name: " + fileName);
				
				String fileSize = getNullTerminatedString();
				System.out.println("File size: " + fileSize);
				
				long length = Long.parseLong(fileSize);
				
				getFile(fileName, length);
				
				System.out.println("Waiting for connection...\n");
			}
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			sock = null;
		}
	}
	
	private String getNullTerminatedString() {
		
		int buffer;
		String data = "";
		try {
			while ((buffer = in.read()) > 0) {
				data += (char)buffer;
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		return data;
	}
	
	private void getFile(String filename, long size) {
		byte[] buffer = new byte[1024];
		try {
			ps = new PrintStream(sock.getOutputStream());
			int bytesLeft = (int) size;
			out = new FileOutputStream(filename);
			while (bytesLeft > 0) {
				int read = in.read(buffer, 0, Math.min(bytesLeft, buffer.length));
				out.write(buffer);
				out.flush();
				bytesLeft -= read;
			}
			out.flush();
			ps.print('@');
			out.close();
			sock.close();
			System.out.println("Got the file.");
			
		}
		catch (IOException e) {
			ps.print("Failed");
			System.out.println("Error getting file: " + e.getMessage());
		}
	}
	
}
