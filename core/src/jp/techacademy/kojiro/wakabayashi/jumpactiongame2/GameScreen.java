package jp.techacademy.kojiro.wakabayashi.jumpactiongame2;

/**
 * Created by wkojiro on 2017/02/22.
 */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter {
//1/60秒ごとに自動的に呼び出されます。

    //物理的なディスプレイのサイズに依存しないサイズ
    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;

    static final float WORLD_WIDTH = 10;
    static final float WORLD_HEIGHT = 15 * 20; // 20画面分登れば終了

    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_READY = 0;
    static final int GAME_STATE_PLAYING = 1;
    static final int GAME_STATE_GAMEOVER = 2;

    // 重力
    static final float GRAVITY = -12;

    private JumpActionGame2 mGame;

    Sprite mBg;

    //カメラを表すOrthographicCameraクラスと、ビューポートのFitViewportクラスをメンバ変数として定義します。
    OrthographicCamera mCamera;
    OrthographicCamera mGuiCamera; // ←追加する

    FitViewport mViewPort;
    FitViewport mGuiViewPort; // ←追加する

    Random mRandom;
    List<Step> mSteps;
    List<Star> mStars;
    List<Enemy> mEnemys;
    Ufo mUfo;
    Player mPlayer;


    float mHeightSoFar; // ←追加する
    int mGameState;
    Vector3 mTouchPoint; // ←追加する

    BitmapFont mFont; // ←追加する
    int mScore; // ←追加する
    int mHighScore; // ←追加する

    Preferences mPrefs; // ←追加する

    BitmapFont mFont2;
    Sound sound1 = Gdx.audio.newSound(Gdx.files.internal("coin.mp3"));
    Sound sound2 = Gdx.audio.newSound(Gdx.files.internal("powerdown.mp3"));

    public GameScreen(JumpActionGame2 game) {
        mGame = game;

        // 背景の準備
        Texture bgTexture = new Texture("back.png");
        // TextureRegionで切り出す時の原点は左上
        mBg = new Sprite( new TextureRegion(bgTexture, 0, 0, 540, 810));
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT);
        mBg.setPosition(0, 0);

        // カメラ、ViewPortを生成、設定する

        //コンストラクタでこれらメンバ変数に初期化して代入します。ポイントはカメラのサイズとビューポートのサイズをどちらもCAMERA_WIDTHとCAMERA_HEIGHTを使って同じにするということです。
        //どちらも同じにしているため縦横比が固定されます。
        // 縦横比が固定されるので物理ディスプレイの比率と異なる場合は上下または左右に隙間ができるということです。
        // 実行する際に複数の端末で試すか、様々な解像度のエミュレータを作成して試すとよく分かることでしょう。
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        // GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera(); // ←追加する
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT); // ←追加する
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera); // ←追加する


        // メンバ変数の初期化
        mRandom = new Random();
        mSteps = new ArrayList<Step>();
        mStars = new ArrayList<Star>();
        mEnemys = new ArrayList<Enemy>();
        mGameState = GAME_STATE_READY;
        mTouchPoint = new Vector3(); // ←追加する
        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false); // ←追加する
        mFont.getData().setScale(0.8f);// ←追加する

        mFont2 = new BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false); // ←追加する
       // mFont2.getData().setScale(0.8f);// ←追加する


        mScore = 0;// ←追加する
        mHighScore = 0;// ←追加する

        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.kojiro.wakabayashi.jumpactiongame2"); // ←追加する
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0); // ←追加する

        //Sound sound1 = Gdx.audio.newSound(Gdx.files.internal("coin.mp3"));

        createStage();
    }

    @Override
    public void render (float delta) {

        // 状態を更新する
        update(delta);



        //Gdx.gl.glClearColor(0, 0, 0, 1);と Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);で画面を描画する準備を行います。
        // glClearColorメソッドは画面をクリアする時の色を赤、緑、青、透過で指定します。
        // Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);で実際にその色でクリア（塗りつぶし）を行います。
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

// カメラの中心を超えたらカメラを上に移動させる つまりキャラが画面の上半分には絶対に行かない
        if (mPlayer.getY() > mCamera.position.y) { // ←追加する
            mCamera.position.y = mPlayer.getY(); // ←追加する
        } // ←追加する

        /*
        描画を行うrenderメソッドではまずカメラのupdateメソッドを呼び出し
        、SpriteBatchクラスのsetProjectionMatrixメソッドをOrthographicCameraクラスのcombinedプロパティを引数に与えて呼び出します。
        これはカメラの座標をアップデート（計算）し、スプライトの表示に反映させるために必要な呼び出しです。これらの呼び出しによって物理ディスプレイに依存しない表示を行うことができます。
        カメラのupdateメソッドでは行列演算を行ってカメラの座標値の再計算を行ってくれるメソッドです。
        そしてsetProjectionMatrixメソッドとcombinedメソッドでその座標をスプライトに反映しています。
        もしこれらの処理をゲームライブラリを使わずに自分で行おうとすると数学に関する高度な知識が要求されます。
        ライブラリを使うことは中身の計算がどのようなことが行われているかまでを深く知ることなくゲームを開発することができることが利点とも言えます。
         */


        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        //もう１つ大事なルールはスプライトなどを描画する際はSpriteBatchクラスのbeginメソッドとendメソッドの間で行うというルールです。
        // ここではJumpActionGameクラスのメンバ変数で保持しているSpriteBatchクラスのオブジェクトを使います。
        // そしてSpriteクラスのdrawメソッドを呼び出すことで描画します。
        mGame.batch.begin();

        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2);
        mBg.draw(mGame.batch);

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).draw(mGame.batch);
        }

        // Star
        for (int i = 0; i < mStars.size(); i++) {
            mStars.get(i).draw(mGame.batch);
        }

        // Enemy
        for (int i = 0; i < mEnemys.size(); i++) {
            mEnemys.get(i).draw(mGame.batch);
        }

        // UFO
        mUfo.draw(mGame.batch);

        //Player
        mPlayer.draw(mGame.batch);

        mGame.batch.end();

        // スコア表示
        mGuiCamera.update(); // ←追加する
        mGame.batch.setProjectionMatrix(mGuiCamera.combined); // ←追加する
        mGame.batch.begin(); // ←追加する
        mFont.draw(mGame.batch, "HighScore: " + mHighScore, 16, GUI_HEIGHT - 15); // ←追加する
        mFont.draw(mGame.batch, "Score: " + mScore, 16, GUI_HEIGHT - 35); // ←追加する
        mGame.batch.end(); // ←追加する

    }

    /*
    最後にresizeメソッドをオーバーライドしてFitViewportクラスのupdateメソッドを呼び出します。
    resizeメソッドは物理的な画面のサイズが変更されたときに呼び出されるメソッドです。
    Androidではcreate直後やバックグランドから復帰したときに呼び出されます。
     */
    @Override
    public void resize(int width, int height) {
        mViewPort.update(width, height);
        mGuiViewPort.update(width, height);
    }

    // ステージを作成する
    private void createStage() {

        // テクスチャの準備
        Texture stepTexture = new Texture("step.png");
        Texture starTexture = new Texture("star.png");
        Texture enemyTexture = new Texture("enemy.png");
        Texture playerTexture = new Texture("uma.png");
        Texture ufoTexture = new Texture("ufo.png");

        // StepとStarをゴールの高さまで配置していく
        float y = 0;

        float maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY);
        while (y < WORLD_HEIGHT - 5) {
            int type = mRandom.nextFloat() > 0.8f ? Step.STEP_TYPE_MOVING : Step.STEP_TYPE_STATIC;
            float x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH);

            Step step = new Step(type, stepTexture, 0, 0, 144, 36);
            step.setPosition(x, y);
            mSteps.add(step);


            if (mRandom.nextFloat() > 0.8f) {
                Enemy enemy = new Enemy(enemyTexture, 0, 0, 144, 144);
                enemy.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Enemy.ENEMY_HEIGHT + mRandom.nextFloat() * 2);
                mEnemys.add(enemy);
            }

            if (mRandom.nextFloat() > 0.6f) {
                Star star = new Star(starTexture, 0, 0, 72, 72);
               // Enemy enemy = new Enemy(enemyTexture, 0, 0, 144, 144);
                star.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3);
               // enemy.setPosition(step.getX() + mRandom.nextFloat(), step.getY() + Enemy.ENEMY_HEIGHT + mRandom.nextFloat() * 2);
                mStars.add(star);
               // mEnemys.add(enemy);
            }

            y += (maxJumpHeight - 0.5f);
            y -= mRandom.nextFloat() * (maxJumpHeight / 3);
        }

        // Playerを配置
        mPlayer = new Player(playerTexture, 0, 0, 72, 72);
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2, Step.STEP_HEIGHT);

        // ゴールのUFOを配置
        mUfo = new Ufo(ufoTexture, 0, 0, 120, 74);
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y);
    }

    // それぞれのオブジェクトの状態をアップデートする
    private void update(float delta) {
        switch (mGameState) {
            case GAME_STATE_READY:
                updateReady();
                break;
            case GAME_STATE_PLAYING:
                updatePlaying(delta);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
        }
    }


    private void updateReady() {
        if (Gdx.input.justTouched()) {
            mGameState = GAME_STATE_PLAYING;
        }
    }

    private void updatePlaying(float delta) {
        float accel = 0;
        if (Gdx.input.isTouched()) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0)); // ←追加する
            Rectangle left = new Rectangle(0, 0, GUI_WIDTH / 2, GUI_HEIGHT); // ←修正する
            Rectangle right = new Rectangle(GUI_WIDTH / 2, 0, GUI_WIDTH / 2, GUI_HEIGHT); // ←修正する
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f;
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f;
            }
        }

        // Step
        for (int i = 0; i < mSteps.size(); i++) {
            mSteps.get(i).update(delta);
        }

        // Player
        if (mPlayer.getY() <= Player.PLAYER_HEIGHT / 2) {
            mPlayer.hitStep();
        }
        mPlayer.update(delta, accel);
        mHeightSoFar = Math.max(mPlayer.getY(), mHeightSoFar);

        // 当たり判定を行う
        checkCollision(); // ←追加する

        // ゲームオーバーか判断する
        checkGameOver();
    }

    private void updateGameOver() {
        
        if (Gdx.input.justTouched()) {
            mGame.setScreen(new ResultScreen(mGame, mScore));

        }

    }
    private void checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.getY()) {
            Gdx.app.log("JampActionGame2", "GAMEOVER");
            mGameState = GAME_STATE_GAMEOVER;
        }
    }


    private void checkCollision() {
        // UFO(ゴールとの当たり判定)
        if (mPlayer.getBoundingRectangle().overlaps(mUfo.getBoundingRectangle())) {
            Gdx.app.log("JampActionGame2", "CLEAR");
            mGameState = GAME_STATE_GAMEOVER;
            return;
        }

        // Starとの当たり判定
        for (int i = 0; i < mStars.size(); i++) {
            Star star = mStars.get(i);

            if (star.mState == Star.STAR_NONE) {
                continue;
            }

            if (mPlayer.getBoundingRectangle().overlaps(star.getBoundingRectangle())) {
                star.get();
                sound1.play();

                mScore++; // ←追加する
                if (mScore > mHighScore) { // ←追加する
                    mHighScore = mScore; // ←追加する
                    //ハイスコアをPreferenceに保存する
                    mPrefs.putInteger("HIGHSCORE", mHighScore); // ←追加する
                    mPrefs.flush(); // ←追加する

                } // ←追加する
                break;
            }
        }

        // Enemyとの当たり判定
        for (int i = 0; i < mEnemys.size(); i++) {
            Enemy enemy = mEnemys.get(i);

            if (enemy.mState == Enemy.ENEMY_NONE) {
                continue;
            }

            if (mPlayer.getBoundingRectangle().overlaps(enemy.getBoundingRectangle())) {
                Gdx.app.log("JampActionGame2", "GAMEOVER");
                mGameState = GAME_STATE_GAMEOVER;
                sound2.play();
                //文字を入れる


            }
        }






        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) {
            return;
        }

        for (int i = 0; i < mSteps.size(); i++) {
            Step step = mSteps.get(i);

            if (step.mState == Step.STEP_STATE_VANISH) {
                continue;
            }

            if (mPlayer.getY() > step.getY()) {
                if (mPlayer.getBoundingRectangle().overlaps(step.getBoundingRectangle())) {
                    mPlayer.hitStep();
                    if (mRandom.nextFloat() > 0.5f) {
                        step.vanish();
                    }
                    break;
                }
            }
        }
    }

}
