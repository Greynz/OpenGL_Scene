package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class MovementQ extends AbstractAction {

	Main main;
	
	public MovementQ(Main m){
		super("q");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().translate(0, -1, 0);	
	}

}
