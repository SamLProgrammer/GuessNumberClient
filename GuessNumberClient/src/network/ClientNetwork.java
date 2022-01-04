package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientNetwork {

	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private int requestType;
	private boolean connected;
	private boolean trying;

	public ClientNetwork() throws UnknownHostException, IOException {
		initComponents();
	}

	private String catchClientStringInput() { // this method catch keyboard string inputs from terminal
		String string = "";
		if (requestType == 0) { // if the keyboard input belongs to a try, we send and advice to Server
			sendString(Responses.CLIENT_SENDS_TRY.toString());
			int triesCounter = 0;
			do { // keep asking, until getting a valid try
				if (triesCounter > 0) {
					System.out.println("Please try a number between 0 and 12 or \"e\" to leave: ");
				}
				InputStreamReader in = new InputStreamReader(System.in); // inputStream
				BufferedReader buffer = new BufferedReader(in);
				try {
					string = buffer.readLine();
					triesCounter++;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!validTry(string)); // validating number input
		} else if (requestType == 1) {
			sendString(Responses.CLIENT_ASKS_TO_EXIT.toString());
			do {// keep asking until getting a valid option
				if (!string.equalsIgnoreCase("")) {
					System.out.println("Invalid Input");
				}
				InputStreamReader in = new InputStreamReader(System.in); // inputStream
				BufferedReader buffer = new BufferedReader(in);
				try {
					string = buffer.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!validExit(string));
		} else {
			sendString(Responses.CLIENT_CHOSE_FINAL_ROUND_OPTION.toString());
			do {// keep asking untill guetting a valid option
				if (!string.equalsIgnoreCase("")) {
					System.out.println("Press:\n" + 
							"\"p\" to play again\n" + 
							"\"q\" to quit.");
				}
				InputStreamReader in = new InputStreamReader(System.in); // inputStream
				BufferedReader buffer = new BufferedReader(in);
				try {
					string = buffer.readLine();
					if (string.equals("p")) {
						socket.setSoTimeout(0);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!validFinalRoundOption(string));
			requestType = 0;
		}
		return string;
	}

	private boolean validTry(String string) { // validates a try inputted by client
		boolean flag = false;
		if (!string.equals("")) {
			if (string.equals("e") || (isNumber(string) && Integer.valueOf(string) < 13 && Integer.valueOf(string) > -1)) {
				flag = true;
			}
		}
		return flag;
	}

	private boolean validExit(String string) { // validates exit option
		return string.equals("e");
	}

	private boolean validFinalRoundOption(String string) {// validates
		return string.equals("q") || string.equals("p");
	}

	private boolean isNumber(String string) { // check if client input is a number
		boolean flag = true;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) < 48 || string.charAt(i) > 57) {
				flag = false;
				i = string.length();
			}
		}
		return flag;
	}

	private void initTryRunner() {// cath input on thread apart, to avoid stacks in inputstream
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (trying) {
					trying = false;
					sendString(catchClientStringInput());
				}
			}
		}).start();
	}

	private void initComponents() {
		InputStreamReader in = new InputStreamReader(System.in); // create an inputstream over keyword input (System.in)
		BufferedReader buffer = new BufferedReader(in); // create buffer for faster reading
		String portNumber = ""; // string variable to save line typed on terminal
		String host = "";
		String name = "Un-Named";
		try {
			System.out.println("Enter the Server's host: \n");
			host = buffer.readLine(); // buffer reads the line on stream
			System.out.println("Enter the Server's port number to connect: \n");
			portNumber = buffer.readLine();
			System.out.println("Please enter your name to login: \n");
			name = buffer.readLine();
			socket = new Socket(host, Integer.valueOf(portNumber));
			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();
			connected = true;
			initConnectionThread();
			sendString(Responses.CLIENT_SENDS_NAME.toString());
			sendString(name);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage()); // catch exception and show reason
		}

	}

	private void initConnectionThread() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (connected) {
					String request = receiveString(); // on hold untill server sends any data
					if (request.equalsIgnoreCase("ASKS_TO_CLIENT_TO_TRY")) {
						System.out.println(receiveString());
						trying = true;
						requestType = 0;
						initTryRunner();
					} else if (request.equalsIgnoreCase("ON_WAIT_FOR_ROUND_PARTNERS_TO_FINISH")) {
						System.out.println("Your round partners are still playing, wait...");
					} else if (request.equalsIgnoreCase("ROUND_FINAL_OPTIONS_ASKED_TO_CLIENT")) {
						System.out.println("==> Round Finished <==");
						System.out.println("You have 10 seconds to decide...\nPress:");
						System.out.println("\"p\" to play again.");
						System.out.println("\"q\" to quit.");
						trying = true;
						requestType = 2;
						initTryRunner();
						try {
							socket.setSoTimeout(10000);
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
					} else {
						if (!request.equals("")) {
							switch (Requests.valueOf(request)) {
							case SEND_INITIAL_WAITING_TIME_TO_CLIENT:
								System.out.println("Remaining Time To Start Round: " + receiveString());
								break;
							case FULL_LOBBY:
								fullServerLobbyMessage();
								break;
							case PING_TO_CLIENT:
								System.out.println("ping recieved");
								break;
							case SEND_WELLCOME_TO_GAME_TO_CLIENT:
								System.out.println(receiveString());
								System.out.println("====> You're in Lobby queue by now <===");
								break;
							case NEXT_ROUND_NAMES_TO_CLIENT:
								System.out.println(receiveString());
								break;
							case SERVER_NOTIFIES_CLIENT_TO_TRY:
								System.out.println(receiveString());
								break;
							case CLIENT_FAILED:
								System.out.println(receiveString());
								break;
							case CONGRATULATION:
								System.out.println("Congratulation!");
								break;
							case TO_GUESS_NUMBER_IS_BIGGER:
								System.out.println("To Guess Number Is Bigger");
								break;
							case TO_GUESS_NUMBER_IS_SMALLER:
								System.out.println("To Guess Number Is Smaller");
								break;
							case CLIENT_REMOVED:
								System.out.println("You've been removed from server");
								connected = false;
								System.exit(0);
								break;
							case KEEP_ALIVE:
								System.out.println("Are you there, try and guess a number between 0 - 12");
								System.out.println(receiveString());
								break;
							case TIME_OUT:
								System.out.println("Time Out!");
								break;
							case CLIENT_MOVED_TO_END_OF_QUEUE:
								System.out.println("=>You've Been moved to end of lobby queue, wait for next round<=");
								break;
							case CURRENT_ROUND_RANK:
								System.out.println(receiveString());
								break;
							default:
								break;
							}
						}
					}
				}
			}
		}).start();

	}

	private void fullServerLobbyMessage() {// if server lobby queue is full...
		connected = false;
		System.out.println("Full Lobby, please try later...");
	}

	private void sendString(String string) { // function to send String data to client
		try {
			try {
				outputStream.write(string.getBytes().length);
				outputStream.write((string).getBytes());
				outputStream.flush();
			} catch (SocketException e) {
				System.out.println("Failed sending input, Server down sorry :c");
				connected = false;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					System.out.println(e1.getMessage());
				}
			}
		} catch (IOException e) {
			
		}
	}

	private String receiveString() { // this function receives string located in the inputStream which server has
										// sent to client (taken from lab)
		String string = "";
		try {
			int length = inputStream.read();
			if (length >= 0) {
				byte[] buffer = new byte[length];
				inputStream.read(buffer);
				string = new String(buffer).trim();
			}
		} catch (IOException e) {
			if (requestType == 2) {
				sendString("q");
				System.out.println("TimeOut!");
			}
		}
		return string;
	}
}
