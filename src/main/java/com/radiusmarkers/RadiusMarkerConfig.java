package com.radiusmarkers;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(RadiusMarkerPlugin.CONFIG_GROUP)
public interface RadiusMarkerConfig extends Config
{
	@ConfigSection(
		name = "Default radiuses",
		description = "Default radius values",
		position = 0
	)
	String defaultRadiusSection = "defaultRadiusSection";

	@ConfigItem(
		keyName = "defaultRadiusWander",
		name = "Wander radius",
		description = "Default NPC wander range radius",
		position = 1,
		section = defaultRadiusSection
	)
	default int defaultRadiusWander()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "defaultRadiusMax",
		name = "Max radius",
		description = "Default NPC max range radius",
		position = 2,
		section = defaultRadiusSection
	)
	default int defaultRadiusMax()
	{
		return 7;
	}

	@ConfigItem(
		keyName = "defaultRadiusAttack",
		name = "Attack radius",
		description = "Default NPC attack range radius",
		position = 3,
		section = defaultRadiusSection
	)
	default int defaultRadiusAttack()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "defaultRadiusHunt",
		name = "Hunt radius",
		description = "Default NPC hunt range radius",
		position = 4,
		section = defaultRadiusSection
	)
	default int defaultRadiusHunt()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "defaultRadiusInteraction",
		name = "Interaction radius",
		description = "Default NPC interaction range radius",
		position = 5,
		section = defaultRadiusSection
	)
	default int defaultRadiusInteraction()
	{
		return 1;
	}

	@ConfigSection(
		name = "Default colours",
		description = "Default radius colour values",
		position = 6
	)
	String defaultColourSection = "defaultColourSection";

	@Alpha
	@ConfigItem(
		keyName = "defaultColourSpawn",
		name = "Spawn point",
		description = "Default NPC spawn tile colour",
		position = 5,
		section = defaultColourSection
	)
	default Color defaultColourSpawn()
	{
		return Color.CYAN;
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourWander",
		name = "Wander range",
		description = "Default NPC wander range colour",
		position = 6,
		section = defaultColourSection
	)
	default Color defaultColourWander()
	{
		return Color.YELLOW;
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourMax",
		name = "Max range",
		description = "Default NPC max range colour",
		position = 7,
		section = defaultColourSection
	)
	default Color defaultColourMax()
	{
		return Color.MAGENTA;
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourAggression",
		name = "Aggression range",
		description = "Default NPC aggression range colour",
		position = 8,
		section = defaultColourSection
	)
	default Color defaultColourAggression()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourRetreatInteraction",
		name = "Retreat interaction range",
		description = "Default NPC retreat interaction range colour",
		position = 9,
		section = defaultColourSection
	)
	default Color defaultColourRetreatInteraction()
	{
		return Color.BLUE;
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourAttack",
		name = "Attack range",
		description = "Default NPC attack range colour",
		position = 10,
		section = defaultColourSection
	)
	default Color defaultColourAttack()
	{
		return new Color(127, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourHunt",
		name = "Hunt range",
		description = "Default NPC hunt range colour",
		position = 11,
		section = defaultColourSection
	)
	default Color defaultColourHunt()
	{
		return new Color(255, 127, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "defaultColourInteraction",
		name = "Interaction range",
		description = "Default NPC interaction range colour",
		position = 12,
		section = defaultColourSection
	)
	default Color defaultColourInteraction()
	{
		return new Color(0, 200, 0);
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Radius marker border width",
		position = 13
	)
	default int borderWidth()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "showMinimap",
		name = "Show on minimap",
		description = "Show radius markers on the minimap",
		position = 14
	)
	default boolean showMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "Show on world map",
		description = "Show radius markers on the world map",
		position = 15
	)
	default boolean showWorldMap()
	{
		return true;
	}
}
