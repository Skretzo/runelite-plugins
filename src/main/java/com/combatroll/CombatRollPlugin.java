package com.combatroll;

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
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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
	private static final int EQUIPMENT_STATS_WIDGET_GROUP_ID = 84;
	private static final int EQUIPMENT_INVENTORY_CONTAINER_ID = 94;

	/**
	 * Equipment attack bonus widget child IDs
	 */
	private static final int ATTACK_HEADER = 23;
	private static final int ATTACK_STAB = 24;
	private static final int ATTACK_SLASH = 25;
	private static final int ATTACK_CRUSH = 26;
	private static final int ATTACK_MAGIC = 27;
	private static final int ATTACK_RANGED = 28;

	/**
	 * Equipment defence bonus widget child IDs
	 */
	private static final int DEFENCE_HEADER = 29;
	private static final int DEFENCE_STAB = 30;
	private static final int DEFENCE_SLASH = 31;
	private static final int DEFENCE_CRUSH = 32;
	private static final int DEFENCE_MAGIC = 33;
	private static final int DEFENCE_RANGED = 34;

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
	protected void shutDown()
	{
		updateEquipmentStats(client.getWidget(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0), true);
		setInfo(ATTACK_HEADER, " ", "bonus");
		setInfo(DEFENCE_HEADER, " ", "bonus");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == EQUIPMENT_STATS_WIDGET_GROUP_ID)
		{
			clientThread.invokeLater(this::updateInfo);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == EQUIPMENT_INVENTORY_CONTAINER_ID)
		{
			updateInfo();
		}
	}

	private void updateInfo()
	{
		Widget equipmentScreen = client.getWidget(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0);
		Widget equipmentInventory = client.getWidget(WidgetInfo.EQUIPMENT_INVENTORY_ITEMS_CONTAINER);

		if (equipmentScreen == null || equipmentScreen.isHidden() ||
			equipmentInventory == null || equipmentInventory.isHidden())
		{
			return;
		}

		updateEquipmentStats(client.getWidget(EQUIPMENT_STATS_WIDGET_GROUP_ID, 0), false);

		setInfo(ATTACK_HEADER, " ", "roll");
		showRoll(ATTACK_STAB, calculateRoll(combatBonuses.get(ATTACK_STAB), CombatRoll.OFFENSIVE_MELEE));
		showRoll(ATTACK_SLASH, calculateRoll(combatBonuses.get(ATTACK_SLASH), CombatRoll.OFFENSIVE_MELEE));
		showRoll(ATTACK_CRUSH, calculateRoll(combatBonuses.get(ATTACK_CRUSH), CombatRoll.OFFENSIVE_MELEE));
		showRoll(ATTACK_MAGIC, calculateRoll(combatBonuses.get(ATTACK_MAGIC), CombatRoll.OFFENSIVE_MAGIC));
		showRoll(ATTACK_RANGED, calculateRoll(combatBonuses.get(ATTACK_RANGED), CombatRoll.OFFENSIVE_RANGED));

		setInfo(DEFENCE_HEADER, " ", "roll");
		showRoll(DEFENCE_STAB, calculateRoll(combatBonuses.get(DEFENCE_STAB), CombatRoll.DEFENSIVE_MELEE));
		showRoll(DEFENCE_SLASH, calculateRoll(combatBonuses.get(DEFENCE_SLASH), CombatRoll.DEFENSIVE_MELEE));
		showRoll(DEFENCE_CRUSH, calculateRoll(combatBonuses.get(DEFENCE_CRUSH), CombatRoll.DEFENSIVE_MELEE));
		showRoll(DEFENCE_MAGIC, calculateRoll(combatBonuses.get(DEFENCE_MAGIC), CombatRoll.DEFENSIVE_MAGIC));
		showRoll(DEFENCE_RANGED, calculateRoll(combatBonuses.get(DEFENCE_RANGED), CombatRoll.DEFENSIVE_RANGED));
	}

	private void setInfo(int displayIndex, String delim, String roll)
	{
		Widget widget = client.getWidget(EQUIPMENT_STATS_WIDGET_GROUP_ID, displayIndex);

		if (widget != null)
		{
			widget.setText(widget.getText().split(delim)[0] + delim + roll);
		}
	}

	private void showRoll(int displayIndex, int roll)
	{
		setInfo(displayIndex, ": ", Integer.toString(roll));
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
		effectiveLevel = applyAdjustmentConstant(effectiveLevel);

		if (isMagicDefenceRoll)
		{
			rollType = CombatRoll.DEFENSIVE_MAGIC;
			int effectiveLevelDefence = (effectiveLevel * 30) / 100;
			int effectiveLevelMagic;
			effectiveLevelMagic = getSkillLevel(rollType);
			effectiveLevelMagic = applyPrayerBoost(effectiveLevelMagic, rollType);
			effectiveLevelMagic = (effectiveLevelMagic * 70) / 100;
			effectiveLevel = effectiveLevelDefence + effectiveLevelMagic;
		}

		int roll = effectiveLevel * (equipmentBonus + 64);

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
			case DEFENSIVE_RANGED:
				skill = Skill.RANGED;
				break;
			case OFFENSIVE_MAGIC:
			case DEFENSIVE_MAGIC:
				skill = Skill.MAGIC;
				break;
			case DEFENSIVE_MELEE:
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
			case DEFENSIVE_MAGIC:
				boost = client.isPrayerActive(Prayer.MYSTIC_WILL) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_LORE) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.MYSTIC_MIGHT) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.AUGURY) ? 125 : boost;
				break;
			case DEFENSIVE_MELEE:
				boost = client.isPrayerActive(Prayer.THICK_SKIN) ? 105 : boost;
				boost = client.isPrayerActive(Prayer.ROCK_SKIN) ? 110 : boost;
				boost = client.isPrayerActive(Prayer.STEEL_SKIN) ? 115 : boost;
				boost = client.isPrayerActive(Prayer.CHIVALRY) ? 120 : boost;
				boost = client.isPrayerActive(Prayer.PIETY) ? 125 : boost;
				break;
			case DEFENSIVE_RANGED:
				boost = client.isPrayerActive(Prayer.RIGOUR) ? 125 : boost;
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

	private int applyAdjustmentConstant(int level)
	{
		final int ADJUSTMENT_CONSTANT = 8;
		return level + ADJUSTMENT_CONSTANT;
	}

	private void updateEquipmentStats(Widget widget, boolean reset)
	{
		if (widget == null)
		{
			return;
		}

		int id = WidgetInfo.TO_CHILD(widget.getId());
		String text = widget.getText();
		if (reset && original.containsKey(id))
		{
			widget.setText(original.remove(id));
		}
		else if (EQUIPMENT_STATS_WIDGET_CHILD_IDS.contains(id) && text != null)
		{
			parseEquipmentStat(id, text);
			original.put(id, text);
		}

		Widget[] children = widget.getStaticChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				updateEquipmentStats(child, reset);
			}
		}
	}

	private void parseEquipmentStat(int id, String text)
	{
		if (text == null)
		{
			return;
		}

		int start = text.indexOf(": ") + 2;

		if (start >= 0)
		{
			try
			{
				combatBonuses.put(id, Integer.parseInt(text.substring(start)));
			}
			catch (NumberFormatException ignored)
			{
			}
		}
	}
}
