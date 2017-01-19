package a4;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import graphicslib3D.Vector3D;

public class MovementRight extends AbstractAction {

	Main main;
	
	public MovementRight(Main m){
		super("right");
		main = m;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		main.getCamera().rotateHeading(5);
	}

}
