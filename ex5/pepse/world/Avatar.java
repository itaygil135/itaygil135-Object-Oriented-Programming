package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Counter;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

public class Avatar extends GameObject {
    public static final Vector2 AVATAR_DIMS = new Vector2(60,140);
    private static final double TIME_BETWEEN_CLIPS = 0.1;
    private static final String[] WALK_IMAGE_PATH =  {"assets/walk1.png", "assets/walk2.png"};
    private static final String[] JUMP_IMAGE_PATH = { "assets/jump.png"};
    private static final String[] FLY_IMAGE_PATH =  {"assets/fly.png"};
    private static final String AVATAR_STANDING = "assets/static.png";
    private static final float PULL_DOWN_FORCE = 500;
    private final UserInputListener inputListener;
    public static final int MOVE_SPEED = 300;

    private float CHANGE_ENERGY = 0.5f;
    private final Renderable standingAvatarImage;
    private final AnimationRenderable avatarWalk;
    private final AnimationRenderable avatarJump;
    private final AnimationRenderable avatarFly;

    private float TOP_ENERGY = 100;
    private float energy;

    private static final float ENERGY_LOCATION = 15;

    private static final float ENERGY_COUNTER_SIZE = 50;

    private int minus = 0;

    private int plus = 0;
    private Counter energyCounter;
    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable, UserInputListener inputListener
    , ImageReader imageReader) {
        super(topLeftCorner, dimensions, renderable);
        this.inputListener = inputListener;
        this.standingAvatarImage = renderable;
        this.avatarWalk = new AnimationRenderable(WALK_IMAGE_PATH, imageReader,
                true,TIME_BETWEEN_CLIPS);
        this.avatarJump = new AnimationRenderable(JUMP_IMAGE_PATH, imageReader,
                true,TIME_BETWEEN_CLIPS);
        this.avatarFly = new AnimationRenderable(FLY_IMAGE_PATH, imageReader,
                true, TIME_BETWEEN_CLIPS);
        energyCounter = new Counter((int)TOP_ENERGY);
        energy = TOP_ENERGY;


    }

    /**
     * creates Avatar instance
     * @param gameObjects   collection of game objects that the game manager holds
     * @param  layer avatar will be added to
     * @param topLeftCorner of avatar
     * @param inputListener of avatar
     * @param imageReader of avatar
     * @return Avatar instance
     */
    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader){
        Renderable standingAvatarImage = imageReader.readImage(AVATAR_STANDING, true);
        Avatar avatar = new Avatar(topLeftCorner, AVATAR_DIMS, standingAvatarImage, inputListener, imageReader);
        gameObjects.addGameObject(avatar, layer);
        avatar.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        avatar.transform().setAccelerationY(PULL_DOWN_FORCE);
        gameObjects.addGameObject(new energyCounterDisplay(avatar.getEnergyCounter(),
                new Vector2(ENERGY_LOCATION, ENERGY_LOCATION),
                new Vector2(ENERGY_COUNTER_SIZE, ENERGY_COUNTER_SIZE),
                gameObjects), Layer.FOREGROUND);
        return avatar;
    }

    /**
     * stops from avatar to go threw the ground or trees
     * @param other game obj
     * @param collision collision strategy
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

        if (other.getTag().equals(Terrain.GROUND_TAG))
        {
            this.transform().setVelocityY(0);

        }
        if (other.getTag().equals(Terrain.UDER_GROUND_TAG))
        {
            this.transform().setVelocityX(0);

        }

    }

    /**
     * updates avatar motion, image based on user's input
     * @param deltaTime time of game manager
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        this.renderer().setRenderable(standingAvatarImage);
        int xVel = 0;
        boolean isFly = false;
        // moving left or right
        xVel = movingRight(xVel);
        xVel = movingLeft(xVel);
        this.transform().setVelocityX(xVel);
        //flying
        if(inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                inputListener.isKeyPressed(KeyEvent.VK_SHIFT) && this.energy > 0){
            this.renderer().setRenderable(avatarFly);
            this.transform().setVelocityY(-1 * MOVE_SPEED);
            isFly = true;
        }
        //jumping
        if(inputListener.isKeyPressed(KeyEvent.VK_SPACE) && getVelocity().y() == 0){
            this.renderer().setRenderable(avatarJump);
            this.transform().setVelocityY(-1 * MOVE_SPEED);
        }
        if (isFly){
            this.energy = this.energy - CHANGE_ENERGY;
        }
        //static motion
        if(getVelocity().x() == 0f && getVelocity().y() == 0f)
        {
            if (this.energy != TOP_ENERGY)
                this.energy = this.energy + CHANGE_ENERGY;
        }
        float diff = getEnergyCounter().value() - energy;
        if(diff == 1f) {
            getEnergyCounter().decrement();
        } else if (diff == -1f) {
            getEnergyCounter().increment();
        }
    }

    /**
     * check for user input to move left, if so moves avatar left
     * @param xVel horizontal velocity of avatar
     * @return new horizontal velocity
     */
    private int movingLeft(int xVel) {
        if(inputListener.isKeyPressed(KeyEvent.VK_LEFT)){
            xVel -= MOVE_SPEED;
            this.renderer().setIsFlippedHorizontally(true);
            this.renderer().setRenderable(avatarWalk);
        }
        return xVel;
    }
    /**
     * check for user input to move right, if so moves avatar right
     * @param xVel horizontal velocity of avatar
     * @return new horizontal velocity
     */
    private int movingRight(int xVel) {
        if(inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) {
            xVel += MOVE_SPEED;
            this.renderer().setIsFlippedHorizontally(false);
            this.renderer().setRenderable(avatarWalk);
        }
        return xVel;
    }

    /**
     * @return energy counter value
     */
    public Counter getEnergyCounter()
    {
        return energyCounter;
    }

}
