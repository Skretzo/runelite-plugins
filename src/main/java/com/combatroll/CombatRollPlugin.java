package com.combatroll;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Combat Roll",
	description = "Player attack and defence roll"
)
public class CombatRollPlugin extends Plugin
{
	private static final int EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID = 12;
	private static final int EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET = 66;
	private static final int EQUIPMENT_STATS_WIDGET_GROUP_ID = 84;

	/**
	 * Equipment attack bonus widget child IDs
	 */
	private static final int ATTACK_HEADER = 23;
	private static final int ATTACK_STAB = ATTACK_HEADER + 1;
	private static final int ATTACK_SLASH = ATTACK_HEADER + 2;
	private static final int ATTACK_CRUSH = ATTACK_HEADER + 3;
	private static final int ATTACK_MAGIC = ATTACK_HEADER + 4;
	private static final int ATTACK_RANGED = ATTACK_HEADER + 5;

	/**
	 * Equipment defence bonus widget child IDs
	 */
	private static final int DEFENCE_HEADER = ATTACK_HEADER + 6;
	private static final int DEFENCE_STAB = ATTACK_HEADER + 7;
	private static final int DEFENCE_SLASH = ATTACK_HEADER + 8;
	private static final int DEFENCE_CRUSH = ATTACK_HEADER + 9;
	private static final int DEFENCE_MAGIC = ATTACK_HEADER + 10;
	private static final int DEFENCE_RANGED = ATTACK_HEADER + 11;

	private static final List<Integer> EQUIPMENT_STATS_WIDGET_CHILD_IDS = Arrays.asList(
		ATTACK_STAB, ATTACK_SLASH, ATTACK_CRUSH, ATTACK_MAGIC, ATTACK_RANGED,
		DEFENCE_STAB, DEFENCE_SLASH, DEFENCE_CRUSH, DEFENCE_MAGIC, DEFENCE_RANGED
	);

	private Map<Integer, Integer> combatBonuses = new HashMap<>();
	private Map<Integer, String> original = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp()
	{
		clientThread.invokeLater(() ->
		{
			updateInfo(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0);
			updateInfo(EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID, EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET);
		});
	}

	@Override
	protected void shutDown()
	{
		updateEquipmentStats(client.getWidget(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0), 0, true);
		updateEquipmentStats(client.getWidget(EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID, 0), EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET, true);
		setInfo(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0, ATTACK_HEADER, " ", "bonus");
		setInfo(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0, DEFENCE_HEADER, " ", "bonus");
		setInfo(EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID, EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET, ATTACK_HEADER, " ", "bonus");
		setInfo(EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID, EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET, DEFENCE_HEADER, " ", "bonus");
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (event.getScriptId() == 545 && event.getScriptEvent() != null) // [clientscript,wear_updateslot]
		{
			Widget widget = event.getScriptEvent().getSource();

			int groupId = WidgetUtil.componentToInterface(widget.getId());
			int childId = WidgetUtil.componentToId(widget.getId());
			int indexSlotHelm = 10;

			if (groupId == EQUIPMENT_STATS_WIDGET_GROUP_ID && childId == indexSlotHelm)
			{
				updateInfo(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0);
			}
			else if (groupId == EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID && childId == indexSlotHelm + EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET)
			{
				updateInfo(EQUIPMENT_STATS_BANK_WIDGET_GROUP_ID, EQUIPMENT_STATS_BANK_WIDGET_CHILD_OFFSET);
			}
		}
	}

