attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_worldView;
varying vec4 v_color;
varying vec2 v_texCoords;
varying vec2 bgtexuniformCoord;
void main()
{
   v_color = vec4(1, 1, 1, 1);
   v_texCoords = a_texCoord0;
   gl_Position =  u_worldView * a_position;
}