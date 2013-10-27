package co.labredes.server;

import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class MainServer {
	
	
	public static final int PUERTO = 9999;

	public static final boolean PROTECT_FROM_DoS = true;

	private static final int TAMANO_POOL = 20;
	
	private static final String LOG_PATH = "./data/";

	
	private long contador;
	
	private SSLServerSocket acceptor;
	
	private ArrayList<String> ipsActivas;
	
	private WorkQueue workQueue;

	private String[] cipherSuites;
	
	private PrintWriter log;
	
	

	public MainServer()
	{
		console("Empezando servidor en puerto " + PUERTO);
		
		ipsActivas = new ArrayList<String>();
		
		File file = new File(LOG_PATH + "log_" +System.currentTimeMillis() + ".txt");
		
		
		
		workQueue = new WorkQueue(TAMANO_POOL);    
		console("Inicializado pool de conversión de " + TAMANO_POOL + " workers.");
		
		
		try {
			
			// inicializar el log
			log = new PrintWriter(file);
			log.println("ID\tCON\tQUEUE");
			// inicializar el servidor seguro
	        SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault(); 
	        
			acceptor = (SSLServerSocket)sslSrvFact.createServerSocket(PUERTO); 
			
			console("Servidor listo para conexiones...");

			 while (true) {

				 
				 SSLSocket nuevo = (SSLSocket) acceptor.accept();
			     console("--- Nueva conexión entrante");
			     
		         cipherSuites = nuevo.getSupportedCipherSuites(); 
		         nuevo.setEnabledCipherSuites(cipherSuites);     
		         nuevo.startHandshake(); 
		         
		         
				 String ipNueva = nuevo.getRemoteSocketAddress().toString().split(":")[0];
				 
				 if(PROTECT_FROM_DoS && ipsActivas.contains(ipNueva))
				 {
					 console("Se rechaza conexión desde " + ipNueva + " - - -");
					 nuevo.close();
					
				 }
				 else
				 {
					 // lo chevere es que desde antes tenía listo el thread esperando a recibir el socket.. creo
					 ipsActivas.add(ipNueva);
					 new ClienteThread(this, nuevo, ++contador).start();
				 }
			}
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		log.close();
	   
	}
	
	public void convertir(Convertidor c)
	{
		workQueue.execute(c);
	}
	
	public void escribirLog (String msg)
	{
		synchronized (log) {
			log.println(msg);
			log.flush();
		}
	}
	
	public void quitarIpLista(String ip)
	{
		ipsActivas.remove(ip);
	}
	
	public void console(String msg)
	{
		System.out.println("S: " + msg);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MainServer ms = new MainServer();
		
	}

}
