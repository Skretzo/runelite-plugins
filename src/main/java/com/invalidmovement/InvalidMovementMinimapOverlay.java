package com.invalidmovement;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.Perspective;
import static net.runelite.api.Perspective.UNIT;
import net.runelite.api.Point;
import net.runelite.api.SpriteID;
import net.runelite.api.Tile;
import net.runelite.api.Varbits;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class InvalidMovementMinimapOverlay extends Overlay
{
	private final Client client;
	private final InvalidMovementConfig config;

	@Inject
	private SpriteManager spriteManager;

	private BufferedImage minimapSpriteFixed;
	private BufferedImage minimapSpriteResizeable;
	private Shape minimapClipFixed;
	private Shape minimapClipResizeable;
	private Rectangle minimapRectangle = new Rectangle();

	@Inject
	InvalidMovementMinimapOverlay(Client client, InvalidMovementConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showMinimap())
		{
			renderMinimap(graphics);
		}
		return null;
	}

	private void renderMinimap(Graphics2D graphics)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		final LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
		final int playerX = playerLocation.getSceneX();
		final int playerY = playerLocation.getSceneY();
		final int radius = config.radiusMinimap() < 0 ? Integer.MAX_VALUE / 2 : config.radiusMinimap();

		WorldView worldView = client.getTopLevelWorldView();

		CollisionData[] collisionData = worldView.getCollisionMaps();

		if (collisionData == null)
		{
			return;
		}

		Shape minimapClipArea = getMinimapClipArea();
		if (minimapClipArea == null)
		{
			return;
		}
		graphics.setClip(minimapClipArea);

		final int z = worldView.getPlane();

		final int[][] flags = collisionData[z].getFlags();

		final Tile[][] tiles = worldView.getScene().getTiles()[z];

		final int startX = Math.max(playerX - radius, 0);
		final int endX = Math.min(playerX + radius, tiles[0].length);
		final int startY = Math.max(playerY - radius, 0);
		final int endY = Math.min(playerY + radius, tiles.length);

		for (int y = startY; y < endY; y++)
		{
			for (int x = startX; x < endX; x++)
			{
				Tile tile = tiles[x][y];
				if (tile == null)
				{
					continue;
				}

				final LocalPoint localPoint = tile.getLocalLocation();
				if (localPoint == null)
				{
					continue;
				}

				final Point posOnMinimap = Perspective.localToMinimap(client, localPoint);
				if (posOnMinimap == null)
				{
					continue;
				}

				final int data = flags[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];

				final Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR))
				{
					drawSquare(graphics, posOnMinimap, config.colourFloor());
				}

				if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT))
				{
					drawSquare(graphics, posOnMinimap, config.colourObject());
				}

				if (tile.getWallObject() != null)
				{
					final double angle = (client.getCameraYawTarget() & 0x7FF) * UNIT;
					final GeneralPath path = new GeneralPath();

					graphics.setColor(config.colourWall());
					graphics.rotate(angle, posOnMinimap.getX(), posOnMinimap.getY());

					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_SOUTH))
					{
						drawWall(path, posOnMinimap, 0, 1, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_WEST))
					{
						drawWall(path, posOnMinimap, 0, 0, 0, 1);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_NORTH))
					{
						drawWall(path, posOnMinimap, 0, 0, 1, 0);
					}
					if (movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_EAST))
					{
						drawWall(path, posOnMinimap, 1, 0, 0, 1);
					}

					graphics.draw(path);
					graphics.rotate(-angle, posOnMinimap.getX(), posOnMinimap.getY());
				}
			}
		}
	}

	private void drawSquare(Graphics2D graphics, Point center, Color color)
	{
		final int x = center.getX();
		final int y = center.getY();
		final double angle = (client.getCameraYawTarget() & 0x7FF) * UNIT;

		final int width = (int) client.getMinimapZoom();
		final int height = (int) client.getMinimapZoom();

		final int a = (width % 2 == 0) ? 1 : 0;
		final int b = (height % 2 == 0)? 1 : 2;

		graphics.setColor(color);
		graphics.rotate(angle, x, y);
		graphics.fillRect(x - width / 2 + a, y - height / 2 - b, width, height);
		graphics.rotate(-angle , x, y);
	}

	private void drawWall(GeneralPath path, Point center, int dx1, int dy1, int dx2, int dy2)
	{
		final int centerX = center.getX();
		final int centerY = center.getY();

		final int width = (int) client.getMinimapZoom() - 1;
		final int height = (int) client.getMinimapZoom() - 1;

		int x = centerX - width / 2;
		int y = centerY - height;

		x += dx1 * width;
		y += dy1 * height;

		path.moveTo(x, y);

		x += dx2 * width;
		y += dy2 * height;

		path.lineTo(x, y);
	}

	private Shape getMinimapClipArea()
	{
		Widget minimapWidget = getMinimapDrawWidget();

		if (minimapWidget == null || minimapWidget.isHidden() || !minimapRectangle.equals(minimapRectangle = minimapWidget.getBounds()))
		{
			minimapClipFixed = null;
			minimapClipResizeable = null;
			minimapSpriteFixed = null;
			minimapSpriteResizeable = null;
		}

		if (client.isResized())
		{
			if (minimapClipResizeable != null)
			{
				return minimapClipResizeable;
			}
			if (minimapSpriteResizeable == null)
			{
				minimapSpriteResizeable = spriteManager.getSprite(SpriteID.RESIZEABLE_MODE_MINIMAP_ALPHA_MASK, 0);
			}
			if (minimapSpriteResizeable != null)
			{
				return minimapClipResizeable = bufferedImageToPolygon(minimapSpriteResizeable);
			}
			return getMinimapClipAreaSimple();
		}
		if (minimapClipFixed != null)
		{
			return minimapClipFixed;
		}
		if (minimapSpriteFixed == null)
		{
			minimapSpriteFixed = spriteManager.getSprite(SpriteID.FIXED_MODE_MINIMAP_ALPHA_MASK, 0);
		}
		if (minimapSpriteFixed != null)
		{
			return minimapClipFixed = bufferedImageToPolygon(minimapSpriteFixed);
		}
		return getMinimapClipAreaSimple();
	}

	private Widget getMinimapDrawWidget()
	{
		if (client.isResized())
		{
			if (client.getVarbitValue(Varbits.SIDE_PANELS) == 1)
			{
				return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_DRAW_AREA);
			}
			return client.getWidget(ComponentID.RESIZABLE_VIEWPORT_MINIMAP_DRAW_AREA);
		}
		return client.getWidget(ComponentID.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
	}

	private Shape getMinimapClipAreaSimple()
	{
		Widget minimapDrawArea = getMinimapDrawWidget();

		if (minimapDrawArea == null || minimapDrawArea.isHidden())
		{
			return null;
		}

		Rectangle bounds = minimapDrawArea.getBounds();

		return new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

	private Polygon bufferedImageToPolygon(BufferedImage image)
	{
		int outsideColour = -1;
		int previousColour;
		final int width = image.getWidth();
		final int height = image.getHeight();
		List<java.awt.Point> points = new ArrayList<>();
		for (int y = 0; y < height; y++)
		{
			previousColour = outsideColour;
			for (int x = 0; x < width; x++)
			{
				int colour = image.getRGB(x, y);
				if (x == 0 && y == 0)
				{
					outsideColour = colour;
					previousColour = colour;
				}
				if (colour != outsideColour && previousColour == outsideColour)
				{
					points.add(new java.awt.Point(x, y));
				}
				if ((colour == outsideColour || x == (width - 1)) && previousColour != outsideColour)
				{
					points.add(0, new java.awt.Point(x, y));
				}
				previousColour = colour;
			}
		}
		int offsetX = 0;
		int offsetY = 0;
		Widget minimapDrawWidget = getMinimapDrawWidget();
		if (minimapDrawWidget != null)
		{
			offsetX = minimapDrawWidget.getBounds().x;
			offsetY = minimapDrawWidget.getBounds().y;
		}
		Polygon polygon = new Polygon();
		for (java.awt.Point point : points)
		{
			polygon.addPoint(point.x + offsetX, point.y + offsetY);
		}
		return polygon;
	}
}
