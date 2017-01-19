package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class ToggleAxes extends AbstractAction {

	Main main;
	
	public ToggleAxes(Main m){
		super("toggle");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.toggleAxes();
	}
	
}
