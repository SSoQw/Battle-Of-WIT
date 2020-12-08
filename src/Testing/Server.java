package Testing;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Server {
	static ServerSocket serverSocket;
	static Vector<ClientHandler> clients = new Vector<ClientHandler>();
	static boolean hasHost = false;
	static boolean Answered = false;
	static int maxscore;
	static String old = "";
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
			}else if (s[1].contains("won")){
				x.w.printf("%s %s%n", s[0], s[1]);
			} else {
				x.w.printf("%s:%s%n", s[0], s[1]);
				System.out.printf("%s:%s%n", s[0], s[1]);
			}
		}
	}

	public static void startGame(String[] Options) throws IOException, InterruptedException {
		int NumQuestions = Integer.parseInt(Options[2]);
		int pickedQ;
		int count = 0;
		String[][] Questions = new String[2][NumQuestions];
		String line;
		List<List<String>> QwA = new ArrayList<List<String>>();
		List<String> selected = new ArrayList<String>();
		BufferedReader br;

		switch (Options[0]) {
		case "random":
			Questions = arithmeticGenerator(Options[1], NumQuestions);
			game(Questions);
			break;

		case "preset":
			String filepath = "COMP2100_Final_Project/src/Testing/Questoins/" + Options[1] + "Questions.csv";
			br = new BufferedReader(new FileReader(filepath));
			try {
				while ((line = br.readLine()) != null) {
					String[] QAsplit = line.split(",");
					QwA.add(Arrays.asList(QAsplit));
				}
			} catch (IOException e) {
				System.out.print("Something broke D:");
				e.printStackTrace();
			}

			int possibleQ = QwA.size() - 1;
			boolean[] used = new boolean[possibleQ];
			Random rand2 = new Random();

			for (int i = 0; i < NumQuestions; i++) {
				pickedQ = rand2.nextInt(possibleQ);
				while (used[pickedQ]) {
					pickedQ = rand2.nextInt(possibleQ);
				}
				used[pickedQ] = true;
				selected = QwA.get(pickedQ);

				for (String entrey : selected) {
					if (Questions[0][count].equals(null)) {
						Questions[0][count] = entrey;
					} else {
						Questions[1][count] = entrey;
					}
				}

				count += 1;
			}
			game(Questions);
			break;

		case "custom":
			filepath = "COMP2100_Final_Project/src/Testing/Questoins/" + Options[1];
			br = new BufferedReader(new FileReader(filepath));
			try {
				while ((line = br.readLine()) != null) {
					String[] QAsplit = line.split(",");
					QwA.add(Arrays.asList(QAsplit));
				}
			} catch (IOException e) {
				System.out.print("Something broke D:, your filename is probably wrong.");
				e.printStackTrace();
			}
			possibleQ = QwA.size() - 1;
			used = new boolean[possibleQ];
			rand2 = new Random();

			for (int i = 0; i < NumQuestions; i++) {
				pickedQ = rand2.nextInt(possibleQ);
				while (used[pickedQ]) {
					pickedQ = rand2.nextInt(possibleQ);
				}
				used[pickedQ] = true;
				selected = QwA.get(pickedQ);

				for (String entrey : selected) {
					if (Questions[0][count].equals(null)) {
						Questions[0][count] = entrey;
					} else {
						Questions[1][count] = entrey;
					}
				}

				count += 1;
			}
			game(Questions);
			break;
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
			for (int i = 0; i < numQuestions; i++) {
				num1 = rand.nextInt(100) + 1;
				num2 = rand.nextInt(100) + 1;
				Questions[0][i] = num1 + " + " + num2 + " =";
				solution = num1 + num2;
				Questions[1][i] = Integer.toString(solution);
			}
			break;

		case "-":
			for (int i = 0; i < numQuestions; i++) {
				num1 = rand.nextInt(100) + 1;
				num2 = rand.nextInt(100) + 1;
				Questions[0][i] = num1 + " - " + num2 + " =";
				solution = num1 - num2;
				Questions[1][i] = Integer.toString(solution);
			}
			break;

		case "*":
			for (int i = 0; i < numQuestions; i++) {
				num1 = rand.nextInt(20) + 1;
				num2 = rand.nextInt(20) + 1;
				Questions[0][i] = num1 + " * " + num2 + " =";
				solution = num1 * num2;
				Questions[1][i] = Integer.toString(solution);
			}
			break;

		case "%":
			for (int i = 0; i < numQuestions; i++) {
				num1 = rand.nextInt(400) + 1;
				num2 = rand.nextInt(20) + 1;
				Questions[0][i] = num1 + " % " + num2 + " =";
				solution = num1 % num2;
				Questions[1][i] = Integer.toString(solution);
			}
			break;
		}
		return (Questions);
	}
	
	public static void game(String[][] Questions) throws IOException {
		int numQ = Questions[0].length - 1;
		System.out.println(Integer.toString(Questions[0].length - 1));
		String[] QnA = new String[2];
		for (int i = 0; i < numQ; i++) {
			if (haveWinner()) {
				endGame();
			} else {
				QnA[0] = Questions[0][i];
				QnA[1] = Questions[1][i];
				sendToAll(QnA);
				while (!Answered) {
					checkForAnswer();
				}
			}
			Answered = false;
		}
	}
	
	public static boolean haveWinner() {
		for (ClientHandler x : clients) {
			if(x.score == maxscore) {
				return true;
			}
		}
		return false;
	}
	
	public static void endGame() {
		String name = "";
		for (ClientHandler x : clients) {
			if(x.score == maxscore) {
				name = x.name;
			}
		}
		String[] winner = new String[2];
		winner[0] = name;
		winner[1] = "has won, they answered " + maxscore + " questions the fastest.";
		sendToAll(winner);
	}
	
	public static void checkForAnswer() throws IOException {
		for (ClientHandler x : clients) {
			String Check = x.in.readLine();
			if (Check != null && !Check.equals(old)) {
				System.out.println("Made it here");
				Answered = true;
				x.score += 1;
				old = Check;
				return;
			}else {
				System.out.println("waiting");
			}
		}
	}
	
}

