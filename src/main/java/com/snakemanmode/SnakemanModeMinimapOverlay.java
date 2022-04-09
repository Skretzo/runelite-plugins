package com.snakemanmode;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class SnakemanModeMinimapOverlay extends Overlay
{
	private static final int TILE_SIZE = 4;

	private final Client client;
	private final SnakemanModeConfig config;
	private final SnakemanModePlugin plugin;

	@Inject
	SnakemanModeMinimapOverlay(Client client, SnakemanModeConfig config, SnakemanModePlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics.setClip(plugin.getMinimapClipArea());

		SnakemanModeChunk fruitChunk = plugin.getFruitChunk();
		BufferedImage fruitImageIcon = plugin.getFruitImageIcon();
		if (fruitImageIcon != null && fruitChunk != null)
		{
			Point minimapPoint = worldToMinimap(fruitChunk.getCenter().dx(1).dy(1));
			if (minimapPoint != null)
			{
				Point offsetPoint = new Point(
					minimapPoint.getX() - fruitImageIcon.getWidth() / 2,
					minimapPoint.getY() - fruitImageIcon.getHeight() / 2);
				graphics.drawImage(fruitImageIcon, offsetPoint.getX(), offsetPoint.getY(), null);
			}
		}

		List<SnakemanModeChunk> unlockedChunks = plugin.getChunks();
		List<Area> unlockAreas = getAreas(unlockedChunks);

		List<SnakemanModeChunk> fruitChunks = new ArrayList<>();
		fruitChunks.add(plugin.getFruitChunk());
		List<Area> fruitChunkAreas = getAreas(fruitChunks);

		Area lockedArea = new Area(plugin.getMinimapDrawWidget().getBounds());
		for (WorldArea worldArea : SnakemanModePlugin.getWhitelistedAreas())
		{
			lockedArea.subtract(getArea(worldArea.toWorldPoint(), worldArea.getWidth(), worldArea.getHeight()));
		}
		for (Area area : fruitChunkAreas)
		{
			lockedArea.subtract(area);
		}
		for (Area area : unlockAreas)
		{
			lockedArea.subtract(area);
		}

		graphics.setColor(config.lockedFillColour());
		graphics.fill(lockedArea);

		graphics.setColor(config.fruitChunkFillColour());
		for (Area area : fruitChunkAreas)
		{
			graphics.fill(area);
		}
		graphics.setColor(config.fruitChunkBorderColour());
		for (Area area : fruitChunkAreas)
		{
			graphics.draw(area);
		}

		graphics.setColor(config.unlockedFillColour());
		for (Area area : unlockAreas)
		{
			graphics.fill(area);
		}
		graphics.setColor(config.unlockedBorderColour());
		for (Area area : unlockAreas)
		{
			graphics.draw(area);
		}

		if (config.showChunkNumber() && !config.drawOutlineOnly())
		{
			int idx = 1;
			graphics.setColor(Color.WHITE);
			for (Area area : unlockAreas)
			{
				String s = "" + idx++;
				int x = (int) (area.getBounds().getCenterX() - graphics.getFontMetrics().getStringBounds(s, graphics).getWidth() / 2 + 1);
				int y = (int) (area.getBounds().getCenterY() + graphics.getFontMetrics().getStringBounds(s, graphics).getHeight() / 2);
				graphics.drawString(s, x, y);
			}
		}

		return null;
	}

	private List<Area> getAreas(List<SnakemanModeChunk> chunks)
	{
		List<Area> areas = new ArrayList<>();
		Area outline = new Area();
		for (SnakemanModeChunk chunk : chunks)
		{
			if (client.getPlane() != chunk.getBottomLeft().getPlane())
			{
				continue;
			}
			Area chunkArea = getArea(chunk.getBottomLeft(), chunk.getSize(), chunk.getSize());
			areas.add(chunkArea);
			if (config.drawOutlineOnly())
			{
				outline.add(chunkArea);
			}
		}
		if (config.drawOutlineOnly())
		{
			areas.clear();
			areas.add(outline);
		}
		return areas;
	}

	private Area getArea(WorldPoint bottomLeft, int width, int height)
	{
		final Point nw = worldToMinimap(bottomLeft.dy(height));
		final Point ne = worldToMinimap(bottomLeft.dx(width).dy(height));
		final Point sw = worldToMinimap(bottomLeft);
		final Point se = worldToMinimap(bottomLeft.dx(width));

		if (nw == null || ne == null || sw == null || se == null)
		{
			return new Area();
		}

		Polygon polygon = new Polygon(
			new int[] { sw.getX(), se.getX(), ne.getX(), nw.getX() },
			new int[] { sw.getY(), se.getY(), ne.getY(), nw.getY() },
			4
		);

		return new Area(polygon);
	}

	private Point worldToMinimap(final WorldPoint worldPoint)
	{
		if (client.getLocalPlayer() == null)
		{
			return null;
		}

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		final LocalPoint localLocation = client.getLocalPlayer().getLocalLocation();
		final LocalPoint playerLocalPoint = LocalPoint.fromWorld(client, playerLocation);

		if (playerLocalPoint == null)
		{
			return null;
		}

		final int offsetX = playerLocalPoint.getX() - localLocation.getX();
		final int offsetY = playerLocalPoint.getY() - localLocation.getY();

		final int x = (worldPoint.getX() - playerLocation.getX()) * TILE_SIZE + offsetX / 32 - TILE_SIZE / 2;
		final int y = (worldPoint.getY() - playerLocation.getY()) * TILE_SIZE + offsetY / 32 - TILE_SIZE / 2 + 1;

		final int angle = client.getMapAngle() & 0x7FF;

		final int sin = (int) (65536.0D * Math.sin((double) angle * Perspective.UNIT));
		final int cos = (int) (65536.0D * Math.cos((double) angle * Perspective.UNIT));

		final Widget minimapDrawWidget = plugin.getMinimapDrawWidget();
		if (minimapDrawWidget == null || minimapDrawWidget.isHidden())
		{
			return null;
		}

		final int xx = y * sin + cos * x >> 16;
		final int yy = sin * x - y * cos >> 16;

		final Point loc = minimapDrawWidget.getCanvasLocation();
		final int minimapX = loc.getX() + xx + minimapDrawWidget.getWidth() / 2;
		final int minimapY = loc.getY() + yy + minimapDrawWidget.getHeight() / 2;

		return new Point(minimapX, minimapY);
	}
}
