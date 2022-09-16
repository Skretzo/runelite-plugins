package com.damagetracker;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class DamageTrackerConditionsOverlay extends Overlay
{
	private final Client client;
	private final DamageTrackerPlugin plugin;

	@Inject
	private DamageTrackerConditionsOverlay(Client client, DamageTrackerPlugin plugin)
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
		if (plugin.getRenameTracker() == null)
		{
			return null;
		}

		DamageTracker tracker = plugin.getRenameTracker().getTracker();

		Widget widgetEquipment = client.getWidget(WidgetInfo.EQUIPMENT);
		Widget widgetPrayer = client.getWidget(WidgetID.PRAYER_GROUP_ID, 0);
		Widget widgetAttackStyle = client.getWidget(WidgetID.COMBAT_GROUP_ID, 0);
		Widget widgetSkills = client.getWidget(WidgetInfo.SKILLS_CONTAINER);
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

		if (widgetEquipment != null && !widgetEquipment.isHidden() && equipment != null)
		{
			Map<Integer, Integer> equipmentIds = tracker.getEquipmentIds();
			for (int childId = 15; childId < 26; childId++)
			{
				Widget child = client.getWidget(WidgetInfo.TO_GROUP(widgetEquipment.getId()), childId);
				int slot = plugin.widgetChildIdToEquipmentSlot(childId);
				if (child != null && !child.isHidden() && equipmentIds.containsKey(slot))
				{
					Rectangle bounds = child.getBounds();
					graphics.setColor((equipment.getItem(slot) != null &&
						equipment.getItem(slot).getId() == equipmentIds.get(slot)) ? Color.GREEN : Color.ORANGE);
					graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
		}
		if (widgetPrayer != null && !widgetPrayer.isHidden())
		{
			List<Integer> prayerIds = tracker.getPrayerIds();
			Prayer[] prayers = Prayer.values();
			for (int childId = 5; childId < 34; childId++)
			{
				Widget child = client.getWidget(WidgetInfo.TO_GROUP(widgetPrayer.getId()), childId);
				int prayerIndex;
				if (child != null && !child.isHidden() &&
					prayerIds.contains(prayerIndex = plugin.prayerNameToPrayerIndex(child.getName())))
				{
					Rectangle bounds = child.getBounds();
					graphics.setColor(client.isPrayerActive(prayers[prayerIndex]) ? Color.GREEN : Color.ORANGE);
					graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
		}
		if (widgetAttackStyle != null && !widgetAttackStyle.isHidden())
		{
			Integer attackStyle = tracker.getAttackStyle();
			for (int childId : DamageTrackerPlugin.WEAPON_STANCE_WIDGET_CHILD_IDS)
			{
				Widget child = client.getWidget(WidgetInfo.TO_GROUP(widgetAttackStyle.getId()), childId);
				if (child != null && !child.isHidden() &&
					Integer.valueOf(plugin.widgetChildIdToAttackStyleIndex(childId)).equals(attackStyle))
				{
					Rectangle bounds = child.getBounds();
					graphics.setColor(client.getVarpValue(VarPlayer.ATTACK_STYLE.getId()) == attackStyle ?
						Color.GREEN : Color.ORANGE);
					graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
		}
		if (widgetSkills != null && !widgetSkills.isHidden())
		{
			Map<Integer, Integer> skillLevels = tracker.getSkillLevels();
			Skill[] skills = Skill.values();
			for (int childId = 1; childId < 24; childId++)
			{
				Widget child = client.getWidget(WidgetInfo.TO_GROUP(widgetSkills.getId()), childId);
				int skillIndex = plugin.widgetChildIdToSkillIndex(childId);
				if (child != null && !child.isHidden() && skillLevels.containsKey(skillIndex))
				{
					Rectangle bounds = child.getBounds();
					graphics.setColor(skillLevels.get(skillIndex).equals(client.getBoostedSkillLevel(skills[skillIndex])) ?
						Color.GREEN : Color.ORANGE);
					graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				}
			}
		}

		return null;
	}
}
