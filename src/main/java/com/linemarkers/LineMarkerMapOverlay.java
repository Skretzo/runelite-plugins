package com.linemarkers;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

class LineMarkerMapOverlay extends Overlay
{
	private final Client client;
	private final LineMarkerConfig config;
	private final LineMarkerPlugin plugin;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	LineMarkerMapOverlay(Client client, LineMarkerConfig config, LineMarkerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(ComponentID.WORLD_MAP_MAPVIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showWorldMap())
		{
			drawWorldMap(graphics);
		}
		return null;
	}

	private void drawWorldMap(Graphics2D graphics)
	{
		final Widget worldMapView = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
		if (worldMapView == null)
		{
			return;
		}

		final Rectangle bounds = worldMapView.getBounds();
		if (bounds == null)
		{
			return;
		}

		graphics.setClip(getWorldMapClipArea(bounds));

		for (final LineGroup group : plugin.getMarkers())
		{
			if (!group.isVisible())
			{
				continue;
			}

			for (final Line line : group.getLines())
			{
				drawLine(graphics, line);
			}
		}
	}

	private void drawLine(Graphics2D graphics, Line line)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(Edge.start(line));
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(Edge.end(line));
		final Point delta = worldMapOverlay.mapWorldPointToGraphicsPoint(Edge.start(line).dx(1));

		if (start == null || end == null || delta == null)
		{
			return;
		}

		final int offset = (delta.getX() - start.getX()) / 2;
		final int x1 = start.getX() - offset;
		final int y1 = start.getY() + offset;
		final int x2 = end.getX() - offset;
		final int y2 = end.getY() + offset;

		graphics.setColor(line.getColour());
		graphics.drawLine(x1, y1, x2, y2);
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
