/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package xrio.worldlocation;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

public class SceneOverlay extends Overlay
{
	private static final int CULL_CHUNK_BORDERS_RANGE = 16;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private final Client client;
	private final WorldLocationConfig config;
	private final TooltipManager tooltipManager;

	@Inject
	public SceneOverlay(Client client, WorldLocationConfig config, TooltipManager tooltipManager)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.config = config;
		this.tooltipManager = tooltipManager;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.tileLines())
		{
			renderLines(graphics, 1, config.tileLineWidth(), config.tileLineColour());
		}

		if (config.chunkLines())
		{
			renderLines(graphics, 8, config.chunkLineWidth(), config.chunkLineColour());
		}

		if (config.regionLines())
		{
			renderLines(graphics, 64, config.regionLineWidth(), config.regionLineColour());
		}

		if (config.tileLocation() && isHoveringScene())
		{
			renderTile(graphics, config.tileColour());
		}

		return null;
	}

	private void renderLines(Graphics2D graphics, int gridSize, int lineWidth, Color lineColour)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}
		WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		int startX = (wp.getX() - CULL_CHUNK_BORDERS_RANGE + gridSize - 1) / gridSize * gridSize;
		int startY = (wp.getY() - CULL_CHUNK_BORDERS_RANGE + gridSize - 1) / gridSize * gridSize;
		int endX = (wp.getX() + CULL_CHUNK_BORDERS_RANGE) / gridSize * gridSize;
		int endY = (wp.getY() + CULL_CHUNK_BORDERS_RANGE) / gridSize * gridSize;

		graphics.setStroke(new BasicStroke(lineWidth));
		graphics.setColor(lineColour);

		GeneralPath path = new GeneralPath();
		for (int x = startX; x <= endX; x += gridSize)
		{
			LocalPoint lp1 = LocalPoint.fromWorld(client, x, wp.getY() - CULL_CHUNK_BORDERS_RANGE);
			LocalPoint lp2 = LocalPoint.fromWorld(client, x, wp.getY() + CULL_CHUNK_BORDERS_RANGE);

			if (lp1 == null || lp2 == null)
			{
				return;
			}

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
		for (int y = startY; y <= endY; y += gridSize)
		{
			LocalPoint lp1 = LocalPoint.fromWorld(client, wp.getX() - CULL_CHUNK_BORDERS_RANGE, y);
			LocalPoint lp2 = LocalPoint.fromWorld(client, wp.getX() + CULL_CHUNK_BORDERS_RANGE, y);

			if (lp1 == null || lp2 == null)
			{
				return;
			}

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

	private void renderTile(Graphics2D graphics, Color tileColour)
	{
		Scene scene = client.getScene();
		Tile[][][] tiles = scene.getTiles();

		int z = client.getPlane();

		for (int x = 0; x < Constants.SCENE_SIZE; ++x)
		{
			for (int y = 0; y < Constants.SCENE_SIZE; ++y)
			{
				Tile tile = tiles[z][x][y];

				if (tile == null)
				{
					continue;
				}

				Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
				if (poly != null &&
					poly.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY()))
				{
					WorldPoint wp = tile.getWorldLocation();
					int tileX = wp.getX();
					int tileY = wp.getY();
					if (InstanceInfoType.TEMPLATE.equals(config.instanceInfoType()) && client.isInInstancedRegion())
					{
						int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
						LocalPoint localPoint = tile.getLocalLocation();
						int chunkData = instanceTemplateChunks[z][localPoint.getSceneX() / 8][localPoint.getSceneY() / 8];

						tileX = (chunkData >> 14 & 0x3FF) * 8 + (tileX % 8);
						tileY = (chunkData >> 3 & 0x7FF) * 8 + (tileY % 8);
					}
					tooltipManager.add(new Tooltip(tileX + ", " + tileY + ", " + z));
					OverlayUtil.renderPolygon(graphics, poly, tileColour);
				}
			}
		}
	}

	private boolean isHoveringScene()
	{
		MenuEntry[] menuEntries = client.getMenuEntries();
		for (int i = menuEntries.length - 1; i >= 0; i--)
		{
			if (MenuAction.WALK.equals(menuEntries[i].getType()))
			{
				return true;
			}
		}
		return false;
	}
}
