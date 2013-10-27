package co.labredes.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
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
	private String nombreArchivo;

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
		console("S -> " + comando);
	}

	public void sendContinue()
	{
		out.println(Constantes.CONTINUE);
		console("S -> " + Constantes.CONTINUE);
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
		console("C -> " + msg);

		if(msg.startsWith(name) && msg.contains(Constantes.SEPARATOR))
		{
			response = msg.split(Constantes.SEPARATOR)[1];
		}
		else
		{
			error(Constantes.INVALID_PROTOCOL);
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
			
			
			String tiempoConectando = getParameter(Constantes.TIME_CONNECTING);
			
			
			//TODO recibir el archivo y darle los paths unicos al convertidor
			recibirArchivo();
			
			send(Constantes.FILE_RECIEVED);
			
			console("Recibido archivo " + nombreArchivo);
			
			// Se crea el convertidor
			Convertidor convertidor = new Convertidor(this, "./data/Wildlife.avi", "./data/"+ id +"-wildlifeNew.mp4");

			// se envía a convertir, encolandonos
			server.convertir(convertidor);

			doWait(); // dormimos el thread mientras se convierte el archivo

			int resultado = convertidor.darResultado();

			send(resultado);

			String tiempoCola = getParameter(Constantes.TIME_WAITED);

			server.escribirLog(id + "\t" +tiempoConectando + "\t" + tiempoCola);





		} catch (Exception e) {

			e.printStackTrace();

			send(Constantes.ERROR_500);
			console("ERROR thread: " + e.getMessage());
		}

		cerrar();
		console("conexión terminada.");

	}

	@SuppressWarnings("unused")
	private void recibirArchivo() throws Exception 
	{
		nombreArchivo = "./data/"+ id +"_" + getParameter(Constantes.FILE_NAME)+".avi";
		int tamano = Integer.parseInt(getParameter(Constantes.FILE_SIZE));
		
		
		 int tamanoBuffer = 0;
	        InputStream is = null;
	        FileOutputStream fos = null;
	        BufferedOutputStream bos = null;
	        
	        try {
	           is = socket.getInputStream();
	           tamanoBuffer = socket.getReceiveBufferSize();
	           String direccion = nombreArchivo;
		       fos = new FileOutputStream(direccion);
		       bos = new BufferedOutputStream(fos);
		       console("Se creó el archivo, ahora se debe escribir");
		       File archivoAVI = new File(direccion);
		        
		    } catch (FileNotFoundException ex) {
		    	console("No se encontró el archivo especificado ");
		    }

		    byte[] bytesRecibidos = new byte[tamanoBuffer];

		    int cantidad=0;
		    int entrada = 0;
		    console("Is" + is);
		    while ((cantidad = is.read(bytesRecibidos)) > 0 && entrada<2815 ) {
		        bos.write(bytesRecibidos, 0, cantidad);			       
		        //console("Está escribiendo cantidad " + cantidad + " entrada: " + entrada);
			    bos.flush();
			    entrada++;

		    }
		
		
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
