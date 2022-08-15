package com.menuentrydragdrop;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class MenuEntryDragDropOverlay extends Overlay
{
	private final Client client;
	private final MenuEntryDragDropPlugin plugin;

	@Inject
	private MenuEntryDragDropOverlay(Client client, MenuEntryDragDropPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;

		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.isMenuOpen() && plugin.getHoverMenuEntryIdx() >= 0 && plugin.isHotKeyPressed())
		{
			int y = client.getMenuY() + client.getMenuHeight() - plugin.getHoverMenuEntryIdx() * 15 - 19;
			graphics.setColor(new Color(255, 255, 0, plugin.isSwapping() ? 200 : 100));
			graphics.drawRect(client.getMenuX(), y, client.getMenuWidth(), 15);
		}
		return null;
	}
}
