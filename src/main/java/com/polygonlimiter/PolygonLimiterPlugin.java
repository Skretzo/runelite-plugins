package com.polygonlimiter;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.Model;
import net.runelite.api.Renderable;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Polygon Limiter",
	description = "Removes objects with too many polygons to improve performance",
	tags = {"performance", "polygons", "vertex", "vertices"}
)
public class PolygonLimiterPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PolygonLimiterConfig config;

	@Provides
	PolygonLimiterConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PolygonLimiterConfig.class);
	}

	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::hide);
		}
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			if (GameState.LOGGED_IN.equals(client.getGameState()))
			{
				// Forces the game to reset the removed/hidden models
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (GameState.LOGGED_IN.equals(gameStateChanged.getGameState()))
		{
			hide();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject != null)
		{
			Renderable renderable = gameObject == null ? null : gameObject.getRenderable();
			if (renderable != null)
			{
				Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
				if (model != null && model.getVerticesCount() > config.gameObjectLimit())
				{
					Scene scene = client.getTopLevelWorldView().getScene();
					scene.removeGameObject(gameObject);
				}
			}
		}
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		GroundObject groundObject = event.getGroundObject();
		if (groundObject != null)
		{
			Renderable renderable = groundObject.getRenderable();
			if (renderable != null)
			{
				Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
				if (model != null && model.getVerticesCount() > config.groundObjectLimit())
				{
					Tile tile = event.getTile();
					if (tile != null)
					{
						tile.setGroundObject(null);
					}
				}
			}
		}
	}

	private void hide()
	{
		Scene scene = client.getTopLevelWorldView().getScene();
		for (int z = 0; z < Constants.MAX_Z; ++z)
		{
			Tile[][] tiles = scene.getTiles()[z];
			for (int x = 0; x < Constants.SCENE_SIZE; ++x)
			{
				for (int y = 0; y < Constants.SCENE_SIZE; ++y)
				{
					Tile tile = tiles[x][y];
					if (tile == null)
					{
						continue;
					}

					if (config.removeTiles())
					{
						scene.removeTile(tile);
						continue;
					}

					for (GameObject gameObject : tile.getGameObjects())
					{
						Renderable renderable = gameObject == null ? null : gameObject.getRenderable();
						if (renderable != null)
						{
							Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
							if (model != null && model.getVerticesCount() > config.gameObjectLimit())
							{
								scene.removeGameObject(gameObject);
							}
						}
					}

					GroundObject groundObject = tile.getGroundObject();
					if (groundObject != null)
					{
						Renderable renderable = groundObject.getRenderable();
						if (renderable != null)
						{
							Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
							if (model != null && model.getVerticesCount() > config.groundObjectLimit())
							{
								tile.setGroundObject(null);
							}
						}
					}
				}
			}
		}
	}
}
