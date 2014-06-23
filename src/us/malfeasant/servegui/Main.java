package us.malfeasant.servegui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

public class Main {
	static final BlockingQueue<String> events = new LinkedBlockingQueue<>();
	private static final ArrayList<ComponentWrapper> objects = new ArrayList<ComponentWrapper>();
	private static Scanner scanner;
	private static boolean run = true;
	private enum Command implements Runnable {
		NEW {	// component type, optional String title
			@Override
			public void run() {
				int index = objects.size();	// this will be the new object's index after it's added
				try {
					ComponentType type = ComponentType.valueOf(scanner.next().toUpperCase());
					String title = scanner.hasNext() ? scanner.nextLine() : "";
					FutureTask<ComponentWrapper> task = new FutureTask<>(() -> type.makeNew(index, title));
					defer(task);
					objects.add(task.get());
				} catch (NoSuchElementException e) {
					error("Invalid component type.");
					return;
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
					System.exit(-1);	// these should not happen
				}
				System.out.println(index);
			}
		},
		DISPOSE {	// index
			@Override
			public void run() {
				if (scanner.hasNextInt()) {
					int index = scanner.nextInt();
					if (index < objects.size()) {
						ComponentWrapper wrapper = objects.get(index);
						if (wrapper != null) {
							SwingUtilities.invokeLater(() -> wrapper.type.dispose(wrapper.component));
							objects.set(index, null);
							System.out.println("Ok.");
						} else {
							error("Object " + index + " already disposed.");
						}
					} else {
						error("Invalid index.");
					}
				} else {
					error("Missing frame index.");
				}
			}
		},
		POLL {
			@Override
			public void run() {
				// this may look like an unsafe check-and-modify... but worst that can happen is an event gets
				// added after checking, so it will not be caught until next time.  no other thread removes
				// events, so will never try to read an empty queue.
				System.out.println(events.isEmpty() ? "-1" : events.poll());
			}
		},
		WAIT {
			@Override
			public void run() {
				try {
					System.out.println(events.take());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(-1);	// this should not happen
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
		void defer(Runnable r) {
			SwingUtilities.invokeLater(r);
		}
	}
	static void error(String e) {
		System.out.println("Error.");
		System.err.println(e);
	}
	public static void main(String[] args) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (run) {
			Command cmd = null;
			try {
				scanner = new Scanner(reader.readLine());
				cmd = Command.valueOf(scanner.next().toUpperCase());
			} catch (IOException e) {
				// TODO i don't think this will ever happen...
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				error("Invalid command.");
			}
			if (cmd != null) cmd.run();
		}
		System.out.println("Shutting down.");
	}
}
