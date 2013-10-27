package co.labredes.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

public class ClienteThread extends Thread {
	
	private SSLSocket socket;
	private BufferedReader in;
	private PrintWriter out;
	private MainServer server;
	
	private long id;
	
	public Object objeto;
	
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
	        String dataRecieved = in.readLine();
	        console("C -> " +dataRecieved);

	        if(dataRecieved.equals(Constantes.REQUEST_CONVERT))
	        {
	        	//puedo encolarlo
	        	send(Constantes.QUEUED);
	        
	        	// en este momento aquí no se hace la cola sino cuando se envía directamente a convertir
	        	//por lo que "tiempo en cola" recibido será 0.
	        	
	        	//TODO recibir el archivo y darle los paths unicos al convertidor
	        	sendContinue();
	        	 String tiempo = getParameter(Constantes.TIME_CONNECTING);
	        	 
	        	 
	        	// Se crea el convertidor
	        	Convertidor convertidor = new Convertidor(this, "./data/Wildlife.avi", "./data/"+ id +"-wildlifeNew.mp4");
	        	
	        	server.convertir(convertidor);
	        	
	        	doWait(); // dormimos el thread mientras se convierte el archivo
	        	
	        	int resultado = convertidor.darResultado();
	        	
	        	send(resultado);
	        	
	        	String tiempocola = getParameter(Constantes.TIME_WAITED);
	        	
	        	server.escribirLog(id + "\t" +tiempo + "\t" + tiempocola);

	        }
	        else
	        	error(Constantes.INVALID_PROTOCOL);
	        	      
	        
	        
	         
		} catch (Exception e) {
			
			cerrar();
			e.printStackTrace();

			
			console("ERROR catch: " + e.getMessage());
		}
		
		cerrar();
		console("conexión terminada.");

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
