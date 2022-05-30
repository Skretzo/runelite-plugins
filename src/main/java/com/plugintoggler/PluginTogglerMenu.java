package com.plugintoggler;

import com.google.common.base.Strings;
import java.awt.Color;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.util.ColorUtil;
import org.apache.commons.text.CaseUtils;
import org.apache.commons.text.WordUtils;

enum PluginTogglerMenu
{
	COMBAT_LEVEL(new PluginTogglerType(WidgetInfo.COMBAT_LEVEL), ColorUtil.wrapWithColorTag("Accurate combat level", JagexColors.MENU_TARGET), "", ""),
	FISHING(new PluginTogglerType(MenuAction.EXAMINE_NPC), ColorUtil.wrapWithColorTag("Fishing info", Color.YELLOW), "", "Fishing spot"),
	MINING(new PluginTogglerType(MenuAction.GAME_OBJECT_FIRST_OPTION), ColorUtil.wrapWithColorTag("Mining info", Color.CYAN), "Mine", ""),
	REPORT_BUTTON(new PluginTogglerType(WidgetInfo.CHATBOX_REPORT_TEXT), "clock", "Report game bug", ""),
	WOODCUTTING(new PluginTogglerType(MenuAction.GAME_OBJECT_FIRST_OPTION), ColorUtil.wrapWithColorTag("Woodcutting info", Color.CYAN), "Chop down", "");

	public static final String MENU_DISPLAY_OPTION = "Toggle";

	private final PluginTogglerType pluginTogglerType;
	@Getter
	private final String menuDisplayTarget;
	private final String menuFindOption;
	private final String menuFindTarget;

	@Setter
	private boolean configEnabled;

	PluginTogglerMenu(
		PluginTogglerType pluginTogglerType,
		String menuDisplayTarget,
		String menuFindOption,
		String menuFindTarget)
	{
		this.pluginTogglerType = pluginTogglerType;
		this.menuDisplayTarget = menuDisplayTarget;
		this.menuFindOption = menuFindOption;
		this.menuFindTarget = menuFindTarget;
	}

	public String getPluginName()
	{
		return WordUtils.capitalizeFully(name(), '_').replace('_', ' ');
	}

	public String getConfigName()
	{
		return CaseUtils.toCamelCase(name(), false, '_');
	}

	public boolean equals(MenuAction menuAction, Client client, String menuFindOption, String menuFindTarget)
	{
		return configEnabled && pluginTogglerType.equals(menuAction, client) &&
			(menuFindOption.equals(this.menuFindOption) || Strings.isNullOrEmpty(this.menuFindOption)) &&
			(menuFindTarget.contains(this.menuFindTarget)|| Strings.isNullOrEmpty(this.menuFindTarget));
	}

	public boolean displayEquals(String menuDisplayOption, String menuDisplayTarget)
	{
		return MENU_DISPLAY_OPTION.equals(menuDisplayOption) && this.menuDisplayTarget.equals(menuDisplayTarget);
	}
}
