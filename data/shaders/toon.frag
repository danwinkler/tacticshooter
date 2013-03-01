varying vec4 diffuse,ambientGlobal, ambient, ecPos;
varying vec3 normal,halfVector;
varying float dist;

uniform sampler2D tex;

uniform float maxl; 

uniform int texenabled = 1;
 
void main()
{
    vec3 n,halfV,viewV,lightDir;
    float NdotL,NdotHV;
    vec4 color = ambientGlobal;
    float att;
    
    vec4 color1 = gl_Color;
    
    if( texenabled )
    {
    	 color1 *= texture( tex, gl_TexCoord[0].xy );
    }
     
    /* a fragment shader can't write a varying variable, hence we need
    a new variable to store the normalized interpolated normal */
    n = normalize(normal);
     
    // Compute the ligt direction
    lightDir = vec3(gl_LightSource[0].position-ecPos);
     
    /* compute the distance to the light source to a varying variable*/
    dist = length(lightDir);
 
     
    /* compute the dot product between normal and ldir */
    NdotL = max(dot(n,normalize(lightDir)),0.0);
 	
    if( NdotL > 0.0 ){
     
        att = 1.0 / (gl_LightSource[0].constantAttenuation +
                gl_LightSource[0].linearAttenuation * dist +
                gl_LightSource[0].quadraticAttenuation * dist * dist);
        color += att * (diffuse * NdotL + ambient);
     
         
        //halfV = normalize(halfVector);
       	//NdotHV = max(dot(n,halfV),0.0);
        //color += att * gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV,gl_FrontMaterial.shininess);
    }
    
    if (color.r > 0.95)      color.r = 1.0;
    else if (color.r > 0.75) color.r = 0.8;
    else if (color.r > 0.50) color.r = 0.6;
    else if (color.r > 0.25) color.r = 0.4;
    else     				 color.r = 0.2;
    
    if (color.g > 0.95)      color.g = 1.0;
    else if (color.g > 0.75) color.g = 0.8;
    else if (color.g > 0.50) color.g = 0.6;
    else if (color.g > 0.25) color.g = 0.4;
    else     				 color.g = 0.2;
    
    if (color.b > 0.95)      color.b = 1.0;
    else if (color.b > 0.75) color.b = 0.8;
    else if (color.b > 0.50) color.b = 0.6;
    else if (color.b > 0.25) color.b = 0.4;
    else     				 color.b = 0.2;
	 
    gl_FragColor = color * color1;
}