// A client-side class that uses a secure TCP/IP socket

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class SecureClient {
	private InetAddress host;
	private int port;
	// This is not a reserved port number 
	static final int DEFAULT_PORT = 8189;
	
	static final String KEYSTORE = "PIERkeystore.ks";
	static final String TRUSTSTORE = "PIERtruststore.ks";
	static final String STOREPASSWD = "111111";
	static final String ALIASPASSWD = "111111";
	
	static final String TERMINATE = "TERMINATE";
	static final String END = "END";
	static final String DOWNLOAD = "DOWNLOAD";
	static final String UPLOAD = "UPLOAD";
	static final String DELETE = "DELETE";
	
	static final String PATH = "files/";
	
	BufferedReader socketIn;
	PrintWriter socketOut;
	static Scanner keyboard = new Scanner(System.in);
	
	// Constructor @param host Internet address of the host where the server is located
	// @param port Port number on the host where the server is listening
	public SecureClient( InetAddress host, int port ) {
		this.host = host;
		this.port = port;
	}
	
	  // The method used to start a client object
		public void run() {
			try {
				KeyStore ks = KeyStore.getInstance( "JCEKS" );
				ks.load( new FileInputStream( KEYSTORE ), STOREPASSWD.toCharArray() );
				
				KeyStore ts = KeyStore.getInstance( "JCEKS" );
				ts.load( new FileInputStream( TRUSTSTORE ), STOREPASSWD.toCharArray() );
				
				KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
				kmf.init( ks, ALIASPASSWD.toCharArray() );
				
				TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
				tmf.init( ts );
				
				SSLContext sslContext = SSLContext.getInstance( "TLS" );
				sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
				SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
				SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port);
				client.setEnabledCipherSuites( client.getSupportedCipherSuites() );
				System.out.println("\n>>>> SSL/TLS handshake completed");

				socketIn = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
				socketOut = new PrintWriter( client.getOutputStream(), true );
				
		    
			}catch( Exception x ) {
				System.out.println( "SOCKET CLOSED" );
				x.printStackTrace();
			}
		}
		
		public void DownLoad() {
			
			// Entering the filename
			System.out.println("enter the filenamn");
			String fileName = keyboard.nextLine();
			
			// Starting the process and sending the filename 
			socketOut.println(DOWNLOAD);
			socketOut.println(fileName);
			socketOut.println(END);
			
			try {
				String content;
				StringBuilder build = new StringBuilder();
				// Saving the content of the file
				while((content = socketIn.readLine()).equals(END))
				{
					build.append(content);
				}
				
				// Building the file with the content
	            try (BufferedWriter writer = new BufferedWriter(
	                    new FileWriter(PATH + fileName))) {
	                writer.write(content, 0, content.length());
	            } catch (IOException x) {
	                System.err.format("IOException: %s%n", x);
	            }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void UpLoad() {
			System.out.println("enter the filenamn");
			String fileName = keyboard.nextLine();
			
			// Starting the process 
			socketOut.println(UPLOAD);
			socketOut.println(fileName);
			socketOut.println(END);
			
			// Reads the content and sends it
            try (BufferedReader reader = new BufferedReader(
                    new FileReader(PATH  + fileName))) {
            	String content;
            	while ((content = reader.readLine()) != null) {
            		//System.out.println("CONTENT: " + content);
                    socketOut.println(content);
                }
            	socketOut.println(END);
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
				
		}

		public void Delete() {
			try {
			System.out.println("enter the filenamn");
			String file = keyboard.nextLine();
			socketOut.println(DELETE);
			socketOut.println(file);
			socketOut.println(END);
			System.out.println( socketIn.readLine() );
			} catch (IOException e) {
				System.out.println( "Something went wrong" );
				e.printStackTrace();
			}
		}
		
	public static void main(String[] args) {
		
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			SecureClient addClient = new SecureClient( host, port );
			addClient.run();
			
			String choose;
	        boolean run = true;
	        
			while(run) {
				
				System.out.println("1: Download file");
				System.out.println("2: Upload file");
				System.out.println("3: Delete file");
				
				choose = keyboard.nextLine();
				switch(choose)
				{
					case DOWNLOAD:
						addClient.DownLoad();
						break;
					case UPLOAD:
						addClient.UpLoad();
						break;
					case DELETE:
						addClient.Delete();
						break;
					case TERMINATE:
						run = false;
					default:
						break;
				}
			}
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}

	}

}
