package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.*;
import pepse.world.Block;

import java.awt.*;
import java.util.Objects;
import java.util.Random;

public class Leaf extends GameObject {
    private static final Color BASE_LEAF_COLOR = new Color(50, 200, 30);
    public static final String LEAF_TAG = "leaf";
    private static final float LEAF_TRANSITION_LENGTH =10;
    private static final int MAX_WAIT_TIME = 10;
    private static final int MAX_TIME_LIFE = 100;
    private static final int FADE_OUT_TIME = 20;
    private static final float FALL_VEL_Y = 40;
    private static final float FALL_VEL_X = 2;
    private static final int MAX_DEATH_TIME = 10;
    private static final float SHRINK_TO = 4;
    private static final float START_ANGLE = 7f;
    private static  Vector2 FINAL_SIZE ;
    private final Random rand;

    private final Vector2 beginTopLeftCorner;
    private final Vector2 center;
    private  ScheduledTask life;
    private float lifeCycleTime;
    private Transition<Float> moveAngle;
    private Transition<Float> changeTilled;
    private Transition<Vector2> cahngeSize;
    private Transition<Float> fadeOut;

    /**
     * Construct a new GameObject instance.
     *
     * @param topLeftCorner Position of the object, in window coordinates (pixels).
     *                      Note that (0,0) is the top-left corner of the window.
     */
    public Leaf(Vector2 topLeftCorner, Random rand) {
        super(topLeftCorner, Vector2.ONES.mult(Block.SIZE),
                new RectangleRenderable(ColorSupplier.approximateColor(BASE_LEAF_COLOR)));
        this.setTag(LEAF_TAG);
        this.beginTopLeftCorner = topLeftCorner;
        this.rand = rand;
        this.physics().setMass(0);
        this.center = this.getCenter();
        rand.nextInt(100);
        rand.nextInt(4);
        this.lifeCycleTime = rand.nextInt(MAX_TIME_LIFE);
        FINAL_SIZE = new Vector2(this.getDimensions().x() - SHRINK_TO, this.getDimensions().y() - SHRINK_TO);
        new ScheduledTask(this,rand.nextInt(MAX_WAIT_TIME) , true, this::transitions);
        new ScheduledTask(this, this.lifeCycleTime,
                false, this::startFallTransition);
    }

    /**
     * transition that starts leaf fall to ground
     */
    private void startFallTransition() {
        this.transform().setVelocity(FALL_VEL_X, FALL_VEL_Y);
        this.moveAngle = new Transition<Float>(this,
                this.renderer()::setRenderableAngle,
                -START_ANGLE,
                START_ANGLE,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                1,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
        this.renderer().fadeOut(FADE_OUT_TIME, () -> new ScheduledTask(this,
                rand.nextInt(MAX_DEATH_TIME),
                false, this::reBerthTransition));
    }

    /**
     * transition that teleports leaf back to its initial location on the tree
     */
    private void reBerthTransition(){
        this.renderer().setOpaqueness(1);
        this.transform().setVelocity(0, 0);
        this.setTopLeftCorner(beginTopLeftCorner);
        this.lifeCycleTime = rand.nextInt(MAX_TIME_LIFE);
        new ScheduledTask(this, this.lifeCycleTime,
                false, this::startFallTransition);
    }

    /**
     * creates transitions in charge of leaf tilled and size change
     */
    private void transitions(){
        this.changeTilled = new Transition<Float>(
                this,
                this.renderer()::setRenderableAngle,
                0F,
                (float) (Math.PI * 2),
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                LEAF_TRANSITION_LENGTH,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);
        this.cahngeSize = new Transition<Vector2>(
                this,
                this::setDimensions,
                FINAL_SIZE,
                this.getDimensions(),
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                LEAF_TRANSITION_LENGTH,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null);

    }

    /**
     * stops leaf from falling threw the ground
     * @param other
     * @param collision
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        this.transform().setVelocity(0, 0);
        this.removeComponent(moveAngle);
    }
}
