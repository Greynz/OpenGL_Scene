package a4;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;

public class Camera {

	private Matrix3D vMat = new Matrix3D();
	
	private Vector3D U = new Vector3D(1,0,0);
	private Vector3D V = new Vector3D(0,1,0);
	private Vector3D N = new Vector3D(0,0,1);
	private Vector3D position = new Vector3D(0,0,0);
	
	private float heading = 0;
	private float elevation = 0;
		
	public void recalculateAngles()
	{	
		N.setX(Math.cos(elevation * Math.sin(heading)));
		N.setY(Math.sin(elevation));
		N.setZ(Math.cos(elevation * Math.cos(heading)));
		
		V = new Vector3D(0,1,0);
		Vector3D x = new Vector3D();
		x = N.cross(V);
		U = x;
		
		x = new Vector3D();
		x = N.cross(U);
		V = x;
		
		N = N.normalize();
		U = U.normalize();
		V = V.normalize();

	}
	
	/*		Vector3D front = new Vector3D(0,0,0);
		front.setX(Math.cos(Math.toRadians(elevation) * Math.cos(Math.toRadians(heading))));
		front.setY(Math.toRadians(heading));
		front.setZ(Math.sin(Math.toRadians(elevation) * Math.cos(Math.toRadians(heading))));
		N = front.normalize();
		U = N.cross(new Vector3D(0,1,0)).normalize();
		V = U.cross(N).normalize();
*/
	
	public void rotateHeading(float a)
	{
		heading += a;
		vMat.rotateY(a);
		if (heading > 360)
			heading -= 360;
		else if (heading < 0)
			heading += 360;
		
		recalculateAngles();
		updateVMat();
	}
	
	public void rotateElevation(float a)
	{
		elevation += a;
		if (elevation > 360)
			elevation -= 360;
		else if (elevation < 0)
			elevation += 360;
		
		recalculateAngles();
		updateVMat();
	}
	
	public void translate(float x, float y, float z)
	{
		position = position.add(new Vector3D(1,0,0).mult(x));
		position = position.add(new Vector3D(0,1,0).mult(y));
		position = position.add(new Vector3D(0,0,1).mult(z));
		updateVMat();
	}
	
	public void updateVMat()
	{
		vMat.setToIdentity();
		vMat.rotateY(heading);
		vMat.rotateX(elevation);
		vMat.translate(position.getX(), position.getY(), position.getZ());
	}
	
	public Matrix3D getVMat()
	{
		return vMat;
	}
	
}
