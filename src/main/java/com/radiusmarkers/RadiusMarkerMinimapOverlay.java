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
import net.runelite.api.Point;
import net.runelite.api.Varbits;
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

			if (marker.isMaxVisible())
			{
				drawSquare(graphics, worldPoint, marker.getMaxColour(), marker.getMaxRadius());
			}

			if (marker.isRetreatVisible())
			{
				drawSquare(graphics, worldPoint, marker.getRetreatColour(), marker.getRetreatRadius());
			}

			if (marker.isWanderVisible())
			{
				drawSquare(graphics, worldPoint, marker.getWanderColour(), marker.getWanderRadius());
			}

			if (marker.isSpawnVisible())
			{
				drawSquare(graphics, worldPoint, marker.getSpawnColour(), 0);
			}
		}
	}

	private void drawSquare(Graphics2D graphics, WorldPoint center, Color color, int radius)
	{
		final WorldPoint southWest = center.dx(-radius).dy(-radius);

		Area minimapClipArea = getMinimapClipArea();
		if (minimapClipArea == null)
		{
			return;
		}

		graphics.setColor(color);
		graphics.setClip(minimapClipArea);

		drawSquareSide(graphics, southWest, radius, 0, 0, 1, 0);
		drawSquareSide(graphics, southWest, radius, 0, 0, 0, 1);
		drawSquareSide(graphics, southWest, radius, 0, 1, 1, 0);
		drawSquareSide(graphics, southWest, radius, 1, 0, 0, 1);
	}

	private void drawSquareSide(Graphics2D graphics, WorldPoint southWest,
		int radius, int dx1, int dy1, int dx2, int dy2)
	{
		final int diameter = radius * 2 + 1;
		final WorldPoint worldPointStart = southWest.dx(dx1 * diameter).dy(dy1 * diameter);
		final WorldPoint worldPointEnd = worldPointStart.dx(dx2 * diameter).dy(dy2 * diameter);

		Point start = worldToMinimap(worldPointStart, dx1, dy1);
		Point end = worldToMinimap(worldPointEnd, dx1 + dx2, dy1 + dy2);

		if (start == null || end == null)
		{
			return;
		}

		graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
	}

	private Point worldToMinimap(final WorldPoint worldPoint, final int dx, final int dy)
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

		final int x = (worldPoint.getX() - playerLocation.getX()) * TILE_SIZE + offsetX / 32 - TILE_SIZE / 2 - dx;
		final int y = (worldPoint.getY() - playerLocation.getY()) * TILE_SIZE + offsetY / 32 - TILE_SIZE / 2 - dy;

		final int angle = client.getMapAngle() & 0x7FF;

		final int sin = (int) (65536.0D * Math.sin((double) angle * Perspective.UNIT));
		final int cos = (int) (65536.0D * Math.cos((double) angle * Perspective.UNIT));

		final Widget minimapDrawWidget = getMinimapDrawWidget();
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

	private Widget getMinimapDrawWidget()
	{
		Widget minimapDrawArea;
		if (client.isResized())
		{
			if (client.getVar(Varbits.SIDE_PANELS) == 1)
			{
				minimapDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
			}
			else
			{
				minimapDrawArea = client.getWidget(WidgetInfo.RESIZABLE_MINIMAP_STONES_DRAW_AREA);
			}
		}
		else
		{
			minimapDrawArea = client.getWidget(WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
		}
		return minimapDrawArea;
	}

	private Area getMinimapClipArea()
	{
		Widget minimapDrawArea = getMinimapDrawWidget();

		if (minimapDrawArea == null || minimapDrawArea.isHidden())
		{
			return null;
		}

		Rectangle bounds = minimapDrawArea.getBounds();
		Ellipse2D ellipse = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());

		return new Area(ellipse);
	}
}
