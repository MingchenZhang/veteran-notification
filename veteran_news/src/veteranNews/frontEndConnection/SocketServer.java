package veteranNews.frontEndConnection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.error.Prompt;

/**
 *
 * @author zmc94
 */
public class SocketServer extends Thread {
	private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
	private TCPRequestHandler requestHandler;

    public SocketServer( int port , TCPRequestHandler requestHandler)
    {
        this.port = port;
		this.requestHandler = requestHandler;
    }

    public void startServer() throws CriticalException
    {
        try
        {
            serverSocket = new ServerSocket( port );
            this.start();
        }
        catch (IOException e)
        {
            Alert.warning("fail to create server");
			Alert.exception(SocketServer.class, e);
			throw new CriticalException("fail to create server");
        }
    }

    public void stopServer()
    {
        running = false;
        this.interrupt();
    }

    @Override
    public void run()
    {
        running = true;
        while( running )
        {
			Prompt.log("Listening for a connection on: "+serverSocket.getLocalPort(),5);
			
			try {
				// Call accept() to receive the next connection
				Socket socket = serverSocket.accept();
				// Pass the socket to the RequestHandler thread for processing
				requestHandler.setSocket(socket);
				requestHandler.clone().start();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
        }
    }
}
