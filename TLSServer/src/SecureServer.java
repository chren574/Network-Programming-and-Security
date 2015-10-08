
// An example class that uses the secure server socket class

import java.io.*;
import javax.net.ssl.*;
import java.security.*;


public class SecureServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	
	static final String KEYSTORE = "LIUkeystore.ks";
	static final String TRUSTSTORE = "LIUtruststore.ks";
	static final String STOREPASSWD = "111111";
	static final String ALIASPASSWD = "111111";
	
	static final String TERMINATE = "TERMINATE";
	static final String END = "END";
	static final String DOWNLOAD = "DOWNLOAD";
	static final String UPLOAD = "UPLOAD";
	static final String DELETE = "DELETE";
	static final String EXIT = "EXIT";
	
	static final String PATH = "files/";
	
	BufferedReader in;
	PrintWriter out;
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureServer( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
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
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );
			
			System.out.println("\n>>>> SecureAdditionServer: active ");
			sss.setNeedClientAuth(true);
			SSLSocket incoming = (SSLSocket)sss.accept();

			in = new BufferedReader( new InputStreamReader( incoming.getInputStream() ) );
			out = new PrintWriter( incoming.getOutputStream(), true );			
			
			String str;
			while ( !(str = in.readLine()).equals(TERMINATE) ) {
				
				switch(str)
				{
					case DOWNLOAD:
						Send();
						break;
					case UPLOAD:
						Recive();
						break;
					case DELETE:
						Delete();
						break;
					case EXIT:
						break;
					default:
						break;
				}
			}
			incoming.close();
			
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	public void Recive() {
		try {
			String content;
			String fileName = null;
			String temp;
			
			while(!(temp = in.readLine()).equals(END))
			{
				fileName = temp;
			}
			
			// Getting file content
			StringBuilder build = new StringBuilder();
			while(!(content = in.readLine()).equals(END))
			{
				//System.out.println("CONTENT: " + content);
				build.append(content);
			}
			
			// Write to file
			String file = build.toString();
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(PATH + fileName))) {
                writer.write(file, 0, file.length());
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void Send() {
		
		String temp;
		String fileName = null;
		try {
			while(!(temp = in.readLine()).equals(END))
			{
				fileName = temp;
			}
			String content;	
	        try (BufferedReader reader = new BufferedReader(
	                new FileReader(PATH + fileName))) {
	        	while ((content = reader.readLine()) != null) {
	                out.println(content);
	            }
	        	out.println(END);
	        } catch (IOException x) {
	            System.err.format("IOException: %s%n", x);
	        }
	        out.println(END);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}

	public void Delete() {
		try {
			String str;
			String fileName = null;
			while(!(str = in.readLine()).equals(END))
			{
				System.out.print(str);
				fileName = str;
			}
			
			if(fileName != null)
			{
				try {
					File file = new File(PATH + fileName);
					file.delete();
					out.println("The file was deleted");
					out.println(END);
				} catch (Exception e) {
					out.println("The file was NOT deleted");
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR");
			e.printStackTrace();
		}
	}
	
	
	/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureServer addServe = new SecureServer( port );
		addServe.run();
	}
}

