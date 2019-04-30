package nf.co.cepi.micropolis;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import javax.xml.soap.Text;


public class Micropolis implements ApplicationListener, InputProcessor{
	private TiledMap tiledMap;
	private OrthographicCamera camera;
	private TiledMapRenderer tiledMapRenderer;
	private Vector2 lastTouch;
	private SpriteBatch spriteBatch;
	private SpriteBatch camSpriteBatch;
	private BitmapFont bitmapFont;
	private String coord = "UNTOUCHED";
	private Vector3 unprojectedCoord;
	private String camcoord = "UNTOUCHED";
	private String isocoord = "UNTOUCHED";
	Texture selectorTexture;
	Sprite selectorSprite;
	Vector2 selectorPos;
	ShapeRenderer sh;
	Viewport viewport;

	@Override
	public void create () {
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		spriteBatch = new SpriteBatch();
		bitmapFont = new BitmapFont();
		bitmapFont.setColor(Color.BLUE);

		camera = new OrthographicCamera();
		camera.setToOrtho(false,400,400f*(h/w));
		viewport = new FitViewport(400,400, camera);
		viewport.apply();

		sh = new ShapeRenderer();


		spriteBatch = new SpriteBatch();
		camSpriteBatch = new SpriteBatch();

		camera.update();
		tiledMap = new TmxMapLoader().load("test.tmx");
		tiledMapRenderer = new IsometricTiledMapRenderer(tiledMap);

		selectorTexture = new Texture("selector.png");
		selectorSprite = new Sprite(selectorTexture);
		selectorSprite.setPosition(64,0);

		selectorPos = new Vector2(0,0);

		Gdx.input.setInputProcessor(this);

	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = 400;
		camera.viewportHeight = 400f * height/width;
		camera.update();
		bitmapFont.getData().setScale(1,1f*height/width);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();
		spriteBatch.begin();
		bitmapFont.draw(spriteBatch,coord, 0,75);
		bitmapFont.draw(spriteBatch,camcoord, 0,50);
		bitmapFont.draw(spriteBatch,isocoord, 0,25);

        spriteBatch.end();

        camSpriteBatch.setProjectionMatrix(camera.combined);
        camSpriteBatch.begin();
        selectorSprite.draw(camSpriteBatch);
        camSpriteBatch.end();

//		sh.setProjectionMatrix(camera.combined);
//		sh.begin(ShapeRenderer.ShapeType.Line);
//		sh.setColor(Color.WHITE);
//		for(int x=0;x<9;x++){
//			sh.line(0+x*16,0-x*8,128+x*16,64-x*8);
//			sh.line(0+x*16,0+x*8,128+x*16,-64+x*8);
//		}
//		sh.end();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose () {
	}

	@Override
	public boolean keyDown(int keycode) {
	    if(keycode == Input.Keys.LEFT)
            camera.translate(-32,0);
        if(keycode == Input.Keys.RIGHT)
            camera.translate(32,0);
        if(keycode == Input.Keys.UP)
            camera.translate(0,-32);
        if(keycode == Input.Keys.DOWN)
            camera.translate(0,32);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
	    lastTouch = new Vector2(screenX,screenY);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
	    Vector3 pos = new Vector3(screenX, screenY, 0);
		coord = "X:" + screenX +"  Y:" + screenY + "   zoom:"+camera.zoom;
		unprojectedCoord = camera.unproject(pos);
		camcoord = "X:" + unprojectedCoord.x + "   Y:" + unprojectedCoord.y + "   Z:"+unprojectedCoord.z;
		Vector2 isovect = screen2isometricgrid(unprojectedCoord.x,unprojectedCoord.y);
		isocoord = "x:" + Math.floor(isovect.x) +"  y:" + Math.floor(isovect.y) + "   zoom:" + camera.zoom;
		Vector2 newTouch = new Vector2(screenX,screenY);

		Vector2 delta = newTouch.cpy().sub(lastTouch);
		camera.translate(delta.x*-0.6f*camera.zoom,delta.y*0.6f*camera.zoom);
		lastTouch = newTouch;
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		Vector3 pos = new Vector3(screenX, screenY, 0);
		coord = "X:" + screenX +"  Y:" + screenY + "   zoom:" + camera.zoom;
		unprojectedCoord = camera.unproject(pos);
		camcoord = "X:" + unprojectedCoord.x + "   Y:" + unprojectedCoord.y + "   Z:"+unprojectedCoord.z;
		Vector2 isovect = screen2isometricgrid(unprojectedCoord.x,unprojectedCoord.y);
		isocoord = "x:" + Math.floor(isovect.x) +"  y:" + Math.floor(isovect.y);

		Vector2 screenPos = isometricgrid2screen((int)Math.floor(isovect.x), (int)Math.floor(isovect.y), true);

		selectorSprite.setPosition(screenPos.x, screenPos.y);


		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		camera.zoom += amount*0.1f;
		return false;
	}

	public Vector2 screen2isometricgrid (float x, float y){
		Vector2 converted = new Vector2();
		converted.x = x/32+y/16;
		converted.y = x/32-y/16;
		return converted;
	}

	public Vector2 isometricgrid2screen (int x, int y, boolean offset){
		Vector2 screen = new Vector2();
		screen.x = x*16+y*16;
		if(offset) {
			screen.y = x * 8 - y * 8 - 8;
		}
		else {
			screen.y = x * 8 - y * 8;
		}
		return screen;
	}

	public Vector2 isometricgrid2screen (Vector2 posInGrid, boolean offset){
		Vector2 screen = new Vector2();
		screen.x = posInGrid.x*16+posInGrid.y*16;
		if(offset) {
			screen.y = posInGrid.x * 8 - posInGrid.y * 8 - 8;
		}
		else {
			screen.y = posInGrid.x * 8 - posInGrid.y * 8;
		}
		return screen;
	}


}
