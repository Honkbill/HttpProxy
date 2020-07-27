package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
	private int port;
	
	public ProxyServer(int port) {
		this.port = port;
	}
	
	public ProxyServer() {
		this(0);
	}
	
	public void listenSocket() {
		ServerSocket server = null;
		Socket client = null;
		
		try {
			server = new ServerSocket(port); 
		}
		catch (IOException e) {
			System.out.println("Could not listen");
			System.exit(-1);
		}
		
		System.out.println("Server listens on port: " + server.getLocalPort());
		
		while(true) {
			try {
				client = server.accept();
				System.out.println("Client connected: " + client.getInetAddress());
			}
			catch (IOException e) {
				System.out.println("Accept failed");
				System.exit(-1);
			}
			
			new ServerThread(client).start();
		}
	}
}
