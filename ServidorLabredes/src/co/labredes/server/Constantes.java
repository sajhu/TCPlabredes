package co.labredes.server;

public class Constantes {

	
	// CONTROL DE FLUJO desde el SERVIDOR
	
	public static final int CONTINUE = 0;
	public static final int QUEUED = 1;
	public static final int CONVERTION_FINISHED = 2;
	public static final int CONVERTION_FAILED = 3;
	

	
	// PETICIONES del CLIENTE
	
	public static final String SEPARATOR = ":::";
	public static final String REQUEST_CONVERT = "CONVERT SERVICE REQUEST";
	public static final String TIME_QUEUED = "QUEUE";
	public static final String TIME_WAITED = "WAITED";
	
	// ERRORES
	// - deben estar antecedidos por ERROR:
	
	public static final int INVALID_PROTOCOL = 1;
	public static final int NO_DATA_RECIEVED = 2;
	public static final int TIME_OUT = 3;
	

}
