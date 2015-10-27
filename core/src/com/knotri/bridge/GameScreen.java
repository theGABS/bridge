package com.knotri.bridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;




/**
 * Created by k on 20.10.15.
 */


public class GameScreen extends AbstractScreen {


    class GameStateManage{
        public GameState state = null;
        private GameState nextState = null;
        public StartGame startGame = new StartGame();
        public GameOver gameOver = new GameOver();
        public PlayGame playGame = new PlayGame();

        public void changeState(GameState state){
            SLogger.log(state.getClass().getName());
            nextState = state;
        }

        public void update(float delta){
            if(state != null){
                state.update(delta);
            }
        }

        public void applyChangeState(){
            if(nextState != null) {
                if(state != null) {
                    state.finishState(nextState);
                    SLogger.log(state.getClass().getName() + " -> " + nextState.getClass().getName());

                }
                state = nextState;
                state.activeState();
                nextState = null;

            }
        }

        abstract class GameState{
            public void finishState(GameState state){}
            public void activeState(){}
            public void update(float delta){};
        }

        class StartGame extends GameState{
            @Override
            public void finishState(GameState state) {
                // Когда мы начали играть, мы должны убрать темную рамку
                blackImage.addAction(Actions.alpha(0,1));
            }

            @Override
            public void update(float delta){
                if(Gdx.input.justTouched()){
                    changeState(playGame);
                }
            }
        }

        class PlayGame extends GameState{
            @Override
            public void finishState(GameState state) {
                // Когда мы начали играть, мы должны убрать темную рамку
                //blackImage.addAction(Actions.alpha(0,1));
            }

            @Override
            public void activeState(){
                score = 0;

                labelScore.setVisible(true);
                labelScore.addAction(Actions.alpha(1f, 2));

                pause = false;

                groupGameOver.setTouchable(Touchable.disabled);
                enemies.clear();

                findBridgeByX(player.x).broken = false;
                findBridgeByX(player.x).timeToBoom = 1000;
                findBridgeByX(player.x).haveBomb = false;
            }
        }

        class GameOver extends GameState{
            @Override
            public void activeState(){
                if(prefs.getInteger("highScore") < score){
                    prefs.putInteger("highScore" , score);
                    prefs.flush();
                }
                groupGameOver.setX(0);
                groupGameOver.setVisible(true);
                groupGameOver.setTouchable(Touchable.enabled);
                pause = true;


                groupGameOver.addAction(Actions.moveTo(0, -stage.getHeight()));
                groupGameOver.addAction(Actions.moveTo(0, 0, 1f, Interpolation.swing));
            }

            @Override
            public void finishState(GameState state) {
                groupGameOver.addAction(Actions.moveTo(0, stage.getHeight(), 1f, Interpolation.swing));
            }
        }
    }

    GameStateManage gameStateManage = new GameStateManage();




    //GUI
    Label labelScore;
    Group groupGameOver = new Group();
    Group groupStart = new Group();

    Image blackImage;




    BitmapFont font = fontGeneration(15);
    MyGame game; // Note it's "MyGame" not "Game"
    SpriteBatch batch = new SpriteBatch();
    OrthographicCamera camera = new OrthographicCamera(1280, 720);
    Player player = new Player();
    Texture background, background2, playerTexture, colomnTexture, archTexture, archBrokenTexture, dynamiteTexture, zombieTexture, cloudTexture;

    Array<Bridge> bridges = new Array<Bridge>();
    Array<Enemy> enemies = new Array<Enemy>();
    Array<Cloud> clouds = new Array<Cloud>();

    float bridgesX = 100;
    int score = 0;
    boolean pause = true;


    //PARTICLE

//    ParticleEffectPool bombEffectPool;
//    Array<ParticleEffectPool.PooledEffect> effects = new Array();
//    ParticleEffect bombEffect = new ParticleEffect();

    // END PARTICLE


    // ANIMATION
    TextureAtlas textureAtlas;
    Animation animation;
    private float elapsedTime = 0;








