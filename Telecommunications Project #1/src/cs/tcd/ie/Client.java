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
	
	Terminal terminal;
	InetSocketAddress dstAddress;
	boolean timeOut;
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
		timeOut = false;
		this.notify();
		terminal.println(content.toString());
	}


	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception 
	{	
		byte[] data= null;
		String seq = "0";
		DatagramPacket packet= null;
		data= (terminal.readString("String to send: ")).getBytes();	
		for(int i = 0; i < data.length-1; i++)
		{
			if(seq.substring(i).equals("0"))
			{
				seq += "1";
			}
			else
			{
				seq += "0";
			}
		}
		System.out.println(seq);
		byte[] ack = seq.getBytes();
		byte[] dataOne = new byte[2];
		for(int i = 0; i < data.length; i++)
		{
			dataOne[0] = data[i];
			dataOne[1] = ack[i];
			terminal.println("Sending packet..." + seq.substring(i, i+1));
			packet= new DatagramPacket(dataOne, dataOne.length, dstAddress);
			socket.send(packet);
			terminal.println("Packet sent");
			timeOut = true;
			this.wait(1000);			
			if(timeOut)
			{
				terminal.println("timed out resending..." + seq.substring(i, i+1));
				socket.send(packet);
				this.wait(1000);
			}
		}
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