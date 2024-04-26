/*
 * Copyright (c) 2018, Alex Kolpa <https://github.com/AlexKolpa>
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.RenderOverview;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class WorldMapOverlay extends Overlay
{
	private static final int LABEL_PADDING = 4;
	private static final Color WHITE_TRANSLUCENT = new Color(255, 255, 255, 127);

	private final Client client;
	private final WorldLocationConfig config;

	@Inject
	private WorldMapOverlay(Client client, WorldLocationConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(Overlay.PRIORITY_HIGH);
		setLayer(OverlayLayer.MANUAL);
		drawAfterLayer(ComponentID.WORLD_MAP_MAPVIEW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.mapTileLines())
		{
			drawMapLines(graphics, 1, config.tileLineColour());
		}

		if (config.mapChunkLines())
		{
			drawMapLines(graphics, 8, config.chunkLineColour());
		}

		if (config.mapRegionLines())
		{
			drawMapLines(graphics, 64, config.regionLineColour());
		}

		return null;
	}

	private void drawMapLines(Graphics2D graphics, int gridSize, Color gridColour)
	{
		final int gridTruncate = ~(gridSize - 1);

		RenderOverview ro = client.getRenderOverview();
		Widget map = client.getWidget(ComponentID.WORLD_MAP_MAPVIEW);
		float pixelsPerTile = ro.getWorldMapZoom();

		if (map == null)
		{
			return;
		}

		if (gridSize * pixelsPerTile < 3)
		{
			return;
		}

		Rectangle worldMapRect = map.getBounds();
		graphics.setClip(worldMapRect);

		int widthInTiles = (int) Math.ceil(worldMapRect.getWidth() / pixelsPerTile);
		int heightInTiles = (int) Math.ceil(worldMapRect.getHeight() / pixelsPerTile);

		Point worldMapPosition = ro.getWorldMapPosition();

		// Offset in tiles from anchor sides
		int yTileMin = worldMapPosition.getY() - heightInTiles / 2;
		int xRegionMin = (worldMapPosition.getX() - widthInTiles / 2) & gridTruncate;
		int xRegionMax = ((worldMapPosition.getX() + widthInTiles / 2) & gridTruncate) + gridSize;
		int yRegionMin = (yTileMin & gridTruncate);
		int yRegionMax = ((worldMapPosition.getY() + heightInTiles / 2) & gridTruncate) + gridSize;
		int regionPixelSize = (int) Math.ceil(gridSize * pixelsPerTile);

		for (int x = xRegionMin; x < xRegionMax; x += gridSize)
		{
			for (int y = yRegionMin; y < yRegionMax; y += gridSize)
			{
				int yTileOffset = -(yTileMin - y);
				int xTileOffset = x + widthInTiles / 2 - worldMapPosition.getX();

				int xPos = ((int) (xTileOffset * pixelsPerTile)) + (int) worldMapRect.getX();
				int yPos = (worldMapRect.height - (int) (yTileOffset * pixelsPerTile)) + (int) worldMapRect.getY();
				// Offset y-position by a single region to correct for drawRect starting from the top
				yPos -= regionPixelSize;

				graphics.setColor(gridColour);

				graphics.drawRect(xPos, yPos, regionPixelSize, regionPixelSize);

				graphics.setColor(WHITE_TRANSLUCENT);

				if (gridSize == 64)
				{
					int regionId = ((x >> 6) << 8) | (y >> 6);
					String regionText = String.valueOf(regionId);
					if (InfoType.LOCAL_COORDINATES.equals(config.gridInfoType()))
					{
						regionText = (x >> 6) + ", " + (y >> 6);
					}
					FontMetrics fm = graphics.getFontMetrics();
					Rectangle2D textBounds = fm.getStringBounds(regionText, graphics);
					int labelWidth = (int) textBounds.getWidth() + 2 * LABEL_PADDING;
					int labelHeight = (int) textBounds.getHeight() + 2 * LABEL_PADDING;
					graphics.fillRect(xPos, yPos, labelWidth, labelHeight);
					graphics.setColor(Color.BLACK);
					graphics.drawString(
						regionText,
						xPos + LABEL_PADDING,
						yPos + (int) textBounds.getHeight() + LABEL_PADDING);
				}
			}
		}
	}
}
