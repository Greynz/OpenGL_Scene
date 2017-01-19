package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import graphicslib3D.Vector3D;

public class MovementUp extends AbstractAction {

	Main main;
	
	public MovementUp(Main m){
		super("up");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().rotateElevation(-5);
	}

}
