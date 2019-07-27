
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
uniform mat4 u_projTrans;

void main() {
        //vec4 tex2color = texture2D(u_texture, vec2(v_texCoords.x, v_texCoords.y));

        vec4 tex1color = texture2D(u_texture, v_texCoords);
        vec4 tex2color = vec4(0, 1, 1, 1); //texture2D(u_texture, v_texCoords);

        gl_FragColor = v_color * mix(
            tex2color,
            tex1color,
            tex1color.a
        );
}