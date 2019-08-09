package com.mygdx.game.desktop;

import com.badlogic.gdx.*;
import static com.badlogic.gdx.Gdx.gl;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Comparator.comparing;
import org.lwjgl.util.vector.Vector2f;

public final class Main implements Screen {
    Player player;
    public final Base Base;
    
    //Constants
    static final float ALMOSTZERO = 0.001f;
    static final float TWO_PI = (float) (Math.PI*2f);
    static final float PI = (float) Math.PI;
    static final float HALF_PI = (float) (Math.PI/2f);
    static final float THREEHALF_PI = (float) ((3f*Math.PI)/2f);
    static final int PRECISION = 1000;
    
    //Raycasting
    float aX, aY, distanceIntersectionX, distanceIntersectionY;
    float tan, cotan;
    boolean horizontal;
    boolean collisionH, collisionV;
    float rayX, rayY;
    float rayXvertical, rayYvertical;
    float distH, distV;
    float darknestLevel;
    
    //Camera
    Vector2f drawPosition, rayIntensity;
    static int width, height, centerX, centerY;
    static float FOV; 
    static SpriteBatch batch;
    static ShapeRenderer shapeRenderer;
    static Camera camera;
    
    //Textures
    Sprite floor, ceil;
    Color floorColor, ceilColor;
    Texture[] screen;
    float[] distances, texturePositions;
    float[] cotans, tans;
    enum ORIENTATION { Vertical, Horizontal; }
    
    //Map & Entities
    TiledMap mapaTemp;
    TiledMapTileLayer[] floors;
    ArrayList<Entity> entity;
    

