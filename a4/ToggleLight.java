package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class ToggleLight extends AbstractAction {

	Main main;
	
	public ToggleLight(Main m){
		super("light");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.toggleLight();
	}

}
