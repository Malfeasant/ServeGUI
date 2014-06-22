package us.malfeasant.servegui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
	private static final BlockingQueue<String> events = new LinkedBlockingQueue<>();
	private static final ArrayList<Object> objects = new ArrayList<Object>();
	private static Scanner scanner;
	private static boolean run = true;
	private enum Command implements Runnable {
		FRAME_NEW {	// optional String title
			@Override
			public void run() {
				int index = objects.size();	// this will be the new object's index after it's added
				String title = scanner.hasNext() ? scanner.nextLine() : "";
				FutureTask<JFrame> task = new FutureTask<>(new Callable<JFrame>() {
					@Override
					public JFrame call() throws Exception {
						JFrame frame = new JFrame(title);
						frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						frame.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent arg0) {
								events.add(index + " Close");
							}
						});
						frame.setVisible(true);
						return frame;
					}
				});
				defer(task);
				try {
					objects.add(task.get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(System.err);
					System.exit(-1);	// this should not happen
				}
				System.out.println(index);
			}
		},
		FRAME_DISPOSE {	// index
			@Override
			public void run() {
				if (scanner.hasNextInt()) {
					int index = scanner.nextInt();
					if (index < objects.size()) {
						Object thing = objects.get(index);
						if ((thing != null) && thing instanceof JFrame) {
							JFrame frame = (JFrame) thing;
							SwingUtilities.invokeLater(() -> frame.dispose());
							objects.set(index, null);
							System.out.println("Ok.");
						} else {
							error("Object " + index + " is not a Frame.");
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (run) {
			try {
				scanner = new Scanner(reader.readLine());
			} catch (IOException e) {
				// i don't think this will ever happen...
				e.printStackTrace(System.err);
				continue;
			}
			if (scanner.hasNext()) {	// in case of blank line, or otherwise nothing but whitespace...
				Command.recognize(scanner.next());
			}
		}
		System.out.println("Shutting down.");
	}
}
