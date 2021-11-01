package com.plugintoggler;

import com.google.common.base.Strings;
import java.awt.Color;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ColorUtil;

enum PluginTogglerMenu
{
	COMBAT_LEVEL("Combat Level", MenuAction.RUNELITE_OVERLAY,
		ColorUtil.wrapWithColorTag("Accurate combat level", JagexColors.MENU_TARGET), "", ""),
	FISHING("Fishing", MenuAction.EXAMINE_NPC,
		ColorUtil.wrapWithColorTag("Fishing info", Color.YELLOW), "", "Fishing spot"),
	MINING("Mining", MenuAction.GAME_OBJECT_FIRST_OPTION,
			ColorUtil.wrapWithColorTag("Mining info", Color.CYAN), "Mine", ""),
	REPORT_BUTTON("Report Button", MenuAction.CC_OP, "clock", "Report game bug", ""),
	WOODCUTTING("Woodcutting", MenuAction.GAME_OBJECT_FIRST_OPTION,
		ColorUtil.wrapWithColorTag("Woodcutting info", Color.CYAN), "Chop down", "");

	public static final String MENU_DISPLAY_OPTION = "Toggle";

	@Getter
	private final String pluginName;
	private final MenuAction menuAction;
	@Getter
	private final String menuDisplayTarget;
	private final String menuFindOption;
	private final String menuFindTarget;

	@Setter
	private boolean configEnabled;

	PluginTogglerMenu(
		String pluginName,
		MenuAction menuAction,
		String menuDisplayTarget,
		String menuFindOption,
		String menuFindTarget)
	{
		this.pluginName = pluginName;
		this.menuAction = menuAction;
		this.menuDisplayTarget = menuDisplayTarget;
		this.menuFindOption = menuFindOption;
		this.menuFindTarget = menuFindTarget;
	}

	public boolean equals(final MenuAction menuAction, final String menuFindOption, final String menuFindTarget)
	{
		return configEnabled && this.menuAction.equals(menuAction) &&
			(menuFindOption.equals(this.menuFindOption) || Strings.isNullOrEmpty(this.menuFindOption)) &&
			(menuFindTarget.contains(this.menuFindTarget)|| Strings.isNullOrEmpty(this.menuFindTarget));
	}

	public boolean displayEquals(final String menuDisplayTarget)
	{
		return this.menuDisplayTarget.equals(menuDisplayTarget);
	}
}
