package a4;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.common.nio.Buffers;

public class Main extends JFrame implements GLEventListener, MouseMotionListener
{	
	private Camera cam = new Camera();
	private boolean axesVisible = true;
	private boolean clipEnabled = false;
	private float clipBegin = 0;
	private float inc = .01f;
	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] shader1Source, shader2Source, shader3Source;
	private int rendering_program1, rendering_program2, rendering_program_cube_map;
	private int vao[] = new int[1];
	private int mv_location, proj_location, vertexLoc, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	private Point3D cameraLoc = new Point3D(0.0, 0.2, 6.0);
	private Point3D lightLoc = new Point3D(-3.8f, 2.2f, 4.0f);
	private boolean lightOn = true;
	
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	private int mouseDeltaX = 0;
	private int mouseDeltaY = 0;
	private int mouseXLastPos = 0;
	private int mouseYLastPos = 0;
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private ModelObject eggPlant = null;
	private ModelObject tomato = null;
	private ModelObject bowl = null;
	
	private ModelObject axes = null;
	private ModelObject bulb = null;
	private BulbMaterial bmat = null;
	
	//cube-map
	private int textureID2;
	private Matrix3D cubeV_matrix = new Matrix3D();
	private int cubevbo[] = new int[1];
	
	public Main()
	{			
		createUI();
		
		FPSAnimator animator = new FPSAnimator(myCanvas, 30);
		animator.start();
	}

	public void createUI()
	{
		setTitle("assignment");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
		
		InputMap imap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap amap = this.getRootPane().getActionMap();
		
		//actions
		MovementW w = new MovementW(this);
		MovementS s = new MovementS(this);
		MovementA a = new MovementA(this);
		MovementD d = new MovementD(this);
		MovementE e = new MovementE(this);
		MovementQ q = new MovementQ(this);
		MovementLeft lft = new MovementLeft(this);
		MovementRight rt = new MovementRight(this);
		MovementUp mUp = new MovementUp(this);
		MovementDown dwn = new MovementDown(this);
		ToggleAxes tog = new ToggleAxes(this);
		ToggleLight lit = new ToggleLight(this);
		ToggleClipping clp = new ToggleClipping(this);
		
		amap.put("w", w);
		amap.put("s", s);
		amap.put("a", a);
		amap.put("d", d);
		amap.put("e", e);
		amap.put("q", q);
		amap.put("left", lft);
		amap.put("right", rt);
		amap.put("up", mUp);
		amap.put("down", dwn);
		amap.put("toggle", tog);  
		amap.put("light", lit);  
		amap.put("clip", clp);  

		KeyStroke wKey = KeyStroke.getKeyStroke('w');
		KeyStroke sKey = KeyStroke.getKeyStroke('s');
		KeyStroke aKey = KeyStroke.getKeyStroke('a');
		KeyStroke dKey = KeyStroke.getKeyStroke('d');
		KeyStroke eKey = KeyStroke.getKeyStroke('e');
		KeyStroke qKey = KeyStroke.getKeyStroke('q');
		KeyStroke leftKey = KeyStroke.getKeyStroke("LEFT");
		KeyStroke rtKey = KeyStroke.getKeyStroke("RIGHT");
		KeyStroke upKey = KeyStroke.getKeyStroke("UP");
		KeyStroke dwnKey = KeyStroke.getKeyStroke("DOWN");
		KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
		KeyStroke fkey = KeyStroke.getKeyStroke('f');
		KeyStroke onekey = KeyStroke.getKeyStroke('1');

		imap.put(wKey, "w");
		imap.put(sKey, "s");
		imap.put(aKey, "a");
		imap.put(dKey, "d");
		imap.put(eKey, "e");
		imap.put(qKey, "q");
		imap.put(leftKey, "left");
		imap.put(rtKey, "right");
		imap.put(upKey, "up");
		imap.put(dwnKey, "down");
		imap.put(spaceKey, "toggle");
		imap.put(fkey, "light");
		imap.put(onekey, "clip");

		myCanvas.addMouseMotionListener(this);
		
		this.requestFocus();
	}
	
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		currentLight.setPosition(lightLoc);
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.7f, 0.8f, 0.9f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);
		if (clipEnabled)
			gl.glEnable(GL_CLIP_DISTANCE0);
		else
			gl.glDisable(GL_CLIP_DISTANCE0);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts

		passOne();

		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glDrawBuffer(GL_FRONT);
		
		drawSkybox();
		passTwo();


	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public void drawSkybox()
	{
		GL4 gl = (GL4) GLContext.getCurrentGL();
		float depthClearVal[] = new float[1]; depthClearVal[0] = 1.0f;
		gl.glClearBufferfv(GL_DEPTH,0,depthClearVal,0);
		gl.glUseProgram(rendering_program_cube_map);
		
		//  put the V matrix into the corresponding uniforms
		cubeV_matrix = (Matrix3D) cam.getVMat().clone();
		cubeV_matrix.scale(1.0, -1.0, -1.0);
		int v_location = gl.glGetUniformLocation(rendering_program_cube_map, "v_matrix");
		gl.glUniformMatrix4fv(v_location, 1, false, cubeV_matrix.getFloatValues(), 0);
		
		// put the P matrix into the corresponding uniform
		int ploc = gl.glGetUniformLocation(rendering_program_cube_map, "p_matrix");
		gl.glUniformMatrix4fv(ploc, 1, false, proj_matrix.getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, cubevbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0+2);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, textureID2);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);
	}
	
	public void passOne()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		

		
		// ---- draw the eggplant
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(eggPlant.getTransform());

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, eggPlant.getVertexVbo());
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, eggPlant.getNumVertices());
		
		// ---- draw the tomato
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(tomato.getTransform());

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, tomato.getVertexVbo());
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, tomato.getNumVertices());
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program2);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		int offset_loc = gl.glGetUniformLocation(rendering_program2, "clip");
		if (clipBegin > 2.0f) inc = -0.03f;
		else if (clipBegin < -2.0f) inc = 0.03f;
		clipBegin += inc;
		
		gl.glProgramUniform1f(rendering_program2, offset_loc, clipBegin);
				
		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");

		v_matrix = cam.getVMat();
		
		
		// draw the tomato ~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		int reflective = gl.glGetUniformLocation(rendering_program2, "reflective");
		gl.glProgramUniform1f(rendering_program2, reflective, 0.3f);
		
		thisMaterial = graphicslib3D.Material.BRONZE;	
		installLights(rendering_program2, v_matrix);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(tomato.getTransform());
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(tomato.getTransform());

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		tomato.displayMe(gl);
		
		// draw the eggPlant ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		thisMaterial = graphicslib3D.Material.BRONZE;	
		gl.glProgramUniform1f(rendering_program2, reflective, 0.3f);
		installLights(rendering_program2, v_matrix);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(eggPlant.getTransform());
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(eggPlant.getTransform());

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		
		eggPlant.displayMe(gl);
		
		// draw the bowl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		
		thisMaterial = graphicslib3D.Material.SILVER;	
		gl.glProgramUniform1f(rendering_program2, reflective, 0.1f);
		installLights(rendering_program2, v_matrix);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(bowl.getTransform());
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(bowl.getTransform());

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		
		bowl.displayMe(gl);
		
		// draw the axes ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (axesVisible){
			thisMaterial = graphicslib3D.Material.SILVER;
			gl.glProgramUniform1f(rendering_program2, reflective, 0.0f);
			installLights(rendering_program2, v_matrix);
		
			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(axes.getTransform());
			
			shadowMVP2.setToIdentity();
			shadowMVP2.concatenate(b);
			shadowMVP2.concatenate(lightP_matrix);
			shadowMVP2.concatenate(lightV_matrix);
			shadowMVP2.concatenate(axes.getTransform());
		
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
			gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
			
			axes.displayMe(gl);
		}
			// draw the bulb ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		if (axesVisible){
			thisMaterial = bmat;
			gl.glProgramUniform1f(rendering_program2, reflective, 0.5f);
			installLights(rendering_program2, v_matrix);
		
			m_matrix.setToIdentity();
			m_matrix.translate(lightLoc.getX(), lightLoc.getY(), lightLoc.getZ());
			
			//  build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			shadowMVP2.setToIdentity();
			shadowMVP2.concatenate(b);
			shadowMVP2.concatenate(lightP_matrix);
			shadowMVP2.concatenate(lightV_matrix);
			shadowMVP2.concatenate(m_matrix);
		
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
			gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
			
			bulb.displayMe(gl);
		}
		}

	public void init(GLAutoDrawable drawable)
	{	
		setupVertices();
		cam.translate(0, 0, -9);
		GL4 gl = (GL4) GLContext.getCurrentGL();
		tomato = new ModelObject("Tomato.obj", "TomatoTex.jpg", vao[0], gl, this);
		tomato.transform.setToIdentity();
		tomato.transform.translate(-1, -.4, 0);
		tomato.transform.rotateX(30.0);
		tomato.transform.rotateY(40.0);
		bowl = new ModelObject("bowlo.obj", "WoodenBowl.jpg", vao[0], gl, this);
		
		eggPlant = new ModelObject("EggPlant.obj", "EggPlantTex.jpg", vao[0], gl, this);
		eggPlant.transform.setToIdentity();
		eggPlant.transform.translate(1, -.3, -1);
		eggPlant.transform.scale(.8, .8, .8);
		eggPlant.transform.rotateX(330);
		eggPlant.transform.rotateY(45);
		eggPlant.transform.rotateZ(20);
		
		axes = new ModelObject("Axes.obj", "AxesTex.jpg", vao[0], gl, this);
		bulb = new ModelObject("lightbulb.obj", "lightbulbtex.jpg", vao[0], gl, this);
		bmat = new BulbMaterial();
		bmat.setSpecular(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
		createShaderPrograms();
		setupShadowBuffers();
		
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		textureID2 = loadCubeMap();
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
	}
	
	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		float[] cube_vertices =
	        {	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(1, cubevbo, 0);
		
		// load the cube vertex coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, cubevbo[0]);
		FloatBuffer cubeVertBuf = Buffers.newDirectFloatBuffer(cube_vertices);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeVertBuf.limit()*4, cubeVertBuf, GL_STATIC_DRAW);
	}
	
	private int loadCubeMap()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		GLProfile glp = gl.getGLProfile();
		Texture tex = new Texture(GL_TEXTURE_CUBE_MAP);
		try {
			TextureData topFile = TextureIO.newTextureData(glp, new File("cubeMap/top.jpg"), false, "jpg");
			TextureData leftFile = TextureIO.newTextureData(glp, new File("cubeMap/left.jpg"), false, "jpg");
			TextureData fntFile = TextureIO.newTextureData(glp, new File("cubeMap/center.jpg"), false, "jpg");
			TextureData rightFile = TextureIO.newTextureData(glp, new File("cubeMap/right.jpg"), false, "jpg");
			TextureData bkFile = TextureIO.newTextureData(glp, new File("cubeMap/back.jpg"), false, "jpg");
			TextureData botFile = TextureIO.newTextureData(glp, new File("cubeMap/bottom.jpg"), false, "jpg");
			
			tex.updateImage(gl, rightFile, GL_TEXTURE_CUBE_MAP_POSITIVE_X);
			tex.updateImage(gl, leftFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
			tex.updateImage(gl, botFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
			tex.updateImage(gl, topFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
			tex.updateImage(gl, fntFile, GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
			tex.updateImage(gl, bkFile, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		} catch (IOException|GLException e) {System.out.println("yoi");}
		
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = tex.getTextureObject();
		
		// reduce seams
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

		return textureID;
	}
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	
		setupShadowBuffers();
	}

	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		shader1Source = GLSLUtils.readShaderSource("./a4/blinnVert1.shader");
		shader2Source = GLSLUtils.readShaderSource("./a4/blinnVert2.shader");
		shader3Source = GLSLUtils.readShaderSource("./a4/blinnFrag2.shader");

		
		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vertexShader1, shader1Source.length, shader1Source, null, 0);
		gl.glShaderSource(vertexShader2, shader2Source.length, shader2Source, null, 0);
		gl.glShaderSource(fragmentShader2, shader3Source.length, shader3Source, null, 0);

		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);

		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();

		gl.glAttachShader(rendering_program1, vertexShader1);
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);

		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);
		
		
		//cube map rendering program
		shader1Source = GLSLUtils.readShaderSource("./a4/vertC.shader");
		shader2Source = util.readShaderSource("./a4/fragC.shader");

		int vertexShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vertexShader, shader1Source.length, shader1Source, null, 0);
		gl.glShaderSource(fragmentShader, shader2Source.length, shader2Source, null, 0);

		gl.glCompileShader(vertexShader);
		gl.glCompileShader(fragmentShader);

		rendering_program_cube_map = gl.glCreateProgram();
		gl.glAttachShader(rendering_program_cube_map, vertexShader);
		gl.glAttachShader(rendering_program_cube_map, fragmentShader);
		gl.glLinkProgram(rendering_program_cube_map);
	}

//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}
	
	public Camera getCamera()
	{
		return cam;
	}
	
	public void toggleAxes()
	{
		axesVisible = !axesVisible;
	}

	public void toggleLight(){
		lightOn = !lightOn;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseDeltaX = e.getX() - mouseXLastPos;
		mouseDeltaY = e.getY() - mouseYLastPos;
		lightLoc.setX(clamp((float) lightLoc.getX() + mouseDeltaX * .03f, -4, 4));
		lightLoc.setY(clamp((float) lightLoc.getY() - mouseDeltaY * .03f, 0, 3));
		mouseXLastPos = e.getX();
		mouseYLastPos = e.getY();
		
	}
	
	public int getSkyboxTexture(){
		return textureID2;
	}
	
	public void toggleClipping(){
		clipEnabled = !clipEnabled;
	}
	
	public float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}