package co.labredes.carga;

/**
 * GLoad Core Class - Task
 * Represents the task to execute in a command pattern
 */
public abstract class Task implements IFallible
{
	/**
	 * Complete Running and Execution Constant 
	 */
	public static final String OK_MESSAGE = "OK_TEST";
	
	/**
	 * Incomplete or Fail Running and Execution Constant 
	 */
	public static final String MENSAJE_FAIL = "FAIL_TEST";
	
	/**
	 * Executes the Task
	 */
	public abstract void execute();


}
