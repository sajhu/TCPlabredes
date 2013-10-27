package co.labredes.carga;

/**
 * GLoad Core Class - Task
 */
public interface IFallible 
{
	/**
	 * Called in the case of report a failure
	 */
	public void fail();
	
	/**
	 * Called in the case of report the success
	 */
	public void success();

}
