package com.mygdx.game.desktop;
import com.badlogic.gdx.graphics.Texture;
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
    
    //*****-->CONSTRUCTOR PERSONAJES<--*****
    public Entity(int tipo, int x, int y) {
        position = new Vector2f(((float)x)+0.5f, ((float)y)+0.5f);
        rayIntensity = new Vector2f();
        visible = true;

        collisionable = true;
        //Personajes
        switch (tipo) {
            case 1: //***NOVIEMBRE
                texture = new Sprite(new Texture("Todo/HISTORIA2/Personajes/Noviembre.png"));
                size = 1.6f;
                break;
        }
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
    
        
    //**********COLISION**********
    public void collision(Main e) {
        float distX = Math.abs(e.player.position.x-position.x);
        float distY = Math.abs(e.player.position.y-position.y);
        if (distX<0.3f && distY<0.3f) {
            e.player.position.x = e.player.oldPosition.x;
            e.player.position.y = e.player.oldPosition.y;
        }
    }
    
        
    //**********ACTUALIZAR GENERAL**********
    public void update(Main e) {
        //Variables
        inVision = false;
        isDrawn = false;
        distance = 0f;
        float x1 = e.player.position.x-position.x;
        float y1 = e.player.position.y-position.y;
        float dist = (float) (Math.sqrt((x1*x1) + (y1*y1)));
        
        int starting = (int) ((size/dist)*-e.width);
        double angle = Math.atan2(position.y-e.player.position.y, position.x-e.player.position.x);
        float comparisonAngle = (float) Math.sin(angle);
        float playerAngle = e.player.angle-0.5f;
        //Ciclo para verificar que esta en rango de vision
        for (int x = starting; x < e.width-starting; x+=2) {
            float rayAngle = playerAngle + ((float)x / (float)e.width);
            if (Math.abs(Math.cos(rayAngle)-comparisonAngle)<0.01f) 
                if (inVision(e, rayAngle, x)) break;
        }
        if (collisionable) collision(e);
    }
    
    
    //**********ACTUALIZAR VARIABLES IMPRESION**********
    public boolean inVision(Main e, float RayoAngulo, int x) {
        rayIntensity.x = (float) Math.sin(RayoAngulo)*0.04f;
        rayIntensity.y = (float) Math.cos(RayoAngulo)*0.04f;

        //Recorrer en busca de Entidad
        float rayoX = e.player.position.x;
        float rayoY = e.player.position.y;
        float distanciaTemp = 0f;
        while (true) {
            distanciaTemp += 0.04f;
            rayoX += rayIntensity.x;
            rayoY += rayIntensity.y;
            if (Math.abs(rayoX-position.x)<0.04f && Math.abs(rayoY-position.y)<0.04f) inVision = true;
            if (distanciaTemp>40f || inVision) break;
        }
        
        //Actualizar variables Impresion
        if (inVision) {
            texture.setSize(-texture.getY()*size, -texture.getY()*size);
            texture.setPosition((x-e.centerX)-(texture.getWidth()/2), ((e.height/distanciaTemp)*-1));
            distance = distanciaTemp;
            return true;
        } else 
            return false;
    }
    
        
    //**********RENDERIZAR**********
    public void Render(Main e) {
        if (visible && inVision && !isDrawn) {
            texture.draw(e.batch);
            isDrawn = true;
        }
    }
    
}