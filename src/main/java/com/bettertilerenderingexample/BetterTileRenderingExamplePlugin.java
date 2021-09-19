package com.bettertilerenderingexample;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Better Tile Rendering Example",
	description = "Test performance of rendering an overlay as tiles or lines",
	tags = {"overlay", "tiles", "lines"}
)
public class BetterTileRenderingExamplePlugin extends Plugin
{
	@Getter
	@Setter
	private long tileDuration = -1;

	@Getter
	@Setter
	private long lineDuration = -1;

	@Inject
	private Client client;

	@Inject
	private BetterTileRenderingExampleConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TileOverlay tileOverlay;

	@Inject
	private LineOverlay lineOverlay;

	@Inject
	private InfoOverlay infoOverlay;

	@Provides
	BetterTileRenderingExampleConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BetterTileRenderingExampleConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(tileOverlay);
		overlayManager.add(lineOverlay);
		overlayManager.add(infoOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(tileOverlay);
		overlayManager.remove(lineOverlay);
		overlayManager.remove(infoOverlay);
	}
}
