package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.PepseGameManager;
import pepse.world.Block;
import pepse.world.Terrain;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import pepse.util.*;

import java.awt.*;
import java.util.Objects;
import java.util.Random;


public class Tree {
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final int MAX_TREE_BLOCKS_TO_ADD = 4 ;
    private static final int MIN_TREE_HEIGHT = 2 ;
    public static final String TRUNK_TAG = "TRUNK";
    private static final int TRUNK_LAYER_FROM_BACKGROUND = 2;
    private final GameObjectCollection gameObjects
            ;
    private final int groundLayer;
    private final Vector2 windowDimensions;


    private Function<Float, Float> groundHeightAtFunc;

    private final int LEAVES_SPACE = 2;
    private final RectangleRenderable renderable;
    private final int seed;

    public Tree(GameObjectCollection gameObjects,
                int groundLayer, Vector2 windowDimensions,
                int seed, Function<Float, Float> func){
        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.windowDimensions = windowDimensions;
        this.seed = seed;
        this.groundHeightAtFunc = func;
        this.renderable = new RectangleRenderable(ColorSupplier.approximateColor(TRUNK_COLOR));
    }

    /**
     * creates trees in given range
     * @param minX min x
     * @param maxX max x in range
     */
    public void createInRange(int minX, int maxX){
        minX = Math.floorDiv(minX, Block.SIZE) * Block.SIZE;
        maxX = Math.floorDiv(maxX,Block.SIZE) * Block.SIZE;
        for (int i = minX; i <=maxX ; i+= Block.SIZE)
        {
            Random rand = new Random(Objects.hash(i, seed));
            if(rand.nextInt(100) < 15){
                createTreeAT(i,rand.nextInt(MAX_TREE_BLOCKS_TO_ADD));
            }
        }
    }

    /**
     * creates a single tree at given x coordinate.
     * @param i x coordinate
     * @param addToHeight size to add to tree height from min tree height constant.
     */
    private void createTreeAT(int i,  int addToHeight) {
        int treeHeight = MIN_TREE_HEIGHT + addToHeight;
        float ground = groundHeightAtFunc.apply((float)i);
        int groundHeight = (int) (((int) (ground / Block.SIZE)) * Block.SIZE);
        int j;
        for (j= 1 ;j <= treeHeight; j++) {
            Block block =  new Block(new Vector2(i, groundHeight - j*Block.SIZE)
                                        , renderable);
            block.setTag(TRUNK_TAG);
            gameObjects.addGameObject(block, Layer.BACKGROUND + TRUNK_LAYER_FROM_BACKGROUND);
        }
        addLeavesAt(i, groundHeight - j * Block.SIZE, treeHeight/2 * 2 + 1);
    }

    /**
     * add lives to a tree trunk
     * @param x coordinate to start from
     * @param y coordinate to start from
     * @param numOfLeavesInRow num of leaves in row
     */
    private void addLeavesAt(int x, int y, int numOfLeavesInRow) {
        Random rand = new Random(Objects.hash(x, seed));
        x = x - LEAVES_SPACE - (numOfLeavesInRow / 2)* (Block.SIZE )  ;
        y = y - LEAVES_SPACE -(numOfLeavesInRow /2) * (Block.SIZE ) ;
        for (int i = 0; i < numOfLeavesInRow; i++) {
            for (int j = 0; j < numOfLeavesInRow; j++)
            {
                Vector2 location = new Vector2(x + i * (Block.SIZE + LEAVES_SPACE),
                        y + j * (Block.SIZE + LEAVES_SPACE));
                boolean flag = true;
                for (int ind = 0; ind < Block.SIZE; ind++)
                {
                    if (location.y() + 2*Block.SIZE + LEAVES_SPACE >= groundHeightAtFunc.apply(location.x() + ind))
                    {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    gameObjects.addGameObject(new Leaf(location,rand), PepseGameManager.LEAF_LAYER);
        }
    }
}
}
