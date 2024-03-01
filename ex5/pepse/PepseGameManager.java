package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.Transition;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.Renderable;
import danogl.util.Counter;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Leaf;
import pepse.world.trees.Tree;


import java.awt.*;
import java.util.Random;

public class PepseGameManager extends GameManager {

    private static final float DAY_TIME_CYCLE = 30;

    private static final int SEED = 526665;
    private static final Color SUN_HALO_COLOR = new Color(255, 255, 0, 20);

    private static final int GROUND_TOP_LAYER = Layer.STATIC_OBJECTS;

    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + 8;
    private static final int SUN_LAYER = Layer.BACKGROUND + 1;

    private static final int NIGHT_LAYER = Layer.FOREGROUND;

    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int AVATAR_LAYER = Layer.DEFAULT;
    public static final int UNDER_GROUND_LAYER = Layer.STATIC_OBJECTS + 1;
    private static final int TRUNK_LAYER = Layer.BACKGROUND + 2;
    public static final int LEAF_LAYER = Layer.STATIC_OBJECTS + 2;
    private int rightBound;
    private int leftBound;

    private Vector2 windowsDimensions;
    private Avatar avatar;
    private Terrain terrain;
    private Tree trees;


    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader, UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.windowsDimensions = windowController.getWindowDimensions();
        int seed = new Random().nextInt(SEED);
        Vector2 initialAvatarLocation = new Vector2(windowController.getWindowDimensions().mult(0.5f));
        this.rightBound = (int) (initialAvatarLocation.x() + windowController.getWindowDimensions().x());
        this.leftBound = (int) (initialAvatarLocation.x() - windowController.getWindowDimensions().x());
        //create sky
        Sky.create(gameObjects(), windowController.getWindowDimensions(), SKY_LAYER);

        this.terrain = new Terrain(gameObjects(), GROUND_TOP_LAYER,
                windowController.getWindowDimensions(),seed);
        terrain.createInRange(leftBound, rightBound);
        //create night
        Night.create(gameObjects(), NIGHT_LAYER, windowController.getWindowDimensions(),
                DAY_TIME_CYCLE);
        //create sun
        GameObject sun = Sun.create(gameObjects(), SUN_LAYER, windowController.getWindowDimensions(), DAY_TIME_CYCLE);
        //create sun halo
        SunHalo.create(gameObjects(), SUN_HALO_LAYER, sun, SUN_HALO_COLOR);
        this.trees = new Tree(gameObjects(),GROUND_TOP_LAYER, windowController.getWindowDimensions(),
                seed, terrain::groundHeightAt);
        //creates trees
        trees.createInRange(0, (int) windowController.getWindowDimensions().x());
        gameObjects().layers().shouldLayersCollide(LEAF_LAYER, GROUND_TOP_LAYER , true);
        //create avatar
        this.avatar =Avatar.create(gameObjects(), AVATAR_LAYER, new Vector2(initialAvatarLocation.x(),Vector2.ZERO.y()), inputListener,
                 imageReader);
        this.setCamera(new Camera(avatar, windowController.getWindowDimensions().mult(0.5f).subtract(new Vector2(windowController.getWindowDimensions().mult(0.5f))) , windowController.getWindowDimensions(),
                windowController.getWindowDimensions()));
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TRUNK_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, GROUND_TOP_LAYER, true);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, UNDER_GROUND_LAYER, true);
    }
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        manageInfiniteWorld();
    }

    /**
     * creates infinite world
     */
    private void manageInfiniteWorld() {
        //when the avatar is close to the end of the world from the right side.
        if (Math.abs(this.avatar.getCenter().x()
                - rightBound) < 0.75 * this.windowsDimensions.x()) {
            this.terrain.createInRange(rightBound, (int) (rightBound + windowsDimensions.x() * 0.5));
            this.trees.createInRange(rightBound, (int) (rightBound + windowsDimensions.x() * 0.5));
            this.rightBound += this.windowsDimensions.x() * 0.5;
            deleteInRange(leftBound, (int) (leftBound + windowsDimensions.x() * 0.5));
            this.leftBound = (int) (leftBound + windowsDimensions.x() * 0.5);
        }
        //when the avatar is close to the rnd of the world from the left side.
        if (Math.abs(this.avatar.getCenter().x() - leftBound) < 0.75 * windowsDimensions.x()) {
            this.terrain.createInRange((int) (leftBound - windowsDimensions.x() * 0.5), leftBound);
            this.trees.createInRange((int) (leftBound - windowsDimensions.x() * 0.5), leftBound);
            deleteInRange((int) (rightBound - (windowsDimensions.x() * 0.5)), rightBound);
            this.rightBound -= this.windowsDimensions.x() * 0.5;
            this.leftBound = (int) (leftBound - (windowsDimensions.x() * 0.5));
        }
    }

    /**
     * deletes any game obj in given range
     * @param min x coordinate
     * @param max x coordinate
     */
    private void deleteInRange(int min, int max) {
        for (GameObject Object : gameObjects()) {
            if (Object.getCenter().x() < max &&
                    Object.getCenter().x() > min) {
                if (Object.getTag().equals(Terrain.GROUND_TAG))
                    gameObjects().removeGameObject(Object, GROUND_TOP_LAYER);
                else if (Object.getTag().equals(Terrain.UDER_GROUND_TAG)) {
                    gameObjects().removeGameObject(Object, UNDER_GROUND_LAYER);
                } else if (Object.getTag().equals(Tree.TRUNK_TAG)) {
                    gameObjects().removeGameObject(Object, TRUNK_LAYER);
                } else if (Object.getTag().equals(Leaf.LEAF_TAG)) {
                    gameObjects().removeGameObject(Object,LEAF_LAYER);
                }
            }
        }
    }

    public static void main(String[] args) {
        new PepseGameManager().run();
    }

}
