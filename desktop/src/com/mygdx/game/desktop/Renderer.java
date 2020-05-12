/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mygdx.game.desktop;

import java.util.*;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.lwjgl.util.vector.Vector2f;
import java.util.ArrayList;

public class Renderer {
    static final float ALMOSTZERO = 0.001f;
    static final float TWO_PI = (float) (Math.PI * 2f);
    static final float PI = (float) Math.PI;
    static final float HALF_PI = (float) (Math.PI / 2f);
    static final float THREEHALF_PI = (float) ((3f * Math.PI) / 2f);
    static final int PRECISION = 1000;
    
    Vector2f drawPosition, ray_intensity;
    static int width, height, centerX, centerY;
    
    //Raycasting
    private float aX, aY, intersection_x, intersection_y;
    private float tan, cotan;
    private boolean horizontal;
    private boolean collision_h, collision_v;
    private float ray_x, ray_y;
    private float ray_x_vertical, ray_y_vertical;
    private float distance_h, distance_v;
    private float darknest_level;
    
    private Texture[] screen;
    private float[] distances, texture_positions;
    private float[] cotans, tans;
    private enum ORIENTATION { 
        Vertical, Horizontal; 
    }
    
    //Textures
    Sprite floor, ceil;
    Color floorColor, ceilColor;
        
    
    public Renderer() {
        setTrigonometryValues();    
        
        //Screen
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        centerX = width / 2;
        centerY = height / 2;
        darknest_level = 15f;
        
        //RayCasting        
        drawPosition = new Vector2f();
        ray_intensity = new Vector2f();
        
        //Arrays
        screen = new Texture[width];
        distances = new float[width];
        texture_positions = new float[width];
        
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
        
        return (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
    }
    
    
    /**
     * Adjust the ray angle if it goes outside of the circle
     * @param ray_angle the angle of the ray
     * @return the adjusted angle of the ray
     */
    public float adjustRayAngle(float ray_angle) {
        if (ray_angle > TWO_PI) {
            ray_angle -= TWO_PI;
        }
        
        if (ray_angle < 0f) {
            ray_angle += TWO_PI;
        }
        
        return ray_angle;
    }
    
    
    /**
     * Returns the distance of the ray with the fixed fish eye effect 
     * @param distance the distance of the ray
     * @param ray_angle the angle of the ray
     * @return the distance with the fish eye effect fixed
     */
    public float fixFishEyeEffect(float distance, float ray_angle) {
        return (float) (distance * Math.cos(Player.angle - ray_angle));
    }
    
    
    /**
     * Set the orientation of the current ray collision with a wall
     */
    public void setCollisionOrientation() {
        if (distance_v < distance_h) {
            horizontal = false;
            ray_x = ray_x_vertical;
            ray_y = ray_y_vertical;
        } else {
            horizontal = true;
        }
    }
    
    
    /**
     * Populate the tangent and cotangent arrays with its respective values
     */
    private void setTrigonometryValues() {
        int length = (int) ((Math.PI * 2 + 0.1f) * PRECISION);
        tans = new float[length];
        cotans = new float[length];
        
        for (int i = 0; i < length; i++) {
            tans[i] = (float) Math.tan((float) i / PRECISION);
            cotans[i] = (float) (1f / tans[i]);
        }
    }
    
    
    /**
     * Updates the values of the trigonometry variables 
     * based in the given angle of the ray
     * @param ray_angle the angle of the ray
     */
    private void updateTrigonometryValues(float ray_angle) {
        int angle = (int) Math.abs(ray_angle * PRECISION);
        
        tan = tans[angle];
        cotan = cotans[angle];
    }
    
    
    /**
     * Sorts the entity list by distance
     * @param entities the list of entities
     */
    private void sortListByDistance(ArrayList<Entity> entities) {
        Collections.sort(entities, Comparator.comparing(Entity::getDistance).reversed());
    }
    
    
    /**
     * Returns the index of the highest value in the array
     * @param array the array
     * @return the index of the highest value in the array
     */
    private int getHigherValue(float[] array) {
        int highestValueIndex = 0;
        for (int i = 0; i < width; i++) {
            if (array[i] >= array[highestValueIndex]) {
                highestValueIndex = i;
            }
        }
        
        return highestValueIndex;
    }
    
    
    /**
     * Sets the luminosity of the Spritebatch for the indicated ray
     * @param batch the sprite batch
     * @param index the horizontal position of the ray in the Screen
     */
    private void setRayLuminosity(SpriteBatch batch, int index) {
        float luminosity = (darknest_level / distances[index]);
        
        if (luminosity > 1f) {
            luminosity = 1f;
        }
        
        batch.setColor(luminosity, luminosity, luminosity, 1);
    }
    
    
    /**
     * Finds the intersection of a ray with a wall in the indicated angle
     * @param floors the floors to check the intersection
     * @param ray_angle the angle of the ray
     */
    private void findIntersectionWalls(TiledMapTileLayer[] floors, float ray_angle) {
        collision_h = collision_v = false;
        ray_x = Player.position.x;
        ray_y = Player.position.y;
        ray_x_vertical = Player.position.x;
        ray_y_vertical = Player.position.y;
        distance_h = distance_v = 0f;
        
        while (true) {
            //HORIZONTAL Intersection
            if (!collision_h) {
                intersection_y = (float) Math.abs(ray_y - Math.floor(ray_y));
                if (ray_angle < HALF_PI || ray_angle > THREEHALF_PI) {
                    intersection_y = 1f - intersection_y;
                }
                
                if (intersection_y < ALMOSTZERO) {
                    intersection_y = 1f;
                }
                
                aX = (float) (intersection_y * tan);
                aY = intersection_y;

                if (ray_angle < HALF_PI || ray_angle > THREEHALF_PI) {
                    ray_y += aY;
                    ray_x += aX;
                    if (floors[0].getCell((int) Math.floor(ray_x), (int) Math.floor(ray_y)) != null) {
                        collision_h = true;   
                    }
                } else {
                    ray_y -= aY;
                    ray_x -= aX;
                    if (floors[0].getCell((int) Math.floor(ray_x), (int) Math.floor(ray_y-1)) != null) {
                        collision_h = true;
                    }
                }
                distance_h = getRayDistance(ray_x, ray_y);
                if (collision_v && (distance_h >= distance_v)) {
                    return;
                }
            }

            //VERTICAL Intersection
            if (!collision_v) {
                intersection_x = (float) Math.abs(ray_x_vertical - Math.floor(ray_x_vertical));
                if (ray_angle < PI) {
                    intersection_x = 1f - intersection_x;
                }
                
                if (intersection_x < ALMOSTZERO) {
                    intersection_x = 1f;
                }
                
                aY = (float) (intersection_x * (cotan));
                aX = intersection_x;

                if (ray_angle < PI) {
                    ray_y_vertical += aY;
                    ray_x_vertical += aX;
                    if (floors[0].getCell((int) Math.floor(ray_x_vertical), (int) Math.floor(ray_y_vertical)) != null) {
                        collision_v = true;
                    }
                } else {
                    ray_y_vertical -= aY;
                    ray_x_vertical -= aX;
                    if (floors[0].getCell((int) Math.floor(ray_x_vertical - 1), (int) Math.floor(ray_y_vertical)) != null) {
                        collision_v = true;
                    }
                }
                
                distance_v = getRayDistance(ray_x_vertical, ray_y_vertical);
                
                if (collision_h && (distance_h <= distance_v)) {
                    return;
                }
            }
            
            if (collision_v && collision_h) {
                return;
            }
        }
    }
    
    
    /**
     * Renders all the map and the entities
     * @param batch the sprite batch
     * @param floors the floors to render
     * @param entity the list of entities to render
     */
    public void render(SpriteBatch batch, TiledMapTileLayer[] floors, ArrayList<Entity> entity) {
        sortListByDistance(entity);
        drawFloorCeil(batch);
        RayCasting(floors);
        generalRender(batch, entity);
    }
   
    
    /**
     * Draws both the floor and the ceiling
     * @param batch the sprite batch
     */
    private void drawFloorCeil(SpriteBatch batch) {
        floor.draw(batch);
        ceil.draw(batch);
    }
    
    
    /**
     * General function of RayCasting
     * @param floors the floors to render
     */
    private void RayCasting(TiledMapTileLayer[] floors) {
        for (int x = 0; x < width; x++) {
            float ray_angle = adjustRayAngle((Player.angle - 0.5f) + ((float) x / (float) width));
            
            updateTrigonometryValues(ray_angle);
            findIntersectionWalls(floors, ray_angle);
            
            distances[x] = fixFishEyeEffect(Math.min(distance_v, distance_h), ray_angle);
            
            setCollisionOrientation();
            
            ORIENTATION wallOrientation = (ray_x - Math.floor(ray_x) < 0.01f) ? 
                    ORIENTATION.Vertical : ORIENTATION.Horizontal;

            float textureX = (float) (ray_x - Math.floor(ray_x)), 
                  textureY = (float) (ray_y - Math.floor(ray_y));
            
            texture_positions[x] = (wallOrientation == ORIENTATION.Vertical) ? 
                            ((textureX > 0.5f) ? textureY : 1 - textureY):
                            ((textureY > 0.5f) ? 1 - textureX : textureX);
            
            //Adjust Ray position in the map if the Ray angle is negative
            if (horizontal && (ray_angle > HALF_PI && ray_angle < THREEHALF_PI)) {
                ray_y--;
            }
        
            if (!horizontal && ray_angle > PI) {
                ray_x--;
            }

            if (floors[0].getCell((int)ray_x, (int)ray_y) != null) {
                screen[x] = floors[0].getCell((int)ray_x, (int)ray_y).getTile().getTextureRegion().getTexture();
            }
        }
    }
    
    
    /**
     * Calls the render function for all the rays and entities in the Screen 
     * @param batch the sprite batch
     * @param entity the list of entities to render
     */
    private void generalRender(SpriteBatch batch, ArrayList<Entity> entity) {
        for (int x = 0; x < width; x++) {
            int mostDistant = getHigherValue(distances);
            for (Entity aux: entity) {
                if (aux.getDistance() > distances[mostDistant] || x == (width - 1)) {
                    aux.Render(batch);
                }
            }
            
            setRayLuminosity(batch, mostDistant);
            rayRender(batch, mostDistant);
            distances[mostDistant] = 0;
        }
    }
    
    
    
    /**
     * Renders one single ray in the indicated index
     * @param batch the sprite batch
     * @param i the horizontal position of the ray in the Screen
     */
    private void rayRender(SpriteBatch batch, int i) {
        int floorPosition = (int) (centerY - height / distances[i]);
        int wallHeight = height - (floorPosition * 2);
        drawPosition.set(i - centerX, floorPosition - centerY);
        
        batch.draw(screen[i], drawPosition.x, drawPosition.y, 1, wallHeight, texture_positions[i], 1, texture_positions[i] - 0.01f, 0);
    }

}
