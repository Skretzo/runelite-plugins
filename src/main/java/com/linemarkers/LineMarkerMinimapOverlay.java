package com.linemarkers;

import com.google.inject.Inject;
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

class LineMarkerMinimapOverlay extends Overlay
{
	private final Client client;
	private final LineMarkerConfig config;
	private final LineMarkerPlugin plugin;

	@Inject
	LineMarkerMinimapOverlay(Client client, LineMarkerConfig config, LineMarkerPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
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
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics.setClip(plugin.getMinimapClipArea());

		for (final LineGroup group : plugin.getMarkers())
		{
			if (!group.isVisible())
			{
				continue;
			}

			for (Line line : group.getLines())
			{
				drawLine(graphics, line);
			}
		}
	}

	private void drawLine(Graphics2D graphics, Line line)
	{
		final Point start = worldToMinimap(Edge.start(line));
		final Point end = worldToMinimap(Edge.end(line));

		if (start == null || end == null)
		{
			return;
		}

		graphics.setColor(line.getColour());
		graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
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

		final int dx = worldPoint.getX() - playerLocation.getX();
		final int dy = worldPoint.getY() - playerLocation.getY();

		final double tileSize = client.getMinimapZoom();

		final int x = (int) (dx * tileSize + offsetX * tileSize / Perspective.LOCAL_TILE_SIZE - tileSize / 2);
		final int y = (int) (dy * tileSize + offsetY * tileSize / Perspective.LOCAL_TILE_SIZE - tileSize / 2 + 1);

		final int angle = client.getCameraYawTarget() & 0x7FF;

		final int sin = Perspective.SINE[angle];
		final int cos = Perspective.COSINE[angle];

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
