package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();

		// *********TERRAIN TEXTURE STUFF***********
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightMap");

		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);

		// *****************************************

		TexturedModel tree = new TexturedModel(OBJLoader.loadObjModel("tree", loader), new ModelTexture(loader.loadTexture("tree")));
		TexturedModel grass = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		TexturedModel flower = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), new ModelTexture(loader.loadTexture("flower")));

		ModelTexture fernTexture = new ModelTexture(loader.loadTexture("fern"));
		fernTexture.setNumberOfRows(2);
		TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTexture);

		TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel("lowPolyTree", loader), new ModelTexture(loader.loadTexture("lowPolyTree")));
		TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));

		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);
		flower.getTexture().setHasTransparency(true);
		flower.getTexture().setUseFakeLighting(true);
		fern.getTexture().setHasTransparency(true);

		List<Entity> entities = new ArrayList<Entity>();
		Random random = new Random();
		for (int i = 0; i < 500; i++) {
			if (i % 7 == 0) {
				float x = random.nextFloat() * 800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));

				x = random.nextFloat() * 800 - 400;
				z = random.nextFloat() * -600;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(grass, new Vector3f(x, y, z), 0, 0, 0, 1.8f));

				x = random.nextFloat() * 800 - 400;
				z = random.nextFloat() * -600;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(flower, new Vector3f(x, y, z), 0, 0, 0, 2.3f));
			}

			if (i % 3 == 0) {
				float x = random.nextFloat() * 800 - 400;
				float z = random.nextFloat() * -600;
				float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(bobble, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, random.nextFloat() * 0.1f + 0.6f));

				x = random.nextFloat() * 800 - 400;
				z = random.nextFloat() * -600;
				y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(tree, new Vector3f(x, y, z), 0, 0, 0, random.nextFloat() * 1 + 4));
			}

		}

		List<Light> lights = new ArrayList<Light>();
		lights.add(new Light(new Vector3f(400, 1000, -7000), new Vector3f(1.0f, 1.0f, 1.0f)));
		lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(5, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 0), new Vector3f(1, 0.01f, 0.002f)));

		entities.add(new Entity(lamp, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
		entities.add(new Entity(lamp, new Vector3f(370, 4.2f, -300), 0, 0, 0, 1));
		entities.add(new Entity(lamp, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1));

		MasterRenderer renderer = new MasterRenderer(loader);

		RawModel playerModel = OBJLoader.loadObjModel("player", loader);
		TexturedModel playerTexturedModel = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));

		Player player = new Player(playerTexturedModel, new Vector3f(153, 5, -274), 0, 100, 0, 0.6f);
		entities.add(player);
		Camera camera = new Camera(player);

		List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
		GuiTexture health = new GuiTexture(loader.loadTexture("gui/health"), new Vector2f(-0.74f, 0.925f), new Vector2f(0.25f, 0.25f));
		guiTextures.add(health);

		GuiRenderer guiRenderer = new GuiRenderer(loader);

		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

		Entity lampEntity = new Entity(lamp, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1);
		entities.add(lampEntity);

		Light light = new Light(new Vector3f(293, 7, -305), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f));
		lights.add(light);

		// *********Water Renderer Set-up*********

		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix());
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(75, -75, 0);
		waters.add(water);

		WaterFrameBuffers buffers = new WaterFrameBuffers();

		while (!Display.isCloseRequested()) {
			player.move(terrain);
			camera.move();
			picker.update();

			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

			// render reflection texture
			buffers.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight()));
			camera.getPosition().y += distance;
			camera.invertPitch();

			// render refraction texture
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));

			// render to screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));
			waterRenderer.render(waters, camera);
			guiRenderer.render(guiTextures);

			DisplayManager.updateDisplay();
		}

		buffers.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}
}