package pathfinding;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import pathfinding.pathfinder.Pathfinder;

public class PathTileOverlay extends Overlay
{
	private final Client client;
	private final PathfindingPlugin plugin;
	private final PathfindingConfig config;

	@Inject
	public PathTileOverlay(Client client, PathfindingPlugin plugin, PathfindingConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.drawTransports())
		{
			drawTransports(graphics);
		}

		if (config.drawCollisionMap())
		{
			drawCollisionMap(graphics);
		}

		if (config.drawTiles())
		{
			drawPath(graphics);
		}

		return null;
	}

	private void drawTransports(Graphics2D graphics)
	{
		Pathfinder pathfinder = plugin.getPathfinder();

		if (pathfinder == null || pathfinder.getTransports() == null)
		{
			return;
		}

		for (WorldPoint a : pathfinder.getTransports().keySet())
		{
			drawTile(graphics, a, config.colourTransports(), -1);

			Point ca = tileCenter(a);

			if (ca == null)
			{
				continue;
			}

			for (WorldPoint b : pathfinder.getTransports().get(a))
			{
				Point cb = tileCenter(b);

				if (cb != null)
				{
					graphics.drawLine(ca.x, ca.y, cb.x, cb.y);
				}
			}

			StringBuilder s = new StringBuilder();
			for (WorldPoint b : pathfinder.getTransports().get(a))
			{
				if (b.getPlane() > a.getPlane())
				{
					s.append("+");
				}
				else if (b.getPlane() < a.getPlane())
				{
					s.append("-");
				}
				else
				{
					s.append("=");
				}
			}

			graphics.setColor(Color.WHITE);
			graphics.drawString(s.toString(), ca.x, ca.y);
		}
	}

	private void drawCollisionMap(Graphics2D graphics)
	{
		Pathfinder pathfinder = plugin.getPathfinder();

		if (pathfinder == null || pathfinder.getMap() == null)
		{
			return;
		}

		for (Tile[] row : client.getScene().getTiles()[client.getPlane()])
		{
			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}

				Polygon tilePolygon = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());

				if (tilePolygon == null)
				{
					continue;
				}

				int x = tile.getWorldLocation().getX();
				int y = tile.getWorldLocation().getY();
				int z = tile.getWorldLocation().getPlane();

				String s = (!pathfinder.getMap().n(x, y, z) ? "n" : "") +
						(!pathfinder.getMap().s(x, y, z) ? "s" : "") +
						(!pathfinder.getMap().e(x, y, z) ? "e" : "") +
						(!pathfinder.getMap().w(x, y, z) ? "w" : ""); // todo: add nw, ne, sw, se

				if (!s.isEmpty() && !s.equals("nsew"))
				{
					graphics.setColor(Color.WHITE);
					int stringX = (int) (tilePolygon.getBounds().getCenterX() - graphics.getFontMetrics().getStringBounds(s, graphics).getWidth() / 2);
					int stringY = (int) tilePolygon.getBounds().getCenterY();
					graphics.drawString(s, stringX, stringY);
				}
				else if (!s.isEmpty())
				{
					graphics.setColor(config.colourCollisionMap());
					graphics.fill(tilePolygon);
				}
			}
		}
	}

	private void drawPath(Graphics2D graphics)
	{
		Pathfinder pathfinder = plugin.getPathfinder();

		if (pathfinder == null)
		{
			return;
		}

		int counter = 0;
		if (pathfinder.getPath() != null)
		{
			for (WorldPoint point : pathfinder.getPath())
			{
				drawTile(graphics, point, new Color(
					config.colourPath().getRed(),
					config.colourPath().getGreen(),
					config.colourPath().getBlue(),
					config.colourPath().getAlpha() / 2),
					counter++);
			}
		}
		else if (pathfinder.getTarget() != null && pathfinder.getCurrentPath() != null)
		{
			for (WorldPoint point : pathfinder.getCurrentPath())
			{
				drawTile(graphics, point, new Color(
					config.colourPathCalculating().getRed(),
					config.colourPathCalculating().getGreen(),
					config.colourPathCalculating().getBlue(),
					config.colourPathCalculating().getAlpha() / 2),
					counter++);
			}
		}
	}

	private Point tileCenter(WorldPoint b)
	{
		if (b.getPlane() != client.getPlane())
		{
			return null;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, b);
		if (lp == null)
		{
			return null;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly == null)
		{
			return null;
		}

		int cx = poly.getBounds().x + poly.getBounds().width / 2;
		int cy = poly.getBounds().y + poly.getBounds().height / 2;

		return new Point(cx, cy);
	}

	private void drawTile(Graphics2D graphics, WorldPoint point, Color color, int counter)
	{
		if (point.getPlane() != client.getPlane())
		{
			return;
		}

		LocalPoint lp = LocalPoint.fromWorld(client, point);
		if (lp == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, lp);
		if (poly == null)
		{
			return;
		}

		graphics.setColor(color);
		graphics.fill(poly);

		Pathfinder pathfinder = plugin.getPathfinder();

		if (counter >= 0 && !TileCounter.DISABLED.equals(config.showTileCounter()) && pathfinder != null)
		{
			if (TileCounter.REMAINING.equals(config.showTileCounter()))
			{
				counter = (!pathfinder.isCalculating() ?
					pathfinder.getPath().size() : pathfinder.getCurrentPath().size()) - counter - 1;
			}
			String counterText = Integer.toString(counter);
			graphics.setColor(Color.WHITE);
			graphics.drawString(
				counterText,
				(int) (poly.getBounds().getCenterX() -
					graphics.getFontMetrics().getStringBounds(counterText, graphics).getWidth() / 2),
				(int) poly.getBounds().getCenterY());
		}
	}
}
