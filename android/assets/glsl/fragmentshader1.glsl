#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 v_texCoords2;
uniform sampler2D bgtexUniform;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform float timedelta;

void main(){                                            
    vec4 tex2color = texture2D(bgtexUniform, v_texCoords);

    vec2 displacement = texture2D(u_texture2, v_texCoords/6.0).xy;
    //float t=v_texCoords.y + (displacement.y * 0.1) - 0.15 +  (sin(v_texCoords.x * 60.0+timedelta) * 0.005);
    float t=v_texCoords.x + (displacement.x * 0.1)  +  (cos(v_texCoords.y * 60.0+timedelta) * 0.005);
    gl_FragColor = v_color * mix(texture2D(u_texture, vec2(t, v_texCoords.y)), tex2color, tex2color.a);

}
