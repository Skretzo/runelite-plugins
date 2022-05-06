package com.snakemanmode;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

public class SnakemanModeWorldMapOverlay extends Overlay
{
	private final Client client;
	private final SnakemanModeConfig config;
	private final SnakemanModePlugin plugin;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	SnakemanModeWorldMapOverlay(Client client, SnakemanModeConfig config, SnakemanModePlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(WidgetInfo.WORLD_MAP_VIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Widget worldMapView = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
		if (worldMapView == null || worldMapView.isHidden() || worldMapView.getBounds() == null)
		{
			return null;
		}

		Area lockedArea = getWorldMapClipArea(worldMapView.getBounds());

		graphics.setClip(lockedArea);

		List<SnakemanModeChunk> chunks = plugin.getChunks();

		final boolean fillUnlocked = config.unlockedFillColourWorldmap().getAlpha() > 0;
		final boolean fillLocked = config.lockedFillColourWorldmap().getAlpha() > 0;
		final boolean fillFruitChunk = config.fruitChunkFillColourWorldmap().getAlpha() > 0;

		final boolean outlineUnlocked = config.unlockedBorderColourWorldmap().getAlpha() > 0;
		final boolean outlineFruitChunk = config.fruitChunkBorderColourWorldmap().getAlpha() > 0;

		Area chunkArea = new Area();
		for (SnakemanModeChunk chunk : chunks)
		{
			drawChunk(graphics, chunk.getBottomLeft(), chunk.getSize(), chunkArea,
				config.unlockedFillColourWorldmap(), config.unlockedBorderColourWorldmap(), chunks.indexOf(chunk));
		}
		if (config.drawOutlineOnly())
		{
			if (fillUnlocked)
			{
				graphics.setColor(config.unlockedFillColourWorldmap());
				graphics.fill(chunkArea);
			}
			if (outlineUnlocked)
			{
				graphics.setColor(config.unlockedBorderColourWorldmap());
				graphics.draw(chunkArea);
			}
		}

		lockedArea.subtract(chunkArea);
		for (WorldArea worldArea : SnakemanModeAreas.WHITELISTED_AREA)
		{
			lockedArea.subtract(getArea(worldArea.toWorldPoint(), worldArea.getWidth(), worldArea.getHeight()));
		}

		SnakemanModeChunk fruitChunk = plugin.getFruitChunk();
		if (fruitChunk != null)
		{
			Area fruitChunkArea = new Area();
			drawChunk(graphics, fruitChunk.getBottomLeft(), fruitChunk.getSize(), fruitChunkArea,
				config.fruitChunkFillColourWorldmap(), config.fruitChunkBorderColourWorldmap(), -1);
			if (config.drawOutlineOnly())
			{
				if (fillFruitChunk)
				{
					graphics.setColor(config.fruitChunkFillColourWorldmap());
					graphics.fill(fruitChunkArea);
				}
				if (outlineFruitChunk)
				{
					graphics.setColor(config.fruitChunkBorderColourWorldmap());
					graphics.draw(fruitChunkArea);
				}
			}
			lockedArea.subtract(fruitChunkArea);
		}

		if (fillLocked)
		{
			graphics.setColor(config.lockedFillColourWorldmap());
			graphics.fill(lockedArea);
		}

		return null;
	}

	private void drawChunk(Graphics2D graphics, WorldPoint bottomLeft, int diameter, Area outline, Color fillColour, Color borderColour, int idx)
	{
		if (client.getPlane() != bottomLeft.getPlane())
		{
			return;
		}

		boolean drawFill = fillColour.getAlpha() > 0;
		boolean drawOutline = borderColour.getAlpha() > 0;

		Area chunk = getArea(bottomLeft, diameter, diameter);
		if (!config.drawOutlineOnly())
		{
			if (drawFill)
			{
				graphics.setColor(fillColour);
				graphics.fill(chunk);
			}
			if (drawOutline)
			{
				graphics.setColor(borderColour);
				graphics.draw(chunk);
			}
		}
		if (config.showChunkNumber() && idx >= 0)
		{
			String s = "" + (idx + 1);
			int x = (int) (chunk.getBounds().getCenterX() - graphics.getFontMetrics().getStringBounds(s, graphics).getWidth() / 2 + 1);
			int y = (int) (chunk.getBounds().getCenterY() + graphics.getFontMetrics().getStringBounds(s, graphics).getHeight() / 2);
			graphics.setColor(Color.WHITE);
			graphics.drawString(s, x, y);
		}
		outline.add(chunk);
	}

	private Area getArea(WorldPoint bottomLeft, int width, int height)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(bottomLeft.dy(height));
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(bottomLeft.dx(width));

		if (start == null || end == null)
		{
			return new Area();
		}

		final int tileSize = (int) client.getRenderOverview().getWorldMapZoom();

		int x = start.getX();
		int y = start.getY();
		final int rectWidth = end.getX() - x;
		final int rectHeight = end.getY() - y;
		x -= tileSize / 2;
		y += tileSize / 2;

		return new Area(new Rectangle(x, y, rectWidth, rectHeight));
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
