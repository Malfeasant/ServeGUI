package us.malfeasant.servegui;

import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

abstract class ComponentWrapper {
	enum Type {
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
				return new FrameWrapper(frame);
			}
		},
		LABEL {
			@Override
			ComponentWrapper makeNew(int index, String text) {
				return new LabelWrapper(new JLabel(text));
			}
		};
		abstract ComponentWrapper makeNew(int index, String text);
	}
	final Type type;
	final Container component;
	
	private ComponentWrapper(Type type, Container comp) {
		this.type = type;
		component = comp;
	}
	void dispose() {
		component.getParent().remove(component);
	}
	void add(ComponentWrapper child) {
		component.add(child.component);
		component.invalidate();
	}
	private static class FrameWrapper extends ComponentWrapper {
		private FrameWrapper(JFrame comp) {
			super(Type.FRAME, comp);
		}
		@Override
		void dispose() {
			((JFrame) component).dispose();
		}
/*		@Override
		void add(ComponentWrapper child) {
			JFrame frame = (JFrame) component;
			frame.add(child.component);
			frame.pack();
		}*/
	}
	private static class LabelWrapper extends ComponentWrapper {
		private LabelWrapper(JLabel comp) {
			super(Type.LABEL, comp);
		}
	}
}
