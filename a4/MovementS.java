package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class MovementS extends AbstractAction {

	Main main;
	
	public MovementS(Main m){
		super("s");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().translate(0, 0, -1);	
	}

}
