package co.labredes.common;

public class Constantes {

	
	// CONTROL DE FLUJO desde el SERVIDOR
	
	public static final int CONTINUE = 0;
	public static final int QUEUED = 1;
	public static final int FILE_RECIEVED = 2;
	public static final int FILE_RECIEVED_FAILED = 3;
	public static final int CONVERTION_FINISHED = 4;
	public static final int CONVERTION_FAILED = 5;
	
	
	public static final int ERROR_500 = 500;


	
	// PETICIONES del CLIENTE
	
	public static final String SEPARATOR = ":::";
	public static final String REQUEST_CONVERT = "CSR";
	public static final String TIME_CONNECTING = "TCON";
	public static final String FILE_NAME = "FNAME";	
	public static final String FILE_SIZE = "FSIZE";	
	public static final String TIME_WAITED = "WAITED";
	
	// ERRORES
	// - deben estar antecedidos por ERROR:
	
	public static final int INVALID_PROTOCOL = 1;
	public static final int NO_DATA_RECIEVED = 2;
	public static final int TIME_OUT = 3;
	

}
