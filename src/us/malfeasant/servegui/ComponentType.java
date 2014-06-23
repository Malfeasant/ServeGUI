package us.malfeasant.servegui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

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
		@Override
		void add(Object parent, JComponent child) {
			((JFrame) parent).add(child);
		}
	},
	LABEL {
		@Override
		ComponentWrapper makeNew(int index, String text) {
			return new ComponentWrapper(LABEL, new JLabel(text));
		}
	};
	abstract ComponentWrapper makeNew(int index, String text);
	void dispose(ComponentWrapper comp) {	// should work for almost everything, others will override
		((JComponent) comp.component).getParent().remove(comp);
	}
	void add(Object parent, JComponent child) {
		((JComponent) parent).add( child);
	}
}
