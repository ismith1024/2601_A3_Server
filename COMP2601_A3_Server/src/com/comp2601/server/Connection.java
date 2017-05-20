package com.comp2601.server;

/*********
 * COMP2601 A3
 * Submitted 2016-03-16
 * By Ian Smith #100910972
 */


import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.comp2601.message.Message;
import com.comp2601.server.Server.OnMessageReceived;

/*Connection class
 * allows asynchronous communication with a single host
 * runs on its own thread
 */

public class Connection extends Thread{
	
    private static final int SERVERPORT = 3005;
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;
    private Random mRandom;
    private Message msgIn;
    private Message megOut;
    
    private Socket client;
    
    public Connection(Socket c, OnMessageReceived ml){
    	client = c;
    	messageListener = ml;
    	
    }
    
    public void stopMe(){
    	running = false;
    }
	
	@Override
    public void run() {
        super.run();

        running = true;

        try {
            System.out.println(this + "Connection: Connecting...");

            try {
            	
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (running) {
                	System.out.println("Connection: Ready for next message");
                	
                    String message = in.readLine();
                    msgIn = Message.parseFromJSON(message);
                    
                    System.out.println("message: " + msgIn.getTx() + " -- " + msgIn.getBody());

                    if (message != null && messageListener != null) { 
                     		messageListener.messageReceived(message, this);
                    }
                }

            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            } finally {
                client.close();
                System.out.println("S: Done.");
            }

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }

    }
	
    public void sendMessage(Message message){
        if (mOut != null && !mOut.checkError()) {
        	System.out.println("S: " + message);
            mOut.println(message.toJSON());
            mOut.flush();
        }
    }   


}
