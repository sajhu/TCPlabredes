package co.labredes.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import co.labredes.common.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class ConexionServer extends Thread {

	private static final String HOST = "localhost";
	private static final int PORT = 9999;

	public static String nombreArchivo = "Wildlife";


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


		}
		catch(Exception e)
		{
			System.out.println("ERROR constructor: " + e.getMessage());
			//e.printStackTrace();
		}

		

	}
	
	@Override
	public void run() {
		super.run();
		
		try
		{

			// tenemos conexión, empezamos protocolo

			comando(Constantes.REQUEST_CONVERT, 1);

			getResponse(Constantes.CONTINUE);
	

			// ya nos llegó el continue
			finCola = System.currentTimeMillis();

			comando(Constantes.TIME_CONNECTING, (finCola - inicio));
			
			// transmitimos
			transmitir();

			if(!getResponse(Constantes.FILE_RECIEVED))
				System.out.println("el servidor no lo recibió");
			
			// Esperamos la cola de conversión
			
			
			// resultado de la conversión			
			if(!getResponse(Constantes.CONVERTION_FINISHED))
				System.out.println("el servidor no lo convirtió bien");
			
			
			finEspera = System.currentTimeMillis();
			
			recibirArchivo();
			
			out = new PrintWriter(socket.getOutputStream(), true);

			comando(Constantes.TIME_WAITED, (finEspera - finCola));
	
			
			//getResponse(Constantes.CLOSING_CONNECTION);


		}
		catch(Exception e)
		{
			System.out.println("ERROR run: " + e.getMessage());
			e.printStackTrace();
		}

		cerrar();
		
	}
	
	
	@SuppressWarnings("unused")
	private void recibirArchivo() throws Exception 
	{
		String pathArchivo = "./data/" + nombreArchivo+".mp4";
		
		
		 int tamanoBuffer = 0;
	        InputStream is = null;
	        FileOutputStream fos = null;
	        BufferedOutputStream bos = null;
	        
	        try {
	           is = socket.getInputStream();
	           tamanoBuffer = socket.getReceiveBufferSize();
	           String direccion = pathArchivo;
		       fos = new FileOutputStream(direccion);
		       bos = new BufferedOutputStream(fos);
		      // System.out.println("Se creó el archivo, ahora se debe escribir");
		       File archivoAVI = new File(direccion);
		        
		    } catch (FileNotFoundException ex) {
		    	System.out.println("No se encontró el archivo especificado ");
		    }

		    byte[] bytesRecibidos = new byte[tamanoBuffer];

		    int cantidad=0;
		    int entrada = 0;
		    while ((cantidad = is.read(bytesRecibidos)) > 0 && entrada<2815 ) {
		        bos.write(bytesRecibidos, 0, cantidad);			       
		       // System.out.println("Está escribiendo cantidad " + cantidad + " entrada: " + entrada);
			    bos.flush();
			    entrada++;

		    }
		    
		    bos.close();
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		
	}

	private void transmitir() throws Exception {
		
		comando(Constantes.FILE_NAME, nombreArchivo);

		File archivoAVI = new File("./data/"+nombreArchivo+".avi");

		// Get the size of the file
		long longitud = archivoAVI.length();
		if (longitud > Integer.MAX_VALUE) {
			System.out.println("El archivo es demasiado largo.");
		}
		
		//comando(Constantes.FILE_SIZE, longitud);
		
		
		// SE EMPIEZA EL ENVIO DEL ARCHIVO
		
	    byte[] bytesAVI = new byte[(int) longitud];
	    FileInputStream fis = new FileInputStream(archivoAVI);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedOutputStream outfile = new BufferedOutputStream(socket.getOutputStream());

	    int cantidadAVI;

	    while ((cantidadAVI = bis.read(bytesAVI)) > 0) {
	    	outfile.write(bytesAVI, 0, cantidadAVI);		       
	    }

	    outfile.flush();
	
	    bis.close();
	    //outfile.close();

	}

	private void comando(String comando, String valor)
	{
		out.println(comando + Constantes.SEPARATOR + valor);
		System.out.println("ENVIADO: " + comando + Constantes.SEPARATOR + valor);

	}
	private void comando(String comando, long valorL)
	{
		out.println(comando + Constantes.SEPARATOR + valorL);
		System.out.println("ENVIADO: " + comando + Constantes.SEPARATOR + valorL);

	}
	private void comando(String comando, int valorI)
	{
		out.println(comando + Constantes.SEPARATOR + valorI);
		System.out.println("ENVIADO: " + comando + Constantes.SEPARATOR + valorI);

	}
	
	public boolean getResponse(int comando) throws Exception
	{
		String msg = in.readLine();
		System.out.println("RECIBIDO: " + msg);

		if(msg.equals("" + comando))
			return true;
		else 
			return false;
	}
	
	public void cerrar(){
		try {
			System.out.println("Conexión terminada");
			socket.close();
		} catch (Exception e) {
			// SE FORZA EL CIERRE DE SESIÓN
		}

	}

	public static void main(String[] args) {
		ConexionServer c = new ConexionServer();
		c.start();
	}
}
