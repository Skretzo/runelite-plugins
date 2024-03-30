package com.identificator;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class IdentificatorTextOverlay extends Overlay
{
	private final IdentificatorPlugin plugin;
	private final Client client;

	@Inject
	IdentificatorTextOverlay(IdentificatorPlugin plugin, Client client)
	{
		this.plugin = plugin;
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderChathead(graphics);

		return null;
	}

	private void renderChathead(Graphics2D graphics)
	{
		Widget chathead = client.getWidget(ComponentID.DIALOG_NPC_HEAD_MODEL);

		if (chathead == null)
		{
			return;
		}

		StringBuilder text = new StringBuilder();

		if (plugin.showNpcChatheadModelId)
		{
			plugin.appendId(text, "M: " + chathead.getModelId());
		}

		if (plugin.showNpcChatheadAnimationId)
		{
			plugin.appendId(text, "A: " + chathead.getAnimationId());
		}

		if (text.length() <= 0)
		{
			return;
		}

		int width = graphics.getFontMetrics().stringWidth(text.toString());
		int textX = Math.max(chathead.getCanvasLocation().getX() - width / 2 + chathead.getWidth() / 2, 15);
		int textY = chathead.getCanvasLocation().getY() - 37;

		graphics.setColor(plugin.colourChathead);
		graphics.drawString(text.toString(), textX, textY);
	}
}
