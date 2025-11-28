package com.invalidmovement;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

class InvalidMovementMapOverlay extends Overlay
{
	private final Client client;
	private final InvalidMovementConfig config;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	InvalidMovementMapOverlay(Client client, InvalidMovementConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(ComponentID.WORLD_MAP_MAPVIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showWorldMap() && client.getWidget(ComponentID.WORLD_MAP_MAPVIEW) != null)
		{
			renderWorldMap(graphics);
		}

		return null;
	}

	private void renderWorldMap(Graphics2D graphics)
	{
		final Rectangle bounds = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW).getBounds();
		if (bounds == null)
		{
			return;
		}
		final Area mapClipArea = getWorldMapClipArea(bounds);

		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		final int playerX = playerLocation.getSceneX();
		final int playerY = playerLocation.getSceneY();
		final int radius = config.radiusWorldMap() < 0 ? Integer.MAX_VALUE / 2 : config.radiusWorldMap();

		WorldView worldView = client.getTopLevelWorldView();

		CollisionData[] collisionData = worldView.getCollisionMaps();

		if (collisionData == null)
		{
			return;
		}

		final int z = worldView.getPlane();

		final int[][] flags = collisionData[z].getFlags();

		final Tile[][] tiles = worldView.getScene().getTiles()[z];

		final int startX = Math.max(playerX - radius, 0);
		final int endX = Math.min(playerX + radius, tiles[0].length);
		final int startY = Math.max(playerY - radius, 0);
		final int endY = Math.min(playerY + radius, tiles.length);

		for (int y = startY; y < endY; y++)
		{
			for (int x = startX; x < endX; x++)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}

				final WorldPoint worldPoint = tile.getWorldLocation();

				final int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

				final Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR))
				{
					drawSquare(graphics, worldPoint, config.colourFloor(), mapClipArea);
				}

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT))
				{
					drawSquare(graphics, worldPoint, config.colourObject(), mapClipArea);
				}

				if (tile.getWallObject() != null)
				{
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(graphics, worldPoint, config.colourWall(), mapClipArea, 0, 1, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(graphics, worldPoint, config.colourWall(), mapClipArea, 0, 0, 0, 1);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(graphics, worldPoint, config.colourWall(), mapClipArea, 0, 0, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(graphics, worldPoint, config.colourWall(), mapClipArea, 1, 0, 0, 1);
					}
				}
			}
		}
	}

	private void drawSquare(Graphics2D graphics, WorldPoint point, Color color, Area mapClipArea)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(point);
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(point.dx(1).dy(-1));

		if (start == null || end == null)
		{
			return;
		}

		int x = start.getX();
		int y = start.getY();
		final int width = end.getX() - x;
		final int height = end.getY() - y;
		x -= width / 2;
		if (!mapClipArea.contains(x, y))
		{
			return;
		}
		y -= height / 2;

		graphics.setColor(color);
		graphics.fillRect(x, y, width, height);
	}

	private void drawWall(Graphics2D graphics, WorldPoint point, Color color, Area mapClipArea,
		int dx1, int dy1, int dx2, int dy2)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(point);
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(point.dx(1).dy(-1));

		if (start == null || end == null)
		{
			return;
		}

		int x = start.getX();
		int y = start.getY();
		int width = end.getX() - x - 1;
		int height = end.getY() - y - 1;
		x -= width / 2;
		if (!mapClipArea.contains(x, y))
		{
			return;
		}
		y -= height / 2;

		int a = (width % 2 == 0) ? 1 : 0;
		int b = (height % 2 == 0) ? 1 : 0;

		x += dx1 * width - 1 + a;
		y += dy1 * height - 1 + b;

		width *= dx2;
		height *= dy2;

		graphics.setColor(color);
		graphics.drawLine(x, y, x + width, y + height);
	}

	private Area getWorldMapClipArea(Rectangle baseRectangle)
	{
		final Widget overview = client.getWidget(ComponentID.WORLD_MAP_OVERVIEW_MAP);
		final Widget surfaceSelector = client.getWidget(ComponentID.WORLD_MAP_SURFACE_SELECTOR);

		Area clipArea = new Area(baseRectangle);

		if (overview != null && !overview.isHidden())
		{
			clipArea.subtract(new Area(overview.getBounds()));
		}

		if (surfaceSelector != null && !surfaceSelector.isHidden())
		{
			clipArea.subtract(new Area(surfaceSelector.getBounds()));
		}

		return clipArea;
	}
}
