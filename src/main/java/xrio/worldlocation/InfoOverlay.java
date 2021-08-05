/*
 * Copyright (c) 2021, Xrio
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class InfoOverlay extends OverlayPanel
{
	private final Client client;
	private final WorldLocationConfig config;

	@Inject
	InfoOverlay(Client client, WorldLocationConfig config)
	{
		this.client = client;
		this.config = config;

		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.gridInfo() || client.getLocalPlayer() == null)
		{
			return null;
		}

		int z = client.getPlane();

		WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		int tileX = wp.getX();
		int tileY = wp.getY();
		final int chunkTileX = tileX % 8;
		final int chunkTileY = tileY % 8;

		if (InstanceInfoType.TEMPLATE.equals(config.instanceInfoType()) && client.isInInstancedRegion())
		{
			int[][][] instanceTemplateChunks = client.getInstanceTemplateChunks();
			LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
			int chunkData = instanceTemplateChunks[z][localPoint.getSceneX() / 8][localPoint.getSceneY() / 8];

			tileX = (chunkData >> 14 & 0x3FF) * 8 + chunkTileX;
			tileY = (chunkData >> 3 & 0x7FF) * 8 + chunkTileY;
		}

		final int chunkX = tileX >> 3;
		final int chunkY = tileY >> 3;
		final int regionX = tileX >> 6;
		final int regionY = tileY >> 6;
		final int regionTileX = tileX % 64;
		final int regionTileY = tileY % 64;

		final int chunkID = (chunkX << 11) | chunkY;
		final int regionID = (regionX << 8) | regionY;

		final boolean useId = InfoType.UNIQUE_ID.equals(config.gridInfoType());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Tile")
			.right(tileX + ", " + tileY + ", " + z)
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Chunk" + (useId ? " ID" : ""))
			.right(useId ? ("" + chunkID) :
				(chunkX + ", " + chunkY + ", " + chunkTileX + ", " + chunkTileY))
			.build());

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Region" + (useId ? " ID" : ""))
			.right(useId ? ("" + regionID) :
				(regionX + ", " + regionY + ", " + regionTileX + ", " + regionTileY))
			.build());

		return super.render(graphics);
	}
}