class ClientHandler extends Thread {
	Socket connectionSocket;
	PrintStream out;
	PrintWriter w;
	BufferedReader in;
	int score = 0;
	String name;
	boolean isHost = false;

	public ClientHandler(Socket s) throws IOException {
		connectionSocket = s;
		start();
	}

	public void run() {
		try {
			

			if (!Server.hasHost) {
				isHost = true;
				Server.hasHost = true;
			}

			out = new PrintStream(new BufferedOutputStream(connectionSocket.getOutputStream()));
			w = new PrintWriter(out, true);
			in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			name = in.readLine();

			System.out.println(name + " has connected!");

			if (isHost) {
				ArrayList<String> Options = new ArrayList<String>();
				w.println("Welcome " + name
						+ "! You are the host of the game. Please follow the instructions to get your game setup.");
				sleep(750);
				w.println("First, would you like to play with Random Aritmetic, Choose a Preset, or Use Custom questions?");
				sleep(750);
				w.println("Enter which mode you want to play (Random, Preset, Custom) case insensitive ");
				String gameMode = in.readLine();
				gameMode = gameMode.toLowerCase();

				while (!gameMode.equals("random") && !gameMode.equals("preset") && !gameMode.equals("custom")) {
					w.println("No valid gamemode slected, please enter which mode you want to play (Random, Preset, Custom) case insensitive");
					gameMode = in.readLine();
					gameMode = gameMode.toLowerCase();
				}
				switch (gameMode) {
				case "random":
					Options.add(gameMode);

					w.println("Please enter the operation you would like to use for your aritmetic set (+, -, *, %)");
					String Operation = in.readLine();

					while (!Operation.equals("+") && !Operation.equals("-") && !Operation.equals("*")
							&& !Operation.equals("%")) {
						w.print("Please enter enter a valid operation(+, -, *, %)");
						Operation = in.readLine();
					}
					Options.add(Operation);

					w.println("Lastly, please enter the number of questons you would like (1-20)");
					String NumQuestoins = in.readLine();

					while (Integer.parseInt(NumQuestoins) > 20 || Integer.parseInt(NumQuestoins) < 1) {
						w.println("Number of out of bounds, please enter a number between 1 and 20");
						NumQuestoins = in.readLine();
					}
					Options.add(NumQuestoins);
					Server.maxscore = (Integer.parseInt(NumQuestoins)/2)+1;

					break;
					
				case "preset":
					Options.add(gameMode);

					w.println("Would you like to use the computer science set, or math set?");
					w.println("Please entere math or compsci, case insesitive ");
					String set = in.readLine().toLowerCase();

					while (!set.equals("math") && !set.equals("compsci")) {
						w.println("No valid set selected, please entere math or compsci, case insesitive ");
						set = in.readLine().toLowerCase();
					}
					Options.add(set);

					w.println("Lastly, please enter the number of questons you would like");
					NumQuestoins = in.readLine();

					while (Integer.parseInt(NumQuestoins) > 20 || Integer.parseInt(NumQuestoins) < 1) {
						w.println("Number of out of bounds, please enter a number between 1 and 20");
						NumQuestoins = in.readLine();
					}
					Options.add(NumQuestoins);
					Server.maxscore = (Integer.parseInt(NumQuestoins)/2)+1;

					break;

				case "custom":
					Options.add(gameMode);
					w.println("If you haven't already, add your csv file with your question/answer set to the Questions folder, then restart the server.");
					w.println("If you have already added the file, please enter the name of the file, otherwise enter quit to restrat.");
					w.println("Please enter the file name, be sure to include .csv at the end ");
					String file = in.readLine();

					if (file.toLowerCase().contains("quit")) {
						System.exit(0);
					}
					Options.add(file);

					w.println("Lastly, please enter the number of questons you would like from your file");
					NumQuestoins = in.readLine();
					Options.add(NumQuestoins);
					Server.maxscore = (Integer.parseInt(NumQuestoins)/2)+1;

					break;
				}
				w.println("You're all setup type start to start the game");
				String start = in.readLine();
				while (!start.contains("start")) {
					start = in.readLine();
				}
				String[] strings = Arrays.stream(Options.toArray()).toArray(String[]::new);
				String[] START = new String[]{"start", " "};
				Server.sendToAll(START);
				sleep(1000);
				Server.startGame(strings);
			} else {
				String[] welcome = new String[2];
				welcome[0] = name;
				welcome[1] = "has joined the game!";
				Server.sendToAll(welcome);
				w.println("Please wait for the host to start the game.");
			}
		} catch (IOException x) {
			System.out.println(x);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
