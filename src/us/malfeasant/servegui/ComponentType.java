package us.malfeasant.servegui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public enum ComponentType {
	FRAME {
		@Override
		ComponentWrapper makeNew(int index, String text) {
			JFrame frame = new JFrame(text);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					Main.events.add(index + " Close");
				}
			});
			frame.setVisible(true);
			return new ComponentWrapper(FRAME, frame);
		}

		@Override
		void dispose(Object o) {
			JFrame frame = (JFrame) o;
			frame.dispose();
		}
	};
	abstract ComponentWrapper makeNew(int index, String text);
	abstract void dispose(Object o);
}
