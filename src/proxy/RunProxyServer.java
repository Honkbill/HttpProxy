package proxy;

public class RunProxyServer {

	public static void main(String[] args) {
		if(args.length == 1) {
			try {
				int port = Integer.parseInt(args[0]);
				if(port >= 0 && port < 65_536)
					new ProxyServer(port).listenSocket();
				else
					System.out.println("The port number is out of bounds");
				
			} catch (NumberFormatException e) {
				System.out.println("The given parameter is not a number.");
			}
		}
		else
			System.out.println("Wrong number of parameters.");
	}

}