    public Main(Base base) {
        Base = base;
        player = Player.getInstance();
        //Batch & Camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch = new SpriteBatch(); 
        batch.setProjectionMatrix(camera.projection);
        //Screen
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        centerX = width / 2;
        centerY = height / 2;
        darknestLevel = 15f;
        //RayCasting        
        drawPosition = new Vector2f();
        rayIntensity = new Vector2f();
        setTans();
        //Arrays
        screen = new Texture[width];
        distances = new float[width];
        texturePositions = new float[width];
        //Floor
        floor = new Sprite(new Texture("degradado.png"));
        floor.setSize(width, centerY);
        floor.setPosition(-centerX, -centerY);
        floorColor = new Color(Color.valueOf("311a12"));
        floor.setColor(floorColor);
        //Ceil
        ceil = new Sprite(new Texture("degradado.png"));
        ceil.setSize(width, centerY);
        ceil.setPosition(-centerX, 0);
        ceil.flip(false, true);
        ceilColor = new Color(Color.valueOf("7f7f7f"));
        ceil.setColor(ceilColor);
        //MAP
        entity = new ArrayList<>();
        floors = new TiledMapTileLayer[3];
        mapaTemp = new TmxMapLoader().load("Mapa.tmx");
        for (int i = 0; i < mapaTemp.getLayers().getCount(); i++) {
            floors[i] = (TiledMapTileLayer) mapaTemp.getLayers().get(i);
        }
        
        mapEntities();
    }
    
    
    /**
     * Loads the entities of the map in the entity list
     */
    public final void mapEntities() {
        for (int x = 0; x < floors[1].getWidth(); x++) {
            for (int y = 0; y < floors[1].getHeight(); y++) {
                //Entities with collision
                if (floors[1].getCell(x, y) != null) { 
                    entity.add(new Entity(floors[1].getCell(x, y).getTile().getTextureRegion(), x, y, true));
                }
                
                //Entities without collision
                if (floors[2].getCell(x, y) != null) {
                    entity.add(new Entity(floors[2].getCell(x, y).getTile().getTextureRegion(), x, y, false));
                }
            }
        }
    }
    
    
    /**
     * Draws both the floor and the ceiling
     */
    public void drawFloorCeil() {
        floor.draw(batch);
        ceil.draw(batch);
    }
    
        
    /**
     * Returns the distance between the player and a point in the indicated position
     * @param x the position in the horizontal axis
     * @param y the position in the vertical axis
     * @return the distance between the player and a point in the indicated position
     */
    public float getRayDistance(float x, float y) {
        float distanceX = Math.abs(x - Player.position.x);
        float distanceY = Math.abs(y - Player.position.y);
        
        return (float) Math.sqrt(distanceX*distanceX + distanceY*distanceY);
    }
    
    
    /**
     * Adjust the ray angle if it goes outside of the circle
     * @param rayAngle the angle of the ray
     * @return the adjusted angle of the ray
     */
    public float adjustRayAngle(float rayAngle) {
        if (rayAngle > TWO_PI) {
            rayAngle -= TWO_PI;
        }
        
        if (rayAngle < 0f) {
            rayAngle += TWO_PI;
        }
        
        return rayAngle;
    }
    
    
    /**
     * Returns the distance of the ray with the fixed fish eye effect 
     * @param distance the distance of the ray
     * @param rayAngle the angle of the ray
     * @return the distance with the fish eye effect fixed
     */
    public float fixFishEyeEffect(float distance, float rayAngle) {
        return (float) (distance * Math.cos(Player.angle - rayAngle));
    }
    
    
    /**
     * Set the orientation of the current ray collision with a wall
     */
    public void setCollisionOrientation() {
        if (distV < distH) {
            horizontal = false;
            rayX = rayXvertical;
            rayY = rayYvertical;
        } else {
            horizontal = true;
        }
    }
    
    
    /**
     * Returns <code>true</code> if the collision happens in the horizontal axis, <code>false</code> otherwhise
     * @return <code>true</code> if the collision happens in the horizontal axis, <code>false</code> otherwhise
     */
    public float getCollisionDistance() {
        return (distV < distH)? distV:distH;
    }
    
    
    /**
     * Populate the tangent and cotangent arrays with its respective values
     */
    public void setTans() {
        int length = (int) ((Math.PI * 2 + 0.1f) * PRECISION);
        tans = new float[length];
        cotans = new float[length];
        
        for (int i = 0; i < length; i++) {
            tans[i] = (float) Math.tan((float) i / PRECISION);
            cotans[i] = (float) (1f / tans[i]);
        }
    }
    
    
    /**
     * Update the values of the trigonometry variables based in the angle of the ray
     * @param rayAngle the angle of the ray
     */
    public void updateTrigonometryValues(float rayAngle) {
        int angle = (int) Math.abs(rayAngle * PRECISION);
        
        tan = tans[angle];
        cotan = cotans[angle];
    }
    
    
    /**
     * Sorts the entity list by distance
     * @param entities the list of entities
     */
    public void sortListByDistance(ArrayList<Entity> entities) {
        Collections.sort(entities, comparing(Entity::getDistance).reversed());
    }
    
    
    /**
     * Returns the index of the highest value in the array
     * @param array the array
     * @return the index of the highest value in the array
     */
    public int getHigherValue(float[] array) {
        int highestValueIndex = 0;
        for (int i = 0; i < width; i++) {
            if (distances[i] >= distances[highestValueIndex]) {
                highestValueIndex = i;
            }
        }
        return highestValueIndex;
    }
    
    
    /**
     * Sets the luminosity of the Spritebatch for the indicated ray
     * @param index the horizontal position of the ray in the Screen
     */
    public void setRayLuminosity(int index) {
        float luminosity = (darknestLevel / distances[index]);
        
        if (luminosity > 1f) {
            luminosity = 1f;
        }
        
        batch.setColor(luminosity, luminosity, luminosity, 1);
    }
    
    
    /**
     * Finds the intersection of a ray with a wall in the indicated angle
     * @param rayAngle the angle of the ray
     */
    public void findIntersectionWalls(float rayAngle) {
        collisionH = collisionV = false;
        rayX = Player.position.x;
        rayY = Player.position.y;
        rayXvertical = Player.position.x;
        rayYvertical = Player.position.y;
        distH = distV = 0f;
        
        while (true) {
            //HORIZONTAL Intersection
            if (!collisionH) {
                distanceIntersectionY = (float) Math.abs(rayY - Math.floor(rayY));
                if (rayAngle < HALF_PI || rayAngle > THREEHALF_PI) {
                    distanceIntersectionY = 1f - distanceIntersectionY;
                }
                
                if (distanceIntersectionY < ALMOSTZERO) {
                    distanceIntersectionY = 1f;
                }
                
                aX = (float) (distanceIntersectionY * (tan));
                aY = distanceIntersectionY;

                if (rayAngle < HALF_PI || rayAngle > THREEHALF_PI) {
                    rayY += aY;
                    rayX += aX;
                    if (floors[0].getCell((int) Math.floor(rayX), (int) Math.floor(rayY))!=null) {
                        collisionH = true;   
                    }
                } else {
                    rayY -= aY;
                    rayX -= aX;
                    if (floors[0].getCell((int) Math.floor(rayX), (int) Math.floor(rayY-1))!=null) {
                        collisionH = true;
                    }
                }
                distH = getRayDistance(rayX, rayY);
                if (collisionV && (distH >= distV)) {
                    return;
                }
            }

            //VERTICAL Intersection
            if (!collisionV) {
                distanceIntersectionX = (float) Math.abs(rayXvertical - Math.floor(rayXvertical));
                if (rayAngle < PI) {
                    distanceIntersectionX = 1f - distanceIntersectionX;
                }
                
                if (distanceIntersectionX < ALMOSTZERO) {
                    distanceIntersectionX = 1f;
                }
                
                aY = (float) (distanceIntersectionX * (cotan));
                aX = distanceIntersectionX;

                if (rayAngle < PI) {
                    rayYvertical += aY;
                    rayXvertical += aX;
                    if (floors[0].getCell((int) Math.floor(rayXvertical), (int) Math.floor(rayYvertical)) != null) {
                        collisionV = true;
                    }
                } else {
                    rayYvertical -= aY;
                    rayXvertical -= aX;
                    if (floors[0].getCell((int) Math.floor(rayXvertical - 1), (int) Math.floor(rayYvertical)) != null) {
                        collisionV = true;
                    }
                }
                
                distV = getRayDistance(rayXvertical, rayYvertical);
                
                if (collisionH && (distH <= distV)) {
                    return;
                }
            }
            
            if (collisionV && collisionH) {
                return;
            }
        }
    }
   
    
    /**
     * General function of RayCasting
     */
    public void RayCasting() {
        for (int x = 0; x < width; x++) {
            float rayAngle = adjustRayAngle((Player.angle - 0.5f) + ((float) x / (float) width));
            
            updateTrigonometryValues(rayAngle);
            findIntersectionWalls(rayAngle);
            
            distances[x] = fixFishEyeEffect(getCollisionDistance(), rayAngle);
            
            setCollisionOrientation();
            
            ORIENTATION wallOrientation = (rayX - Math.floor(rayX) < 0.01f) ? 
                    ORIENTATION.Vertical : ORIENTATION.Horizontal;

            float textureX = (float) (rayX - Math.floor(rayX)), 
                  textureY = (float) (rayY - Math.floor(rayY));
            
            texturePositions[x] = (wallOrientation == ORIENTATION.Vertical) ? 
                            ((textureX > 0.5f) ? textureY : 1 - textureY):
                            ((textureY > 0.5f) ? 1 - textureX : textureX);
            
            //Adjust Ray position in the map if the Ray angle is negative
            if (horizontal && (rayAngle > HALF_PI && rayAngle < THREEHALF_PI)) {
                rayY --;
            }
        
            if (!horizontal && rayAngle > PI) {
                rayX --;
            }

            if (floors[0].getCell((int)rayX, (int)rayY) != null) {
                screen[x] = floors[0].getCell((int)rayX, (int)rayY).getTile().getTextureRegion().getTexture();
            }
        }
    }
    

