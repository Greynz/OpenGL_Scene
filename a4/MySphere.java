package a4;

import java.io.File;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import graphicslib3D.Vertex3D;
import graphicslib3D.shape.Sphere;

public class MySphere extends Sphere {

	private int vbo[] = new int[3];
	private int vao[] = new int[1];
	private int texture;

	GL4 gl;
	
	public MySphere(String tex, int precision, int va, GL4 g)
	{
		super(precision);
		gl = g;
		vao[0] = va;
		setupVertices();
		Texture tmp = loadTexture(tex);
		texture = tmp.getTextureObject();
		
	}
	
	public void displayMe(GL4 gl)
	{
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);   
		gl.glEnableVertexAttribArray(0); 
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); 
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);  
		gl.glEnableVertexAttribArray(1);  
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		gl.glActiveTexture(GL_TEXTURE0);   
		gl.glBindTexture(GL_TEXTURE_2D, texture);   
		gl.glEnable(GL_DEPTH_TEST);  
		gl.glDepthFunc(GL_LEQUAL);   
		gl.glDrawArrays(GL_TRIANGLES, 0, getIndices().length);
	}
	
	private void setupVertices() 
	{ 
		Vertex3D[] vertices = getVertices(); 
		int[] indices = getIndices(); 
		float[] pvalues = new float[indices.length*3];  
		float[] tvalues = new float[indices.length*2];  
		float[] nvalues = new float[indices.length*3];  
		for (int i=0; i<indices.length; i++) 
		{ 
			pvalues[i*3] = (float) (vertices[indices[i]]).getX(); 
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY(); 
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();  
			
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();   
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT(); 
			
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX(); 
			nvalues[i*3+1] = (float)(vertices[indices[i]]).getNormalY();   
			nvalues[i*3+2 ]=(float) (vertices[indices[i]]).getNormalZ();  
		}  
		gl.glGenVertexArrays(vao.length, vao, 0);  
		gl.glBindVertexArray(vao[0]);  
		gl.glGenBuffers(3, vbo, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);  
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);  
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW); 
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); 
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);  
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);  
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues); 
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4, norBuf, GL_STATIC_DRAW); 
	}
	
	private Texture loadTexture(String textureFileName)
	{
		Texture tex = null;
		try {tex = TextureIO.newTexture(new File(textureFileName), false);}
		catch (Exception e) {e.printStackTrace();}
		return tex;
	}
}
	

