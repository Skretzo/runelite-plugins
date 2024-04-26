/*
 * Copyright (c) 2020, Xrio
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.worldlocation;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "World Location",
	description = "Shows world tile location and grid lines",
	tags = {"world", "tile", "location", "coordinates", "grid", "lines"}
)
public class WorldLocationPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoOverlay infoOverlay;

	@Inject
	private SceneOverlay sceneOverlay;

	@Inject
	private MinimapOverlay minimapOverlay;

	@Inject
	private WorldMapOverlay worldMapOverlay;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	private Shape minimapClipFixed;
	private Shape minimapClipResizeable;
	private BufferedImage minimapSpriteFixed;
	private BufferedImage minimapSpriteResizeable;
	private Rectangle minimapRectangle = new Rectangle();

	@Provides
	WorldLocationConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WorldLocationConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(infoOverlay);
		overlayManager.add(sceneOverlay);
		overlayManager.add(minimapOverlay);
		overlayManager.add(worldMapOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(infoOverlay);
		overlayManager.remove(sceneOverlay);
		overlayManager.remove(minimapOverlay);
		overlayManager.remove(worldMapOverlay);
	}

	public Widget getMinimapDrawWidget()
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

	public Shape getMinimapClipArea()
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
				minimapClipResizeable = bufferedImageToPolygon(minimapSpriteResizeable);
				return minimapClipResizeable;
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
			minimapClipFixed = bufferedImageToPolygon(minimapSpriteFixed);
			return minimapClipFixed;
		}
		return getMinimapClipAreaSimple();
	}

	private Polygon bufferedImageToPolygon(BufferedImage image)
	{
		Color outsideColour = null;
		Color previousColour;
		final int width = image.getWidth();
		final int height = image.getHeight();
		List<Point> points = new ArrayList<>();
		for (int y = 0; y < height; y++)
		{
			previousColour = outsideColour;
			for (int x = 0; x < width; x++)
			{
				int rgb = image.getRGB(x, y);
				int a = (rgb & 0xff000000) >>> 24;
				int r = (rgb & 0x00ff0000) >> 16;
				int g = (rgb & 0x0000ff00) >> 8;
				int b = (rgb & 0x000000ff) >> 0;
				Color colour = new Color(r, g, b, a);
				if (x == 0 && y == 0)
				{
					outsideColour = colour;
					previousColour = colour;
				}
				if (!colour.equals(outsideColour) && previousColour.equals(outsideColour))
				{
					points.add(new java.awt.Point(x, y));
				}
				if ((colour.equals(outsideColour) || x == (width - 1)) && !previousColour.equals(outsideColour))
				{
					points.add(0, new java.awt.Point(x, y));
				}
				previousColour = colour;
			}
		}
		int offsetX = minimapRectangle.x;
		int offsetY = minimapRectangle.y;
		Polygon polygon = new Polygon();
		for (java.awt.Point point : points)
		{
			polygon.addPoint(point.x + offsetX, point.y + offsetY);
		}
		return polygon;
	}
}
