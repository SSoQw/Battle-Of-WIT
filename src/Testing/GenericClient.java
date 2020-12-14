package Testing;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.net.*;
import com.google.common.base.*;

public class GenericClient {

	public static void main(String[] args) throws Exception {
		
		try {
			Socket connectionSocket = new Socket("localhost", 25565);
			new MessageRead(connectionSocket).start();
			new MessageWrite(connectionSocket).start();
		} catch (UnknownHostException ex) {
			System.out.println("Server not found: " + ex.getMessage());
		} catch (ConnectException ex) {
			System.out.println("Server not found: " + ex.getMessage());
		} catch (SocketException ex) {
			System.out.println("Server closed the connection");
		}
	}

}

class MessageRead extends Thread {
	Socket s;
	BufferedReader in;
	static String name;
	static String answer;
	static boolean donesetup;

	public MessageRead(Socket s) {
		try {
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch (IOException ex) {
			System.out.println("Error with IO stream: " + ex.getMessage());
		}
	}

	public void run() {
		while (true) {
			try {
				String message = in.readLine();
				String[] output = message.split(":");
				
				if (message.equals("start: ")) {
					donesetup = true;
				}else if(message.contains("won")){
					System.out.println(message);
					System.exit(0);
				}else if (message.contains("has joined the game!")) {
					if(!output[0].contains(name)){
						System.out.println(message);
					}
				} else if (output.length>1 && output[1] != null) {
					System.out.printf("%s\n", output[0]);
					answer = output[1];
				}else {
					System.out.println(message);
				}
				sleep(500);
			} catch (IOException | InterruptedException ex) {
				System.out.println("Error reading: " + ex.getMessage());
				ex.printStackTrace();
				break;
			}
		}
	}
}

class MessageWrite extends Thread {
	Socket s;
	PrintWriter w;
	

	public MessageWrite(Socket s) {
		try {
			OutputStream out = new PrintStream(new BufferedOutputStream(s.getOutputStream()));
			w = new PrintWriter(out, true);
		} catch (IOException ex) {
			System.out.println("Error with IO stream: " + ex.getMessage());
		}
	}

	public void run() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Welcome to Battle of WIT, please enter your name...");
		String name = sc.next();
		MessageRead.name = name;
		String message;
		
		
		w.println(name);
	
	while(!MessageRead.donesetup) {
			message = sc.next();
			w.println(message);
			try {
				sleep(100);
			} catch (InterruptedException e) {
				System.out.println("broked, probably fix later D:");
			}
		} 
			
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		String Canswer;
		do {
			Canswer = sc.next();
			if (Canswer.contains(MessageRead.answer)) {
				long time = stopwatch.elapsed(TimeUnit.NANOSECONDS);
				System.out.printf("Correct, you answered in %.4f seconds\n", time*Math.pow(10, -9));
				w.printf("%s answered first in %.4f seconds.\r\n", name, time*Math.pow(10, -9));
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("broked, probably fix later D:");
				}
				stopwatch = Stopwatch.createStarted();
			}else {
				System.out.printf("Incorrect, please try again: ");
			}
		} while (!Canswer.equals("{quit}"));

		sc.close();
		System.exit(0);

	}
}