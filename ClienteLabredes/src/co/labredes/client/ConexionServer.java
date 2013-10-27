package co.labredes.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;


import co.labredes.common.*;



import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class ConexionServer extends Thread {

	private static final String HOST = "localhost";
	private static final int PORT = 9999;

	public static final String nombreArchivo = "Wildlife";


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

			// tenemos conexión, empezamos protocolo

			comando(Constantes.REQUEST_CONVERT, 1);

			String respuestaServidor ="";



			while(!in.readLine().equals("0")) // esperamos a que nos den permiso
			{ }

			// ya nos llegó el continue
			finCola = System.currentTimeMillis();

			comando(Constantes.TIME_CONNECTING, (finCola - inicio));
			
			// transmitimos
			transmitir();

			respuestaServidor =  in.readLine(); 
			System.out.println(respuestaServidor);
			if(!respuestaServidor.equals("2"))
				System.out.println("el servidor no lo recibió");
			
			// Esperamos la cola de conversión
			
			respuestaServidor = in.readLine(); // resultado de la conversión
			System.out.println(respuestaServidor);
			finEspera = System.currentTimeMillis();
			System.out.println("WAITED:::" + (finEspera - finCola));
			
			comando(Constantes.TIME_WAITED, (finEspera - finCola));


		}
		catch(Exception e)
		{
			System.out.println("ERROR catch: " + e.getMessage());
			//e.printStackTrace();
		}

		cerrar();

	}

	private void transmitir() throws Exception {
		
		comando(Constantes.FILE_NAME, nombreArchivo);

		File archivoAVI = new File("./data/"+nombreArchivo+".avi");

		// Get the size of the file
		long longitud = archivoAVI.length();
		if (longitud > Integer.MAX_VALUE) {
			System.out.println("El archivo es demasiado largo.");
		}
		
		comando(Constantes.FILE_SIZE, longitud);
		
		
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
		
	    outfile.close();
	    bis.close();

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
