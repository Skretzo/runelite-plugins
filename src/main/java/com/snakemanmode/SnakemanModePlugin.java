package com.snakemanmode;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Model;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Snakeman Mode",
	description = "The snake game with chunks. Find fruits to grow bigger, or get stuck",
	tags = {"snake", "game", "mode", "chunk", "fruit"}
)
public class SnakemanModePlugin extends Plugin
{
	public static final String CONFIG_GROUP = "snakemanmode";
	private static final String CONFIG_KEY_FRUIT_CHUNK = "fruit_chunk";
	private static final String CONFIG_KEY_CHUNKS = "chunks";
	private static final String CHUNK = ColorUtil.wrapWithColorTag("Chunk", JagexColors.MENU_TARGET);
	private static final String PATH_FRUITS = "fruits.txt";
	private static final String PATH_FRUIT_IMAGE = "fruit_image.png";
	private static final String UNLOCK = "Unlock";
	private static final String WALK_HERE = "Walk here";
	private static final int FRUIT_MODEL_ID = 30124;
	private static final WorldPoint FIRST_FRUIT_LOCATION = new WorldPoint(3227, 3244, 0);
	private static final WorldPoint LUMBRIDGE_SPAWN_POINT = new WorldPoint(3221, 3219, 0);
	@Getter
	private static final WorldArea[] whitelistedAreas = new WorldArea[]
	{
		new WorldArea(3008, 3008, 128, 128, 0),
		new WorldArea(3136, 3072, 64, 64, 0),
		new WorldArea(3072, 9472, 64, 64, 0),
		new WorldArea(1600, 6016, 192, 192, 0),
		new WorldArea(1664, 12480, 64, 64, 0)
	};

	@Getter(AccessLevel.PACKAGE)
	private final List<SnakemanModeChunk> chunks = new ArrayList<>();
	private final List<SnakemanModeChunk> fruitChunks = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private SnakemanModeChunk fruitChunk;
	private BufferedImage fruitImage;
	private RuneLiteObject fruit;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SnakemanModeConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private SnakemanModeInfoOverlay infoOverlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SnakemanModeSceneOverlay sceneOverlay;

	@Inject
	private SnakemanModeWorldMapOverlay worldMapOverlay;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Provides
	SnakemanModeConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SnakemanModeConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		loadChunks();
		loadFruitChunk();
		loadFruitChunks();

		clientThread.invokeLater(this::showFruit);

		overlayManager.add(infoOverlay);
		overlayManager.add(sceneOverlay);
		overlayManager.add(worldMapOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(infoOverlay);
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(worldMapOverlay);

		worldMapPointManager.removeIf(SnakemanModeWorldMapPoint.class::isInstance);
		clientThread.invokeLater(this::removeFruit);

		chunks.clear();
		fruitChunks.clear();
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (client.isKeyPressed(KeyCode.KC_SHIFT) &&
			event.getOption().equals(WALK_HERE) &&
			event.getTarget().isEmpty() &&
			getAvailableUnlocks() >= 1 &&
			isNearbyChunk() &&
			!isUnlockedChunk())
		{
			client.createMenuEntry(-1)
				.setOption(UNLOCK)
				.setTarget(CHUNK)
				.setParam0(event.getActionParam0())
				.setParam1(event.getActionParam1())
				.setIdentifier(event.getIdentifier())
				.setType(MenuAction.RUNELITE)
				.onClick(this::unlockChunk);
		}
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (GameState.LOGGED_IN.equals(event.getGameState()))
		{
			showFruit();
		}
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		if (client.getLocalPlayer() == null || fruitChunk == null)
		{
			return;
		}

		// Unlock chunks automatically when walking into not yet unlocked chunks with enough xp gained
		SnakemanModeChunk chunk = new SnakemanModeChunk(client, client.getLocalPlayer().getWorldLocation());
		if (getAvailableUnlocks() >= 1 && !chunks.contains(chunk))
		{
			addChunk(chunk);
		}
	}

