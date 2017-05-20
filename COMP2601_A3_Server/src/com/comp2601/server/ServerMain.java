package com.comp2601.server;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.awt.event.*; 
import java.awt.*;

import javax.swing.*; 
import javax.swing.event.*; 

/*********
 * COMP2601 A3
 * Submitted 2016-03-16
 * By Ian Smith #100910972
 */


//Class ServerMain --
//Purpose is to provide a simple GUI
//Allows the user to configure port
//Allows the user to start and stop the server
//Provides the local IP address to allow connections over the LAN instead of localhost
public class ServerMain extends JFrame{
	private JTextField confPortTextField;
	private String localhost;
	private String lanAddress;
	private int port = 3010;
	private static Server theServer;
	private Thread serverThread;
	
    public ServerMain() { 
        super();
        
        lanAddress = "unknown";        
                
        try{
        	lanAddress = InetAddress.getLocalHost().getHostAddress().toString();
       
        
        } catch(UnknownHostException e){}
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        getContentPane().setLayout(gridBagLayout);
        
        JLabel lblFoo = new JLabel(localhost);
        GridBagConstraints gbc_lblFoo = new GridBagConstraints();
        gbc_lblFoo.insets = new Insets(0, 0, 5, 0);
        gbc_lblFoo.gridx = 1;
        gbc_lblFoo.gridy = 0;
        getContentPane().add(lblFoo, gbc_lblFoo);
        
        JLabel ipAddressLabel = new JLabel("LAN Address:");
        GridBagConstraints gbc_ipAddressLabel = new GridBagConstraints();
        gbc_ipAddressLabel.anchor = GridBagConstraints.EAST;
        gbc_ipAddressLabel.insets = new Insets(0, 0, 5, 5);
        gbc_ipAddressLabel.gridx = 0;
        gbc_ipAddressLabel.gridy = 1;
        getContentPane().add(ipAddressLabel, gbc_ipAddressLabel);
        
        JLabel ipDisplayLabel = new JLabel(lanAddress);
        GridBagConstraints gbc_ipDisplayLabel = new GridBagConstraints();
        gbc_ipDisplayLabel.anchor = GridBagConstraints.WEST;
        gbc_ipDisplayLabel.insets = new Insets(0, 0, 5, 0);
        gbc_ipDisplayLabel.gridx = 1;
        gbc_ipDisplayLabel.gridy = 1;
        getContentPane().add(ipDisplayLabel, gbc_ipDisplayLabel);
        
        JLabel confPortLabel = new JLabel("Configure Port:");
        GridBagConstraints gbc_confPortLabel = new GridBagConstraints();
        gbc_confPortLabel.insets = new Insets(0, 0, 5, 5);
        gbc_confPortLabel.anchor = GridBagConstraints.EAST;
        gbc_confPortLabel.gridx = 0;
        gbc_confPortLabel.gridy = 2;
        getContentPane().add(confPortLabel, gbc_confPortLabel);
        
        confPortTextField = new JTextField("3010");
        GridBagConstraints gbc_confPortTextField = new GridBagConstraints();
        gbc_confPortTextField.insets = new Insets(0, 0, 5, 0);
        gbc_confPortTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_confPortTextField.gridx = 1;
        gbc_confPortTextField.gridy = 2;
        getContentPane().add(confPortTextField, gbc_confPortTextField);
        confPortTextField.setColumns(10);
        
        JButton startServerButton = new JButton("START SERVER");
        GridBagConstraints gbc_startServerButton = new GridBagConstraints();
        gbc_startServerButton.anchor = GridBagConstraints.EAST;
        gbc_startServerButton.insets = new Insets(0, 0, 5, 5);
        gbc_startServerButton.gridx = 0;
        gbc_startServerButton.gridy = 3;
        getContentPane().add(startServerButton, gbc_startServerButton);
        
        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	serverThread = new Thread(){
                	@Override
                	public void run(){
                    	
                    	//one try to parse a port number from the text field...
                    	//it's 3010 if the user messes up.                		
                		try{
                    		port = Integer.parseInt(confPortTextField.getText());
                    	} catch(NumberFormatException f){
                    		port = 3010;
                    	}                    	
                    		theServer = Server.getInstance(port);
                    		theServer.startServer(); 
                	} 
                	
                };            	
            	serverThread.start();            	
            }
        });
        
        JButton stopServerButton = new JButton("STOP SERVER");
        GridBagConstraints gbc_stopServerButton = new GridBagConstraints();
        gbc_stopServerButton.anchor = GridBagConstraints.EAST;
        gbc_stopServerButton.insets = new Insets(0, 0, 5, 5);
        gbc_stopServerButton.gridx = 0;
        gbc_stopServerButton.gridy = 4;
        getContentPane().add(stopServerButton, gbc_stopServerButton);
        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {            	
            	System.exit(0);
            }
        });
      
  
    }
    
   
    public static void main(String[] args) { 
        WindowListener listener;
	     ServerMain frame = new ServerMain(); 

	     listener = new WindowAdapter() { 
            public void windowClosing(WindowEvent theEvent) { 
                  System.exit(0); 
             } 
        }; 	
        frame.addWindowListener(listener); 
        frame.pack(); //set window size to wrap components 
        frame.setVisible(true); //make GUI window visible
   }

}

//References: http://stackoverflow.com/questions/17252018/getting-my-lan-ip-address-192-168-xxxx-ipv4
