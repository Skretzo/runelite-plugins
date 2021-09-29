package com.invalidmovement;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.UNIT;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class InvalidMovementMinimapOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 20;
	private static final int TILE_SIZE = 4;

	private final Client client;
	private final InvalidMovementConfig config;

	@Inject
	InvalidMovementMinimapOverlay(Client client, InvalidMovementConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showMinimap())
		{
			renderMinimap(graphics);
		}
		return null;
	}

	private void renderMinimap(Graphics2D graphics)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

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

				if (playerLocation.distanceTo(worldPoint) >= MAX_DRAW_DISTANCE)
				{
					continue;
				}

				final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
				if (localPoint == null)
				{
					continue;
				}

				final Point posOnMinimap = Perspective.localToMinimap(client, localPoint);
				if (posOnMinimap == null)
				{
					continue;
				}

				final int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

				final Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

				Area minimapClipArea = getMinimapClipArea();
				if (minimapClipArea == null)
				{
					return;
				}
				graphics.setClip(minimapClipArea);

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR))
				{
					drawSquare(graphics, posOnMinimap, config.colourFloor());
				}

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT))
				{
					drawSquare(graphics, posOnMinimap, config.colourObject());
				}

				if (tile.getWallObject() != null)
				{
					final double angle = client.getMapAngle() * UNIT;
					final GeneralPath path = new GeneralPath();

					graphics.setColor(config.colourWall());
					graphics.rotate(angle, posOnMinimap.getX(), posOnMinimap.getY());

					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(path, posOnMinimap, 0, 1, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(path, posOnMinimap, 0, 0, 0, 1);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(path, posOnMinimap, 0, 0, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(path, posOnMinimap, 1, 0, 0, 1);
					}

					graphics.draw(path);
					graphics.rotate(-angle, posOnMinimap.getX(), posOnMinimap.getY());
				}
			}
		}
	}

	private void drawSquare(Graphics2D graphics, Point center, Color color)
	{
		final int x = center.getX();
		final int y = center.getY();
		final double angle = client.getMapAngle() * UNIT;

		final int width = TILE_SIZE;
		final int height = TILE_SIZE;

		final int a = (width % 2 == 0) ? 1 : 0;
		final int b = (height % 2 == 0)? 1 : 2;

		graphics.setColor(color);
		graphics.rotate(angle, x, y);
		graphics.fillRect(x - width / 2 + a, y - height / 2 - b, width, height);
		graphics.rotate(-angle , x, y);
	}

	private void drawWall(GeneralPath path, Point center, int dx1, int dy1, int dx2, int dy2)
	{
		final int centerX = center.getX();
		final int centerY = center.getY();

		final int width = TILE_SIZE - 1;
		final int height = TILE_SIZE - 1;

		int x = centerX - width / 2;
		int y = centerY - height;

		x += dx1 * width;
		y += dy1 * height;

		path.moveTo(x, y);

		x += dx2 * width;
		y += dy2 * height;

		path.lineTo(x, y);
	}

	private Area getMinimapClipArea()
	{
		final Widget resizeableDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
		final Widget resizeableStonesDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
		final Widget fixedDrawArea = client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);

		final Widget minimapDrawArea = client.isResized() ?
			(resizeableDrawArea == null ? resizeableStonesDrawArea : resizeableDrawArea) : fixedDrawArea;

		if (minimapDrawArea == null || minimapDrawArea.isHidden())
		{
			return null;
		}

		Rectangle bounds = minimapDrawArea.getBounds();
		Ellipse2D ellipse = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

		return new Area(ellipse);
	}
}
