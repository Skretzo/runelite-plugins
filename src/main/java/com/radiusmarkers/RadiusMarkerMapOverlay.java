package com.radiusmarkers;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;

class RadiusMarkerMapOverlay extends Overlay
{
	private final Client client;
	private final RadiusMarkerConfig config;
	private final RadiusMarkerPlugin plugin;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	RadiusMarkerMapOverlay(Client client, RadiusMarkerConfig config, RadiusMarkerPlugin plugin)
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
		if (config.showWorldMap() && client.getWidget(WidgetInfo.WORLD_MAP_VIEW) != null)
		{
			drawWorldMap(graphics);
		}
		return null;
	}

	private void drawWorldMap(Graphics2D graphics)
	{
		final Rectangle bounds = client.getWidget(WidgetInfo.WORLD_MAP_VIEW).getBounds();
		if (bounds == null)
		{
			return;
		}
		final Area mapClipArea = getWorldMapClipArea(bounds);

		final List<ColourRadiusMarker> markers = plugin.getMarkers();

		for (final ColourRadiusMarker marker : markers)
		{
			if (!marker.isVisible())
			{
				continue;
			}

			final WorldPoint worldPoint = marker.getWorldPoint();

			if (marker.isMaxVisible())
			{
				drawSquare(graphics, worldPoint, marker.getMaxColour(), mapClipArea, marker.getMaxRadius());
			}

			if (marker.isRetreatVisible())
			{
				drawSquare(graphics, worldPoint, marker.getRetreatColour(), mapClipArea, marker.getRetreatRadius());
			}

			if (marker.isWanderVisible())
			{
				drawSquare(graphics, worldPoint, marker.getWanderColour(), mapClipArea, marker.getWanderRadius());
			}

			if (marker.isSpawnVisible())
			{
				drawSquare(graphics, worldPoint, marker.getSpawnColour(), mapClipArea, 0);
			}
		}
	}

	private void drawSquare(Graphics2D graphics, WorldPoint worldPoint, Color color, Area mapClipArea, int radius)
	{
		final Point start = worldMapOverlay.mapWorldPointToGraphicsPoint(worldPoint.dx(-radius).dy(radius));
		final Point end = worldMapOverlay.mapWorldPointToGraphicsPoint(worldPoint.dx(radius + 1).dy(-(radius + 1)));

		if (start == null || end == null)
		{
			return;
		}

		final int x = start.getX();
		final int y = start.getY();
		final int width = end.getX() - x;
		final int height = end.getY() - y;

		graphics.setColor(color);
		graphics.setClip(mapClipArea);
		graphics.draw(new Area(new Rectangle(x, y, width, height)));
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
