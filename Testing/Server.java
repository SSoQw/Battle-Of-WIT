package Testing;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
	static ServerSocket serverSocket;
    static Vector<ClientHandler> clients = new Vector<ClientHandler>();
    static Queue<String> buffer = new LinkedList<>();
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
    				x.w.printf("%s has left the chat.%n", s[0]);
    		}else if(s[1].contains("has joined the chat!")){
    				x.w.printf("%s %s%n", s[0], s[1]);
    		}else if(s[1].contains("Send buffer")){
    			for(String element : buffer) {
    				x.w.println(element);
    			}
    		}else {
    			if(buffer.size()<5) {
    				buffer.add(s[0]+": "+s[1]);
    			}else {
    				buffer.add(s[0]+": "+s[1]);
    				buffer.remove();
    			}
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
	        boolean done = false;
	        out = new PrintStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
	        w = new PrintWriter(out, true);
	        BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	        String message;
	        String[] messageSplit;
	        String str = in.readLine();
	        System.out.println(str + " has joined the chat!");
	        String[] welcome =  new String[2];
	        welcome[0] = str;
	        welcome[1] = "has joined the chat!";
	        ChatServer.sendToAll(welcome);
	        String[] bufferreq = new String[2];
	        bufferreq[1] = "Send buffer";
	        ChatServer.sendToAll(bufferreq);
	        while(!done){
	        	message = in.readLine();
	        	if(message == null) {
	        		done = true;
	        		continue;
	        	} 
	            messageSplit = message.split(":::");
	            if(messageSplit.length>1) {
            		ChatServer.sendToAll(messageSplit);
	            }
	        }
        } catch (IOException x) {
            System.out.println(x);
        }   
    }
}
