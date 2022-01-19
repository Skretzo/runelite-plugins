package pathfinding;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.RenderOverview;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import pathfinding.pathfinder.Pathfinder;

@PluginDescriptor(
	name = "Shortest Path Experimental",
	description = "Draws the shortest path on foot between two selected tiles.<br>" +
		"Right click on the world map or shift right click in the game scene to create a path.",
	tags = {"pathfinder", "pathfinding", "collision"}
)
public class PathfindingPlugin extends Plugin
{
	public static final File COLLISION_MAP_DIR = new File(RuneLite.RUNELITE_DIR, "collision-map");
	public static final String COLLISION_MAP_FILE_EXTENSION = ".bin";
	public static final String COLLISION_MAP_FILE_DELIMITER = "_";
	public static final int FLAG_COUNT = 10; // (unblocked + blocked + 8 cardinal directions)
	public static final int FLAG_MULTIPLIER = 2; // x2 walls per tile
	public static final int NORTH_WEST = 1;
	public static final int NORTH = 2;
	public static final int NORTH_EAST = 3;
	public static final int WEST = 4;
	public static final int BLOCKED = 5;
	public static final int EAST = 6;
	public static final int SOUTH_WEST = 7;
	public static final int SOUTH = 8;
	public static final int SOUTH_EAST = 9;
	public static final int WALL_1 = 0;
	public static final int WALL_2 = 1;
	public static final int[] WALLS = new int[] { WALL_1, WALL_2 };
	public static final int[] WALL_INFO = new int[] { 1, 2, 4, 8, 16, 32, 64, 128 }; // WallObject.getOrientationA()
	public static final int[] FLAGS = new int[] { WEST, NORTH, EAST, SOUTH, NORTH_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST };

	private static final String CLEAR = "Clear";
	private static final String PATH = ColorUtil.wrapWithColorTag("Path", JagexColors.MENU_TARGET);
	private static final String SET = "Set";
	private static final String START = ColorUtil.wrapWithColorTag("Start", JagexColors.MENU_TARGET);
	private static final String TARGET = ColorUtil.wrapWithColorTag("Target", JagexColors.MENU_TARGET);
	private static final String WALK_HERE = "Walk here";
	private static final BufferedImage MARKER_IMAGE = ImageUtil.loadImageResource(PathfindingPlugin.class, "marker.png");

	@Inject
	public Client client;

	@Inject
	public PathfindingConfig config;

	@Inject
	public InfoOverlay infoOverlay; // todo: remove, only for testing

	@Inject
	public OverlayManager overlayManager;

	@Inject
	public PathTileOverlay pathOverlay;

	@Inject
	public PathMinimapOverlay pathMinimapOverlay;

	@Inject
	public PathMapOverlay pathMapOverlay;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Getter
	private Pathfinder pathfinder;
	private Point lastMenuOpenedPoint;
	private WorldMapPoint marker;

