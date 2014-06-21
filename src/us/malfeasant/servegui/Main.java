package us.malfeasant.servegui;

import java.util.Scanner;

public class Main {
	private static Scanner scanner = new Scanner(System.in);
	private enum Command {
		EXIT {
			@Override
			void act() {
				run = false;
			}
		};
		static void recognize(String in) {
			System.err.println("Received: " + in);
			try {
				Command.valueOf(in.toUpperCase()).act();
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid command.");
			}
		}
		abstract void act();
	}
	static boolean run = true;
	public static void main(String[] args) {
		while (run) {
			Command.recognize(scanner.next());
//			String command = scanner.next();
		}
		System.err.println("Shutting down.");
		scanner.close();
	}
}