	private boolean hasEquipment(int... ids)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipment != null)
		{
			for (int id : ids)
			{
				if (equipment.contains(id))
				{
					return true;
				}
			}
		}

		return false;
	}

	private void updateInfo(int widgetId, int offset)
	{
		Widget equipmentScreen = client.getWidget(widgetId, 0);

		if (equipmentScreen == null || equipmentScreen.isHidden())
		{
			return;
		}

		updateEquipmentStats(equipmentScreen, offset, false);

		setInfo(widgetId, offset, ATTACK_HEADER, " ", "roll");
		showRoll(widgetId, offset, ATTACK_STAB, calculateRoll(combatBonuses.getOrDefault(ATTACK_STAB, 0), CombatRoll.OFFENSIVE_MELEE));
		showRoll(widgetId, offset, ATTACK_SLASH, calculateRoll(combatBonuses.getOrDefault(ATTACK_SLASH, 0), CombatRoll.OFFENSIVE_MELEE));
		showRoll(widgetId, offset, ATTACK_CRUSH, calculateRoll(combatBonuses.getOrDefault(ATTACK_CRUSH, 0), CombatRoll.OFFENSIVE_MELEE));
		showRoll(widgetId, offset, ATTACK_MAGIC, calculateRoll(combatBonuses.getOrDefault(ATTACK_MAGIC, 0), CombatRoll.OFFENSIVE_MAGIC));
		showRoll(widgetId, offset, ATTACK_RANGED, calculateRoll(combatBonuses.getOrDefault(ATTACK_RANGED, 0), CombatRoll.OFFENSIVE_RANGED));

		setInfo(widgetId, offset, DEFENCE_HEADER, " ", "roll");
		showRoll(widgetId, offset, DEFENCE_STAB, calculateRoll(combatBonuses.getOrDefault(DEFENCE_STAB, 0), CombatRoll.DEFENSIVE_MELEE));
		showRoll(widgetId, offset, DEFENCE_SLASH, calculateRoll(combatBonuses.getOrDefault(DEFENCE_SLASH, 0), CombatRoll.DEFENSIVE_MELEE));
		showRoll(widgetId, offset, DEFENCE_CRUSH, calculateRoll(combatBonuses.getOrDefault(DEFENCE_CRUSH, 0), CombatRoll.DEFENSIVE_MELEE));
		showRoll(widgetId, offset, DEFENCE_MAGIC, calculateRoll(combatBonuses.getOrDefault(DEFENCE_MAGIC, 0), CombatRoll.DEFENSIVE_MAGIC));
		showRoll(widgetId, offset, DEFENCE_RANGED, calculateRoll(combatBonuses.getOrDefault(DEFENCE_RANGED, 0), CombatRoll.DEFENSIVE_RANGED));
	}

	private void setInfo(int widgetId, int offset, int displayIndex, String delim, String roll)
	{
		Widget widget = client.getWidget(widgetId, offset + displayIndex);

		if (widget != null)
		{
			widget.setText(widget.getText().split(delim)[0] + delim + roll);
		}
	}

	private void showRoll(int widgetId, int offset, int displayIndex, int roll)
	{
		setInfo(widgetId, offset, displayIndex, ": ", Integer.toString(roll));
	}

	private int calculateRoll(int equipmentBonus, CombatRoll rollType)
	{
		final int weaponTypeValue = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
		final int attackStyleValue = client.getVarpValue(VarPlayer.ATTACK_STYLE) +
			client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

		AttackStyle attackStyle = WeaponType.getAttackStyle(weaponTypeValue, attackStyleValue);

		final boolean isMagicDefenceRoll = CombatRoll.DEFENSIVE_MAGIC.equals(rollType);
		if (isMagicDefenceRoll)
		{
			rollType = CombatRoll.DEFENSIVE_MELEE;
		}

		int effectiveLevel = getSkillLevel(rollType);
		effectiveLevel = applyPrayerBoost(effectiveLevel, rollType);
		effectiveLevel = applyStanceBonus(effectiveLevel, rollType, attackStyle);
		effectiveLevel = applyAdjustmentConstant(effectiveLevel, isMagicDefenceRoll);
		effectiveLevel = applyVoidBoost(effectiveLevel, rollType);

		if (isMagicDefenceRoll)
		{
			rollType = CombatRoll.DEFENSIVE_MAGIC;
			int effectiveLevelDefence = (effectiveLevel * 30) / 100;
			int effectiveLevelMagic;
			effectiveLevelMagic = getSkillLevel(rollType);
			effectiveLevelMagic = applyPrayerBoost(effectiveLevelMagic, rollType);
			effectiveLevelMagic = applyStanceBonus(effectiveLevelMagic, rollType, attackStyle);
			effectiveLevelMagic = applyAdjustmentConstant(effectiveLevelMagic, isMagicDefenceRoll);
			effectiveLevelMagic = applyVoidBoost(effectiveLevelMagic, rollType);
			effectiveLevelMagic = (effectiveLevelMagic * 70) / 100;
			effectiveLevel = effectiveLevelDefence + effectiveLevelMagic;
		}

		int roll = effectiveLevel * (equipmentBonus + 64);
		roll = Math.max(applySlayerBoost(roll, rollType), applySalveBoost(roll, rollType));

		return roll;
	}

	private int getSkillLevel(CombatRoll rollType)
	{
		Skill skill = null;

		switch (rollType)
		{
			case OFFENSIVE_MELEE:
				skill = Skill.ATTACK;
				break;
			case OFFENSIVE_RANGED:
				skill = Skill.RANGED;
				break;
			case OFFENSIVE_MAGIC:
			case DEFENSIVE_MAGIC:
				skill = Skill.MAGIC;
				break;
			case DEFENSIVE_MELEE:
			case DEFENSIVE_RANGED:
				skill = Skill.DEFENCE;
				break;
		}

		return client.getBoostedSkillLevel(skill);
	}

	private int applyPrayerBoost(int level, CombatRoll rollType)
	{
		final int SCALE = 100;
		int boost = SCALE;

		switch (rollType)
		{
			case OFFENSIVE_MELEE:
				boost = client.isPrayerActive(Prayer.CLARITY_OF_THOUGHT) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.IMPROVED_REFLEXES) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.INCREDIBLE_REFLEXES) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.CHIVALRY) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.PIETY) ? 120 : boost;
				break;
			case OFFENSIVE_RANGED:
				boost = client.isPrayerActive(Prayer.SHARP_EYE) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.HAWK_EYE) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.EAGLE_EYE) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.RIGOUR) ? 120 : boost;
				break;
			case OFFENSIVE_MAGIC:
				boost = client.isPrayerActive(Prayer.MYSTIC_WILL) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_LORE) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_MIGHT) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.AUGURY) ? 125 : boost;
				break;
			case DEFENSIVE_MELEE:
			case DEFENSIVE_RANGED:
			case DEFENSIVE_MAGIC:
				boost = client.isPrayerActive(Prayer.MYSTIC_WILL) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_LORE) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_MIGHT) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.THICK_SKIN) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.ROCK_SKIN) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.STEEL_SKIN) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.CHIVALRY) ? 120 : boost;
				boost = client.isPrayerActive(Prayer.PIETY) ? 125 : boost;
				boost = client.isPrayerActive(Prayer.RIGOUR) ? 125 : boost;
				boost = client.isPrayerActive(Prayer.AUGURY) ? 125 : boost;
				break;
		}

		return (level * boost) / SCALE;
	}

	private int applyStanceBonus(int level, CombatRoll rollType, AttackStyle attackStyle)
	{
		int bonus = 0;

		switch (rollType)
		{
			case OFFENSIVE_MELEE:
				bonus = AttackStyle.ACCURATE.equals(attackStyle) ? 3 : bonus;
				bonus = AttackStyle.CONTROLLED.equals(attackStyle) ? 1 : bonus;
				break;
			case OFFENSIVE_RANGED:
				bonus = AttackStyle.ACCURATE.equals(attackStyle) ? 3 : bonus;
				bonus = AttackStyle.SHORT_FUSE.equals(attackStyle) ? 3 : bonus;
				break;
			case OFFENSIVE_MAGIC:
				bonus = AttackStyle.ACCURATE.equals(attackStyle) ? 3 : bonus;
				bonus = AttackStyle.LONGRANGE.equals(attackStyle) ? 1 : bonus;
				break;
			case DEFENSIVE_MELEE:
				bonus = AttackStyle.CONTROLLED.equals(attackStyle) ? 1 : bonus;
				bonus = AttackStyle.DEFENSIVE.equals(attackStyle) ? 3 : bonus;
				bonus = AttackStyle.LONGRANGE.equals(attackStyle) ? 3 : bonus;
				break;
		}

		return level + bonus;
	}

	private int applyAdjustmentConstant(int level, boolean isMagicDefenceRoll)
	{
		int ADJUSTMENT_CONSTANT = 8;
		if (isMagicDefenceRoll)
		{
			ADJUSTMENT_CONSTANT = 0;
		}
		return level + ADJUSTMENT_CONSTANT;
	}

	private int applyVoidBoost(int level, CombatRoll rollType)
	{
		final int SCALE = 100;
		int boost = SCALE;

		boolean gloves = hasEquipment(ItemID.VOID_KNIGHT_GLOVES, ItemID.VOID_KNIGHT_GLOVES_L,
			ItemID.VOID_KNIGHT_GLOVES_LOR, ItemID.VOID_KNIGHT_GLOVES_OR);
		boolean legs = hasEquipment(ItemID.VOID_KNIGHT_ROBE, ItemID.VOID_KNIGHT_ROBE_L,
			ItemID.VOID_KNIGHT_ROBE_LOR, ItemID.VOID_KNIGHT_ROBE_OR);
		boolean eliteLegs = hasEquipment(ItemID.ELITE_VOID_ROBE, ItemID.ELITE_VOID_ROBE_L,
			ItemID.ELITE_VOID_ROBE_LOR, ItemID.ELITE_VOID_ROBE_OR);
		boolean body = hasEquipment(ItemID.VOID_KNIGHT_TOP, ItemID.VOID_KNIGHT_TOP_L,
			ItemID.VOID_KNIGHT_TOP_LOR, ItemID.VOID_KNIGHT_TOP_OR);
		boolean eliteBody = hasEquipment(ItemID.ELITE_VOID_TOP, ItemID.ELITE_VOID_TOP_L,
			ItemID.ELITE_VOID_TOP_LOR, ItemID.ELITE_VOID_TOP_OR);
		boolean helmMelee = hasEquipment(ItemID.VOID_MELEE_HELM, ItemID.VOID_MELEE_HELM_L,
			ItemID.VOID_MELEE_HELM_LOR, ItemID.VOID_MELEE_HELM_OR);
		boolean helmRanged = hasEquipment(ItemID.VOID_RANGER_HELM, ItemID.VOID_RANGER_HELM_L,
			ItemID.VOID_RANGER_HELM_LOR, ItemID.VOID_RANGER_HELM_OR);
		boolean helmMagic = hasEquipment(ItemID.VOID_MAGE_HELM, ItemID.VOID_MAGE_HELM_L,
			ItemID.VOID_MAGE_HELM_LOR, ItemID.VOID_MAGE_HELM_OR);

		boolean set = gloves && (legs || eliteLegs) && (body || eliteBody) && (helmMelee || helmRanged || helmMagic);

		if (set)
		{
			switch (rollType)
			{
				case OFFENSIVE_MELEE:
				case OFFENSIVE_RANGED:
					boost = (helmMelee || helmRanged) ? 110 : boost;
					break;
				case OFFENSIVE_MAGIC:
					boost = helmMagic ? 145 : boost;
					break;
			}
		}

		return (level * boost) / SCALE;
	}

	private int applySlayerBoost(int roll, CombatRoll rollType)
	{
		final int SCALE = 60;
		int boost = SCALE;

		boolean helm = hasEquipment(ItemID.SLAYER_HELMET, ItemID.BLACK_SLAYER_HELMET,
			ItemID.GREEN_SLAYER_HELMET, ItemID.HYDRA_SLAYER_HELMET, ItemID.PURPLE_SLAYER_HELMET,
			ItemID.RED_SLAYER_HELMET, ItemID.TURQUOISE_SLAYER_HELMET, ItemID.TWISTED_SLAYER_HELMET,
			ItemID.TZKAL_SLAYER_HELMET, ItemID.TZTOK_SLAYER_HELMET, ItemID.VAMPYRIC_SLAYER_HELMET);
		boolean helmImbued = hasEquipment(ItemID.SLAYER_HELMET_I, ItemID.SLAYER_HELMET_I_25177, ItemID.SLAYER_HELMET_I_26674,
			ItemID.BLACK_SLAYER_HELMET_I, ItemID.BLACK_SLAYER_HELMET_I_25179, ItemID.BLACK_SLAYER_HELMET_I_26675,
			ItemID.GREEN_SLAYER_HELMET_I, ItemID.GREEN_SLAYER_HELMET_I_25181, ItemID.GREEN_SLAYER_HELMET_I_26676,
			ItemID.HYDRA_SLAYER_HELMET_I, ItemID.HYDRA_SLAYER_HELMET_I_25189, ItemID.HYDRA_SLAYER_HELMET_I_26680,
			ItemID.PURPLE_SLAYER_HELMET_I, ItemID.PURPLE_SLAYER_HELMET_I_25185, ItemID.PURPLE_SLAYER_HELMET_I_26678,
			ItemID.RED_SLAYER_HELMET_I, ItemID.RED_SLAYER_HELMET_I_25183, ItemID.RED_SLAYER_HELMET_I_26677,
			ItemID.TURQUOISE_SLAYER_HELMET_I, ItemID.TURQUOISE_SLAYER_HELMET_I_25187, ItemID.TURQUOISE_SLAYER_HELMET_I_26679,
			ItemID.TWISTED_SLAYER_HELMET_I, ItemID.TWISTED_SLAYER_HELMET_I_25191, ItemID.TWISTED_SLAYER_HELMET_I_26681,
			ItemID.TZKAL_SLAYER_HELMET_I, ItemID.TZKAL_SLAYER_HELMET_I_25914, ItemID.TZKAL_SLAYER_HELMET_I_26684,
			ItemID.TZTOK_SLAYER_HELMET_I, ItemID.TZTOK_SLAYER_HELMET_I_25902, ItemID.TZTOK_SLAYER_HELMET_I_26682,
			ItemID.VAMPYRIC_SLAYER_HELMET_I, ItemID.VAMPYRIC_SLAYER_HELMET_I_25908, ItemID.VAMPYRIC_SLAYER_HELMET_I_26683);
		boolean mask = hasEquipment(ItemID.BLACK_MASK, ItemID.BLACK_MASK_1, ItemID.BLACK_MASK_2,
			ItemID.BLACK_MASK_3, ItemID.BLACK_MASK_4, ItemID.BLACK_MASK_5, ItemID.BLACK_MASK_6,
			ItemID.BLACK_MASK_7, ItemID.BLACK_MASK_8, ItemID.BLACK_MASK_9, ItemID.BLACK_MASK_10);
		boolean maskImbued = hasEquipment(ItemID.BLACK_MASK_I, ItemID.BLACK_MASK_I_25276, ItemID.BLACK_MASK_I_26781,
			ItemID.BLACK_MASK_1_I, ItemID.BLACK_MASK_1_I_25275, ItemID.BLACK_MASK_1_I_26780,
			ItemID.BLACK_MASK_2_I, ItemID.BLACK_MASK_2_I_25274, ItemID.BLACK_MASK_2_I_26779,
			ItemID.BLACK_MASK_3_I, ItemID.BLACK_MASK_3_I_25273, ItemID.BLACK_MASK_3_I_26778,
			ItemID.BLACK_MASK_4_I, ItemID.BLACK_MASK_4_I_25272, ItemID.BLACK_MASK_4_I_26777,
			ItemID.BLACK_MASK_5_I, ItemID.BLACK_MASK_5_I_25271, ItemID.BLACK_MASK_5_I_26776,
			ItemID.BLACK_MASK_6_I, ItemID.BLACK_MASK_6_I_25270, ItemID.BLACK_MASK_6_I_26775,
			ItemID.BLACK_MASK_7_I, ItemID.BLACK_MASK_7_I_25269, ItemID.BLACK_MASK_7_I_26774,
			ItemID.BLACK_MASK_8_I, ItemID.BLACK_MASK_8_I_25268, ItemID.BLACK_MASK_8_I_26773,
			ItemID.BLACK_MASK_9_I, ItemID.BLACK_MASK_9_I_25267, ItemID.BLACK_MASK_9_I_26772,
			ItemID.BLACK_MASK_10_I, ItemID.BLACK_MASK_10_I_25266, ItemID.BLACK_MASK_10_I_26771);

		switch (rollType)
		{
			case OFFENSIVE_MELEE:
				boost = (helm || helmImbued || mask || maskImbued) ? 70 : boost; // +16.667 %
				break;
			case OFFENSIVE_RANGED:
			case OFFENSIVE_MAGIC:
				boost = (helmImbued || maskImbued) ? 69 : boost; // +15 %
				break;
		}

		return (roll * boost) / SCALE;
	}

	private int applySalveBoost(int roll, CombatRoll rollType)
	{
		final int SCALE = 60;
		int boost = SCALE;

		boolean salve = hasEquipment(ItemID.SALVE_AMULET);
		boolean salveI = hasEquipment(ItemID.SALVE_AMULETI, ItemID.SALVE_AMULETI_25250, ItemID.SALVE_AMULETI_26763);
		boolean salveE = hasEquipment(ItemID.SALVE_AMULET_E);
		boolean salveEI = hasEquipment(ItemID.SALVE_AMULETEI, ItemID.SALVE_AMULETEI_25278, ItemID.SALVE_AMULETEI_26782);

		switch (rollType)
		{
			case OFFENSIVE_MELEE:
				boost = (salve || salveI) ? 70 : boost; // +16.667 %
				boost = (salveE || salveEI) ? 72 : boost; // +20 %
				break;
			case OFFENSIVE_RANGED:
				boost = salveI ? 70 : boost; // +16.667 %
				boost = salveEI ? 72 : boost; // +20 %
				break;
			case OFFENSIVE_MAGIC:
				boost = salveI ? 69 : boost; // +15 %
				boost = salveEI ? 72 : boost; // +20 %
				break;
		}

		return (roll * boost) / SCALE;
	}

	private void updateEquipmentStats(Widget widget, int offset, boolean reset)
	{
		if (widget == null)
		{
			return;
		}

		int id = WidgetUtil.componentToId(widget.getId()) - offset;
		String text = widget.getText();
		if (reset && original.containsKey(id))
		{
			widget.setText(original.remove(id));
		}
		else if (EQUIPMENT_STATS_WIDGET_CHILD_IDS.contains(id) && !Strings.isNullOrEmpty(text))
		{
			parseEquipmentStat(id, text);
			original.put(id, text);
		}

		Widget[] children = widget.getStaticChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				updateEquipmentStats(child, offset, reset);
			}
		}
	}

	private void parseEquipmentStat(int id, String text)
	{
		if (Strings.isNullOrEmpty(text))
		{
			return;
		}

		int start = text.indexOf(": ");

		if (start >= 0)
		{
			try
			{
				combatBonuses.put(id, Integer.parseInt(text.substring(start + 2)));
			}
			catch (NumberFormatException ignored)
			{
			}
		}
	}
}