	@Provides
	PathfindingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PathfindingConfig.class);
	}

	@Override
	protected void startUp()
	{
		executor.submit(() ->
		{
			pathfinder = new Pathfinder(Util.loadCollisionMap(), Util.loadTransports(), client, config);
		});
		overlayManager.add(pathOverlay);
		overlayManager.add(pathMinimapOverlay);
		overlayManager.add(pathMapOverlay);
		overlayManager.add(infoOverlay); // todo: remove, only for testing
	}

	@Override
	protected void shutDown()
	{
		if (pathfinder != null)
		{
			pathfinder.clear();
		}
		toggleMarker();
		pathfinder = null;

		overlayManager.remove(pathOverlay);
		overlayManager.remove(pathMinimapOverlay);
		overlayManager.remove(pathMapOverlay);
		overlayManager.remove(infoOverlay); // todo: remove, only for testing
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!GameState.LOGGED_IN.equals(event.getGameState()) || pathfinder == null)
		{
			return;
		}

		pathfinder.addCollisionMap();
		pathfinder.start();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (pathfinder != null)
		{
			pathfinder.updatePath();
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		lastMenuOpenedPoint = client.getMouseCanvasPosition();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (pathfinder == null)
		{
			return;
		}

		if (client.isKeyPressed(KeyCode.KC_SHIFT) && event.getOption().equals(WALK_HERE) && event.getTarget().isEmpty())
		{
			addMenuEntry(event, SET, TARGET, 1);
			if (pathfinder.getTarget() != null)
			{
				addMenuEntry(event, SET, START, 1);
			}
			WorldPoint selectedTile = getSelectedWorldPoint();
			if (pathfinder.getPath() != null || pathfinder.getCurrentPath() != null)
			{
				for (WorldPoint tile : (pathfinder.getPath() == null ? pathfinder.getCurrentPath() : pathfinder.getPath()))
				{
					if (tile.equals(selectedTile))
					{
						addMenuEntry(event, CLEAR, PATH, 1);
						break;
					}
				}
			}
		}

		final Widget map = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);

		if (map == null)
		{
			return;
		}

		if (map.getBounds().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
		{
			addMenuEntry(event, SET, TARGET, 0);
			if (pathfinder.getTarget() != null)
			{
				addMenuEntry(event, SET, START, 0);
				addMenuEntry(event, CLEAR, PATH, 0);
			}
		}
	}

	private void addMenuEntry(MenuEntryAdded event, String option, String target, int position)
	{
		for (MenuEntry entry : client.getMenuEntries())
		{
			if (entry.getOption().equals(option) && entry.getTarget().equals(target))
			{
				return;
			}
		}

		client.createMenuEntry(position)
			.setOption(option)
			.setTarget(target)
			.setParam0(event.getActionParam0())
			.setParam1(event.getActionParam1())
			.setIdentifier(event.getIdentifier())
			.setType(MenuAction.RUNELITE)
			.onClick(this::adjustPath);
	}

	private void adjustPath(MenuEntry entry)
	{
		if (entry.getOption().equals(SET) && entry.getTarget().equals(TARGET))
		{
			pathfinder.setTarget(getSelectedWorldPoint());
			pathfinder.start();
		}

		if (entry.getOption().equals(SET) && entry.getTarget().equals(START))
		{
			pathfinder.setStart(getSelectedWorldPoint());
			pathfinder.start();
		}

		if (entry.getOption().equals(CLEAR) && entry.getTarget().equals(PATH))
		{
			pathfinder.clear();
		}

		toggleMarker();
	}

	private WorldPoint getSelectedWorldPoint()
	{
		if (client.getWidget(WidgetInfo.WORLD_MAP_VIEW) == null)
		{
			if (client.getSelectedSceneTile() != null)
			{
				return client.getSelectedSceneTile().getWorldLocation();
			}
		}
		else
		{
			return calculateMapPoint(client.isMenuOpen() ? lastMenuOpenedPoint : client.getMouseCanvasPosition());
		}
		return null;
	}

	private WorldPoint calculateMapPoint(Point point)
	{
		RenderOverview renderOverview = client.getRenderOverview();
		float zoom = renderOverview.getWorldMapZoom();
		final WorldPoint mapPoint = new WorldPoint(
				renderOverview.getWorldMapPosition().getX(),
				renderOverview.getWorldMapPosition().getY(),
				0);
		final Point middle = worldMapOverlay.mapWorldPointToGraphicsPoint(mapPoint);

		if (point == null || middle == null)
		{
			return null;
		}

		final int dx = (int) ((point.getX() - middle.getX()) / zoom);
		final int dy = (int) ((-(point.getY() - middle.getY())) / zoom);

		return mapPoint.dx(dx).dy(dy);
	}

	private void toggleMarker()
	{
		if (pathfinder == null || pathfinder.getTarget() == null)
		{
			worldMapPointManager.remove(marker);
			marker = null;
		}
		else
		{
			worldMapPointManager.removeIf(x -> x == marker);
			marker = new WorldMapPoint(pathfinder.getTarget(), MARKER_IMAGE);
			marker.setName(TARGET);
			marker.setTarget(marker.getWorldPoint());
			marker.setJumpOnClick(true);
			worldMapPointManager.add(marker);
		}
	}
}
