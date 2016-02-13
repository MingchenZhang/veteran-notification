/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.frontEndConnection;

import java.net.Socket;

/**
 *
 * @author zmc94
 */
public abstract class TCPRequestHandler extends Thread implements Cloneable {
	protected Socket socket;
	
	public TCPRequestHandler(Socket socket){
		this.socket = socket;
	}
	
	public final void setSocket(Socket socket){
		this.socket = socket;
	}
	
	@Override
	public abstract void run();
	
	@Override
	public abstract TCPRequestHandler clone();
}
