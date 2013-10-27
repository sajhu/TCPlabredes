package co.labredes.server;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;
import it.sauronsoftware.jave.VideoAttributes;
import it.sauronsoftware.jave.VideoSize;

import java.io.File;

import co.labredes.common.Constantes;


public class Convertidor extends Thread {

	private long id;
	private String origenPath;
	private String destinoPath;
	private ClienteThread cliente;
	
	private int estado = Constantes.QUEUED;
	
	public Convertidor(ClienteThread cliente, String origenString, String destinoString) {
		this.cliente = cliente;
		this.id = cliente.darId();
		this.origenPath = origenString;
		this.destinoPath = destinoString;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		try {
			sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			encodeMP4(origenPath, destinoPath);
			estado = Constantes.CONVERTION_FINISHED;
		} catch (Exception e) {
			estado = Constantes.CONVERTION_FAILED;
			e.printStackTrace();
		} 
		
		// una vez terminamos de convertir despertamos al thread
		
		    synchronized(cliente.objeto){
		    	cliente.objeto.notify();
		    }
		  
		
	}
	
	public int darResultado()
	{
		return estado;
	}
	
	
	public File encodeMP4(String ori, String dest) throws IllegalArgumentException, InputFormatException, EncoderException
	{
		System.out.println("				T"+ id +": DESDE: " + ori + " hacia " + dest);
		File source = new File(ori);
		File mp4File = new File(dest);

		AudioAttributes audio = new AudioAttributes();	
		audio.setCodec("libfaac");
		audio.setBitRate(new Integer(64000));
		audio.setChannels(new Integer(2));
		audio.setSamplingRate(new Integer(44100));

		VideoAttributes video=new VideoAttributes();
		video.setCodec("mpeg4");
		video.setBitRate(new Integer(160000));
		video.setFrameRate(new Integer(15));
		video.setSize(new VideoSize(400, 300));

		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setFormat("mp4");
		attrs.setAudioAttributes(audio);
		attrs.setVideoAttributes(video);
		Encoder encoder = new Encoder();
		
		encoder.encode(source, mp4File, attrs);
		
		
		return mp4File;
		
	}
}
