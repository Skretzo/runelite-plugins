package com.menuentrydragdrop;

import com.google.inject.Inject;
import java.awt.event.MouseEvent;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
	name = "Menu Entry Drag-Drop",
	description = "Swap menu entries by holding ALT and moving them around with the mouse",
	tags = {"menu", "entry", "drag", "drop", "swapper"}
)
public class MenuEntryDragDropPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	private int hoverMenuEntryIdx;

	@Getter(AccessLevel.PACKAGE)
	private boolean isHotKeyPressed;

	@Getter(AccessLevel.PACKAGE)
	private boolean isMenuOpen;

	@Getter(AccessLevel.PACKAGE)
	private boolean isSwapping;

	private int lastHoverMenuEntryIdx = -1;

	private HotkeyListener hotkeyListener = new HotkeyListener(() -> Keybind.ALT)
	{
		@Override
		public void hotkeyPressed()
		{
			isHotKeyPressed = true;
		}

		@Override
		public void hotkeyReleased()
		{
			isHotKeyPressed = false;
		}
	};

	private MouseAdapter mouseListener = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1 && e.isAltDown())
			{
				isSwapping = true;
				e.consume();
			}
			return super.mousePressed(e);
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1 && e.isAltDown())
			{
				isSwapping = false;
				e.consume();
			}
			return super.mouseReleased(e);
		}
	};

	@Inject
	private Client client;

	@Inject
	private MenuEntryDragDropOverlay menuOverlay;

	@Inject
	private KeyManager keyManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(hotkeyListener);
		mouseManager.registerMouseListener(mouseListener);
		overlayManager.add(menuOverlay);
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(hotkeyListener);
		mouseManager.unregisterMouseListener(mouseListener);
		overlayManager.remove(menuOverlay);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		isMenuOpen = false;
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		isMenuOpen = true;
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		MenuEntry[] entries = client.getMenuEntries();
		hoverMenuEntryIdx = hoveredMenuEntry(entries);

		if (isMenuOpen && isSwapping && isHotKeyPressed && hoverMenuEntryIdx >= 0 && hoverMenuEntryIdx != lastHoverMenuEntryIdx)
		{
			MenuEntry entrySwap = entries[hoverMenuEntryIdx];
			entries[hoverMenuEntryIdx] = entries[lastHoverMenuEntryIdx];
			entries[lastHoverMenuEntryIdx] = entrySwap;
			client.setMenuEntries(entries);
		}
		lastHoverMenuEntryIdx = hoverMenuEntryIdx;
	}

	private int hoveredMenuEntry(final MenuEntry[] menuEntries)
	{
		final int menuX = client.getMenuX();
		final int menuY = client.getMenuY();
		final int menuWidth = client.getMenuWidth();
		final Point mousePosition = client.getMouseCanvasPosition();

		int dy = mousePosition.getY() - menuY;
		dy -= 19; // Height of Choose Option
		if (dy < 0)
		{
			return -1;
		}

		int idx = dy / 15; // Height of each menu option
		idx = menuEntries.length - 1 - idx;

		if (mousePosition.getX() > menuX && mousePosition.getX() < menuX + menuWidth &&
			idx >= 0 && idx < menuEntries.length)
		{
			return idx;
		}
		return -1;
	}
}
