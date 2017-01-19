package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import graphicslib3D.Vector3D;

public class MovementDown extends AbstractAction {

	Main main;
	
	public MovementDown(Main m){
		super("down");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().rotateElevation(5);
	}

}
