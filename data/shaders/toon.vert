varying vec3 Normal;

void main()
{   
	gl_TexCoord[0] = gl_MultiTexCoord0; 
	
   	Normal = normalize(gl_Normal);
    #ifdef __GLSL_CG_DATA_TYPES // Fix clipping for Nvidia and ATI
    gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    #endif
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    
    gl_FrontColor = gl_Color;
} 