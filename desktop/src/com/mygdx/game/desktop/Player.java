package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import org.lwjgl.util.vector.Vector2f;

public final class Player {
    static Vector2f position, old_position;
    static float angle, delta;
    private static float speed_movement, speed_lateral, speed_rotation;
    static Player instance;
    
    
    public Player() {
        position = new Vector2f(10, 9);
        old_position = new Vector2f(position.x, position.y);
        angle = 5.75f;
        speed_movement = 5;
        speed_rotation = 1.7f;
        speed_lateral = speed_movement / 2f;
    }
    
    
    //Singleton
    public static Player getInstance() {
        return instance = (instance == null) ? new Player() : instance;
    }
    
    
    /**
     * Allows the rotation of the player
     */
    public void rotation() {
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            angle += speed_rotation * delta;
        }
            
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            angle -= speed_rotation * delta;
        }
    }
    
    
    /**
     * Adjusts the angle of the player if it goes outside of the circle
     */
    public void adjustAngle() {
        if (angle > Renderer.TWO_PI) {
            angle = 0;
        }
        
        if (angle < 0) {
            angle = Renderer.TWO_PI;
        }
    }
    
    
    /**
     * Allows the movement of the player
     */
    public void movement() {
        old_position.set(position);
        float currentSpeedMovement = (delta * speed_movement);
        float currentLateralSpeedMovement = (delta * speed_lateral);
        
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.x += Math.sin(angle) * currentSpeedMovement;
            position.y += Math.cos(angle) * currentSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.x -= Math.sin(angle) * currentSpeedMovement;
            position.y -= Math.cos(angle) * currentSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            position.x += Math.sin(angle+Renderer.HALF_PI) * currentLateralSpeedMovement;
            position.y += Math.cos(angle+Renderer.HALF_PI) * currentLateralSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            position.x += Math.sin(angle-Renderer.HALF_PI) * currentLateralSpeedMovement;
            position.y += Math.cos(angle-Renderer.HALF_PI) * currentLateralSpeedMovement;
        }
    }
    
    
    /**
     * Blocks the movement of the player if its collides with a wall
     * @param floor the floor to check the collision
     */
    public void collision(TiledMapTileLayer floor) {
        int actualX = (int) Math.floor(position.x),
            actualY = (int) Math.floor(position.y);
        
        if (floor.getCell((int)actualX, (int)actualY) != null) {
            position.x = old_position.x;
            position.y = old_position.y;
        }
    }

    
    /**
     * Updates the player in general
     * @param e the main state
     */
    public void update(Main e) {
        delta = Gdx.graphics.getDeltaTime();
        adjustAngle();
        rotation();
        movement();
        collision(e.floors[0]);
    }
}