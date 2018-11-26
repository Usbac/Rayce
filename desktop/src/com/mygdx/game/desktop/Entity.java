package com.mygdx.game.desktop;

import com.badlogic.gdx.graphics.g2d.*;
import org.lwjgl.util.vector.Vector2f;

public class Entity {
    static Vector2f rayIntensity;
    Vector2f position;
    float distance, size;
    Sprite texture;
    boolean inVision, visible, isDrawn;
    boolean collisionable;
    
    public float getDistance() {
        return distance;
    }

    public Entity() {
        visible = true;
    }
    

    public Entity(TextureRegion text, int x, int y, boolean c) {
        position = new Vector2f(x, y);
        rayIntensity = new Vector2f();
        texture = new Sprite(text);
        visible = true;
        size = 1.8f;
        collisionable = c;
    }
    

    /**
     * Executes the collision of the entity with the player
     * @return <code>true</code> if the entity collides with the player, <code>false</code> otherwise
     */
    public boolean collision() {
        if (!collisionable) 
            return false;
        float distX = Math.abs(Player.position.x-position.x);
        float distY = Math.abs(Player.position.y-position.y);
        if (distX<0.3f && distY<0.3f) {
            Player.position.x = Player.oldPosition.x;
            Player.position.y = Player.oldPosition.y;
        }
        return (distX<0.3f && distY<0.3f);
    }
    
    
    /**
     * Returns <code>true</code> if the entity is visible by the player, <code>false</code> otherwise
     * @param distance The distance between the entity and the player
     * @return <code>true</code> if the entity is visible by the player, <code>false</code> otherwise
     */
    public boolean inVision(float distance) {
        float comparisonAngle = (float) Math.cos(Math.atan2(position.x - Player.position.x, 
                                                            position.y - Player.position.y));
        int start = (int) (size/distance) * -Main.width;
        for (int x = start; x < Main.width-start; x += 2) {
            float rayAngle = (Player.angle - 0.5f) + ((float) x / Main.width);
            if (Math.abs(Math.cos(rayAngle)-comparisonAngle) < 0.01f && touchedByRay(rayAngle, x)) 
                return true;
        }
        return false;
    }
    
    
    /**
     * Returns <code>true</code> if the indicated ray touches the entity, <code>false</code> otherwise
     * @param rayAngle The angle of the ray
     * @param x the horizontal position in the screen
     * @return <code>true</code> if the indicated ray touches the entity, <code>false</code> otherwise
     */
    public boolean touchedByRay(float rayAngle, int x) {
        float offset = 0.02f;
        rayIntensity.x = (float) Math.sin(rayAngle) * offset;
        rayIntensity.y = (float) Math.cos(rayAngle) * offset;
        //Search entity
        float rayX = Player.position.x;
        float rayY = Player.position.y;
        float temporalDistance = 0f;
        while (temporalDistance < 40f) {
            temporalDistance += offset;
            rayX += rayIntensity.x;
            rayY += rayIntensity.y;
            if (Math.abs(rayX-position.x) < offset && Math.abs(rayY-position.y) < offset) {
                inVision = true;
                break;
            }
        }
        
        //Update variables
        if (inVision) {
            distance = temporalDistance;
            texture.setSize(-texture.getY()*size, -texture.getY()*size);
            texture.setPosition((x-Main.centerX)-(texture.getWidth()/2), -(Main.height/distance));
        }
        
        return inVision;
    }
    
    
    /**
     * Render the entity in the SpriteBatch
     */
    public void Render() {
        if (!visible || !inVision || isDrawn)
            return;
        texture.draw(Main.batch);
        isDrawn = true;
    }
    
    
    /**
     * Update the entity in General
     */
    public void update() {
        inVision = isDrawn = false;
        distance = 0f;
        float x1 = Player.position.x - position.x;
        float y1 = Player.position.y - position.y;
        
        inVision((float) (Math.sqrt((x1*x1) + (y1*y1))));
        collision();
    }
    
}