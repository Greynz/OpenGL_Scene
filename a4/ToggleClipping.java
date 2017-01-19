package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class ToggleClipping extends AbstractAction {

	Main main;
	
	public ToggleClipping(Main m){
		super("clip");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.toggleClipping();
	}

}
