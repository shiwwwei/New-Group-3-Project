/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @
 */
public class Client {

    private int port;
    private Socket socket;

    //Constructor
    public Client(int port, String host) {
	this.port = port;
	try {
	    this.socket = new Socket(host, port);
	    System.out.println("Connected to " + host + " in port " + port);
	} catch (IOException ex) {
	    ex.printStackTrace();
	}
    }

    //Read file
    public static String readfile(String filename) {
	String data = "";
	if (!filename.equals("")) {

	    File f = new File(filename);

	    BufferedReader b;
	    try {
		b = new BufferedReader(new FileReader(f));
		String readLine = "";
		while ((readLine = b.readLine()) != null) {
		    //System.out.println(readLine);
		    data = data + readLine;
		}
		//System.out.println("data = " + data);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }

	}
	return data;

    }

    //Send string to server
    public void sendData(String data) {
	ObjectOutputStream out;
	try {
	    out = new ObjectOutputStream(this.socket.getOutputStream());
	    out.writeObject(data);
	    out.close();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
	// TODO code application logic here
	try {
	    if (args.length < 2) {
		System.out.println("Missing parameteres");
		System.exit(1);
	    }
	    //Server listening in port
	    int port = 10000;
	    
	    //Create Client Instance
	    Client cl = new Client(port, args[0]);

	    System.out.println("Reading file = " + args[1]);
	    String data = Client.readfile(args[1]);

	    
	    JSONObject json = new JSONObject(data);

	    System.out.println("Sending json message");
	    System.out.println("json = " + json);
	    //sending the message
	    cl.sendData(json.toString());
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

}
