package com.bettertilerenderingexample;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class InfoOverlay extends OverlayPanel
{
	private final Client client;
	private final BetterTileRenderingExampleConfig config;
	private final BetterTileRenderingExamplePlugin plugin;

	@Inject
	InfoOverlay(Client client, BetterTileRenderingExampleConfig config, BetterTileRenderingExamplePlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showInfo() || client.getLocalPlayer() == null)
		{
			return null;
		}

		if (config.renderTiles())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Tiles:")
				.right(plugin.getTileDuration() + " ms")
				.build());
		}

		if (config.renderLines())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Lines:")
				.right(plugin.getLineDuration() + " ms")
				.build());
		}

		return super.render(graphics);
	}
}
