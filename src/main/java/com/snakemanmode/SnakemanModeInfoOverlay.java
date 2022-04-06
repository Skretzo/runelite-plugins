package com.snakemanmode;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class SnakemanModeInfoOverlay extends OverlayPanel
{
	private static final String LABEL_LOCKED_AREA = "Go to your unlocked area!";
	private static final String LABEL_UNLOCKED = "Unlocked:";
	private static final String LABEL_XP_TO_UNLOCK = "XP to unlock:";

	private final Client client;
	private final SnakemanModeConfig config;
	private final SnakemanModePlugin plugin;

	@Inject
	SnakemanModeInfoOverlay(Client client, SnakemanModeConfig config, SnakemanModePlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;

		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showInfo() || client.getLocalPlayer() == null)
		{
			return null;
		}

		if (!plugin.isUnlockedChunk(client.getLocalPlayer().getWorldLocation(), false))
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(LABEL_LOCKED_AREA)
				.build());
		}
		else
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(LABEL_XP_TO_UNLOCK)
				.right("" + plugin.getXpToUnlock())
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left(LABEL_UNLOCKED)
				.right("" + plugin.getChunks().size())
				.build());
		}

		return super.render(graphics);
	}
}
