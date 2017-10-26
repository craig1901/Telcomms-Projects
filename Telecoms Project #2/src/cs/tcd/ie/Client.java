/**
 * 
 */
package cs.tcd.ie;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import tcdIO.*;

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node {
	static final int DEFAULT_SRC_PORT = 50000;
	static final int DEFAULT_DST_PORT = 50001;
	static final String DEFAULT_DST_NODE = "localhost";	
	
	boolean timeout;
	Terminal terminal;
	InetSocketAddress dstAddress;
	
	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(Terminal terminal, String dstHost, int dstPort, int srcPort) {
		try {
			this.terminal= terminal;
			dstAddress= new InetSocketAddress(dstHost, dstPort);
			socket= new DatagramSocket(srcPort);
			listener.go();
		}
		catch(java.lang.Exception e) {e.printStackTrace();}
	}

	
	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public synchronized void onReceipt(DatagramPacket packet) {
		StringContent content= new StringContent(packet);
		timeout = false;
		this.notify();
		terminal.println(content.toString());
	}

	
	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception {
		try
		{
			byte[] data= null;
			String seq = "";
			data= (terminal.readString("String to send: ")).getBytes();	
			byte [] window = new byte[2];
			//window size is 2, see code below and report...
			for(int i = 0; i < data.length; i++)
			{
				seq += i;
			}
			
			byte [] ack = seq.getBytes();
			DatagramPacket packet= null;
			
			for(int i = 0; i < data.length; i++)
			{
				window[0] = data[i];
				window[1] = ack[i];
				terminal.println("Sending packet..." + seq.substring(i, i+1));
				packet= new DatagramPacket(window, window.length, dstAddress);
				socket.send(packet);
				timeout = true;
				terminal.println("Packet sent");
				this.wait(1000);
				if(timeout)
				{
					terminal.println("Timed out, resending packet..." + seq.substring(i, i+1));
					socket.send(packet);
					this.wait();
				}
				window[0] = data[i+1];
				window[1] = data[i+1];
				packet = new DatagramPacket(window, window.length, dstAddress);
				socket.send(packet);
			}
		}catch(ArrayIndexOutOfBoundsException e){}
	}


	/**
	 * Test method
	 * 
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) {
		try {					
			Terminal terminal= new Terminal("Client");		
			(new Client(terminal, DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
			terminal.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
}