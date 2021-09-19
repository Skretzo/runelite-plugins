package com.bettertilerenderingexample;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class LineOverlay extends Overlay
{
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private final Client client;
	private final BetterTileRenderingExampleConfig config;
	private final BetterTileRenderingExamplePlugin plugin;

	@Inject
	private LineOverlay(Client client, BetterTileRenderingExampleConfig config, BetterTileRenderingExamplePlugin plugin)
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
		if (!config.renderLines() || client.getLocalPlayer() == null)
		{
			return null;
		}

		final long timeStart = System.currentTimeMillis();

		renderLines(graphics, client.getLocalPlayer().getWorldLocation(), config.radius());

		plugin.setLineDuration(System.currentTimeMillis() - timeStart);

		return null;
	}

	private void renderLines(Graphics2D graphics, final WorldPoint worldPoint, final int radius)
	{
		graphics.setStroke(new BasicStroke((int) config.borderWidth()));
		graphics.setColor(config.colourLines());

		GeneralPath path = new GeneralPath();

		final int startX = worldPoint.getX() - radius;
		final int startY = worldPoint.getY() - radius;
		final int z = worldPoint.getPlane();

		final int diameter = (2 * radius + 1);

		try
		{
			// Starting corner
			moveTo(path, startX, startY, z);

			// 4 external lines
			for (int j = 1; j <= diameter; j++)
			{
				lineTo(path, startX, startY + j, z);
			}
			for (int j = 1; j <= diameter; j++)
			{
				lineTo(path, startX + j, startY + diameter, z);
			}
			for (int j = (diameter - 1); j >= 0; j--)
			{
				lineTo(path, startX + diameter, startY + j, z);
			}
			for (int j = (diameter - 1); j >= 0; j--)
			{
				lineTo(path, startX + j, startY, z);
			}

			// Horizontal internal lines
			for (int i = 1; i <= (diameter - 1); i++)
			{
				moveTo(path, startX, startY + i, z);
				for (int j = 1; j <= diameter; j++)
				{
					lineTo(path, startX + j, startY + i, z);
				}
			}

			// Vertical internal lines
			for (int i = 1; i <= (diameter - 1); i++)
			{
				moveTo(path, startX + i, startY, z);
				for (int j = 1; j <= diameter; j++)
				{
					lineTo(path, startX + i, startY + j, z);
				}
			}

			graphics.draw(path);
		}
		catch (NullPointerException npe)
		{
			graphics.draw(path);
		}
	}

	private void moveTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		path.moveTo(point.getX(), point.getY());
	}

	private void lineTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		path.lineTo(point.getX(), point.getY());
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
