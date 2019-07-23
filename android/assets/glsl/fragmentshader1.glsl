#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 bgtexUniform;
uniform sampler2D u_texture;
uniform sampler2D u_texture2;
uniform float timedelta;

void main(){                                            
  if( texture2D( u_texture,  v_texCoords ) == vec4( 0.0, 0.0, 0.0, 1.0 ) ){
    vec2 displacement = texture2D(u_texture2, v_texCoords/6.0).xy;
    float t=v_texCoords.y +displacement.y*0.1-0.15+  (sin(v_texCoords.x * 60.0+timedelta) * 0.005); 
    gl_FragColor = v_color * texture2D(u_texture, vec2(v_texCoords.x,t));
  } else{ 
    discard; 
  }
}
