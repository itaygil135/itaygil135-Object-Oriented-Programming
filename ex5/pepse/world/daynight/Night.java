package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class Night
{
    private static final String NIGHT_TAG = "night";

    private static final Float MIDNIGHT_OPACITY = 0.5F;

    /**
     * creates the night game object the fades the screen to black in a circular motion.
     * @param gameObjects  collection of game objects that the game manager holds
     * @param layer layer night will be added to
     * @param windowDimensions dimensions of main window
     * @param cycleLength   time between picks of fade
     * @return  game object "Night"
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer,
                                    Vector2 windowDimensions, float cycleLength) {

        GameObject night = new GameObject(Vector2.ZERO, windowDimensions, new RectangleRenderable(Color.black));
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        gameObjects.addGameObject(night);
        new Transition<Float>(
                night, // the game object being changed
                night.renderer()::setOpaqueness, // the method to call
                MIDNIGHT_OPACITY, // initial transition value
                (float) 0, // final transition value
                Transition.CUBIC_INTERPOLATOR_FLOAT, // use a cubic interpolator
                cycleLength / 2, // transtion fully over half a day
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, // Choose appropriate ENUM value
                null); // nothing further to execute upon reaching final value
        return night;
    }
}