    /**
     * Calls the render function for all the rays and entities in the Screen 
     */
    public void generalRender() {
        for (int x = 0; x < width; x++) {
            int mostDistant = getHigherValue(distances);
            for (Entity aux: entity) {
                if (aux.distance > distances[mostDistant] || x == (width - 1)) {
                    aux.Render();
                }
            }
            
            setRayLuminosity(mostDistant);
            rayRender(mostDistant);
            distances[mostDistant] = 0;
        }
    }
    
    
    /**
     * Renders one single ray in the indicated index
     * @param i the horizontal position of the ray in the Screen
     */
    public void rayRender(int i) {
        int floorPosition = (int) (centerY - height/distances[i]);
        int wallHeight = height - (floorPosition * 2);
        drawPosition.set(i - centerX, floorPosition - centerY);
        
        batch.draw(screen[i], drawPosition.x, drawPosition.y, 1, wallHeight, texturePositions[i], 1, texturePositions[i] - 0.01f, 0);
    }
    
    

    @Override
    public void render(float f) {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        entity.forEach((aux) -> aux.update());
        sortListByDistance(entity);
        
        batch.begin();
        drawFloorCeil();
        RayCasting();
        generalRender();
        player.update(this);
        batch.end();
    }
    
    @Override
    public void show() {}
    @Override
    public void resize(int i, int i1) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {}
    
}
