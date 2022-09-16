package com.damagetracker;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;

/**
 * Used for visualization of tracked damage
 */
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
class DamageTracker implements Comparable<DamageTracker>
{
	@Getter(AccessLevel.NONE)
	private final DamageTrackerPlugin plugin;
	private final long id;
	@Getter(AccessLevel.NONE)
	private final Map<Integer, Integer> damageDistribution;
	private final Map<Integer, DamageTrackerBar> trackerBars = new HashMap<>();

	private String name;
	private boolean enabled;
	private boolean collapsed;
	private boolean onlyOwnDamage;
	private String targetName;
	private Map<Integer, Integer> equipmentIds;
	private List<Integer> prayerIds;
	private Integer attackStyle;
	private Map<Integer, Integer> skillLevels;

	@Setter(AccessLevel.NONE)
	private int maximum;
	@Setter(AccessLevel.NONE)
	private int total;

	DamageTracker(DamageTrackerPlugin plugin, DamageTrackerData data)
	{
		this.plugin = plugin;

		id = data.getId();
		name = data.getName() != null ? data.getName() : "Tracker";
		enabled = data.isEnabled();
		collapsed = data.isCollapsed();
		onlyOwnDamage = data.isOnlyOwnDamage();
		targetName = data.getTargetName() != null ? data.getTargetName() : "";
		equipmentIds = data.getEquipmentIds() != null ? data.getEquipmentIds() : new HashMap<>();
		prayerIds = data.getPrayerIds() != null ? data.getPrayerIds() : new ArrayList<>();
		attackStyle = data.getAttackStyle();
		skillLevels = data.getSkillLevels() != null ? data.getSkillLevels() : new HashMap<>();
		damageDistribution = data.getDamageDistribution() != null ? data.getDamageDistribution() : new HashMap<>();

		for (Map.Entry<Integer, Integer> entry : damageDistribution.entrySet())
		{
			int damage = entry.getKey();
			int count = entry.getValue();
			maximum = Math.max(maximum, count);
			total += count;
			trackerBars.put(damage, new DamageTrackerBar(damage, count, this));
		}
	}

	public void update(int damage)
	{
		trackerBars.putIfAbsent(damage, new DamageTrackerBar(damage, 0, this));
		DamageTrackerBar bar = trackerBars.get(damage);
		int count = bar.update();
		damageDistribution.put(damage, count);

		maximum = Math.max(maximum, count);
		total++;

		bar.updateInfo();
	}

	public boolean shouldUpdate(boolean isOwnDamage, String targetName, int targetId, Client client)
	{
		String[] targetInfoParts = this.targetName.split("#");
		String specifiedTargetName = targetInfoParts[0];
		int specifiedTargetId = (targetInfoParts.length >= 2) ? Integer.parseInt(targetInfoParts[1]) : -1;

		String userName = "";
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			userName = client.getLocalPlayer().getName();
		}

		boolean correctTargetDamage = !onlyOwnDamage || isOwnDamage;
		boolean correctTargetName = (Strings.isNullOrEmpty(specifiedTargetName) && !userName.equals(targetName)) ||
			specifiedTargetName.equals(targetName);
		boolean correctTargetId = specifiedTargetId == -1 || specifiedTargetId == targetId;

		return enabled && correctTargetDamage && correctTargetName && correctTargetId &&
			hasCorrectEquipment(client) && hasCorrectPrayers(client) &&
			hasCorrectAttackStyle(client) && hasCorrectSkillLevels(client);
	}

	public DamageTrackerData toDamageTrackerData()
	{
		return new DamageTrackerData(id, name, enabled, collapsed,
			onlyOwnDamage, targetName, equipmentIds, prayerIds, attackStyle, skillLevels, damageDistribution);
	}

	public double getAverage()
	{
		long sum = 0;
		int total = 0;

		for (Map.Entry<Integer, Integer> entry : damageDistribution.entrySet())
		{
			int damage = entry.getKey();
			int count = entry.getValue();
			sum += damage * count;
			total += count;
		}

		if (total <= 0)
		{
			return 0;
		}

		return sum / (double) total;
	}

	public void reset()
	{
		damageDistribution.clear();
	}

	@Override
	public int compareTo(DamageTracker other)
	{
		return name.compareTo(other.name);
	}

	private boolean hasCorrectEquipment(Client client)
	{
		if (equipmentIds == null || equipmentIds.isEmpty())
		{
			return true;
		}
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return false;
		}
		for (int id : equipmentIds.values())
		{
			if (!equipment.contains(id))
			{
				return false;
			}
		}
		return true;
	}

	private boolean hasCorrectPrayers(Client client)
	{
		Prayer[] prayers = Prayer.values();
		for (int prayerIndex : prayerIds)
		{
			if (!client.isPrayerActive(prayers[prayerIndex]))
			{
				return false;
			}
		}
		return true;
	}

	private boolean hasCorrectAttackStyle(Client client)
	{
		return attackStyle == null || client.getVarpValue(VarPlayer.ATTACK_STYLE.getId()) == attackStyle;
	}

	private boolean hasCorrectSkillLevels(Client client)
	{
		Skill[] skills = Skill.values();
		for (int skillIndex : skillLevels.keySet())
		{
			if (skillLevels.get(skillIndex) != client.getBoostedSkillLevel(skills[skillIndex]))
			{
				return false;
			}
		}
		return true;
	}
}
