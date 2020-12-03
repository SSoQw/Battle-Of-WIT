package Testing;

import java.util.*;
import java.io.*;
import java.net.*;
import com.google.common.base.Stopwatch; //By Kevin Bourrillion

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
				String[] output = message.split(": ");
				System.out.println(output[0]);
				
				if (message.toLowerCase().contains("start")) {
					donesetup = true;
				} else if (output[0].contains(name + " has joined the game!")) {
					System.out.print("");
				} else if (output.length>1) {
					System.out.printf("%s%n", output[0]);
					answer = output[1];
					MessageWrite.stopwatch.reset();
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
	static Stopwatch stopwatch = Stopwatch.createUnstarted();
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
		System.out.println("Welcome to the chatroom, please enter your name...");
		String name = sc.next();
		MessageRead.name = name;

		w.println(name);

		System.out.println("Hello " + name + "! If you ever want to leave type {quit}");
		
		String message;
		
		while(!MessageRead.donesetup) {
			message = sc.next();
			w.println(message);
		}
		do {
			message = sc.next();

			if (message.equals(MessageRead.answer)) {
				w.println(stopwatch.toString());

			} else if (message.equals("pass")) {
				w.println("100000000 ms");
			}

		} while (!message.equals("{quit}"));

		sc.close();
		System.exit(0);

	}
}