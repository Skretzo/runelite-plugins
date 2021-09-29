package com.radiusmarkers;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.UNIT;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

class RadiusMarkerMinimapOverlay extends Overlay
{
	private static final int TILE_SIZE = 4;

	private final Client client;
	private final RadiusMarkerConfig config;
	private final RadiusMarkerPlugin plugin;

	@Inject
	RadiusMarkerMinimapOverlay(Client client, RadiusMarkerConfig config, RadiusMarkerPlugin plugin)
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
		if (config.showMinimap())
		{
			drawMinimap(graphics);
		}
		return null;
	}

	private void drawMinimap(Graphics2D graphics)
	{
		final List<ColourRadiusMarker> markers = plugin.getMarkers();

		for (final ColourRadiusMarker marker : markers)
		{
			if (!marker.isVisible())
			{
				continue;
			}

			final WorldPoint worldPoint = marker.getWorldPoint();

			final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
			if (localPoint == null)
			{
				continue;
			}

			final Point point = Perspective.localToMinimap(client, localPoint);
			if (point == null)
			{
				continue;
			}

			if (marker.isMaxVisible())
			{
				drawSquare(graphics, point, marker.getMaxColour(), marker.getMaxRadius() * 2 + 1);
			}

			if (marker.isRetreatVisible())
			{
				drawSquare(graphics, point, marker.getRetreatColour(), marker.getRetreatRadius() * 2 + 1);
			}

			if (marker.isWanderVisible())
			{
				drawSquare(graphics, point, marker.getWanderColour(), marker.getWanderRadius() * 2 + 1);
			}

			if (marker.isSpawnVisible())
			{
				drawSquare(graphics, point, marker.getSpawnColour(), 1);
			}
		}
	}

	private void drawSquare(Graphics2D graphics, Point center, Color color, int diameter)
	{
		diameter *= TILE_SIZE;
		final double angle = client.getMapAngle() * UNIT;

		final int x = center.getX() - diameter / 2;
		final int y = center.getY() - diameter / 2;

		Area minimapClipArea = getMinimapClipArea();
		if (minimapClipArea == null)
		{
			return;
		}

		graphics.setColor(color);
		graphics.setClip(minimapClipArea);
		graphics.rotate(angle, center.getX(), center.getY());
		graphics.draw(new Area(new Rectangle(x, y, diameter, diameter)));
		graphics.rotate(-angle, center.getX(), center.getY());
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
