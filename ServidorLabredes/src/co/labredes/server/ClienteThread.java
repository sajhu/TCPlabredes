package co.labredes.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

import co.labredes.common.Constantes;

public class ClienteThread extends Thread {

	private SSLSocket socket;
	private BufferedReader in;
	private PrintWriter out;
	private MainServer server;

	private long id;

	public Object objeto;
	
	private String pathArchivo;
	private String pathConvertido;
	private String fileName;

	public ClienteThread(MainServer main, SSLSocket clienteSocket, long contador) throws Exception {

		server = main;
		socket = clienteSocket;
		id = contador;
		objeto = new Object();
	}

	public void console(String msg)
	{
		System.out.println("T" + id + ": " + msg);
	}

	public void cerrar()
	{
		send(Constantes.CLOSING_CONNECTION);

		try {
			out.close();
			in.close();
			socket.close();


		} catch (IOException e) {

			e.printStackTrace();
		}
		server.quitarIpLista(socket.getRemoteSocketAddress().toString().split(":")[0]);
	}

	public void send(int comando)
	{
		out.println(comando);
		//console("S -> " + comando);
	}
	

	
	public void sendContinue()
	{
		send(Constantes.CONTINUE);
	}

	public void error(int type) throws Exception
	{
		out.println("ERROR"+ Constantes.SEPARATOR +" " + type);
		console("S -> " + "ERROR: " + type);

		// para terminar la conexión
		throw new Exception("ERROR"+ Constantes.SEPARATOR +" " + type);
	}

	public String getParameter(String name) throws Exception
	{
		String msg = in.readLine();
		String response = "";
		//console("C -> " + msg);

		if(msg.startsWith(name) && msg.contains(Constantes.SEPARATOR))
		{
			response = msg.split(Constantes.SEPARATOR)[1];
		}
		else
		{
			//error(Constantes.INVALID_PROTOCOL);
		}

		return response;
	}

	public void run()
	{
		try {
			console("Estableciendo conexión con " + socket.getRemoteSocketAddress());

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


			getParameter(Constantes.REQUEST_CONVERT);

			sendContinue();
			
			
			int tiempoConectando = Integer.parseInt(getParameter(Constantes.TIME_CONNECTING));
			
			long afterConectado = System.currentTimeMillis();
			
			//TODO recibir el archivo y darle los paths unicos al convertidor
			recibirArchivo();
			
			send(Constantes.FILE_RECIEVED);
			
			console("Recibido archivo " + pathArchivo);
			
			long inicioCola = System.currentTimeMillis();
			
			// Se crea el convertidor
			Convertidor convertidor = new Convertidor(this, pathArchivo, pathConvertido);

			// se envía a convertir, encolandonos
			server.convertir(convertidor);

			doWait(); // dormimos el thread mientras se convierte el archivo

			int resultado = convertidor.darResultado();

			send(resultado);
			
			(new File(pathArchivo)).delete();

			
			long finCola = System.currentTimeMillis();

			transmitir();

			//String tiempoCola = getParameter(Constantes.TIME_WAITED);

			long fin = System.currentTimeMillis();
			
			server.escribirLog(id + "," +tiempoConectando + "," + (finCola-inicioCola) + "," + (fin-afterConectado + tiempoConectando));
			
			(new File(pathConvertido)).delete();


		} catch (Exception e) {

			//e.printStackTrace();

			send(Constantes.ERROR_500);
			console("ERROR thread: " + e.getMessage());
		}

		cerrar();
		console("conexión terminada.");

	}

	@SuppressWarnings("unused")
	private void recibirArchivo() throws Exception 
	{
		fileName = getParameter(Constantes.FILE_NAME);
		pathArchivo 		= "./data/"+ id +"_" + fileName + ".avi";
		pathConvertido 	= "./data/"+ id +"_" + fileName + ".mp4";
		
		//int tamano = Integer.parseInt(getParameter(Constantes.FILE_SIZE));
		
		
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
		      // console("Se creó el archivo, ahora se debe escribir");
		       File archivoAVI = new File(direccion);
		        
		    } catch (FileNotFoundException ex) {
		    	console("No se encontró el archivo especificado ");
		    }

		    byte[] bytesRecibidos = new byte[tamanoBuffer];

		    int cantidad=0;
		    int entrada = 0;
		    while ((cantidad = is.read(bytesRecibidos)) > 0 && entrada<2815 ) {
		        bos.write(bytesRecibidos, 0, cantidad);			       
		        //console("Está escribiendo cantidad " + cantidad + " entrada: " + entrada);
			    bos.flush();
			    entrada++;

		    }
		    
		    bos.close();
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		
	}
	
private void transmitir() throws Exception {
		

		File archivoAVI = new File(pathConvertido);

		// Get the size of the file
		long longitud = archivoAVI.length();
		if (longitud > Integer.MAX_VALUE) {
			System.out.println("El archivo es demasiado largo.");
		}
		
		
		
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

	public long darId()
	{
		return id;
	}

	public void doWait(){
		synchronized(objeto){
			try{
				objeto.wait();
			} catch(InterruptedException e){

			}
		}
	}

}
