package com.snakemanmode;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.util.List;
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

public class SnakemanModeSceneOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private final Client client;
	private final SnakemanModeConfig config;
	private final SnakemanModePlugin plugin;

	@Inject
	SnakemanModeSceneOverlay(Client client, SnakemanModeConfig config, SnakemanModePlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<SnakemanModeChunk> chunks = plugin.getChunks();

		if (chunks.isEmpty())
		{
			return null;
		}

		final boolean fillUnlocked = config.unlockedFillColour().getAlpha() > 0;
		final boolean fillLocked = config.drawLockedArea() && config.lockedFillColour().getAlpha() > 0;
		final boolean fillFruitChunk = config.fruitChunkFillColour().getAlpha() > 0;
		if (fillUnlocked || fillLocked || fillFruitChunk)
		{
			Tile[][] tiles = client.getScene().getTiles()[client.getPlane()];

			for (Tile[] tileRow : tiles)
			{
				for (Tile tile : tileRow)
				{
					if (tile == null || tile.getLocalLocation() == null)
					{
						continue;
					}
					Polygon tilePolygon = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
					if (tilePolygon == null)
					{
						continue;
					}

					SnakemanModeChunk chunk = new SnakemanModeChunk(client, tile.getWorldLocation());
					final boolean isUnlocked = chunks.contains(chunk);
					final boolean isFruitChunk = chunk.equals(plugin.getFruitChunk());

					if (!isUnlocked && plugin.isUnlockedChunk(tile.getWorldLocation(), true))
					{
						continue;
					}

					if (fillUnlocked && isUnlocked)
					{
						graphics.setColor(config.unlockedFillColour());
						graphics.fill(tilePolygon);
					}
					else if (fillLocked && !isUnlocked && !isFruitChunk)
					{
						graphics.setColor(config.lockedFillColour());
						graphics.fill(tilePolygon);
					}
					else if (fillFruitChunk && isFruitChunk)
					{
						graphics.setColor(config.fruitChunkFillColour());
						graphics.fill(tilePolygon);
					}
				}
			}
		}

		graphics.setStroke(new BasicStroke((float) config.chunkBorderWidth()));

		for (SnakemanModeChunk chunk : chunks)
		{
			List<SnakemanModeChunk> neighbourChunks = chunk.getNeighbourChunks(client);
			boolean[] neighbours = new boolean[]
			{
				chunks.contains(neighbourChunks.get(6)), chunks.contains(neighbourChunks.get(4)),
				chunks.contains(neighbourChunks.get(1)), chunks.contains(neighbourChunks.get(3))
			};
			drawChunk(graphics, chunk.getBottomLeft(), chunk.getSize(),
				config.unlockedBorderColour(), neighbours);
		}

		SnakemanModeChunk fruitChunk = plugin.getFruitChunk();
		if (fruitChunk != null)
		{
			List<SnakemanModeChunk> neighbourChunks = fruitChunk.getNeighbourChunks(client);
			boolean[] neighbours = new boolean[]
			{
				chunks.contains(neighbourChunks.get(6)), chunks.contains(neighbourChunks.get(4)),
				chunks.contains(neighbourChunks.get(1)), chunks.contains(neighbourChunks.get(3))
			};
			drawChunk(graphics, fruitChunk.getBottomLeft(), fruitChunk.getSize(),
				config.fruitChunkBorderColour(), neighbours);
		}

		return null;
	}

	private void drawChunk(Graphics2D graphics, WorldPoint bottomLeft, int diameter, Color borderColour, boolean[] neighbours)
	{
		if (client.getPlane() != bottomLeft.getPlane() || client.getLocalPlayer() == null)
		{
			return;
		}

		GeneralPath path = new GeneralPath();

		int startX = bottomLeft.getX();
		int startY = bottomLeft.getY();
		final int z = bottomLeft.getPlane();

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		if (client.isInInstancedRegion())
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, playerLocation);
			if (localPoint != null)
			{
				WorldPoint templatePlayerLocation = WorldPoint.fromLocalInstance(client, localPoint);
				startX += playerLocation.getX() - templatePlayerLocation.getX();
				startY += playerLocation.getY() - templatePlayerLocation.getY();
			}
		}

		int x = startX;
		int y = startY;

		final int[] xs = new int[4 * diameter + 1];
		final int[] ys = new int[xs.length];

		for (int i = 0; i < xs.length; i++)
		{
			if (i < diameter)
			{
				xs[0 * diameter + i] = startX + i;
				xs[1 * diameter + i] = startX + diameter;
				xs[2 * diameter + i] = startX + diameter - i;
				xs[3 * diameter + i] = startX;
				ys[0 * diameter + i] = startY;
				ys[1 * diameter + i] = startY + i;
				ys[2 * diameter + i] = startY + diameter;
				ys[3 * diameter + i] = startY + diameter - i;
			}
			else if (i == diameter)
			{
				xs[xs.length - 1] = xs[0];
				ys[ys.length - 1] = ys[0];
			}

			boolean hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}

			x = xs[i];
			y = ys[i];

			if (config.drawOutlineOnly())
			{
				int side = (i - 1) / diameter;
				if ((side == 0 && neighbours[0]) || (side == 1 && neighbours[1]) ||
					(side == 2 && neighbours[2]) || (side == 3 && neighbours[3]))
				{
					continue;
				}
			}

			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}

		graphics.setColor(borderColour);
		graphics.draw(path);
	}

	private boolean moveTo(GeneralPath path, int x, int y, int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.moveTo(point.getX(), point.getY());
			return true;
		}
		return false;
	}

	private void lineTo(GeneralPath path, int x, int y, int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.lineTo(point.getX(), point.getY());
		}
	}

	private Point XYToPoint(int x, int y, int z)
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
