package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import graphicslib3D.Vector3D;

public class MovementLeft extends AbstractAction {

	Main main;
	
	public MovementLeft(Main m){
		super("left");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().rotateHeading(-5);
	}

}
