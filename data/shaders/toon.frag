varying vec3 Normal;
varying vec4 pos;

uniform vec4 LightPosition; // = vec3(10.0, 10.0, 20.0);
uniform vec4 tcol;
uniform int useTexture = 0;

uniform sampler2D tex;

uniform float lightscalar;

void main()
{
    vec4 color1 = tcol;
    if( useTexture == 1 )
    {
    	color1 *= texture( tex, gl_TexCoord[0].xy );
    }
    vec4 color2;
	
	float z = LightPosition.z;
	
	LightPosition = gl_ModelViewMatrix * LightPosition;
	
	LightPosition.x -= pos.x;
	LightPosition.y -= pos.y;
	LightPosition.z = z;
	
	float length = sqrt((LightPosition.x*LightPosition.x)+(LightPosition.y*LightPosition.y)+(LightPosition.z*LightPosition.z));
	
    float intensity = dot(normalize(LightPosition),Normal) * min((1.f/length) * lightscalar, 1.5f);

    if (intensity > 0.95)      color2 = vec4(1.0, 1.0, 1.0, 1.0);
    else if (intensity > 0.75) color2 = vec4(0.8, 0.8, 0.8, 1.0);
    else if (intensity > 0.50) color2 = vec4(0.6, 0.6, 0.6, 1.0);
    else if (intensity > 0.25) color2 = vec4(0.4, 0.4, 0.4, 1.0);
    else                       color2 = vec4(0.2, 0.2, 0.2, 1.0);

    gl_FragColor = color1 * color2;
}