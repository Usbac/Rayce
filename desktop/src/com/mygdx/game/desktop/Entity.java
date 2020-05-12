package com.mygdx.game.desktop;

import com.badlogic.gdx.graphics.g2d.*;
import org.lwjgl.util.vector.Vector2f;

public class Entity {
    static final float DEFAULT_SIZE = 1.8f;
    
    static Vector2f rayIntensity;
    private Vector2f position;
    private float distance, size;
    private Sprite texture;
    private boolean in_vision, visible, is_drawn, collisionable;

    
    public Entity() {
        visible = true;
    }
    

    public Entity(TextureRegion text, int x, int y, boolean c) {
        position = new Vector2f(x, y);
        rayIntensity = new Vector2f();
        texture = new Sprite(text);
        visible = true;
        size = DEFAULT_SIZE;
        collisionable = c;
    }
    
    
    /**
     * Returns the entity distance
     * @return the entity distance
     */
    public float getDistance() {
        return distance;
    }
    

    /**
     * Executes the collision of the entity with the player
     * @return <code>true</code> if the entity collides with the player, <code>false</code> otherwise
     */
    private boolean collision() {
        if (!collisionable) {
            return false;
        }
        
        float distX = Math.abs(Player.position.x-position.x);
        float distY = Math.abs(Player.position.y-position.y);
        if (distX < 0.3f && distY < 0.3f) {
            Player.position.x = Player.old_position.x;
            Player.position.y = Player.old_position.y;
        }
        
        return (distX < 0.3f && distY < 0.3f);
    }
    
    
    /**
     * Returns <code>true</code> if the entity is visible by the player, <code>false</code> otherwise
     * @param distance The distance between the entity and the player
     * @return <code>true</code> if the entity is visible by the player, <code>false</code> otherwise
     */
    private boolean inVision(float distance) {
        float comparison_angle = (float) Math.cos(Math.atan2(position.x - Player.position.x, 
                                                            position.y - Player.position.y));
        int start = (int) (size / distance) * -Renderer.width;
        
        for (int x = start; x < Renderer.width - start; x += 2) {
            float ray_angle = (Player.angle - 0.5f) + ((float) x / Renderer.width);
            if (Math.abs(Math.cos(ray_angle)-comparison_angle) < 0.01f && touchedByRay(ray_angle, x)) 
                return true;
        }
        
        return false;
    }
    
    
    /**
     * Returns <code>true</code> if the indicated ray touches the entity, <code>false</code> otherwise
     * @param ray_angle The angle of the ray
     * @param x the horizontal position in the screen
     * @return <code>true</code> if the indicated ray touches the entity, <code>false</code> otherwise
     */
    private boolean touchedByRay(float ray_angle, int x) {
        float offset = 0.02f;
        rayIntensity.x = (float) Math.sin(ray_angle) * offset;
        rayIntensity.y = (float) Math.cos(ray_angle) * offset;
        //Search entity
        float ray_x = Player.position.x;
        float ray_y = Player.position.y;
        float tmp_distance = 0f;
        while (tmp_distance < 40f) {
            tmp_distance += offset;
            ray_x += rayIntensity.x;
            ray_y += rayIntensity.y;
            if (Math.abs(ray_x-position.x) < offset && Math.abs(ray_y-position.y) < offset) {
                in_vision = true;
                break;
            }
        }
        
        //Update variables
        if (in_vision) {
            distance = tmp_distance;
            texture.setSize(-texture.getY()*size, -texture.getY()*size);
            texture.setPosition((x - Renderer.centerX) - (texture.getWidth() / 2), -(Renderer.height / distance));
        }
        
        return in_vision;
    }
    
    
    /**
     * Render the entity in the SpriteBatch
     * @param batch the sprite batch
     */
    public void Render(SpriteBatch batch) {
        if (!visible || !in_vision || is_drawn) {
            return;
        }

        texture.draw(batch);
        is_drawn = true;
    }
    
    
    /**
     * Update the entity in General
     */
    public void update() {
        in_vision = is_drawn = false;
        distance = 0f;
        float x1 = Player.position.x - position.x;
        float y1 = Player.position.y - position.y;
        float player_distance = (float) (Math.sqrt((x1*x1) + (y1*y1)));
        
        inVision(player_distance);
        collision();
    }
    
}