    // constructor to keep a reference to the main Game class
    public GameScreen(MyGame game){

        gameStateManage.changeState(gameStateManage.startGame);
        gameStateManage.applyChangeState();
        textureAtlas = new TextureAtlas(Gdx.files.internal("export-packed/pack.atlas"));
        animation = new Animation(1/60f, textureAtlas.getRegions());

        float margin = stage.getWidth()*0.02f;

        TextButton buttonPlay = new TextButton("play again", skin);
        buttonPlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                newGame();
                pause = false;
            }
        });

        blackImage = new Image(new Texture("black.png"));
        blackImage.setSize(stage.getWidth(), stage.getHeight());
        blackImage.setColor(0,0,0, 0.6f);
        groupStart.addActor(blackImage);


        labelScore = new Label("score : 500", skin);
        labelScore.setY(stage.getHeight() - labelScore.getHeight());
        labelScore.setX(margin);
        labelScore.addAction(Actions.alpha(0));
        stage.addActor(labelScore);

        Label labelStart = new Label("Welcome to bridger", skin);
        labelStart.setX(margin);
        labelStart.setY(stage.getHeight() - labelStart.getHeight());

        Label labelRecord = new Label("Record : " + prefs.getInteger("highScore",0), skin);
        labelRecord.setX(margin);
        labelRecord.setY(stage.getHeight() - labelStart.getHeight() * 2);

        Label labelHowPlay = new Label("Tap to start, tap to mine ", skin);
        labelHowPlay.setX(margin);
        labelHowPlay.setY(stage.getHeight() - labelStart.getHeight() * 3);


        groupStart.addActor(labelRecord);
        groupStart.addActor(labelStart);
        groupStart.addActor(labelHowPlay);


        Label labelGameOVer = new Label("Game Over", skin);
        labelGameOVer.setY(stage.getHeight() - labelScore.getHeight() * 2);
        labelGameOVer.setX(margin);

        groupGameOver = new Group();
        groupGameOver.addActor(buttonPlay);
        groupGameOver.addActor(labelGameOVer);
        groupGameOver.setVisible(false);
        stage.addActor(groupGameOver);
        stage.addActor(groupStart);


        this.game = game;
        background = new Texture("background2.png");
        background2 = new Texture("background3.png");
        playerTexture = new Texture("player.png");
        colomnTexture = new Texture("column.png");
        archTexture = new Texture("arch.png");
        dynamiteTexture = new Texture("dynamite.png");
        zombieTexture = new Texture("zombi.png");
        cloudTexture = new Texture("cloud.png");
        archBrokenTexture = new Texture("archBroken.png");


        ParticleEffect tmp = new ParticleEffect();
        tmp.load(Gdx.files.internal("rock2.p"), Gdx.files.internal(""));
        ParticleManage.addParticle("rockBoom", tmp);





        //bombEffectPool = new ParticleEffectPool(bombEffect, 1, 1);


        bridgesX = -400;
        for(int i = 0; i < 10; i++){
            Bridge bridge = new Bridge();
            bridge.width = (float) (300 + Math.random()*300);
            bridge.x = bridgesX;
            bridgesX += bridge.width;
            bridges.add(bridge);
        }

        for(int i = 0; i < 30; i++){
            Cloud cloud = new Cloud();
            cloud.x = (float) (Math.random()*10000);
            cloud.y = (float) (400 + Math.random()*200);
            //bridgesX += bridge.width;
            clouds.add(cloud);
        }

        //newGame();
    }

    public void newGame(){
        //bridges.clear();
//        enemies.clear();
//        //clouds.clear();
////        bridgesX = -400;
////        for(int i = 0; i < 10; i++){
////            Bridge bridge = new Bridge();
////            bridge.width = (float) (300 + Math.random()*300);
////            bridge.x = bridgesX;
////            bridgesX += bridge.width;
////            bridges.add(bridge);
////        }
//
//
//        findBridgeByX(player.x).broken = false;
//        findBridgeByX(player.x).haveBomb = false;
//
////        for(int i = 0; i < 30; i++){
////            Cloud cloud = new Cloud();
////            cloud.x = (float) (Math.random()*10000);
////            cloud.y = (float) (400 + Math.random()*200);
////            //bridgesX += bridge.width;
////            clouds.add(cloud);
////        }
//
////        for(int i = 0; i < 10; i++){
////            Enemy enemy = new Enemy();
////            enemy.x = 100;
////            enemies.add(enemy);
////        }
//
//        //player.x = 0;
//        score = 0;
//
//        labelScore.setVisible(true);
//        labelScore.addAction(Actions.alpha(0.1f, 2));
//
//        groupGameOver.setTouchable(Touchable.disabled);

        gameStateManage.changeState(gameStateManage.playGame);

        //groupGameOver.setVisible(false);
        //pause = true;
    }


    public Bridge findBridgeByX(float x){
        for (int j = 0; j < bridges.size - 1; j++) {
            if (bridges.get(j).x < x && bridges.get(j).x + bridges.get(j).width > x) {
                return bridges.get(j);
            }
        }
        return null;
    }

    @Override
    public void render(float delta) {

        if(delta > 0.1) { delta = 0.1f; }
        gameStateManage.applyChangeState();
//        // update and draw stuff
//        if (Gdx.input.justTouched()) // use your own criterion here
//            game.setScreen(game.anotherScreen);



//        if(Gdx.input.justTouched()){
//            pause = false;
//            labelScore.addAction(Actions.alpha(1f, 2));
//        }

        gameStateManage.update(delta);

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cameraUpdate();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float viewportWidth = background.getWidth();
        float leftEdge = player.x - viewportWidth / 2;
        batch.draw(background, (float) (Math.floor(leftEdge / viewportWidth) * viewportWidth), 0);
        batch.draw(background, (float) (Math.floor(leftEdge / viewportWidth) * viewportWidth + background.getWidth()), 0);

        batch.setColor(1, 1, 1, Math.max(0, 1 - score * 0.0001f));
        batch.draw(background2, (float) (Math.floor(leftEdge / viewportWidth) * viewportWidth), 0);
        batch.draw(background2, (float) (Math.floor(leftEdge / viewportWidth) * viewportWidth + background2.getWidth()), 0);
        batch.setColor(1, 1, 1, 1);




        // DRAW
        for(Cloud cloud : clouds){
            //cloud.x + player.x - player.x/10 = cloud.x + player.x (1 - 1/10);
            batch.draw(cloudTexture, cloud.x + player.x*0.5f, cloud.y);
        }

        //batch.draw(playerTexture, player.x, player.y);
        elapsedTime += delta;
        batch.draw(animation.getKeyFrame(elapsedTime, true), player.x, player.y);


        for(Bridge bridge : bridges){
            batch.draw(colomnTexture, bridge.x, 135);
        }




        for (int i = 0; i < bridges.size - 1; i++) {
            batch.setColor(1, 1, 1, (float) (0.3f + 0.7f / Math.pow(358 - bridges.get(i).y, 0.2f)));

            if(bridges.get(i).y < 357){
                batch.draw(archBrokenTexture, bridges.get(i).x + 91, bridges.get(i).y, bridges.get(i).width - 91, 96);
            }else {
                batch.draw(archTexture, bridges.get(i).x + 91, bridges.get(i).y, bridges.get(i).width - 91, 96);
            }
            if (bridges.get(i).haveBomb) {
                batch.draw(dynamiteTexture, bridges.get(i).x + 45 + bridges.get(i).width / 2, bridges.get(i).y + 20);
            }

            batch.setColor(1, 1, 1, 1);

//            if (bridges.get(i).timeToBoom < 100 && bridges.get(i).timeToBoom > 50) {
//                ParticleEffectPool.PooledEffect effect = bombEffectPool.obtain();
//                effect.setPosition(bridges.get(i).x + 45 + bridges.get(i).width / 2, bridges.get(i).y + 20);
//                effects.add(effect);
//            }
        }



        for(Enemy enemy : enemies){
            batch.draw(zombieTexture, enemy.x, enemy.y);
        }


        // Update and draw effects:
//        for (int i = effects.size - 1; i >= 0; i--) {
//            ParticleEffectPool.PooledEffect effect = effects.get(i);
//            effect.draw(batch, delta);
//            if (effect.isComplete()) {
//                effect.free();
//                effects.removeIndex(i);
//            }
//        }

        ParticleManage.draw(batch, delta);



        batch.end();

        // DRAW END

        if(!pause) {

            //groupGameOver.setX(-player.x * Gdx.graphics.getWidth() / camera.viewportWidth );
            groupStart.setX(-player.x * Gdx.graphics.getWidth() / camera.viewportWidth );

            Gdx.app.log("what", "player " + player.x);



            for (int i = enemies.size - 1; i >= 0; i--) {

                enemies.get(i).update(delta);
                //enemies.get(i).x += 4 + Math.random()*2.1f;

                for (int j = 0; j < bridges.size - 1; j++) {
                    if (bridges.get(j).x < enemies.get(i).x && bridges.get(j).x + bridges.get(j).width > enemies.get(i).x) {
                        if (bridges.get(j).broken) {
                            if (!enemies.get(i).down) {
                                enemies.get(i).down = true;
                                score += 10;

                                for(Enemy enemy : enemies){
                                    enemy.x -= 10;
                                }
                            }
                        }
                        float R = 50;
                        float x = R * 2 * (0.5f - (enemies.get(i).x - bridges.get(j).x) / bridges.get(j).width);
                        if (!enemies.get(i).down) enemies.get(i).y = 405 + R - 1 / R * x * x;
                        break;
                    }
                }

                if(enemies.get(i).y < 100){
                    enemies.removeIndex(i);
                    continue;
                }

                //batch.draw(zombieTexture, enemies.get(i).x, enemies.get(i).y);

            }

            for (int i = 0; i < bridges.size; i++) {
                //batch.draw(colomnTexture, bridges.get(i).x, 135);
                bridges.get(i).update(16);
            }







            player.update(delta);


//            for (int i = 0; i < bridges.size; i++) {
//                if (bridges.get(i).x < player.x && bridges.get(i).x + bridges.get(i).width > player.x) {
//
//
//                    if (bridges.size - i < 8) {
//                        for (int k = 0; k < 10; k++) {
//                            Bridge bridge = new Bridge();
//                            bridge.width = (float) (300 + Math.random() * 300);
//                            bridge.x = bridgesX;
//                            bridgesX += bridge.width;
//                            bridges.add(bridge);
//                        }
//                    }
//                    if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
//                        bridges.get(i).mining();
//                    }
//
//
//                    float R = 50;
//                    float x = R * 2 * (0.5f - (player.x - bridges.get(i).x) / bridges.get(i).width);
//                    player.y = 405 + R - 1 / R * x * x;
//
//                    if (bridges.get(i).broken) {
//                        gameover();
//                    }
//                }
//            }

            if (bridges.get(bridges.size-1).x - 3000  < player.x ) {
                        for (int k = 0; k < 10; k++) {
                            Bridge bridge = new Bridge();
                            bridge.width = (float) (300 + Math.random() * 300);
                            bridge.x = bridgesX;
                            bridgesX += bridge.width;
                            bridges.add(bridge);
                        }
                    }

            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.justTouched()) {
                findBridgeByX(player.x).mining();
            }

            float R = 50;
            float x = R * 2 * (0.5f - (player.x - findBridgeByX(player.x).x) / findBridgeByX(player.x).width);
            player.y = 405 + R - 1 / R * x * x;

            if (findBridgeByX(player.x).broken) {
                gameover();
            }



            for(Enemy enemy : enemies){
                if(enemy.x > player.x - 20){
                    gameover();
                    break;
                }
            }


            if (Math.random() < 0.1/(enemies.size+1)) {
                Enemy enemy = new Enemy();
                enemy.x = (float) (player.x - 55 - 130 * Math.pow(2.7f, -score/1000.0f ) - enemies.size*35);
                enemies.add(enemy);
            }

            score += 10*delta * 10;
        }

        labelScore.setText("score :" + score);
        stage.act(delta);
        stage.draw();


    }

    public void gameover(){
//        if(prefs.getInteger("highScore") < score){
//            prefs.putInteger("highScore" , score);
//            prefs.flush();
//        }
//        groupGameOver.setX(0);
//        groupGameOver.setVisible(true);
//        groupGameOver.setTouchable(Touchable.enabled);
//        pause = true;
//
//
//        groupGameOver.addAction(Actions.scaleTo(0.9f, 0.9f));
//        groupGameOver.addAction(Actions.scaleTo(1, 1, 2));

        gameStateManage.changeState(gameStateManage.gameOver);
        //newGame();
    }


    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = 720 * width/height;
        camera.viewportHeight = 720;
        //camera.position.set(player.x, 0, 0);
        camera.update();

        if(font != null){
            font.dispose();
        }
        font = fontGeneration((int) (camera.viewportHeight/15));
    }

    public void cameraUpdate(){
        camera.position.x = player.x;
        camera.position.y = camera.viewportHeight/2;
        camera.update();
    }




    @Override
    public void dispose() {
        // never called automatically

//        // Reset all effects:
//        for (int i = effects.size - 1; i >= 0; i--)
//            effects.get(i).free();
//        effects.clear();
    }
}