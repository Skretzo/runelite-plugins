package com.damagetracker;

import java.util.List;
import java.util.Map;
import lombok.Value;

/**
 * Used for serialization of tracked damage
 */
@Value
class DamageTrackerData
{
	long id;

	String name;
	boolean enabled;
	boolean collapsed;

	boolean onlyOwnDamage;
	String targetName;
	Map<Integer, Integer> equipmentIds;
	List<Integer> prayerIds;
	Integer attackStyle;
	Map<Integer, Integer> skillLevels;

	Map<Integer, Integer> damageDistribution;
}
