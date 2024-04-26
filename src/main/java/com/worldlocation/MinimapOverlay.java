package com.worldlocation;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class MinimapOverlay extends Overlay
{
	private final Client client;
	private final WorldLocationPlugin plugin;
	private final WorldLocationConfig config;

	@Inject
	private MinimapOverlay(Client client, WorldLocationPlugin plugin, WorldLocationConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics.setClip(plugin.getMinimapClipArea());

		if (config.minimapTileLines())
		{
			renderLines(graphics, 1, config.tileLineColour());
		}
		if (config.minimapChunkLines())
		{
			renderLines(graphics, 8, config.chunkLineColour());
		}
		if (config.minimapRegionLines())
		{
			renderLines(graphics, 64, config.regionLineColour());
		}

		return null;
	}

	private void renderLines(Graphics2D graphics, int gridSize, Color lineColour)
	{
		int size = Perspective.SCENE_SIZE;
		int startX = client.getBaseX();
		int startY = client.getBaseY();
		int endX = startX + size;
		int endY = startY + size;

		graphics.setColor(lineColour);

		for (int x = startX; x < endX; x += gridSize)
		{
			x = x / gridSize * gridSize;
			if (x < startX)
			{
				continue;
			}
			Point start = worldToMinimap(x, startY);
			Point end = worldToMinimap(x, startY + size);
			if (start != null && end != null)
			{
				graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
			}
		}
		for (int y = startY; y < endY; y += gridSize)
		{
			y = y / gridSize * gridSize;
			if (y < startY)
			{
				continue;
			}
			Point start = worldToMinimap(startX, y);
			Point end = worldToMinimap(startX + size, y);
			if (start != null && end != null)
			{
				graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
			}
		}
	}

	private Point worldToMinimap(int worldX, int worldY)
	{
		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
		LocalPoint playerLocalPoint = LocalPoint.fromWorld(client, playerLocation);

		if (playerLocalPoint == null)
		{
			return null;
		}

		int offsetX = playerLocalPoint.getX() - localLocation.getX();
		int offsetY = playerLocalPoint.getY() - localLocation.getY();

		int dx = worldX - playerLocation.getX();
		int dy = worldY - playerLocation.getY();

		double tileSize = client.getMinimapZoom();

		int x = (int) (dx * tileSize + offsetX * tileSize / Perspective.LOCAL_TILE_SIZE - tileSize / 2);
		int y = (int) (dy * tileSize + offsetY * tileSize / Perspective.LOCAL_TILE_SIZE - tileSize / 2 + 1);

		int angle = client.getCameraYawTarget() & 0x7FF;

		int sin = Perspective.SINE[angle];
		int cos = Perspective.COSINE[angle];

		Widget minimapDrawWidget = plugin.getMinimapDrawWidget();
		if (minimapDrawWidget == null || minimapDrawWidget.isHidden())
		{
			return null;
		}

		int xx = y * sin + cos * x >> 16;
		int yy = sin * x - y * cos >> 16;

		Point loc = minimapDrawWidget.getCanvasLocation();
		int minimapX = loc.getX() + xx + minimapDrawWidget.getWidth() / 2;
		int minimapY = loc.getY() + yy + minimapDrawWidget.getHeight() / 2;

		return new Point(minimapX, minimapY);
	}
}
