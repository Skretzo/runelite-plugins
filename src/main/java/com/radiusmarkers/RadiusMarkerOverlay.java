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
			WorldPoint worldPoint = marker.getWorldPoint();
			if (worldPoint.getPlane() != client.getPlane() || !marker.isVisible())
			{
				continue;
			}

			if (marker.isAggroVisible())
			{
				drawBox(graphics, worldPoint, marker.getAggroRadius(), marker.getAggroColour(), stroke);
			}

			if (marker.isRetreatVisible())
			{
				drawBox(graphics, worldPoint, marker.getRetreatRadius(), marker.getRetreatColour(), stroke);
			}

			if (marker.isWanderVisible())
			{
				drawBox(graphics, worldPoint, marker.getWanderRadius(), marker.getWanderColour(), stroke);
			}

			if (marker.isSpawnVisible())
			{
				drawBox(graphics, worldPoint, 0, marker.getSpawnColour(), stroke);
			}
		}

		return null;
	}

	private void drawBox(Graphics2D graphics, WorldPoint worldPoint, int radius, Color borderColour, Stroke borderStroke)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();

		graphics.setStroke(borderStroke);
		graphics.setColor(borderColour);

		final GeneralPath path = new GeneralPath();

		final int startX = worldPoint.getX() - radius;
		final int startY = worldPoint.getY() - radius;
		final int z = worldPoint.getPlane();

		final int diameter = 2 * radius + 1;

		int x = startX;
		int y = startY;
		boolean hasFirst;
		for (int i = 1; i <= diameter; i++)
		{
			hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}
			x = startX;
			y = startY + i;
			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}
		for (int i = 1; i <= diameter; i++)
		{
			hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}
			x = startX + i;
			y = startY + diameter;
			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}
		for (int i = (diameter - 1); i >= 0; i--)
		{
			hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}
			x = startX + diameter;
			y = startY + i;
			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}
		for (int i = (diameter - 1); i >= 0; i--)
		{
			hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}
			x = startX + i;
			y = startY;
			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < MAX_DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}
		}

		graphics.draw(path);
	}

	private boolean moveTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.moveTo(point.getX(), point.getY());
			return true;
		}
		return false;
	}

	private void lineTo(GeneralPath path, final int x, final int y, final int z)
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
