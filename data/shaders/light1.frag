#version 120

//texture 0
uniform sampler2D u_texture;

//our screen resolution, set from Java whenever the display is resized
uniform vec2 resolution;

//"in" attributes from our vertex shader
varying vec4 vColor;
varying vec2 vTexCoord;

//RADIUS of our vignette, where 0.5 results in a circle fitting the screen
const float RADIUS = 0.75;

//softness of our vignette, between 0.0 and 1.0
const float SOFTNESS = 0.45;

void main() {
	//sample our texture
	vec4 texColor = texture2D(u_texture, vTexCoord);
	
	//determine center
	vec2 position = (gl_FragCoord.xy / resolution.xy) - vec2(0.5);
	
	//OPTIONAL: correct for aspect ratio
	//position.x *= resolution.x / resolution.y;
	
	//determine the vector length from center
	float len = length(position);
	
	//our vignette effect, using smoothstep
	float vignette = smoothstep(RADIUS, RADIUS-SOFTNESS, len);
	
	//apply our vignette
	texColor.rgb *= vignette;
	
	gl_FragColor = texColor;
}