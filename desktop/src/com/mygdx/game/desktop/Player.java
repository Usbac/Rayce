package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.lwjgl.util.vector.Vector2f;

public final class Player {
    static Vector2f position, oldPosition;
    static float angle, delta;
    static float speedMovement, lateralSpeedMovement, speedRotation;
    static Player instance;
    
    public Player() {
        position = new Vector2f(10, 9);
        oldPosition = new Vector2f(position.x, position.y);
        angle = 5.75f;
        speedMovement = 5;
        speedRotation = 1.7f;
        lateralSpeedMovement = speedMovement / 2f;
    }
    
    //Singleton
    public static Player getInstance() {
        return instance = (instance==null) ? new Player() : instance;
    }
    
    
    /**
     * Allows the rotation of the player
     */
    public void rotation() {
        if (Gdx.input.isKeyPressed(Input.Keys.D)) 
            angle += speedRotation * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) 
            angle -= speedRotation * delta;
    }
    
    
    /**
     * Adjusts the angle of the player if it goes outside of the circle
     */
    public void adjustAngle() {
        if (angle > Main.TWO_PI) {
            angle = 0;
        }
        
        if (angle < 0) {
            angle = Main.TWO_PI;
        }
    }
    
    
    /**
     * Allows the movement of the player
     */
    public void movement() {
        oldPosition.set(position);
        float currentSpeedMovement = (delta * speedMovement);
        float currentLateralSpeedMovement = (delta * lateralSpeedMovement);
        
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.x += Math.sin(angle) * currentSpeedMovement;
            position.y += Math.cos(angle) * currentSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.x -= Math.sin(angle) * currentSpeedMovement;
            position.y -= Math.cos(angle) * currentSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            position.x += Math.sin(angle+Main.HALF_PI) * currentLateralSpeedMovement;
            position.y += Math.cos(angle+Main.HALF_PI) * currentLateralSpeedMovement;
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            position.x += Math.sin(angle-Main.HALF_PI) * currentLateralSpeedMovement;
            position.y += Math.cos(angle-Main.HALF_PI) * currentLateralSpeedMovement;
        }
    }
    
    
    /**
     * Blocks the movement of the player if its collides with a wall
     * @param e the main state
     */
    public void collision(Main e) {
        int actualX = (int) Math.floor(position.x),
            actualY = (int) Math.floor(position.y);
        
        if (e.floors[0].getCell((int)actualX, (int)actualY)!=null) {
            position.x = oldPosition.x;
            position.y = oldPosition.y;
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
        collision(e);
    }
}