	@Subscribe
	public void onStatChanged(final StatChanged event)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		// Punish gaining xp outside your unlocked chunks by resetting the xp until the next unlock
		if (client.getOverallExperience() > config.lastUnlockXp() &&
			!chunks.contains(new SnakemanModeChunk(client, client.getLocalPlayer().getWorldLocation())))
		{
			config.lastUnlockXp(client.getOverallExperience());
		}
	}

	@Subscribe
	public void onConfigChanged(final ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		// Toggle the fruit indicator on the world map
		worldMapPointManager.removeIf(SnakemanModeWorldMapPoint.class::isInstance);
		if (config.showFruitIndicator() && fruitChunk != null)
		{
			worldMapPointManager.add(new SnakemanModeWorldMapPoint(fruitChunk.getCenter(), getFruitImage()));
		}
	}

	public long getXpToUnlock()
	{
		long now = client.getOverallExperience();
		long last = config.lastUnlockXp();
		int unlockXp = config.unlockXp();
		return Math.max(unlockXp - (now - last), 0);
	}

	public long getAvailableUnlocks()
	{
		long now = client.getOverallExperience();
		long last = config.lastUnlockXp();
		int unlockXp = config.unlockXp();
		if (unlockXp <= 0)
		{
			return Integer.MAX_VALUE;
		}
		return (now - last) / unlockXp;
	}

	public boolean isUnlockedChunk(WorldPoint worldPoint, boolean includeWhitelistedAreas)
	{
		if (includeWhitelistedAreas)
		{
			for (WorldArea area : whitelistedAreas)
			{
				if (includeWhitelistedAreas = area.distanceTo(worldPoint) == 0)
				{
					break;
				}
			}
		}
		return includeWhitelistedAreas || chunks.contains(new SnakemanModeChunk(client, worldPoint));
	}

	private void loadChunks()
	{
		chunks.clear();
		chunks.addAll(getChunkIds().stream().map(SnakemanModeChunk::new).collect(Collectors.toList()));
		if (chunks.isEmpty())
		{
			chunks.add(new SnakemanModeChunk(client, LUMBRIDGE_SPAWN_POINT));
		}
	}

	private void saveChunks()
	{
		List<Integer> chunkIds = getChunkIds();

		if (chunkIds == null || chunkIds.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY_CHUNKS);
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_CHUNKS, gson.toJson(chunkIds));
	}

	private void loadFruitChunks()
	{
		fruitChunks.clear();

		Scanner scanner = new Scanner(SnakemanModePlugin.class.getResourceAsStream(PATH_FRUITS));
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();

			if (line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}

			String[] parts = line.replace(" ", "").split(",");

			if (parts.length != 3)
			{
				continue;
			}

			final int x = Integer.parseInt(parts[0]);
			final int y = Integer.parseInt(parts[1]);
			final int z = Integer.parseInt(parts[2]);
			fruitChunks.add(new SnakemanModeChunk(client, new WorldPoint(x, y, z)));
		}
	}

	private void loadFruitChunk()
	{
		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_FRUIT_CHUNK);

		if (Strings.isNullOrEmpty(json))
		{
			fruitChunk = new SnakemanModeChunk(client, FIRST_FRUIT_LOCATION);
			saveFruitChunk();
			return;
		}

		fruitChunk = gson.fromJson(json, new TypeToken<SnakemanModeChunk>(){}.getType());
	}

	private void saveFruitChunk()
	{
		if (fruitChunk == null)
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY_FRUIT_CHUNK);
			return;
		}
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_FRUIT_CHUNK, gson.toJson(fruitChunk));
	}

	private void unlockChunk(MenuEntry menuEntry)
	{
		if (client.getSelectedSceneTile() == null)
		{
			return;
		}

		addChunk(new SnakemanModeChunk(client, client.getSelectedSceneTile().getWorldLocation()));
	}

	private void addChunk(SnakemanModeChunk chunk)
	{
		if (chunks.contains(chunk))
		{
			return;
		}

		chunks.add(0, chunk);
		config.lastUnlockXp(client.getOverallExperience());
		if (chunk.equals(fruitChunk))
		{
			rollFruitChunk();
		}
		else
		{
			chunks.remove(chunks.size() - 1);
		}

		saveChunks();
	}

	private void showFruit()
	{
		if (client.getLocalPlayer() == null || fruitChunk == null)
		{
			return;
		}

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		WorldPoint fruitChunkCenter = fruitChunk.getCenter();

		// Display a fruit on the world map
		if (config.showFruitIndicator())
		{
			worldMapPointManager.removeIf(SnakemanModeWorldMapPoint.class::isInstance);
			worldMapPointManager.add(new SnakemanModeWorldMapPoint(fruitChunkCenter, getFruitImage()));
		}

		// Display a fruit in the fruit chunk when the player is nearby
		if (playerLocation.distanceTo(fruitChunkCenter) < Constants.SCENE_SIZE)
		{
			fruit = client.createRuneLiteObject();

			LocalPoint fruitLocation = LocalPoint.fromWorld(client, fruitChunkCenter);
			if (fruitLocation == null)
			{
				return;
			}
			fruitLocation = new LocalPoint(fruitLocation.getX() + 64, fruitLocation.getY() + 64);

			Model model = client.loadModel(FRUIT_MODEL_ID);

			if (model == null)
			{
				final Instant loadTimeOutInstant = Instant.now().plus(Duration.ofSeconds(5));

				clientThread.invoke(() ->
				{
					if (Instant.now().isAfter(loadTimeOutInstant))
					{
						return true;
					}

					Model reloadedModel = client.loadModel(FRUIT_MODEL_ID);

					if (reloadedModel == null)
					{
						return false;
					}

					fruit.setModel(reloadedModel);

					return true;
				});
			}
			else
			{
				fruit.setModel(model);
			}

			fruit.setLocation(fruitLocation, fruitChunkCenter.getPlane());
			fruit.setActive(true);
		}
	}

	private void removeFruit()
	{
		if (fruit != null)
		{
			fruit.setActive(false);
		}
		fruit = null;
		fruitChunk = null;
	}

	private void rollFruitChunk()
	{
		List<Integer> indices = new ArrayList<>(fruitChunks.size());
		for (int i = 0; i < fruitChunks.size(); i++)
		{
			indices.add(i);
		}

		fruitChunk = new SnakemanModeChunk(client, FIRST_FRUIT_LOCATION);
		if (fruit != null)
		{
			fruit.setActive(false);
		}

		Random random = new Random();

		while (!indices.isEmpty())
		{
			int idx = random.nextInt(indices.size());
			SnakemanModeChunk chunk = fruitChunks.get(indices.get(idx));
			if (!chunks.contains(chunk))
			{
				fruitChunk = chunk;
				break;
			}
			indices.remove(idx);
		}

		saveFruitChunk();
		showFruit();
	}

	private boolean isNearbyChunk()
	{
		if (client.getSelectedSceneTile() == null || client.getLocalPlayer() == null)
		{
			return false;
		}

		SnakemanModeChunk selectedChunk = new SnakemanModeChunk(client, client.getSelectedSceneTile().getWorldLocation());
		SnakemanModeChunk playerChunk = new SnakemanModeChunk(client, client.getLocalPlayer().getWorldLocation());

		List<SnakemanModeChunk> nearbyChunks = playerChunk.getNeighbourChunks(client);
		nearbyChunks.add(playerChunk);

		return nearbyChunks.contains(selectedChunk) && chunks.contains(playerChunk);
	}

	private boolean isUnlockedChunk()
	{
		if (client.getSelectedSceneTile() == null)
		{
			return true;
		}
		return isUnlockedChunk(client.getSelectedSceneTile().getWorldLocation(), false);
	}

	private BufferedImage getFruitImage()
	{
		if (fruitImage != null)
		{
			return fruitImage;
		}

		fruitImage = ImageUtil.loadImageResource(getClass(), PATH_FRUIT_IMAGE);

		return fruitImage;
	}

	private List<Integer> getChunkIds()
	{
		if (!chunks.isEmpty())
		{
			return chunks.stream().map(SnakemanModeChunk::getId).collect(Collectors.toList());
		}

		String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_CHUNKS);

		if (Strings.isNullOrEmpty(json))
		{
			return new ArrayList<>();
		}

		return gson.fromJson(json, new TypeToken<List<Integer>>(){}.getType());
	}
}
