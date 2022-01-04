package tests;

import java.io.IOException;
import network.ClientNetwork;

public class ClientTest {

	public static void main(String[] args) { // main class, runs the Client
		try {
			new ClientNetwork();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
