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
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

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
		setPriority(OverlayPriority.LOW);
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

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		if (client.getCollisionMaps() == null)
		{
			return;
		}

		final BasicStroke borderStroke = new BasicStroke((float) config.wallWidth());

		final int z = client.getPlane();

		final int[][] flags = client.getCollisionMaps()[z].getFlags();

		final Tile[][] tiles = client.getScene().getTiles()[z];

		for (Tile[] tileRows : tiles)
		{
			for (Tile tile : tileRows)
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

					final int x = worldPoint.getX();
					final int y = worldPoint.getY();

					graphics.setColor(config.colourWall());

					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(path, playerLocation, x, y, z, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(path, playerLocation, x, y, z, 0, 1);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(path, playerLocation, x, y + 1, z, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(path, playerLocation, x + 1, y, z, 0, 1);
					}

					graphics.draw(path);
				}
			}
		}
	}

	private void drawWall(final GeneralPath path, WorldPoint playerLocation, int x, int y, int z, int dx, int dy)
	{
		final boolean hasFirst = moveTo(path, x, y, z);

		x += dx;
		y += dy;

		if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
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
		LocalPoint localPoint = LocalPoint.fromWorld(client, x, y);

		if (localPoint == null)
		{
			return null;
		}

		return Perspective.localToCanvas(
			client,
			new LocalPoint(localPoint.getX() - LOCAL_TILE_SIZE / 2, localPoint.getY() - LOCAL_TILE_SIZE / 2),
			z);
	}
}
