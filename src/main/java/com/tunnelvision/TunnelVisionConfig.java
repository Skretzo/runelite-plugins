package com.tunnelvision;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(TunnelVisionConfig.GROUP)
public interface TunnelVisionConfig extends Config
{
	String GROUP = "tunnelvision";
	String RADIUS_KEY = "radius";

	@Range(min = 10, max = 3000)
	@Units(Units.PIXELS)
	@ConfigItem(
		keyName = RADIUS_KEY,
		name = "Circle radius",
		description = "Radius of the visible circle. Holding ALT and scrolling also changes (and saves) this.",
		position = 1
	)
	default int radius()
	{
		return 200;
	}

	@Range(min = 1, max = 200)
	@Units(Units.PIXELS)
	@ConfigItem(
		keyName = "scrollStep",
		name = "ALT + scroll step",
		description = "How much the radius changes per scroll tick while holding ALT",
		position = 2
	)
	default int scrollStep()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "hideWhenCursorAway",
		name = "Hide when cursor away",
		description = "Hide the client completely when the mouse leaves the client's area. "
			+ "Moving the mouse anywhere back over that area brings the circle back under the cursor.",
		position = 3
	)
	default boolean hideWhenCursorAway()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fullWindowHotkey",
		name = "Full window hotkey",
		description = "Toggles between the circle and the full window, so you can move and resize the client",
		position = 4
	)
	default Keybind fullWindowHotkey()
	{
		return new Keybind(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	}

	@ConfigItem(
		keyName = "alwaysShowTitleBar",
		name = "Always show title bar",
		description = "Always show the window's title bar (minimise, maximise and close buttons) as well as the circle. "
			+ "When off, the title bar is only visible where the circle passes over it.",
		position = 5
	)
	default boolean alwaysShowTitleBar()
	{
		return false;
	}

	@ConfigItem(
		keyName = "keepSidebar",
		name = "Keep sidebar visible",
		description = "Always show the RuneLite sidebar (plugin icons and panels) as well as the circle",
		position = 6
	)
	default boolean keepSidebar()
	{
		return false;
	}
}
