package com.mygdx.game.desktop;

import com.badlogic.gdx.*;
import static com.badlogic.gdx.Gdx.gl;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import java.util.ArrayList;

public final class Main implements Screen {
    Player player;
    public final Base Base;
    Renderer renderer;
    
    //Camera
    static SpriteBatch batch;
    static ShapeRenderer shape_renderer;
    static Camera camera;
    
    //Map & Entities
    TiledMapTileLayer[] floors;
    ArrayList<Entity> entity;
    

    public Main(Base base) {
        Base = base;
        player = Player.getInstance();
        renderer = new Renderer();
        
        //Batch & Camera
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape_renderer = new ShapeRenderer();
        shape_renderer.setProjectionMatrix(camera.combined);
        batch = new SpriteBatch(); 
        batch.setProjectionMatrix(camera.projection);

        //Map
        entity = new ArrayList<>();
        floors = new TiledMapTileLayer[3];
        TiledMap tmp_map = new TmxMapLoader().load("Mapa.tmx");
        for (int i = 0; i < tmp_map.getLayers().getCount(); i++) {
            floors[i] = (TiledMapTileLayer) tmp_map.getLayers().get(i);
        }
        
        initEntities();
    }
    
    
    /**
     * Loads the entities of the map in the entity list
     */
    private void initEntities() {
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


    @Override
    public void render(float f) {
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        entity.forEach((aux) -> aux.update());
        
        batch.begin();
        renderer.render(batch, floors, entity);
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
