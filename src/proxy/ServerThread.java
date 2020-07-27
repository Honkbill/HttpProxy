package proxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import utility.LineReader;

public class ServerThread extends Thread{
	protected Socket sClient;

	public ServerThread(Socket socket) {
		super();
		this.sClient = socket;
	}
	
	public void run() {
		Socket sServer = null;
		
		try {
			LineReader in = new LineReader(sClient.getInputStream());
			
			String headers;
			StringBuffer sb = new StringBuffer();
			
			String line;
			while((line = in.readLine()) != null && !line.isEmpty()) {
				System.out.println("[c] " + line);
				sb.append(line + '\n');
			}
			headers = sb.toString();
			System.out.println();
			
			//tu przerobienie headerow
			//wydobycie url z request
			String url;
			if(headers.contains("https://") || headers.contains("CONNECT"))
				throw new HTTPSException();
				
			if(!headers.contains("Host: ")) {
				url = headers.substring(headers.indexOf(' ')+8, headers.indexOf('\n')); //8 bo ta spacja i http://
				url = url.substring(0, url.indexOf('/'));
				headers = headers.substring(0, headers.indexOf('\n')) + "Host: " + url + headers.substring(headers.indexOf('\n'+1));
			}
			else {
				url = headers.substring(headers.indexOf("Host: ")+6);
				url = url.substring(0, url.indexOf('\n'));
			}
			
			//trzeba pozbyc sie url z requesta bo to tylko dla proxy
			headers = headers.substring(0, headers.indexOf(' ')+1) + headers.substring(headers.indexOf(url) + url.length());
			
			//trzeba zmienic bo tez tylko dla proxy
			if(headers.contains("Proxy-Connection: keep-alive"))
				headers = headers.replace("Proxy-Connection: keep-alive", "Proxy-Connection: close");
			
			//teraz trzeba wydobyc adres i nr portu, a jak nie ma nr portu to domyslnie 80
			String address;
			int port = 80;
			if(url.contains(":")) {
				address = url.substring(0, url.indexOf(':'));
				port = Integer.parseInt(url.substring(url.indexOf(':')+1));
			}
			else
				address = url;
			
			//rozpoczecie komunikacji
			try {
				if(port == 443)
					throw new HTTPSException();
				
				sServer = new Socket(address, port);
				
				PrintWriter out = new PrintWriter(sServer.getOutputStream(), true);
				
				//przeslanie headerow do serwera
				String[] tmp = headers.split("\n");
				for(int i = 0; i < tmp.length; i++) {
					System.out.println("[s] " + tmp[i]);
					out.println(tmp[i]);
				}
				System.out.println();
				out.println();
				
				byte[] request = new byte[2_048];
				byte[] reply = new byte[request.length*2];
				 
				DataInputStream inClient = new DataInputStream(sClient.getInputStream());
				DataOutputStream outClient = new DataOutputStream(sClient.getOutputStream());
				
				DataInputStream inServer = new DataInputStream(sServer.getInputStream());
				DataOutputStream outServer = new DataOutputStream(sServer.getOutputStream());
				
				//watek na przesylanie danych klient -> serwer
				new Thread() {
					public void run() {
						try {
							int len;
							while((len = inClient.read(request)) != -1) {
								outServer.write(request, 0, len);
								outServer.flush();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}.start();
				
				//obecny watek przesyla dane serwer -> klient
				try {
					int len;
					while((len = inServer.read(reply)) != -1) {
						outClient.write(reply, 0, len);
						outClient.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch(UnknownHostException e) {
				PrintWriter o = new PrintWriter(sClient.getOutputStream());
				o.println("HTTP/1.1 404 Not Found");
				o.println();
				o.println();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HTTPSException e) {
			PrintWriter o;
			try {
				o = new PrintWriter(sClient.getOutputStream());
				o.println("HTTP/1.1 501 Not Implemented");
				o.println();
				o.println();
				
				sClient.close();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
