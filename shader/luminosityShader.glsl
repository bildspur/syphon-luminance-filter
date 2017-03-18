#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D texture;

varying vec4 vertColor;
varying vec4 vertTexCoord;

uniform float brightness;

void main() {
    vec3 luminanceVector = vec3(0.2125, 0.7154, 0.0721);
    vec4 c = texture2D(texture, vertTexCoord.st) * vertColor;

    float luminance = dot(luminanceVector, c.xyz);

    if(luminance > 0)
        c = vec4(max(c.x, brightness), max(c.y, brightness), max(c.z, brightness), c.a);

    gl_FragColor = c;
}