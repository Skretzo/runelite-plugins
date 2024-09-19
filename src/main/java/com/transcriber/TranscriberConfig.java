package com.transcriber;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("transcriber")
public interface TranscriberConfig extends Config
{
	@ConfigItem(
		keyName = "animationIds",
		name = "Include animation IDs",
		description = "Whether to include animation IDs in the transcript, e.g. <animationID=100>"
	)
	default boolean animationIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "fontIds",
		name = "Include font IDs",
		description = "Whether to include font IDs in the transcript, e.g. <fontID=497>"
	)
	default boolean fontIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "itemIds",
		name = "Include item IDs",
		description = "Whether to include item IDs in the transcript, e.g. <itemID=4151>"
	)
	default boolean itemIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "modelIds",
		name = "Include model IDs",
		description = "Whether to include model IDs in the transcript, e.g. <modelID=11365>"
	)
	default boolean modelIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "spriteIds",
		name = "Include sprite IDs",
		description = "Whether to include sprite IDs in the transcript, e.g. <spriteID=537>"
	)
	default boolean spriteIds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "removeUnnecessaryTags",
		name = "Remove unnecessary colour tags",
		description = "Whether to exclude unnecessary black colour tags, e.g. <col=000000>"
	)
	default boolean removeUnnecessaryTags()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTranscriptionOutline",
		name = "Show transcription outline",
		description = "Whether to outline the selected portion of the transcription"
	)
	default boolean showTranscriptionOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "transcribeNpcOverheadText",
		name = "Transcribe NPC overhead text",
		description = "Whether to transcribe overhead text for NPCs"
	)
	default boolean transcribeNpcOverheadText()
	{
		return true;
	}

	@ConfigItem(
		keyName = "widgetBlacklist",
		name = "Blacklist",
		description = "A list of widget group IDs to exclude" +
			"<br>Examples:" +
			"<br>4 = " +
			"<br>7 = CHAT_CHANNEL_TAB" +
			"<br>12 = BANK_INVENTORY" +
			"<br>15 = BANK_INVENTORY_TAB" +
			"<br>65 = " +
			"<br>69 = WORLD_SWITCHER_TAB" +
			"<br>76 = GROUPING_TAB" +
			"<br>77 = QUICK_PRAYERS" +
			"<br>84 = EQUIPMENT" +
			"<br>94 = CHAT_CHANNEL" +
			"<br>109 = ACCOUNT_MANAGEMENT_TAB" +
			"<br>116 = SETTINGS_TAB" +
			"<br>122 = " +
			"<br>134 = SETTINGS" +
			"<br>137 = EXPERIENCE_TRACKER" +
			"<br>149 = INVENTORY_TAB" +
			"<br>156 = REPORT_GAME_BUG" +
			"<br>160 = " +
			"<br>162 = CHATBOX_PARENT" +
			"<br>182 = LOGOUT_TAB" +
			"<br>184 = CERTER" +
			"<br>193 = DIALOG_SPRITE_TEXT" +
			"<br>201 = " +
			"<br>214 = SKILLGUIDE" +
			"<br>216 = EMOTES_TAB" +
			"<br>217 = " +
			"<br>218 = MAGIC_SPELLS" +
			"<br>219 = " +
			"<br>229 = DIALOG_TEXT" +
			"<br>231 = DIALOG_NPC" +
			"<br>237 = GRAND_EXCHANGE_PRICE_EXAMPLES" +
			"<br>238 = " +
			"<br>239 = MUSIC_TAB" +
			"<br>245 = " +
			"<br>259 = " +
			"<br>278 = " +
			"<br>300 = SHOP_INVENTORY" +
			"<br>310 = POLL_BOOTH_OVERVIEW" +
			"<br>320 = SKILLS_TAB" +
			"<br>345 = POLL_BOOTH" +
			"<br>370 = " +
			"<br>372 = " +
			"<br>378 = " +
			"<br>383 = GRAND_EXCHANGE_HISTORY" +
			"<br>387 = EQUIPMENT_TAB" +
			"<br>399 = QUEST_LIST_TAB" +
			"<br>402 = COLLECTION_BOX" +
			"<br>429 = FRIENDS_TAB" +
			"<br>432 = IGNORE_TAB" +
			"<br>451 = GRAND_EXCHANGE_ITEM_SETS" +
			"<br>464 = " +
			"<br>465 = GRAND_EXCHANGE" +
			"<br>516 = MAKEOVER" +
			"<br>527 = MUSEUM_MAP" +
			"<br>528 = MUSEUM_DISPLAY" +
			"<br>534 = MUSEUM_DISPLAY" +
			"<br>541 = PRAYERS_TAB" +
			"<br>553 = " +
			"<br>593 = COMBAT_OPTIONS_TAB" +
			"<br>595 = WORLDMAP" +
			"<br>600 = " +
			"<br>621 = COLLECTION_LOG" +
			"<br>626 = " +
			"<br>629 = " +
			"<br>659 = NEWSPAPER" +
			"<br>701 = " +
			"<br>702 = " +
			"<br>707 = CLAN_TAB" +
			"<br>712 = " +
			"<br>713 = " +
			"<br>714 = " +
			"<br>715 = " +
			"<br>716 = " +
			"<br>717 = COMBAT_ACHIEVEMENTS" +
			"<br>741 = ACHIEVEMENT_DIARY_JOURNAL" +
			"<br>782 = QUEST_JOURNAL" +
			"<br>821 = WORLD_SWITCHER_SETTINGS" +
			"<br>861 = BOND_POUCH" +
			"<br>875 = REPORT_ABUSE",
		position = 100
	)
	default String widgetBlacklist()
	{
		return "4,7,65,69,76,77,84,94,109,116,122,134,137,149,156,160,162,182,193,201,216,217," +
			"218,219,229,231,238,239,245,259,278,310,320,370,372,378,387,399,429,432,464," +
			"527,541,553,593,595,600,621,626,629,701,702,707,712,713,714,715,716,717";
	}
}
