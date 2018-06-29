package com.mygdx.game.desktop;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new Base(), config);
        //Configuracion graficos
        config.title="RAYCE - Raycasting Engine";
        config.width = 1280;
        config.height = 720;
        config.fullscreen = false;
        config.resizable = false;
    }
}
