package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class MovementW extends AbstractAction {

	Main main;
	
	public MovementW(Main m){
		super("w");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().translate(0, 0, 1);	
	}
	
}
