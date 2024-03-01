package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class Sun {
    private static final float SUN_SIZE_TO_WINDOW_DIMS = 1/10F;
    private static final String SUN_TAG = "sun";
    private static final float Y_ADD_RATIO =4 / 10F;
    private static final float X_ADD_RATIO =5 / 10F;

    /**
     * creates the sun game object the spins in circle fashion around the game's avatar.
     * @param gameObjects  collection of game objects that the game manager holds
     * @param layer layer sun will be added to
     * @param windowDimensions dimensions of main window
     * @param cycleLength   time of a full cycle
     * @return  game object "Sun"
     */
    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            Vector2 windowDimensions,
            float cycleLength){
        Vector2 sunDims = new Vector2(windowDimensions.x() * SUN_SIZE_TO_WINDOW_DIMS,
                                         windowDimensions.x() * SUN_SIZE_TO_WINDOW_DIMS);
        GameObject sun = new GameObject(Vector2.ZERO, sunDims, new OvalRenderable(Color.YELLOW));
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(SUN_TAG);
        gameObjects.addGameObject(sun, layer);
        Vector2 screenCenter = new Vector2(windowDimensions.x()/2, windowDimensions.y()/2);
        new Transition<Float>(
                sun, // the game object being changed
                angel -> sun.setCenter(screenCenter.add(new Vector2(
                          (float) Math.sin(angel) * windowDimensions.x()* X_ADD_RATIO,
                                  (float) Math.cos(angel) *windowDimensions.y() * Y_ADD_RATIO))), // the method to call
                0F, // initial transition value
                (float) Math.PI * 2 , // final transition value
                Transition.LINEAR_INTERPOLATOR_FLOAT, // use a cubic interpolator
                cycleLength, // transtion fully over half a day
                Transition.TransitionType.TRANSITION_LOOP, // Choose appropriate ENUM value
                null); // nothing further to execute upon reaching final value
        return sun;
    }
}
