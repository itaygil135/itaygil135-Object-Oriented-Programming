package pepse.world;

import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.NoiseGenerator;
import pepse.util.*;
import java.awt.*;

public class Terrain {
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int MINIMAL_BLOCKS_NUM = 4;
    private static final int SMOOTH_PARAM =  Block.SIZE;
    private static final int TERRAIN_DEPTH = 20;
    public static final String GROUND_TAG = "ground";
    public static final String UDER_GROUND_TAG = "under ground filling blocks";

    private final float groundHeightAtX0;
    private final NoiseGenerator perlinNoise;
    private final int seed;
    private final int groundLayer;
    private final Vector2 windowDimensions;
    private final RectangleRenderable renderable;
    private final GameObjectCollection gameObjects;
    public static final int ADD_FROM_STATIC_TO_LEAF_LAYER = 2;

    public Terrain(GameObjectCollection gameObjects,
                   int groundLayer, Vector2 windowDimensions,
                   int seed) {
        this.gameObjects = gameObjects;
        this.seed = seed;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        this.groundHeightAtX0 = windowDimensions.y()*(2/3F);
        this.perlinNoise = new NoiseGenerator(seed);
        this.renderable = new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));
    }


    public void createInRange(int minX, int maxX) {
        minX = Math.floorDiv(minX,Block.SIZE) * Block.SIZE;
        maxX = Math.floorDiv(maxX,Block.SIZE) * Block.SIZE + Block.SIZE;
        for(int i = minX; i<= maxX; i+=Block.SIZE)
        {
            Vector2 topLeft = new Vector2(i, ((int) Math.floor(groundHeightAt(i) / Block.SIZE)) * Block.SIZE);
            Block block = new Block(topLeft, this.renderable);
            block.setTag(GROUND_TAG);
            gameObjects.addGameObject(block, groundLayer);
            for (int j = 1; j<TERRAIN_DEPTH;j++)
            {
                topLeft = new Vector2(i, (j+(int) Math.floor(groundHeightAt(i) / Block.SIZE)) * Block.SIZE);
                block = new Block(topLeft, this.renderable);
                block.setTag(UDER_GROUND_TAG);
                gameObjects.addGameObject(block, groundLayer +1);
            }
        }
    }


    public float groundHeightAt(float x) {
        float groundHeight = Block.SIZE *(float) perlinNoise.noise(x / Block.SIZE) *SMOOTH_PARAM;
        if(groundHeight<0){
            return groundHeightAtX0;
        }else if(groundHeight + groundHeightAtX0 >= windowDimensions.y()){
            return windowDimensions.y() - MINIMAL_BLOCKS_NUM * Block.SIZE;
        }
        return groundHeight + groundHeightAtX0;

    }

    public int getLeavesLayer() {
        return this.groundLayer + ADD_FROM_STATIC_TO_LEAF_LAYER;
    }
    public int getGroundLayer(){
        return this.groundLayer;
    }
}

