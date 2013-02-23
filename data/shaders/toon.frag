varying vec3 Normal;

uniform vec3 LightPosition; // = vec3(10.0, 10.0, 20.0);
uniform vec4 tcol;

void main()
{
    vec4 color1 = tcol;
    vec4 color2;

    float intensity = dot(normalize(LightPosition),Normal);

    if (intensity > 0.95)      color2 = vec4(1.0, 1.0, 1.0, 1.0);
    else if (intensity > 0.75) color2 = vec4(0.8, 0.8, 0.8, 1.0);
    else if (intensity > 0.50) color2 = vec4(0.6, 0.6, 0.6, 1.0);
    else if (intensity > 0.25) color2 = vec4(0.4, 0.4, 0.4, 1.0);
    else                       color2 = vec4(0.2, 0.2, 0.2, 1.0);

    gl_FragColor = color1 * color2;
}