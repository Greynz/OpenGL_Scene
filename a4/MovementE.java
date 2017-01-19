package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class MovementE extends AbstractAction {

	Main main;
	
	public MovementE(Main m){
		super("e");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().translate(0, 1, 0);	
	}

}
