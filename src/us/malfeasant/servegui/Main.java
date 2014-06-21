package us.malfeasant.servegui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

public class Main {
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	private static Scanner scanner;
	private static final Map<String, Object> objects = new HashMap<>();
	private static boolean run = true;
	private enum Command implements Runnable {
		NEW {	// new [fq class name] id
			@Override
			public void run() {
				String which = scanner.next();
				String id = scanner.next();
				if (objects.containsKey(id)) {
					error("Id must be unique.");
					return;
				}
				try {
					Class<?> cl = Class.forName(which);
					Constructor<?> con = cl.getConstructor();
					FutureTask<Object> task = new FutureTask<>(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							return con.newInstance();
						}
					});
					defer(task);
					objects.put(id, task.get());
					System.out.println("Ok: " + id);
				} catch (ClassNotFoundException e) {
					error("Class not found: " + which);
					return;
				} catch (NoSuchMethodException e) {
					error("Class " + which + " does not have a no-arg constructor.");
					return;
				} catch (SecurityException | IllegalArgumentException | InterruptedException | ExecutionException e) {
					// doubt any of these will happen...
					e.printStackTrace();
				}
			}
		},
		CALL {	// call slot method args
			@Override
			public void run() {
				String slot = scanner.next();
				Object obj = objects.get(slot);
				if (obj == null) {
					error("Slot " + slot + " not found");
				}
			}
		},
		EXIT {
			@Override
			public void run() {
				run = false;
			}
		},
		PING {	// just a silly test
			@Override
			public void run() {
				System.out.println("Pong");
			}
		};
		static void recognize(String in) {
			try {
				Command.valueOf(in.toUpperCase()).run();
			} catch (IllegalArgumentException e) {
				error("Invalid command: " + in);
			}
		}
		void defer(Runnable r) {
			SwingUtilities.invokeLater(r);
		}
		static void error(String e) {
			System.out.println("Error.");
			System.err.println(e);
		}
	}
	public static void main(String[] args) {
		while (run) {
			try {
				scanner = new Scanner(reader.readLine());
			} catch (IOException e) {
				// i don't think this will ever happen...
				e.printStackTrace(System.err);
				continue;
			}
			Command.recognize(scanner.next());
		}
		System.out.println("Shutting down.");
	}
}
