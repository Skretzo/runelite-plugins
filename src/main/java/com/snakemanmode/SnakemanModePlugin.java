package com.snakemanmode;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
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
	public static final String CONFIG_RESET_KEY = "lastUnlockXp";
	public static final int CONFIG_RESET_VALUE = 0;
	private static final String CONFIG_KEY_FRUIT_CHUNK = "fruit_chunk";
	private static final String CONFIG_KEY_CHUNKS = "chunks";
	private static final String CHUNK = ColorUtil.wrapWithColorTag("Chunk", JagexColors.MENU_TARGET);
	private static final String PATH_FRUIT_IMAGE = "fruit_image.png";
	private static final String PATH_FRUIT_IMAGE_ICON = "fruit_image_icon.png";
	private static final String UNLOCK = "Unlock";
	private static final String WALK_HERE = "Walk here";
	private static final int FRUIT_MODEL_ID = 30124;
	private static final WorldPoint FIRST_FRUIT_LOCATION = new WorldPoint(3227, 3244, 0);
	private static final WorldPoint LUMBRIDGE_SPAWN_POINT = new WorldPoint(3221, 3219, 0);

	@Getter(AccessLevel.PACKAGE)
	private final List<SnakemanModeChunk> chunks = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private SnakemanModeChunk fruitChunk;
	private BufferedImage fruitImage;
	private BufferedImage fruitImageIcon;
	private BufferedImage minimapSpriteFixed;
	private BufferedImage minimapSpriteResizeable;
	private Area minimapClipFixed;
	private Area minimapClipResizeable;
	private RuneLiteObject fruit;
	private long lastOverallExperience;
	private int startTickCount;

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
	private SnakemanModeMinimapOverlay minimapOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SnakemanModeSceneOverlay sceneOverlay;

	@Inject
	private SpriteManager spriteManager;

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

		clientThread.invokeLater(this::showFruit);

		overlayManager.add(infoOverlay);
		overlayManager.add(minimapOverlay);
		overlayManager.add(sceneOverlay);
		overlayManager.add(worldMapOverlay);

		if (lastOverallExperience == 0)
		{
			lastOverallExperience = client.getOverallExperience();
		}

		startTickCount = client.getTickCount();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(infoOverlay);
		overlayManager.remove(minimapOverlay);
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(worldMapOverlay);

		worldMapPointManager.removeIf(SnakemanModeWorldMapPoint.class::isInstance);
		clientThread.invokeLater(this::removeFruit);

		chunks.clear();
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

			if (lastOverallExperience == 0)
			{
				lastOverallExperience = client.getOverallExperience();
			}
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
		if (getAvailableUnlocks() >= 1)
		{
			addChunk(chunk);
		}
	}

	@Subscribe
	public void onStatChanged(final StatChanged event)
	{
		if (client.getLocalPlayer() == null || !GameState.LOGGED_IN.equals(client.getGameState()))
		{
			return;
		}

		Skill skill = event.getSkill();

		long xpGained = client.getOverallExperience() - lastOverallExperience;

		// Do not progress the xp until the next unlock if the skill has been excluded by the user,
		// and only account for xp gained in your unlocked chunks
		if (xpGained > 0 &&
			startTickCount != client.getTickCount() &&
			((Skill.AGILITY.equals(skill) && !config.excludeAgility()) ||
			(Skill.ATTACK.equals(skill) && !config.excludeAttack()) ||
			(Skill.CONSTRUCTION.equals(skill) && !config.excludeConstruction()) ||
			(Skill.COOKING.equals(skill) && !config.excludeCooking()) ||
			(Skill.CRAFTING.equals(skill) && !config.excludeCrafting()) ||
			(Skill.DEFENCE.equals(skill) && !config.excludeDefence()) ||
			(Skill.FARMING.equals(skill) && !config.excludeFarming()) ||
			(Skill.FIREMAKING.equals(skill) && !config.excludeFiremaking()) ||
			(Skill.FISHING.equals(skill) && !config.excludeFishing()) ||
			(Skill.FLETCHING.equals(skill) && !config.excludeFletching()) ||
			(Skill.HERBLORE.equals(skill) && !config.excludeHerblore()) ||
			(Skill.HITPOINTS.equals(skill) && !config.excludeHitpoints()) ||
			(Skill.HUNTER.equals(skill) && !config.excludeHunter()) ||
			(Skill.MAGIC.equals(skill) && !config.excludeMagic()) ||
			(Skill.MINING.equals(skill) && !config.excludeMining()) ||
			(Skill.PRAYER.equals(skill) && !config.excludePrayer()) ||
			(Skill.RANGED.equals(skill) && !config.excludeRanged()) ||
			(Skill.RUNECRAFT.equals(skill) && !config.excludeRunecraft()) ||
			(Skill.SLAYER.equals(skill) && !config.excludeSlayer()) ||
			(Skill.SMITHING.equals(skill) && !config.excludeSmithing()) ||
			(Skill.STRENGTH.equals(skill) && !config.excludeStrength()) ||
			(Skill.THIEVING.equals(skill) && !config.excludeThieving()) ||
			(Skill.WOODCUTTING.equals(skill) && !config.excludeWoodcutting())) &&
			chunks.contains(new SnakemanModeChunk(client, client.getLocalPlayer().getWorldLocation())))
		{
			config.unlockProgress(config.unlockProgress() + xpGained);
		}

		lastOverallExperience = client.getOverallExperience();
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

		// Add additional plugin resetting to the config reset button
		if (CONFIG_RESET_KEY.equals(event.getKey()))
		{
			if (Integer.toString(CONFIG_RESET_VALUE).equals(event.getNewValue()))
			{
				lastOverallExperience = client.getOverallExperience();
				config.unlockProgress(0);
				config.lastUnlockXp(lastOverallExperience);

				// Reset all unlocked chunks and restart in Lumbridge
				chunks.clear();
				saveChunks(null);
				loadChunks();

				// Reset to initial fruit chunk in Lumbridge
				removeFruit();
				saveFruitChunk();
				loadFruitChunk();
				showFruit();
			}
		}
	}

	public long getXpToUnlock()
	{
		int unlockXp = config.unlockXp();
		long unlockProgress = config.unlockProgress();
		return Math.min(Math.max(unlockXp - unlockProgress, 0), config.unlockXp());
	}

	public long getAvailableUnlocks()
	{
		long unlockProgress = config.unlockProgress();
		int unlockXp = config.unlockXp();
		if (unlockXp <= 0)
		{
			return Integer.MAX_VALUE;
		}

		return unlockProgress / unlockXp;
	}

	public BufferedImage getFruitImageIcon()
	{
		if (fruitImageIcon != null)
		{
			return fruitImageIcon;
		}

		fruitImageIcon = ImageUtil.loadImageResource(getClass(), PATH_FRUIT_IMAGE_ICON);

		return  fruitImageIcon;
	}

	public Widget getMinimapDrawWidget()
	{
		if (client.isResized())
		{
			if (client.getVar(Varbits.SIDE_PANELS) == 1)
			{
				return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
			}
			return client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
		}
		return client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
	}

	public Area getMinimapClipArea()
	{
		if (client.isResized())
		{
			if (minimapClipResizeable != null)
			{
				return minimapClipResizeable;
			}
			minimapClipResizeable = new Area(bufferedImageToPolygon(getMinimapSprite()));
			return minimapClipResizeable;
		}
		if (minimapClipFixed != null)
		{
			return minimapClipFixed;
		}
		minimapClipFixed = new Area(bufferedImageToPolygon(getMinimapSprite()));
		return minimapClipFixed;
	}

	public boolean isUnlockedChunk(WorldPoint worldPoint, boolean includeWhitelistedAreas)
	{
		if (includeWhitelistedAreas)
		{
			for (WorldArea area : SnakemanModeAreas.WHITELISTED_AREA)
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
		saveChunks(getChunkIds());
	}

	private void saveChunks(List<Integer> chunkIds)
	{
		if (chunkIds == null || chunkIds.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY_CHUNKS);
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_CHUNKS, gson.toJson(chunkIds));
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
		config.unlockProgress(0);
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
		int lastId = -1;
		if (fruitChunk != null)
		{
			lastId = fruitChunk.getId();
		}

		removeFruit();

		Random random = new Random();

		List<Integer> fruitChunks = config.onlyFreeToPlay() ? SnakemanModeAreas.FREE_TO_PLAY : SnakemanModeAreas.FRUIT_AREA;

		int idx = 0;
		int size = fruitChunks.size();
		while (++idx < Integer.MAX_VALUE)
		{
			int i = random.nextInt(size);

			int id = fruitChunks.get(i);

			if (id == lastId || (config.onlyFreeToPlay() && !SnakemanModeAreas.FRUIT_AREA.contains(id)))
			{
				continue;
			}

			SnakemanModeChunk chunk = new SnakemanModeChunk(id);

			if (!chunks.contains(chunk))
			{
				fruitChunk = chunk;
				break;
			}
		}

		if (fruitChunk == null)
		{
			fruitChunk = new SnakemanModeChunk(client, FIRST_FRUIT_LOCATION);
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

	private BufferedImage getMinimapSprite()
	{
		if (client.isResized())
		{
			if (minimapSpriteResizeable != null)
			{
				return minimapSpriteResizeable;
			}
			minimapSpriteResizeable = spriteManager.getSprite(SpriteID.RESIZEABLE_MODE_MINIMAP_ALPHA_MASK, 0);
			return minimapSpriteResizeable;
		}
		if (minimapSpriteFixed != null)
		{
			return minimapSpriteFixed;
		}
		minimapSpriteFixed = spriteManager.getSprite(SpriteID.FIXED_MODE_MINIMAP_ALPHA_MASK, 0);
		return minimapSpriteFixed;
	}

	private Polygon bufferedImageToPolygon(BufferedImage image)
	{
		Color outsideColour = null;
		Color previousColour;
		final int width = image.getWidth();
		final int height = image.getHeight();
		List<Point> points = new ArrayList<>();
		for (int y = 0; y < height; y++)
		{
			previousColour = outsideColour;
			for (int x = 0; x < width; x++)
			{
				int rgb = image.getRGB(x, y);
				int a = (rgb & 0xff000000) >>> 24;
				int r   = (rgb & 0x00ff0000) >> 16;
				int g = (rgb & 0x0000ff00) >> 8;
				int b  = (rgb & 0x000000ff) >> 0;
				Color colour = new Color(r, g, b, a);
				if (x == 0 && y == 0)
				{
					outsideColour = colour;
					previousColour = colour;
				}
				if (!colour.equals(outsideColour) && previousColour.equals(outsideColour))
				{
					points.add(new Point(x, y));
				}
				if ((colour.equals(outsideColour) || x == (width - 1)) && !previousColour.equals(outsideColour))
				{
					points.add(0, new Point(x, y));
				}
				previousColour = colour;
			}
		}
		int offsetX = 0;
		int offsetY = 0;
		Widget minimapDrawWidget = getMinimapDrawWidget();
		if (minimapDrawWidget != null)
		{
			offsetX = minimapDrawWidget.getBounds().x;
			offsetY = minimapDrawWidget.getBounds().y;
		}
		Polygon polygon = new Polygon();
		for (Point point : points)
		{
			polygon.addPoint(point.x + offsetX, point.y + offsetY);
		}
		return polygon;
	}
}
