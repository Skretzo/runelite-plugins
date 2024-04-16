package com.linemarkers;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class LineMarkerSceneOverlay extends Overlay
{
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;
	private static final int MAX_DISTANCE = Perspective.SCENE_SIZE / 4;

	private final Client client;
	private final LineMarkerPlugin plugin;

	@Inject
	private LineMarkerSceneOverlay(Client client, LineMarkerPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (final LineGroup group : plugin.getMarkers())
		{
			if (!group.isVisible())
			{
				continue;
			}

			for (final Line line : group.getLines())
			{
				if (line.getLocation().getPlane() != client.getPlane())
				{
					continue;
				}

				drawLine(graphics, line);
			}
		}

		return null;
	}

	private void drawLine(Graphics2D graphics, Line line)
	{
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getWorldLocation().distanceTo(line.getLocation()) > MAX_DISTANCE)
		{
			return;
		}

		final Point start = worldToScene(Edge.start(line));
		final Point end = worldToScene(Edge.end(line));

		if (start == null || end == null)
		{
			return;
		}

		graphics.setColor(line.getColour());
		graphics.setStroke(new BasicStroke((float) line.getWidth()));
		graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
	}

	private Point worldToScene(WorldPoint location)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, location);

		if (localPoint == null)
		{
			return null;
		}

		return Perspective.localToCanvas(
			client,
			new LocalPoint(localPoint.getX() - LOCAL_TILE_SIZE / 2, localPoint.getY() - LOCAL_TILE_SIZE / 2),
			location.getPlane());
	}
}
