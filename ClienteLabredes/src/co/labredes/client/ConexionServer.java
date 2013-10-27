package co.labredes.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;



import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class ConexionServer extends Thread {
	
	private static final String HOST = "localhost";
	private static final int PORT = 9999;
	
	
	
	private SSLSocket socket;
	private PrintWriter out;
	private BufferedReader in;
	private long inicio;
	private long finCola;
	private long finEspera;
	
	/**
	 * @param args
	 */
	public ConexionServer() {
		
		try
		{
			System.out.println("Conectando al servidor " + HOST + ":" + PORT);
	    	inicio = System.currentTimeMillis();

			SSLSocketFactory factorySSL = (SSLSocketFactory)SSLSocketFactory.getDefault(); 
	        socket = (SSLSocket) factorySSL.createSocket(HOST, PORT);       
	        String[] cipherSuites = socket.getSupportedCipherSuites(); 
	        socket.setEnabledCipherSuites(cipherSuites);     
	        socket.startHandshake(); 
			
		   // socket = new Socket(HOST, PORT);
	        
		    out = new PrintWriter(socket.getOutputStream(), true);
		    in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
		    
		    System.out.println("Connection established.");

		    
		    out.println("CONVERT SERVICE REQUEST");
		    
		    String respuestaServidor = in.readLine();
		    System.out.println(respuestaServidor);
		    

		    if(respuestaServidor.equals("0"))
		    {
		    	// enviar tiempo en cola 0;
		    	//transmitir();
		    }
		    else if(respuestaServidor.equals("1"))
		    {
		    	// nos encolamos
		    	// empezamos a contabilizar
		    }
		    
		   	while(!in.readLine().equals("0")) // esperamos a que nos den permiso
		    { }
		   	
		   	// ya nos lleg� el continue
	    	 finCola = System.currentTimeMillis();
	    	 
	    	 out.println("TCON:::" + (finCola - inicio));
	    	 // transmitimos
	    	 // transmitir();
	    	 
		   	respuestaServidor = in.readLine(); // resultado de la conversi�n
		   	System.out.println(respuestaServidor);
	    	 finEspera = System.currentTimeMillis();
	    	 out.println("WAITED:::" + (finEspera - finCola));

		   	
		   	
		    
		    
		    	
		}
		catch(Exception e)
		{
			//System.out.println("ERROR catch: " + e.getMessage());
			//e.printStackTrace();
		}
		
		cerrar();
	
	 }

	public void cerrar(){
		try {
			System.out.println("Conexi�n terminada");
			socket.close();
		} catch (Exception e) {
			// SE FORZA EL CIERE DE SESI�N
		}
		 
	}
	   
	 public static void main(String[] args) {
		ConexionServer c = new ConexionServer();
		c.start();
	}
}