package com.invalidmovement;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class InvalidMovementSceneOverlay extends Overlay
{
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;
	private static final int MAX_DRAW_DISTANCE = 32;

	private final Client client;
	private final InvalidMovementConfig config;

	@Inject
	public InvalidMovementSceneOverlay(Client client, InvalidMovementConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showScene())
		{
			renderScene(graphics);
		}
		return null;
	}

	private void renderScene(Graphics2D graphics)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		final int playerX = playerLocation.getSceneX();
		final int playerY = playerLocation.getSceneY();
		final int radius = config.radiusScene() < 0 ? Integer.MAX_VALUE / 2 : config.radiusScene();

		if (client.getCollisionMaps() == null)
		{
			return;
		}

		final BasicStroke borderStroke = new BasicStroke((float) config.wallWidth());

		final int z = client.getPlane();

		final int[][] flags = client.getCollisionMaps()[z].getFlags();

		final Tile[][] tiles = client.getScene().getTiles()[z];

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

				final LocalPoint localPoint = tile.getLocalLocation();
				if (localPoint == null)
				{
					continue;
				}

				final Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
				if (poly == null)
				{
					continue;
				}

				final int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

				final Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

				graphics.setStroke(borderStroke);

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR))
				{
					graphics.setColor(config.colourFloor());
					graphics.fill(poly);
				}

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT))
				{
					graphics.setColor(config.colourObject());
					graphics.fill(poly);
				}

				if (tile.getWallObject() != null)
				{
					final GeneralPath path = new GeneralPath();

					graphics.setColor(config.colourWall());

					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(path, localPoint.getX(), localPoint.getY(), z, LOCAL_TILE_SIZE, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(path, localPoint.getX(), localPoint.getY(), z, 0, LOCAL_TILE_SIZE);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(path, localPoint.getX(), localPoint.getY() + LOCAL_TILE_SIZE, z, LOCAL_TILE_SIZE, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(path, localPoint.getX() + LOCAL_TILE_SIZE, localPoint.getY(), z, 0, LOCAL_TILE_SIZE);
					}

					graphics.draw(path);
				}
			}
		}
	}

	private void drawWall(final GeneralPath path, int x, int y, int z, int dx, int dy)
	{
		final boolean hasFirst = moveTo(path, x, y, z);

		x += dx;
		y += dy;

		if (hasFirst)
		{
			lineTo(path, x, y, z);
		}
	}

	private boolean moveTo(final GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.moveTo(point.getX(), point.getY());
			return true;
		}
		return false;
	}

	private void lineTo(final GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.lineTo(point.getX(), point.getY());
		}
	}

	private Point XYToPoint(final int x, final int y, final int z)
	{
		return Perspective.localToCanvas(
			client,
			new LocalPoint(x - LOCAL_TILE_SIZE / 2, y - LOCAL_TILE_SIZE / 2),
			z);
	}
}
