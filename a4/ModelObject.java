package a4;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_FRAMEBUFFER;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_CUBE_MAP;
import static com.jogamp.opengl.GL.GL_TRIANGLES;

import java.io.File;
import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vertex3D;

public class ModelObject extends ImportedModel {

	private int vbo[] = new int[3];
	private int vao[] = new int[1];
	private int texture;
	private Main main;
	public Matrix3D transform;
	
	public ModelObject(String filename, String texturename, int v, GL4 gl, Main m) {
		super(filename);
		Texture tmp = loadTexture(texturename);
		texture = tmp.getTextureObject();
		vao[0] = v;
		setupVertices(gl);
		transform = new Matrix3D();
		main = m;
	}
	
	public void displayMe(GL4 gl)
	{
		//vertices
		gl.glBindBuffer(GL_ARRAY_BUFFER, getVertexVbo());
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		//normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, getNormalVbo());
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		//textures
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); 
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);  
		gl.glEnableVertexAttribArray(2);	
		
		gl.glActiveTexture(GL_TEXTURE0 + 1);   
		gl.glBindTexture(GL_TEXTURE_2D, texture);

		gl.glActiveTexture(GL_TEXTURE0+2);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, main.getSkyboxTexture());
		
		gl.glEnable(GL_CULL_FACE);
		gl.glEnable(GL_DEPTH_TEST);  
		gl.glDepthFunc(GL_LEQUAL);
		gl.glFrontFace(GL_CCW);

		gl.glDrawArrays(GL_TRIANGLES, 0, getNumVertices());
	}
	
	public int getVertexVbo(){
		return vbo[0];
	}
	
	public int getTextureVbo(){
		return vbo[1];
	}
	
	public int getNormalVbo(){
		return vbo[2];
	}
	
	private void setupVertices(GL4 gl)
	{	
		Vertex3D[] vertices = getVertices();
		int numObjVertices = getNumVertices();
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).getX();
			pvalues[i*3+1] = (float) (vertices[i]).getY();
			pvalues[i*3+2] = (float) (vertices[i]).getZ();
			tvalues[i*2]   = (float) (vertices[i]).getS();
			tvalues[i*2+1] = (float) (vertices[i]).getT();
			nvalues[i*3]   = (float) (vertices[i]).getNormalX();
			nvalues[i*3+1] = (float) (vertices[i]).getNormalY();
			nvalues[i*3+2] = (float) (vertices[i]).getNormalZ();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
	}
	
	private Texture loadTexture(String textureFileName)
	{
		Texture tex = null;
		try {tex = TextureIO.newTexture(new File(textureFileName), false);}
		catch (Exception e) {e.printStackTrace();}
		return tex;
	}
	
	public Matrix3D getTransform()
	{
		return transform;
	}
	
}
