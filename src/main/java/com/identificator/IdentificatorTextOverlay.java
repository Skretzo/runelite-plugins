package com.identificator;

import com.google.inject.Inject;

import java.awt.Color;
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
		renderInterfaces(graphics);

		renderChathead(graphics);

		renderInventory(graphics);

		return null;
	}

	private void renderInterfaces(Graphics2D graphics)
	{
		renderInterface(graphics, 214, 29); // Skill guide icons
	}

	private void renderInterface(Graphics2D graphics, int groupId, int childId)
	{
		Widget widget = client.getWidget(groupId, childId);

		if (widget == null)
		{
			return;
		}
		
		for (Widget child : widget.getDynamicChildren())
		{
			if (child == null)
			{
				continue;
			}

			StringBuilder text = new StringBuilder();

			if (plugin.showInterfaceItemId && child.getItemId() >= 0 && child.getItemId() != 7620) // blank
			{
				plugin.appendId(text, "I: " + child.getItemId());
			}
	
			if (plugin.showInterfaceModelId && child.getModelId() >= 0)
			{
				plugin.appendId(text, "M: " + child.getModelId());
			}
	
			if (plugin.showInterfaceSpriteId && child.getSpriteId() >= 0)
			{
				plugin.appendId(text, "S: " + child.getSpriteId());
			}

			if (text.length() <= 0)
			{
				return;
			}
	
			int width = graphics.getFontMetrics().stringWidth(text.toString());
			int textX = Math.max(child.getCanvasLocation().getX() - width / 2 + child.getWidth() / 2, 15);
			int textY = child.getCanvasLocation().getY();
	
			graphics.setColor(plugin.colourInterface);
			graphics.drawString(text.toString(), textX, textY);
		}
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

	private void renderInventory(Graphics2D graphics)
	{
		Widget inventory = client.getWidget(ComponentID.INVENTORY_CONTAINER);

		if (inventory == null || inventory.isHidden() ||
			!plugin.showOverheadInfo || !plugin.showInventoryItemId)
		{
			return;
		}

		for (Widget item : inventory.getDynamicChildren())
		{
			if (item.getItemId() == 6512) // null
			{
				continue;
			}
			StringBuilder text = new StringBuilder();
			plugin.appendId(text, item.getItemId());

			int textWidth = graphics.getFontMetrics().stringWidth(text.toString());
			int textHeight = graphics.getFontMetrics().getHeight();
			int textX = Math.max(item.getCanvasLocation().getX() - textWidth / 2 + item.getWidth() / 2, 15);
			int textY = item.getCanvasLocation().getY() + textHeight / 2 + item.getHeight() / 2;

			graphics.setColor(Color.BLACK);
			graphics.drawString(text.toString(), textX + 1, textY + 1);
			graphics.setColor(plugin.colourInventory);
			graphics.drawString(text.toString(), textX, textY);
		}
	}
}
