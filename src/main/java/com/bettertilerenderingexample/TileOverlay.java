package com.bettertilerenderingexample;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class TileOverlay extends Overlay
{
	private static final int MAX_DRAW_DISTANCE = 32;

	private final Client client;
	private final BetterTileRenderingExampleConfig config;
	private final BetterTileRenderingExamplePlugin plugin;

	@Inject
	private TileOverlay(Client client, BetterTileRenderingExampleConfig config, BetterTileRenderingExamplePlugin plugin)
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
		if (!config.renderTiles() || client.getLocalPlayer() == null)
		{
			return null;
		}

		final long timeStart = System.currentTimeMillis();

		final WorldPoint worldPoint = client.getLocalPlayer().getWorldLocation();
		final int x = worldPoint.getX();
		final int y = worldPoint.getY();
		final int radius = config.radius();
		final Stroke stroke = new BasicStroke((float) config.borderWidth());

		for (int i = -radius; i < (radius + 1); i++)
		{
			for (int j = -radius; j < (radius + 1); j++)
			{
				WorldPoint wp = new WorldPoint(x + i, y + j, worldPoint.getPlane());
				drawTile(graphics, wp, config.colourTiles(), stroke);
			}
		}

		plugin.setTileDuration(System.currentTimeMillis() - timeStart);

		return null;
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, Color colour, Stroke borderStroke)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		if (point.distanceTo(playerLocation) >= MAX_DRAW_DISTANCE)
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly != null)
		{
			OverlayUtil.renderPolygon(graphics, poly, colour, borderStroke);
		}
	}
}
