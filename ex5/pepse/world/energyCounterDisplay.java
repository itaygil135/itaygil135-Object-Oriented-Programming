package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Counter;
import danogl.util.Vector2;

import java.awt.*;

    /**
     * Display a graphic object on the game window showing a numeric count of energy left.
     */
    public class energyCounterDisplay extends GameObject {
        private static final TextRenderable renderable = new TextRenderable("");
        private final Counter energyCounter;
        private static final String ENERGY_DISPLAY_MSG = "Energy: %d";
        private final GameObjectCollection gameObjectCollection;

        public energyCounterDisplay(Counter energyCounter, Vector2 topLeftCorner, Vector2 dimensions,
                                    GameObjectCollection gameObjectCollection) {
            super(topLeftCorner, dimensions, renderable);
            this.energyCounter = energyCounter;
            this.gameObjectCollection = gameObjectCollection;
            renderable.setString(String.format(ENERGY_DISPLAY_MSG, this.energyCounter.value()));
            renderable.setColor(new Color(5, 206, 170));
            this.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        }

        /**
         * sets display to mach energy of avatar
         * @param deltaTime
         */
        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            renderable.setString(String.format(ENERGY_DISPLAY_MSG, this.energyCounter.value()));
        }

    }

