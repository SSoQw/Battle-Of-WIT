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
		for (ClientHandler x : clients) {
			if (s[1].contains("{quit}")) {
				x.w.printf("%s has left the game.%n", s[0]);
			} else if (s[1].contains("has joined the game!")) {
				x.w.printf("%s %s%n", s[0], s[1]);
			} else {
				x.w.printf("%s: %s%n", s[0], s[1]);
			}
		}
	}
	
	//Todo: pick random questions and answers from the CSVs and add them to their own question array
	public static void startGame(String[] Options) throws FileNotFoundException {
		int NumQuestions = Integer.parseInt(Options[3]);
		String[][] Questions = new String[2][NumQuestions];;
		String line;
		List<List<String>> QwA = new ArrayList<>();
		BufferedReader br;
		
		switch(Options[1]) {
		case "random":
			Questions = arithmeticGenerator(Options[2], NumQuestions);
		break;
		
		case "preset":
			String filepath = "COMP2100_Final_Project/src/Testing/Questoins/"+Options[2]+"Questions.csv";
			br = new BufferedReader(new FileReader(filepath));
		    try {
				while ((line = br.readLine()) != null) {
				    String[] values = line.split(",");
				    QwA.add(Arrays.asList(values));
				}
			} catch (IOException e) {
				System.out.print("Something broke D:");
				e.printStackTrace();
			}
		    
			for (int i = 0; i < NumQuestions; i++) {
				
			}
		break;
		
		case "custom":
			filepath = "COMP2100_Final_Project/src/Testing/Questoins/"+Options[2];
			br = new BufferedReader(new FileReader(filepath));
		    try {
				while ((line = br.readLine()) != null) {
				    String[] values = line.split(",");
				    QwA.add(Arrays.asList(values));
				}
			} catch (IOException e) {
				System.out.print("Something broke D:");
				e.printStackTrace();
			}
			
			for (int i = 0; i < NumQuestions; i++) {
				
			}

		}
	}

	public static String[][] arithmeticGenerator(String Op, int numQuestions) {
		Random rand = new Random();
		int num1;
		int num2;
		int solution;
		String[][] Questions = new String[2][numQuestions];

		switch (Op) {
			case "+":
				for(int i = 0; i<numQuestions; i++) {
					num1 = rand.nextInt(100) + 1;
					num2 = rand.nextInt(100) + 1;
					Questions[0][i] = num1 + " + " + num2 + " =";
					solution = num1 + num2;
					Questions[1][i] = Integer.toString(solution);
				}
			break;

			case "-":
				for(int i = 0; i<numQuestions; i++) {
					num1 = rand.nextInt(100) + 1;
					num2 = rand.nextInt(100) + 1;
					Questions[0][i] = num1 + " - " + num2 + " =";
					solution = num1 - num2;
					Questions[1][i] = Integer.toString(solution);
				}
			break;

			case "*":
				for(int i = 0; i<numQuestions; i++) {
					num1 = rand.nextInt(20) + 1;
					num2 = rand.nextInt(20) + 1;
					Questions[0][i] = num1 + " * " + num2 + " =";
					solution = num1 * num2;
					Questions[1][i] = Integer.toString(solution);
				}
			break;

			case "%":
				for(int i = 0; i<numQuestions; i++) {
					num1 = rand.nextInt(400) + 1;
					num2 = rand.nextInt(20) + 1;
					Questions[0][i] = num1 + " % " + num2 + " =";
					solution = num1 % num2;
					Questions[1][i] = Integer.toString(solution);
				}
			break;
		}
		return(Questions);
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

			if (!Server.hasHost) {
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
			
			System.out.println(str + " has connected!");


			
			if(isHost) {
				ArrayList<String> Options = new ArrayList<String>();
				w.write("Welcome "+ str + "! You are the host of the game. Please follow the instructions to get your game setup.");
				w.write("First, would you like to play with Random Aritmetic, Choose a Preset, or Use Custom questions?");
				w.write("Enter which mode you want to play (Random, Preset, Custom) case insensitive: ");
				String gameMode = in.readLine().toLowerCase();
				
				while(!gameMode.equals("random") && !gameMode.equals("preset") && !gameMode.equals("custom")) {
					w.write("No valid gamemode slected, please enter which mode you want to play (Random, Preset, Custom) case insensitive:");
					gameMode = in.readLine().toLowerCase();
				}
				switch(gameMode) {
					case "random":
						Options.add(gameMode);
						
						w.write("Please enter the operation you would like to use for your aritmetic set (+, -, *, %)");
						String Operation = in.readLine(); 
						
						while(!Operation.equals("+") && !Operation.equals("-") && !Operation.equals("*") && !Operation.equals("%")) {
						w.write("Please enter enter a valid operation(+, -, *, %)");
						Operation = in.readLine(); 
						}
						Options.add(Operation); 
						
						w.write("Lastly, please enter the number of questons you would like (1-20):");
						String NumQuestoins = in.readLine();
						
						while(Integer.parseInt(NumQuestoins) > 20 || Integer.parseInt(NumQuestoins) < 1) {
							w.write("Number of out of bounds, please enter a number between 1 and 20");
							NumQuestoins = in.readLine();
						}
						Options.add(NumQuestoins); 
						
					break;
					
					case "preset":
						Options.add(gameMode); 
						
						w.write("Would you like to use the computer science set, or math set?");
						w.write("Please entere math or compsci, case insesitive: ");
						String set = in.readLine().toLowerCase();
						
						while(!set.equals("math") && !set.equals("compsci")) {
							w.write("No valid set selected, please entere math or compsci, case insesitive: ");
							set = in.readLine().toLowerCase();
						}
						Options.add(set);
						
						//Values here are place holders, as question sets aren't finalized.
						w.write("Lastly, please enter the number of questons you would like (1-20):");
						NumQuestoins = in.readLine();
						
						while(Integer.parseInt(NumQuestoins) > 20 || Integer.parseInt(NumQuestoins) < 1) {
							w.write("Number of out of bounds, please enter a number between 1 and 20");
							NumQuestoins = in.readLine();
						}
						Options.add(NumQuestoins); 
						
					break;
					
					case "custom":
						Options.add(gameMode); 
						w.write("If you haven't already, add your csv file with your question/answer set to the Questions folder, then restart the server.");
						w.write("If you have already added the file, please enter the name of the file, otherwise enter quit to restrat.");
						w.write("Please enter the file name, be sure to include .csv at the end: ");
						String file = in.readLine();
						
						if(file.toLowerCase().contains("quit")) {
							System.exit(0);
						}
						Options.add(file);
						
						w.write("Lastly, please enter the number of questons you would like from your file:");
						NumQuestoins = in.readLine();
						Options.add(NumQuestoins); 
						
					break;
				}
				w.write("You're all setup, type start to start the game.");
				String start = in.readLine();
				while(!start.equalsIgnoreCase("start")) {
					start = in.readLine();	
				}
				String[] strings = Arrays.stream(Options.toArray()).toArray(String[]::new);
				Server.startGame(strings);
			} else {
				String[] welcome = new String[2];
				welcome[0] = str;
				welcome[1] = "has joined the game!";
				Server.sendToAll(welcome);
				w.write("Please wait for the host to start the game.");
			}
			
		

			while (!done) {
				message = in.readLine();
				if (message == null) {
					done = true;
					continue;
				}
				messageSplit = message.split(":::");
				if (messageSplit.length > 1) {
					Server.sendToAll(messageSplit);
				}
			}
		} catch (IOException x) {
			System.out.println(x);
		}
	}
}
