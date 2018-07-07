package com.mygdx.game.desktop;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.lwjgl.util.vector.Vector2f;

public class Player {
    Vector2f position, oldPosition;
    float angle, delta;
    float speedMovement, speedRotation;
    static Player instance;
    
    //*****-----> CONSTRUCTOR <-----*****
    public Player() {
        position = new Vector2f(10, 9);
        oldPosition = new Vector2f(position.x, position.y);
        angle = 0f;
        speedMovement = 5;
        speedRotation = 1.7f;
    }
    
    //*****-----> SINGLETON <-----*****
    public static Player getInstance() {
        return instance = (instance==null)? new Player():instance;
    }
    
    
    //*****-----> MOVIMIENTO <-----*****
    public void movement(Main e) {
        //Variables
        delta = Gdx.graphics.getDeltaTime();
        oldPosition.set(position.x, position.y);

        //Rotacion
        if (Gdx.input.isKeyPressed(Input.Keys.D)) angle += speedRotation*delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) angle -= speedRotation*delta;
        //Ajustar angulo
        if (angle>Main.TWO_PI) angle = 0;
        if (angle<0) angle = Main.TWO_PI;
        
        //Movimiento Normal
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.x += Math.sin(angle) * (delta*speedMovement);
            position.y += Math.cos(angle) * (delta*speedMovement);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.x -= Math.sin(angle) * (delta*speedMovement);
            position.y -= Math.cos(angle) * (delta*speedMovement);
        }
        //Movimiento lateral
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            position.x += Math.sin(angle+Main.HALF_PI) * (delta*(speedMovement/2));
            position.y += Math.cos(angle+Main.HALF_PI) * (delta*(speedMovement/2));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            position.x += Math.sin(angle-Main.HALF_PI) * (delta*(speedMovement/2));
            position.y += Math.cos(angle-Main.HALF_PI) * (delta*(speedMovement/2));
        }
    }
    
    
    //*****-----> COLISION <-----*****
    public void collision(Main e) {
        int actualX = (int) Math.floor(position.x),
            actualY = (int) Math.floor(position.y);
        if (e.floors[0].getCell((int)actualX, (int)actualY)!=null) {
            position.x = oldPosition.x;
            position.y = oldPosition.y;
        }
    }

    
    //*****-----> ACTUALIZAR <-----*****
    public void update(Main e) {
        movement(e);
        collision(e);
    }
}