package co.labredes.carga;

import co.labredes.client.ConexionServer;



/**
 * GLoad Core Class - Task
 *  
 * ------------------------------------------------------------
 * Example Class Client Server:
 * This Class Represents the task that we want to generate in a concurrent way
 * ------------------------------------------------------------
 * 
 */
public class ClientServerTask extends Task
{

	@Override
	public void execute() 
	{
		// TODO Auto-generated method stub
		ConexionServer client = new ConexionServer();
		client.start();		
	}

	@Override
	public void fail() 
	{
		// TODO Auto-generated method stub
		System.out.println(Task.MENSAJE_FAIL);
		
	}

	@Override
	public void success() 
	{
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
		
	}

}
