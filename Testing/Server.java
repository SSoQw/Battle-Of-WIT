package Testing;

import java.io.*;
import java.net.*;
import java.util.*;




public class Server {
	static ServerSocket serverSocket;
    static Vector<ClientHandler> clients = new Vector<ClientHandler>();
    static boolean hasHost = false;
    
    public static void main(String[] args) throws IOException {
        serverSocket = new ServerSocket(25565); 
    	System.out.println("Waiting for users...");
        
        while (true) {
            try {
                Socket s = serverSocket.accept();
                clients.add(new ClientHandler(s));
            } catch (Exception x) {
                System.out.println(x);
            }
        }
    }
    public static void sendToAll(String[] s) {
    	for(ClientHandler x : clients) {
    		if(s[1].contains("{quit}")) {
    				x.w.printf("%s has left the game.%n", s[0]);
    		}else if(s[1].contains("has joined the game!")){
    				x.w.printf("%s %s%n", s[0], s[1]);
    		}else {
        			x.w.printf("%s: %s%n", s[0], s[1]);
    			}
    		}
    	}
}

class ClientHandler extends Thread {
	  	Socket connectionSocket;
	    PrintStream out;
	    PrintWriter w;
	    public ClientHandler(Socket s) throws IOException {
	        connectionSocket = s;
	        start();
	    }

		public void run() {
	        try {
	        	boolean isHost = false;
	        	
	        	if(!Server.hasHost) {
	        		isHost = true;
	        		Server.hasHost = false;
	        	}

		        boolean done = false;
		        out = new PrintStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
		        w = new PrintWriter(out, true);
		        BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		        
		        String message;
		        String[] messageSplit;
		        String str = in.readLine();
		        System.out.println(str + " has joined the game!");
		        
		        String[] welcome =  new String[2];
		        welcome[0] = str;
		        welcome[1] = "has joined the game!";
		        Server.sendToAll(welcome);

		        
		        while(!done){
		        	message = in.readLine();
		        	if(message == null) {
		        		done = true;
		        		continue;
		        	} 
		            messageSplit = message.split(":::");
		            if(messageSplit.length>1) {
		            	Server.sendToAll(messageSplit);
		            }
		        }
	        } catch (IOException x) {
	            System.out.println(x);
	        }   
	    }
}
