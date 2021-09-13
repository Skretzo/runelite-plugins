package com.radiusmarkers;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class RadiusMarkerOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private final Client client;
	private final RadiusMarkerConfig config;
	private final RadiusMarkerPlugin plugin;

	@Inject
	private RadiusMarkerOverlay(Client client, RadiusMarkerConfig config, RadiusMarkerPlugin plugin)
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
		final Collection<ColourRadiusMarker> markers = plugin.getMarkers();

		if (markers.isEmpty())
		{
			return null;
		}

		Stroke stroke = new BasicStroke((float) config.borderWidth());

		for (final ColourRadiusMarker marker : markers)
		{
			WorldPoint point = marker.getWorldPoint();
			if (point.getPlane() != client.getPlane() || !marker.isVisible())
			{
				continue;
			}

			if (marker.isAggroVisible())
			{
				drawBox(graphics, point, marker.getAggroRadius(), marker.getAggroColour(), stroke);
			}

			if (marker.isRetreatVisible())
			{
				drawBox(graphics, point, marker.getRetreatRadius(), marker.getRetreatColour(), stroke);
			}

			if (marker.isWanderVisible())
			{
				drawBox(graphics, point, marker.getWanderRadius(), marker.getWanderColour(), stroke);
			}

			if (marker.isSpawnVisible())
			{
				drawBox(graphics, point, 0, marker.getSpawnColour(), stroke);
			}
		}

		return null;
	}

	private void drawBox(Graphics2D graphics, WorldPoint point, int radius, Color borderColour, Stroke borderStroke)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		// To-do: improve this check for large radiuses
		if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);
		if (lp == null)
		{
			return;
		}

		renderBox(graphics, point, radius, borderColour, borderStroke);
	}

	private void renderBox(Graphics2D graphics, WorldPoint point, int radius, Color borderColour, Stroke borderStroke)
	{
		int startX = point.getX() - radius;
		int startY = point.getY() - radius;
		int endX = point.getX() + radius + 1;
		int endY = point.getY() + radius + 1;

		graphics.setStroke(borderStroke);
		graphics.setColor(borderColour);

		GeneralPath path = new GeneralPath();
		for (int x = startX; x <= endX; x += radius * 2 + 1)
		{
			LocalPoint lp1 = LocalPoint.fromWorld(client, x, startY);
			LocalPoint lp2 = LocalPoint.fromWorld(client, x, endY);

			boolean first = true;
			for (int y = lp1.getY(); y <= lp2.getY(); y += LOCAL_TILE_SIZE)
			{
				Point p = Perspective.localToCanvas(client,
						new LocalPoint(lp1.getX() - LOCAL_TILE_SIZE / 2, y - LOCAL_TILE_SIZE / 2),
						client.getPlane());
				if (p != null)
				{
					if (first)
					{
						path.moveTo(p.getX(), p.getY());
						first = false;
					}
					else
					{
						path.lineTo(p.getX(), p.getY());
					}
				}
			}
		}
		for (int y = startY; y <= endY; y += radius * 2 + 1)
		{
			LocalPoint lp1 = LocalPoint.fromWorld(client, startX, y);
			LocalPoint lp2 = LocalPoint.fromWorld(client, endX, y);

			boolean first = true;
			for (int x = lp1.getX(); x <= lp2.getX(); x += LOCAL_TILE_SIZE)
			{
				Point p = Perspective.localToCanvas(client,
						new LocalPoint(x - LOCAL_TILE_SIZE / 2, lp1.getY() - LOCAL_TILE_SIZE / 2),
						client.getPlane());
				if (p != null)
				{
					if (first)
					{
						path.moveTo(p.getX(), p.getY());
						first = false;
					}
					else
					{
						path.lineTo(p.getX(), p.getY());
					}
				}
			}
		}
		graphics.draw(path);
	}
}
