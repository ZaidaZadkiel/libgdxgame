package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class MyGdxGame implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture texture;
	private Sprite sprite;

	FileHandle vertexShader;/* =
			"attribute vec4 a_position;    \n"
					+ "attribute vec2 a_texCoord0;\n"
					+ "uniform mat4 u_worldView;\n"
					+ "varying vec4 v_color;"
					+ "varying vec2 v_texCoords;\n"
					+ "varying vec2 bgtexuniformCoord;\n"
					+ "void main()                  \n"
					+ "{                            \n"
					+ "   v_color = vec4(1, 1, 1, 1); \n"
					+ "   v_texCoords = a_texCoord0; \n"
					+ "   gl_Position =  u_worldView * a_position;  \n"
					+ "}                            \n";
*/
	FileHandle fragmentShader;
	/*= "#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "uniform sampler2D u_texture2;\n"
			+ "uniform float timedelta;\n"
			+ "void main()                                  \n"
			+ "{                                            \n"
			+ " if( " +
			//"dot( v_texCoords, v_texCoords ) > 0.5 " +
			"Sampler2d( bgtexUniform, v_texCoords ) == vec4( 0.0, 0.0, 0.0, 1.0 ) " +
			"){ \n"
			+ "  vec2 displacement = texture2D(u_texture2, v_texCoords/6.0).xy;\n" //
			+ "  float t=v_texCoords.y +displacement.y*0.1-0.15+  (sin(v_texCoords.x * 60.0+timedelta) * 0.005); \n" //
			+ "  gl_FragColor = v_color * texture2D(u_texture, vec2(v_texCoords.x,t));\n"
			+ "} else{ discard; }\n"
			+ "}";
*/
	FileHandle fragmentShader2;
	/*= "#ifdef GL_ES\n"
			+ "precision mediump float;\n"
			+ "#endif\n"
			+ "varying vec4 v_color;\n"
			+ "varying vec2 v_texCoords;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "void main()                                  \n"
			+ "{                                            \n"

			+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"

			+ "}";
*/

	ShaderProgram shader;
	ShaderProgram waterShader;

	Matrix4 matrix;
	float time;

	Mesh waterMesh;

	private Texture texture2;
	private Texture texture3;

	@Override
	public void create() {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(1, h / w);
		batch = new SpriteBatch();

		texture = new Texture(Gdx.files.internal("DragonDormido.png"));
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		texture2 =new Texture(Gdx.files.internal("data/water.png"));
		texture2.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		texture3 = new Texture(Gdx.files.internal("data/waterdisplacement.png"));
		texture3.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		texture3.bind();
		matrix = new Matrix4();

		TextureRegion region = new TextureRegion(texture, 0, 0, texture.getWidth(), texture.getHeight());
		ShaderProgram.pedantic=false;

		fragmentShader  = Gdx.files.internal("glsl/fragmentshader1.glsl");
		vertexShader    = Gdx.files.internal("glsl/vertexshader.glsl");
		fragmentShader2 = Gdx.files.internal("glsl/fragmentshader2.glsl");

		shader = new ShaderProgram(vertexShader, fragmentShader);
		waterShader = new ShaderProgram(vertexShader, fragmentShader2);
		waterShader.setUniformMatrix("u_projTrans", matrix);

		waterMesh = createQuad(-1, -1, 1, -1, 1, 1.0f, -1, 1.0f);

		//BACKGROUND SPRITE
		sprite = new Sprite(region);
		sprite.setSize(1f, 1f );
		sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
		sprite.setPosition(-sprite.getWidth() / 2, -sprite.getHeight() / 2);

		time=1f;
	}

	@Override
	public void dispose() {
		batch.dispose();
		texture.dispose();
		texture2.dispose();
		texture3.dispose();
	}

	@Override
	public void render() {

		float dt = Gdx.graphics.getDeltaTime();
		time += dt;
		float angle = time * (2 * MathUtils.PI);
		if (angle > (2 * MathUtils.PI))
			angle -= (2 * MathUtils.PI);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl20.glEnable(GL20.GL_BLEND);


		//RENDER BACKGROUND
		batch.setShader(waterShader);
		texture.bind(1);
		batch.begin();
		shader.setUniformi("u_texture", 1);
		waterShader.setUniformMatrix("u_worldView", camera.combined);

		sprite.draw(batch);
		batch.end();


		//texture2.bind(1);
		texture2.bind(1);
		texture3.bind(2);
		texture.bind(3);

		shader.begin();
		shader.setUniformMatrix("u_worldView",  matrix);
		shader.setUniformi("u_texture", 1);
		shader.setUniformi("u_texture2", 2);
		shader.setUniformi("bgtexUniform", 3);

		shader.setUniformf("timedelta", -angle);
		waterMesh.render(shader, GL20.GL_TRIANGLE_FAN);
		shader.end();




	}



	public Mesh createQuad(float x1, float y1, float x2, float y2, float x3,
						   float y3, float x4, float y4) {
		float[] verts = new float[20];
		int i = 0;

		verts[i++] = x1; // x1
		verts[i++] = y1; // y1
		verts[i++] = 0;
		verts[i++] = 1f; // u1
		verts[i++] = 1f; // v1

		verts[i++] = x2; // x2
		verts[i++] = y2; // y2
		verts[i++] = 0;
		verts[i++] = 0f; // u2
		verts[i++] = 1f; // v2

		verts[i++] = x3; // x3
		verts[i++] = y3; // y2
		verts[i++] = 0;
		verts[i++] = 0f; // u3
		verts[i++] = 0f; // v3

		verts[i++] = x4; // x4
		verts[i++] = y4; // y4
		verts[i++] = 0;
		verts[i++] = 1f; // u4
		verts[i++] = 0f; // v4

		Mesh mesh = new Mesh(true, 4, 0, // static mesh with 4 vertices and no
				// indices
				new VertexAttribute(Usage.Position, 3,
						ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
				Usage.TextureCoordinates, 2,
				ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		mesh.setVertices(verts);
		return mesh;

	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}