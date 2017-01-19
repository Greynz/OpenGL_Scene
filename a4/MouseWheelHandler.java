package a4;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseWheelHandler implements MouseWheelListener{

	private Main main;
	
	public MouseWheelHandler(Main m)
	{
		main = m;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//main.scale(e.getWheelRotation());
	}

}
