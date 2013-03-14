varying vec3 Normal;

uniform vec3 LightPosition = vec3(-1, 1, -1);

uniform sampler2D tex;

uniform int texenabled = 1;

void main()
{
    vec4 color1 = gl_Color;
    
    if( texenabled )
    {
    	 color1 *= texture( tex, gl_TexCoord[0].xy );
    }
    
    vec4 color2;

    float intensity = dot(normalize(LightPosition),Normal);

    if (intensity > 0.8)      color2 = vec4(1.0, 1.0, 1.0, 1.0);
    else if (intensity > 0.6) color2 = vec4(0.8, 0.8, 0.8, 1.0);
    else if (intensity > 0.4) color2 = vec4(0.6, 0.6, 0.6, 1.0);
    else if (intensity > 0.2) color2 = vec4(0.4, 0.4, 0.4, 1.0);
    else                       color2 = vec4(0.2, 0.2, 0.2, 1.0);

    gl_FragColor = color1 * color2;
}