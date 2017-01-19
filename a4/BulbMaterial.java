package a4;

import graphicslib3D.Material;

public class BulbMaterial extends Material {

	public BulbMaterial(){
		this.setAmbient(new float[]{5,5,5,1});
		this.setDiffuse(new float[]{5,5,5,5});
		this.setSpecular(new float[]{0,0,0,1});
		this.setShininess(2.0f);
	}
	
}
