package com.invalidmovement;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

public class InvalidMovementMapOverlay extends Overlay
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
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(WidgetInfo.WORLD_MAP_VIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showWorldMap() && client.getWidget(WidgetInfo.WORLD_MAP_VIEW) != null)
		{
			renderWorldMap(graphics);
		}

		return null;
	}

	private void renderWorldMap(Graphics2D graphics)
	{
		final Rectangle bounds = client.getWidget(WidgetInfo.WORLD_MAP_VIEW).getBounds();
		if (bounds == null)
		{
			return;
		}
		final Area mapClipArea = getWorldMapClipArea(bounds);

		if (client.getCollisionMaps() == null)
		{
			return;
		}

		final int z = client.getPlane();

		final int[][] flags = client.getCollisionMaps()[z].getFlags();

		final Tile[][] tiles = client.getScene().getTiles()[z];

		for (final Tile[] tileRows : tiles)
		{
			for (final Tile tile : tileRows)
			{
				if (tile == null)
				{
					continue;
				}

				final WorldPoint worldPoint = tile.getWorldLocation();

				final int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

				final Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FULL))
				{
					drawSquare(graphics, worldPoint, config.colour(), mapClipArea);
				}
				else
				{
					if (tile.getWallObject() == null)
					{
						continue;
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(graphics, worldPoint, config.colour(), mapClipArea, 0, 1, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(graphics, worldPoint, config.colour(), mapClipArea, 0, 0, 0, 1);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(graphics, worldPoint, config.colour(), mapClipArea, 0, 0, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(graphics, worldPoint, config.colour(), mapClipArea, 1, 0, 0, 1);
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
		y -= height / 2;

		if (!mapClipArea.contains(x, y) || !mapClipArea.contains(x + width, y + height))
		{
			return;
		}

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
		y -= height / 2;

		if (!mapClipArea.contains(x, y) || !mapClipArea.contains(x + width, y + height))
		{
			return;
		}

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
		final Widget overview = client.getWidget(WidgetInfo.WORLD_MAP_OVERVIEW_MAP);
		final Widget surfaceSelector = client.getWidget(WidgetInfo.WORLD_MAP_SURFACE_SELECTOR);

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
