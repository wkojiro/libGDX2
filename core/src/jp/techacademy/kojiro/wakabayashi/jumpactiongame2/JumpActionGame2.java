package jp.techacademy.kojiro.wakabayashi.jumpactiongame2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class JumpActionGame2 extends Game {
	// publicにして外からアクセスできるようにする
	public SpriteBatch batch;
	public ActivityRequestHandler mRequestHandler;

	public JumpActionGame2(ActivityRequestHandler requestHandler) {
		super();
		mRequestHandler = requestHandler;
	}

	@Override
	public void create () {
		batch = new SpriteBatch();

		// GameScreenを表示する
		setScreen(new GameScreen(this));
	}
}
