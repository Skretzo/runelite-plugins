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
		renderInterface(graphics, plugin.interfaceGroupId, 0);
	}

	private void renderInterface(Graphics2D graphics, int groupId, int childId)
	{
		Widget widget = client.getWidget(groupId, childId);

		if (widget == null)
		{
			return;
		}

		if (widget.getDynamicChildren() != null)
		{
			for (Widget child : widget.getDynamicChildren())
			{
				renderInterfaceWidget(graphics, child);
				renderInterface(graphics, child, childId);
			}
		}
		if (widget.getStaticChildren() != null)
		{
			for (Widget child : widget.getStaticChildren())
			{
				renderInterfaceWidget(graphics, child);
				renderInterface(graphics, child, childId);
			}
		}
		if (widget.getChildren() != null)
		{
			for (Widget child : widget.getChildren())
			{
				renderInterfaceWidget(graphics, child);
				renderInterface(graphics, child, childId);
			}
		}
	}

	private void renderInterface(Graphics2D graphics, Widget widget, int lastChildId)
	{
		if (widget == null)
		{
			return;
		}

		int id = widget.getId();
		int groupId = id >> 16;
		int childId = id & 0xffff;

		if (childId > lastChildId)
		{
			renderInterface(graphics, groupId, childId);
		}
	}

	private void renderInterfaceWidget(Graphics2D graphics, Widget widget)
	{
		if (widget == null)
		{
			return;
		}

		StringBuilder text = new StringBuilder();

		if (plugin.showInterfaceItemId && widget.getItemId() >= 0 && widget.getItemId() != 7620) // blank
		{
			plugin.appendId(text, "I: " + widget.getItemId());
		}

		if (plugin.showInterfaceModelId && widget.getModelId() >= 0)
		{
			plugin.appendId(text, "M: " + widget.getModelId());
		}

		if (plugin.showInterfaceSpriteId && widget.getSpriteId() >= 0)
		{
			plugin.appendId(text, "S: " + widget.getSpriteId());
		}

		if (text.length() <= 0)
		{
			return;
		}

		int width = graphics.getFontMetrics().stringWidth(text.toString());
		int textX = Math.max(widget.getCanvasLocation().getX() - width / 2 + widget.getWidth() / 2, 15);
		int textY = widget.getCanvasLocation().getY() + widget.getHeight() / 2;

		graphics.setColor(plugin.colourInterface);
		graphics.drawString(text.toString(), textX, textY);
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
