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
				
				if (message.toLowerCase().contains("start")) {
					donesetup = true;
				} else if (output[0].contains(name + " has joined the game!")) {
					System.out.print("");
				} else if (output.length>1) {
					System.out.printf("%s", output[0]);
					answer = output[1];
				}else {
					System.out.print(output[0]);
				}
			} catch (IOException ex) {
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
		}
		System.out.println("If you ever want to pass a question, type pass");
		Stopwatch stopwatch = Stopwatch.createStarted();
		do {
			message = sc.next();
			if (message.contains(MessageRead.answer)) {
				long time = stopwatch.elapsed(TimeUnit.NANOSECONDS);
				System.out.printf("Correct, you answered in %.4f sec", time*Math.pow(10, -9));
				w.println(time*Math.pow(10, -9));
				stopwatch = Stopwatch.createStarted();
			} else if (message.equalsIgnoreCase("pass")) {
				System.out.print("You've passed on this questoin.");
				w.println("100000000");
			}else {
				System.out.print("Incorrect, please try again: ");
			}
		} while (!message.equals("{quit}"));

		sc.close();
		System.exit(0);

	}
}