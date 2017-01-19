#version 430


in vec3 vertEyeSpacePos;
in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
in vec2 tc;
out vec4 fragColor;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform float reflective;
uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D samp;
layout (binding=2) uniform samplerCube t;

void main(void)
{	
	vec3 r = reflect(normalize(-vVertPos), normalize(vNormal));
	vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0);	// bluish gray
	float fogStart = 3.0;
	float fogEnd = 15.0;

	vec3 L = normalize(vLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(vHalfVec);
	
	float inShadow = textureProj(shadowTex, shadow_coord);
	
	fragColor = texture(samp, tc) * (globalAmbient * material.ambient
				+ light.ambient * material.ambient);
	
	if (inShadow != 0.0)
	{	fragColor += light.diffuse * texture(samp, tc) * max(dot(L,N),0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0);
	}
	
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	fragColor = mix(fogColor, fragColor + (texture(t,r) * reflective), fogFactor);
	//fragColor = mix(fragColor, texture(t,r), reflective);
}
