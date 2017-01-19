#version 430

layout (location=0) in vec3 vertPos;

uniform mat4 shadowMVP;
vec4 clip_plane = vec4(-1.5, 0.0, 0, -1.0);

void main(void)
{	
	gl_ClipDistance[0] = dot(clip_plane.xyz, vertPos) + clip_plane.w;
	gl_Position = shadowMVP * vec4(vertPos,1.0);
}
