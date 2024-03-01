package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

public class SunHalo {
    private static final String SUN_HALO_TAG = "sunHalo";
    private static final float HALO_TO_SUN_RATIO = 3;

    /**
     * creates the sunHalo game object that accompanies the Sun gameObject.
     * @param gameObjects collection of game objects that the game manager holds
     * @param layer layer sunHalo will be added to
     * @param sun   the "Sun" that "SunHalo" will accompany
     * @param color the color of the sunHalo
     * @return "SunHalo" game object
     */
    public static GameObject create(
            GameObjectCollection gameObjects,
            int layer,
            GameObject sun,
            Color color){
        GameObject sunHalo = new GameObject(sun.getCenter(),sun.getDimensions().mult(HALO_TO_SUN_RATIO),
                new OvalRenderable(color));
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setTag(SUN_HALO_TAG);
        gameObjects.addGameObject(sunHalo, Layer.BACKGROUND + 1);
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));
        return sunHalo;
    }
}
