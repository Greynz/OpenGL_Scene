package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class MovementD extends AbstractAction {

	Main main;
	
	public MovementD(Main m){
		super("d");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().translate(-1, 0, 0);	
	}

}
