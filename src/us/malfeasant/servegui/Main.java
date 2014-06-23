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
	private enum Command {
		NEW {	// component type, optional String title
			@Override
			void run() {
				int index = objects.size();	// this will be the new object's index after it's added
				try {
					ComponentWrapper.Type type = ComponentWrapper.Type.valueOf(scanner.next().toUpperCase());
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
			void run() {
				int index = nextComponentIndex();
				if (index < 0) error("Bad index.");
				else {
					ComponentWrapper wrapper = objects.get(index);
					SwingUtilities.invokeLater(() -> wrapper.dispose());
					objects.set(index, null);
					System.out.println("Ok.");
				}
			}
		},
		ADD {	// index of parent, index of child
			@Override
			void run() {
				int parent = nextComponentIndex();
				int child = nextComponentIndex();
				if (parent < 0 || child < 0) error("Bad index.");
				else {
					ComponentWrapper parentWrapper = objects.get(parent);
					ComponentWrapper childWrapper = objects.get(child);
					SwingUtilities.invokeLater(() -> parentWrapper.add(childWrapper));
					System.out.println("Ok.");
				}
			}
		},
		POLL {
			@Override
			void run() {
				// this may look like an unsafe check-and-modify... but worst that can happen is an event gets
				// added after checking, so it will not be caught until next time.  no other thread removes
				// events, so will never try to read an empty queue.
				System.out.println(events.isEmpty() ? "-1" : events.poll());
			}
		},
		WAIT {
			@Override
			void run() {
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
			void run() {
				run = false;
			}
		},
		PING {	// just a silly test
			@Override
			void run() {
				System.out.println("Pong");
			}
		};
		abstract void run();
		void defer(Runnable r) {
			SwingUtilities.invokeLater(r);
		}
		int nextComponentIndex() {
			if (scanner.hasNextInt()) {
				int index = scanner.nextInt();
				if (index < objects.size()) {
					if (objects.get(index) != null) return index;
				}
			}
			return -1;
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
