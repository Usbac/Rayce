package com.mygdx.game.desktop;
import com.badlogic.gdx.*;
import static com.badlogic.gdx.Gdx.gl;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Comparator.comparing;
import org.lwjgl.util.vector.Vector2f;

public class Main implements Screen {
    Player player;
    public final Base Base;
    //Constantes
    static final float TWO_PI = (float) (Math.PI*2f);
    static final float PI = (float) Math.PI;
    static final float HALF_PI = (float) (Math.PI/2f);
    static final float THREEHALF_PI = (float) ((3f*Math.PI)/2f);
    //Variables Render & Raycasting
    Vector2f drawPosition, rayIntensity;
    int width, height, centerX, centerY;
    float delta, FOV; 
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    Camera camera;
    //Variables Texturas
    Sprite floor, ceil;
    Color floorColor, ceilColor;
    Texture[] screen;
    float[] distances,
            texturePosition;
    //MAPA
    TiledMap mapaTemp;
    TiledMapTileLayer[] floors;
    //Otros
    ArrayList<Entity> entity;
    
    
    //**********CONSTRUCTOR**********
    public Main(Base base) {
        //Estados & Personaje
        Base = base;
        player = Player.getInstance();
        //Batch & Camara
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch = new SpriteBatch(); 
        batch.setProjectionMatrix(camera.projection);
        //Variables pantalla
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        centerX = width/2;
        centerY = height/2;
        //Variables RayCasting        
        drawPosition = new Vector2f();
        rayIntensity = new Vector2f();
        //Arreglos de Impresion
        screen = new Texture[width];
        distances = new float[width];
        texturePosition = new float[width];
        //Piso
        floor = new Sprite(new Texture("degradado.png"));
        floor.setSize(width, centerY);
        floor.setPosition(-centerX, -centerY);
        floorColor = new Color(Color.valueOf("311a12"));
        floor.setColor(floorColor);
        //Techo
        ceil = new Sprite(new Texture("degradado.png"));
        ceil.setSize(width, centerY);
        ceil.setPosition(-centerX, 0);
        ceil.flip(false, true);
        ceilColor = new Color(Color.valueOf("7f7f7f"));
        ceil.setColor(ceilColor);
        //MAPA
        entity = new ArrayList<>();
        floors = new TiledMapTileLayer[3];
        mapaTemp = new TmxMapLoader().load("Mapa.tmx");
        for (int i = 0; i < mapaTemp.getLayers().getCount(); i++) 
            floors[i] = (TiledMapTileLayer) mapaTemp.getLayers().get(i);
        mapEntities();
    }
    
    
    //**********METODO GENERAR ENTIDADES DEL MAPA Y PASARLAS A ARREGLO**********
    public final void mapEntities() {
        for (int x = 0; x < floors[1].getWidth(); x++) 
            for (int y = 0; y < floors[1].getHeight(); y++) {
                //Entidades colisionables
                if (floors[1].getCell(x, y)!=null) 
                    entity.add(new Entity(floors[1].getCell(x, y).getTile().getTextureRegion(), x, y, true));
                //Entidades no colisionables (luces de techo, etc)
                if (floors[2].getCell(x, y)!=null) 
                    entity.add(new Entity(floors[2].getCell(x, y).getTile().getTextureRegion(), x, y, false));
            }
    }

    
    //**********METODO RAY CASTING**********
    public void RayCasting() {
        //Imprimir Piso & Techo
        floor.draw(batch);
        ceil.draw(batch);
        
        //IMPRIMIR PAREDES
        float playerAngle = player.angle-0.5f;
        for (int x = 0; x < width; x++) {
            //Inicializar variables Rayo
            float rayAngle = playerAngle + ((float) x / (float) width);
            if (rayAngle>TWO_PI) rayAngle -= TWO_PI;
            if (rayAngle<0f) rayAngle += TWO_PI;
            
            //Busqueda de Paredes
            float aX, aY, distX, distY;
            boolean horizontal = true;
            boolean collisionH = false, 
                    collisionV = false;
            float tan = (float) (Math.tan(rayAngle)),
                cotan = (float) (1f/tan);
            float rayX = player.position.x,
                  rayY = player.position.y;
            float rayX2 = player.position.x,
                  rayY2 = player.position.y;
            float distH = 0f, 
                  distV = 0f;
            while (true) {
                //>>HORIZONTAL
                if (!collisionH) {
                    distY = (float) Math.abs(rayY - Math.floor(rayY));
                    if (rayAngle<HALF_PI || rayAngle>THREEHALF_PI) distY = 1f-distY;
                    if (distY<0.001f) distY = 1f;
                    aX = (float) (distY * (tan));
                    aY = distY;

                    if (rayAngle<HALF_PI || rayAngle>THREEHALF_PI) {
                        rayY += aY;
                        rayX += aX;
                        if (floors[0].getCell((int) Math.floor(rayX), (int) Math.floor(rayY))!=null) collisionH = true;
                    } else {
                        rayY -= aY;
                        rayX -= aX;
                        if (floors[0].getCell((int) Math.floor(rayX), (int) Math.floor(rayY-1))!=null) collisionH = true;
                    }
                    distH = (float) Math.sqrt((Math.pow(Math.abs(rayX-player.position.x), 2)) + (Math.pow(Math.abs(rayY-player.position.y), 2)));
                    if (collisionV && (distH>=distV)) break;
                }
                
                //>>VERTICAL
                if (!collisionV) {
                    distX = (float) Math.abs(rayX2 - Math.floor(rayX2));
                    if (rayAngle<PI) distX = 1f - distX;
                    if (distX<0.001f) distX = 1f;
                    aY = (float) (distX * (cotan));
                    aX = distX;

                    if (rayAngle<PI) {
                        rayY2 += aY;
                        rayX2 += aX;
                        if (floors[0].getCell((int) Math.floor(rayX2), (int) Math.floor(rayY2))!=null) collisionV = true;
                    } else {
                        rayY2 -= aY;
                        rayX2 -= aX;
                        if (floors[0].getCell((int) Math.floor(rayX2-1), (int) Math.floor(rayY2))!=null) collisionV = true;
                    }
                    distV = (float) Math.sqrt((Math.pow(Math.abs(rayX2-player.position.x), 2)) + (Math.pow(Math.abs(rayY2-player.position.y), 2)));
                    if (collisionH && (distH<=distV)) break;
                }
                if (collisionV && collisionH) break;
            }
            //Establecer Distancia mas cercana entre Interseccion Horizontal y Vertical
            float distance = (distV<distH)? distV:distH;
            if (distV<distH) {
                horizontal = false;
                rayX = rayX2;
                rayY = rayY2;
            }
            
            //Arreglar efecto FishEye & Establecer lado Pared
            distance = (float) (distance * Math.cos(player.angle-rayAngle));
            int wallSide = (rayX-Math.floor(rayX)<0.01f)? 1:0;

            //Variables de Impresion
            float textureX = (float) (rayX-Math.floor(rayX)), 
                  textureY = (float) (rayY-Math.floor(rayY));
            float comienzo = 0;
            
            if (wallSide==1) comienzo = (textureX>0.5f)? textureY : 1-textureY;
            if (wallSide==0) comienzo = (textureY>0.5f)? 1-textureX : textureX;
            
            texturePosition[x] = comienzo;
            distances[x] = (float) distance;
            
            //Ajuste por Posicion en Eje negativo
            if (horizontal && (rayAngle>HALF_PI && rayAngle<THREEHALF_PI)) rayY --;
            if (!horizontal && rayAngle>PI) rayX --;

            if (floors[0].getCell((int)rayX, (int)rayY)!=null)
                screen[x] = floors[0].getCell((int)rayX, (int)rayY).getTile().getTextureRegion().getTexture();
        }
        generalRender();
    }
    
    
    //**********METODO DISTANCIA/IMPRESION DE RAYOS Y ENTIDADES**********
    public void generalRender() {
        //Ordenar Entidades de Mas Lejos a Mas Cerca
        Collections.sort(entity, comparing(Entity::getDistance).reversed());
        //Ciclo Impresion
        for (int x = 0; x < width; x++) {
            //Buscar mas distante
            int moreDistant = 0;
            for (int i = 0; i < width; i++) 
                if (distances[i]>=distances[moreDistant])
                    moreDistant = i;
            //Imprimir Entidades antes de Imprimir pared mas cercana
            for (Entity aux: entity) 
                if (aux.distance>distances[moreDistant] || x==(width-1)) 
                    aux.Render(this);
            //Imprimir Rayo
            rayRender(moreDistant);
            distances[moreDistant] = 0;
        }
    }
    
    
    //**********METODO IMPRESION DE RAYOS**********
    public void rayRender(int i) {
        //Sombra por distancia
        float luminosity = 15f/distances[i];
        if (luminosity>1) luminosity = 1;
        batch.setColor(luminosity, luminosity, luminosity, 1);

        //Variables
        int floorPosition = (int) (centerY - height/distances[i]);
        int wallHeight = height-(floorPosition*2);
        drawPosition.x = i-centerX;
        drawPosition.y = floorPosition-centerY;

        //Impresion
        if (screen[i]!=null)
            batch.draw(screen[i], drawPosition.x, drawPosition.y, 1, wallHeight, texturePosition[i], 1, texturePosition[i]-0.01f, 0);
    }
    

    //********** R E N D E R **********
    @Override
    public void render(float f) {
        delta = f;
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for (Entity aux: entity) aux.update(this);
        
        batch.begin();
        RayCasting();
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
