package com.invalidmovement;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Invalid Movement",
	description = "Display invalid movement blocked tiles",
	tags = {"invalid", "blocked", "movement", "tile", "flags"}
)
public class InvalidMovementPlugin extends Plugin
{
	@Inject
	private InvalidMovementConfig config;

	@Inject
	private InvalidMovementMapOverlay invalidMovementMapOverlay;

	@Inject
	private InvalidMovementSceneOverlay invalidMovementSceneOverlay;

	@Inject
	private InvalidMovementMinimapOverlay invalidMovementMinimapOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Provides
	InvalidMovementConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InvalidMovementConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(invalidMovementMapOverlay);
		overlayManager.add(invalidMovementSceneOverlay);
		overlayManager.add(invalidMovementMinimapOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(invalidMovementMapOverlay);
		overlayManager.remove(invalidMovementSceneOverlay);
		overlayManager.remove(invalidMovementMinimapOverlay);
	}
}
