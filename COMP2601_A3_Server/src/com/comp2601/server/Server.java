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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.comp2601.message.Message;


/**
 * Server object - this is a singleton created by a thread running on the ServerMain class
 * Maintains a collection of connections and reacts to messages sent by the connections
 */
public class Server {

	private ServerSocket serverSocket;
	private static final String ADMINNAME = "Admin";
	private MessageBox msgBox;
	
	private static Dispatcher dispatcher;
	
	private static Server instance;
	public static Server getInstance(){return instance;}
	public static Server getInstance(int p){
		if(instance == null){
			instance = new Server(new Server.OnMessageReceived() {
	            @Override
	            public void messageReceived(String incoming, Connection cnx) {
	                System.out.println("\n" + incoming);
	                Message message = Message.parseFromJSON(incoming);
	                dispatcher.dispatch(message, cnx);
	            }
	        }, p);
		}
		return instance;
	}
	
	//Broadcasts the notification to users with the current users list
	//Used by the clients to keep the logged in users list up to date
	//Called whenever somebody joins or leaves
	private static void updateUsers(){
		//broadcast a user list update to all users
		String users = "|";
		for(Map.Entry<String, Connection> entry: connections.entrySet()){
			users += entry.getKey() + "|";
		}		
		users = users.trim();
		
		//makes a "usersUpdate" message
		Message msg = new Message(Message.EVERYONE, Server.ADMINNAME, false, false, true, users);
		
		for(Map.Entry<String, Connection> entry: connections.entrySet()){
			entry.getValue().sendMessage(msg);
		}  

	
	}
	
    private int serverPort = 3010;
    private boolean running; 
    private PrintWriter mOut;
    private OnMessageReceived messageListener;
   
    public void startServer(){
    	running = true;
    	System.out.println("Starting server...");
    }
    
    public void stopServer(){
    	running = false;
    	System.out.println("Stopping server...");
    }
    
    private static HashMap<String, Connection> connections;
    public static HashMap<String, Connection> getConnections(){return connections;}

    public interface OnMessageReceived {
        public void messageReceived(String message, Connection cnx);
    }
    
    /**
     * Constructor of the class
     * @param messageListener listens for the messages
     * c_port is the port that the server will listen on
     */
    private Server(OnMessageReceived messageListener, int c_port){
        this.messageListener = messageListener;
        dispatcher = new Dispatcher();
        msgBox = new MessageBox("Message all users");
        connections = new HashMap<String, Connection>();
        serverPort = c_port;
        
        running = true;
        
        System.out.println("S: Constructing server ... ");
        
        closeSocket();
        try{
	        serverSocket = new ServerSocket(serverPort);
	        msgBox.setVisible(true);
	        while(running){
	           Socket client = serverSocket.accept();
	           System.out.println("Accepted client - " + client);
	           Connection clientConnection = new Connection(client, messageListener);
	           clientConnection.start();
	        }
	    } catch(IOException e){
            System.out.println("S: Error");
            e.printStackTrace();
        } finally {
            System.out.println("S: Done.");
            msgBox.dispose();
            closeSocket();
        }        
    }
    
    public void closeSocket(){
        if(serverSocket != null)
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    
    ///// Small utility GUI to message all users from the server
    private class MessageBox extends JFrame{
    	private Button sendButton;
    	private JTextField messageField;
    	
    	private MessageBox(String title){
    		super(title);
    		getContentPane().setLayout(null); 
    		setSize(320, 100);
    		messageField = new JTextField();
    		messageField.setSize(310,25);
    		messageField.setLocation(5,5);
    		getContentPane().add(messageField);
    		sendButton = new Button("MESSAGE ALL USERS");
       		sendButton.setLocation(5, 45);
    		sendButton.setSize(200,25);    		
    		getContentPane().add(sendButton);
    		sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                	String msgString = messageField.getText();
                	Message msg = new Message(Message.EVERYONE, Server.ADMINNAME, false, false, false, msgString);
                	if(!connections.isEmpty()){
                		Connection arbitrary = connections.values().iterator().next();
                		dispatcher.dispatch(msg, arbitrary);
                	}
                }
            });
    	
    	
    	}
    }
    
    ///////// REACTOR PATTERN IMPLEMENTATION
    private interface MessageReactor{
    	public void reactTo(Message msg, Connection cnx);  	
    }    
    
    //Dispatcher class
    //NOTE: there are exactly three message types that the client is able to send
    //DATA, LOGIN, and LOGOUT
    //I am registering these reactors in the Dispatcher constructor
    private class Dispatcher{
    	private HashMap<String, MessageReactor> reactors;
    	
    	public void register(String s, MessageReactor r){reactors.put(s,r);}
    	
    	public void dispatch(Message msg, Connection cnx){
    		reactors.get(msg.getType()).reactTo(msg, cnx);
    	}
    	
    	public Dispatcher(){
    		reactors = new HashMap<String, MessageReactor>();
    		    		
    		//LOGIN message reactor
    		register(Message.LOGIN, new MessageReactor(){
    			@Override
    			public void reactTo(Message msg, Connection cnx){
    				System.out.println("Adding user: " + msg.getTx());
    				Message notification = new Message(Message.EVERYONE, Server.ADMINNAME, false, false, false, "📢 " + msg.getTx() + Message.JOIN_NOTIFICATION);
    			  	connections.put(msg.getTx(), cnx);
    		    	updateUsers();		
					for(Map.Entry<String, Connection> entry: connections.entrySet()){
						entry.getValue().sendMessage(notification);
					}    				
    			}    			
    		});
    		
    		//LOGOUT message reactor
    		register(Message.LOGOUT, new MessageReactor(){
    			@Override
    			public void reactTo(Message msg, Connection cnx){
    				connections.get(msg.getTx()).stopMe();
    				connections.remove(msg.getTx());
    				updateUsers();    				
    				Message notification = new Message(Message.EVERYONE, Server.ADMINNAME, false, false, false, "📢 " + msg.getTx() + Message.LEAVE_NOTIFICATION);
					for(Map.Entry<String, Connection> entry: connections.entrySet()){
						entry.getValue().sendMessage(notification);
					}
    			}    			
    		});
    		
    		//DATA message reactor
    		register(Message.DATA, new MessageReactor(){
    			@Override
    			public void reactTo(Message msg, Connection cnx){
    				if(msg.getRx().equals(Message.EVERYONE)){
    					//the '📢' emoji is used to show users that the message is to everyone
    					msg.setBody("📢 " + msg.getBody());
    					for(Map.Entry<String, Connection> entry: connections.entrySet()){
    						entry.getValue().sendMessage(msg);
    					}
    		        } else{
    					connections.get(msg.getRx()).sendMessage(msg);
    		        }
    			}    			
    		});    		
    	}
    }
}

