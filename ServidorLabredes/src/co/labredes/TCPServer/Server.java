package co.labredes.TCPServer;


import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;
import it.sauronsoftware.jave.VideoAttributes;
import it.sauronsoftware.jave.VideoSize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private static File aviFile;
	
	public void receiveFile()
	{
		
	}
	
	public static File encodeMP4(String dir)
	{
		File source = aviFile;
		File mp4File = new File("./data/wildlifeNew.mp4");

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
		try {
			encoder.encode(source, mp4File, attrs);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InputFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EncoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mp4File;
		
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {
	    ServerSocket serverSocket = null;

	    try {
	        serverSocket = new ServerSocket(12345);
	    } catch (IOException ex) {
	        System.out.println("Can't setup server on this port number. ");
	    }

	    Socket socket = null;
	    InputStream is = null;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    int bufferSize = 0;

	    try {
	        socket = serverSocket.accept();
	    } catch (IOException ex) {
	        System.out.println("Can't accept client connection. ");
	    }

	    try {
	        is = socket.getInputStream();

	        bufferSize = socket.getReceiveBufferSize();
	        System.out.println("Buffer size: " + bufferSize);
	    } catch (IOException ex) {
	        System.out.println("Can't get socket input stream. ");
	    }
    	String dir = "./data/wildlifeNewAvi.avi";

	    try {
	        fos = new FileOutputStream(dir);
	        bos = new BufferedOutputStream(fos);
	        aviFile = new File(dir);

	    } catch (FileNotFoundException ex) {
	        System.out.println("File not found. ");
	    }

	    byte[] bytes = new byte[bufferSize];

	    int count;

	    while ((count = is.read(bytes)) > 0) {
	        bos.write(bytes, 0, count);
	    }
	    
	    
        File f = encodeMP4(dir);

	    
	    bos.flush();
	    bos.close();
	    is.close();
	    socket.close();
	    serverSocket.close();
	}

}