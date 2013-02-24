varying vec4 diffuse,ambientGlobal, ambient, ecPos;
varying vec3 normal,halfVector;
varying float dist;

uniform sampler2D tex;

uniform float maxl; 
 
void main()
{
    vec3 n,halfV,viewV,lightDir;
    float NdotL,NdotHV;
    vec4 color = ambientGlobal;
    float att;
    
    vec4 color1 = texture( tex, gl_TexCoord[0].xy ) * gl_Color;
     
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
    } else {
     	color.x = 0;
    }
    
    if (color.x > 0.95)      color = vec4(1.0, 1.0, 1.0, 1.0);
    else if (color.x > 0.75) color = vec4(0.8, 0.8, 0.8, 1.0);
    else if (color.x > 0.50) color = vec4(0.6, 0.6, 0.6, 1.0);
    else if (color.x > 0.25) color = vec4(0.4, 0.4, 0.4, 1.0);
    else     				 color = vec4(0.2, 0.2, 0.2, 1.0);
 
    gl_FragColor = color * color1;
}