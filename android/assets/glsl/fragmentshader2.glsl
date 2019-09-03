#ifdef GL_ES
precision highp float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_water;
uniform sampler2D u_noise;
uniform mat4 u_projTrans;
uniform float timedelta;

void main() {
    vec4 tex2color = texture2D(u_texture, v_texCoords);

    vec2 displacement = texture2D(u_noise, v_texCoords/6.0).xy;
    //float t=v_texCoords.y + (displacement.y * 0.1) - 0.15 +  (sin(v_texCoords.x * 60.0+timedelta) * 0.005);
    float t=v_texCoords.x + (displacement.x * 0.1)  +  (cos(v_texCoords.y * 60.0+timedelta) * 0.005);
    gl_FragColor = v_color * mix(
        texture2D(u_water, vec2(t, v_texCoords.y) ),
        tex2color,
        tex2color.a
    );

}