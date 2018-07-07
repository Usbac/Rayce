package com.mygdx.game.desktop;
import com.badlogic.gdx.graphics.g2d.*;
import org.lwjgl.util.vector.Vector2f;

public class Entity {
    //Variables Impresion
    Vector2f rayIntensity;
    boolean isDrawn;
    //Variables generales
    Vector2f position;
    float distance, size;
    Sprite texture;
    boolean inVision, visible, collisionable;
    
    //Getter Distancia
    public float getDistance() {
        return distance;
    }

    //*****-->CONSTRUCTOR VACIO (ITEMS & ENEMIGOS)<--*****
    public Entity() {
        visible = true;
    }
    
    //*****-->CONSTRUCTOR OBJETOS EN MAPA<--*****
    public Entity(TextureRegion text, int x, int y, boolean c) {
        position = new Vector2f(x, y);
        rayIntensity = new Vector2f();
        visible = true;
        texture = new Sprite(text);
        size = 1.8f;
        collisionable = c;
    }
    
        
    //*****-->COLISION<--*****
    public void collision(Main e) {
        float distX = Math.abs(e.player.position.x-position.x);
        float distY = Math.abs(e.player.position.y-position.y);
        if (distX<0.3f && distY<0.3f) {
            e.player.position.x = e.player.oldPosition.x;
            e.player.position.y = e.player.oldPosition.y;
        }
    }
    
        
    //*****-->ACTUALIZACION GENERAL<--*****
    public void update(Main e) {
        //Variables
        inVision = false;
        isDrawn = false;
        distance = 0f;
        float x1 = e.player.position.x-position.x;
        float y1 = e.player.position.y-position.y;
        float dist = (float) (Math.sqrt((x1*x1) + (y1*y1)));
        
        //Comienzo Impresion: Ancho de pantalla - ancho Entidad
        int starting = (int) ((size/dist)*-e.width);
        double angle = Math.atan2(position.x-e.player.position.x, position.y-e.player.position.y);
        float comparisonAngle = (float) Math.cos(angle);
        float playerAngle = e.player.angle-0.5f;
        //Ciclo para verificar que esta en rango de vision
        for (int x = starting; x < e.width-starting; x+=2) {
            float rayAngle = playerAngle + ((float)x / (float)e.width);
            if (Math.abs(Math.cos(rayAngle)-comparisonAngle)<0.01f) 
                if (inVision(e, rayAngle, x)) break;
        }
        if (collisionable) collision(e);
    }
    
    
    //*****-->ACTUALIZACION VARIABLES IMPRESION<--*****
    public boolean inVision(Main e, float RayoAngulo, int x) {
        float offset = 0.02f;
        rayIntensity.x = (float) Math.sin(RayoAngulo) * offset;
        rayIntensity.y = (float) Math.cos(RayoAngulo) * offset;
        //Recorrer en busca de Entidad
        float rayoX = e.player.position.x;
        float rayoY = e.player.position.y;
        float temporalDistance = 0f;
        while (true) {
            temporalDistance += offset;
            rayoX += rayIntensity.x;
            rayoY += rayIntensity.y;
            if (Math.abs(rayoX-position.x)<offset && Math.abs(rayoY-position.y)<offset) inVision = true;
            if (temporalDistance>40f || inVision) break;
        }
        
        //Actualizar variables Impresion
        if (inVision) {
            distance = temporalDistance;
            texture.setSize(-texture.getY()*size, -texture.getY()*size);
            texture.setPosition((x-e.centerX)-(texture.getWidth()/2), -(e.height/distance));
            return true;
        } else 
            return false;
    }
    
        
    //*****-->RENDERIZAR<--*****
    public void Render(Main e) {
        if (visible && inVision && !isDrawn) {
            texture.draw(e.batch);
            isDrawn = true;
        }
    }
    
}