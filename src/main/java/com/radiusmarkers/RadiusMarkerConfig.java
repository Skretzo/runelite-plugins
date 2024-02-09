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
		name = "Marker features",
		description = "Customize the marker range parameters to be displayed in the panel",
		position = 0
	)
	String markerFeaturesSection = "markerFeaturesSection";

	@ConfigItem(
		keyName = "includeWanderRange",
		name = "Include wander range",
		description = "Whether the markers should have the wander range parameter",
		position = 1,
		section = markerFeaturesSection
	)
	default boolean includeWanderRange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeMaxRange",
		name = "Include max range",
		description = "Whether the markers should have the max range parameter",
		position = 2,
		section = markerFeaturesSection
	)
	default boolean includeMaxRange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeAggressionRange",
		name = "Include aggression range",
		description = "Whether the markers should have the aggression range parameter",
		position = 3,
		section = markerFeaturesSection
	)
	default boolean includeAggressionRange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeRetreatInteractionRange",
		name = "Include retreat interaction range",
		description = "Whether the markers should have the retreat interaction range parameter",
		position = 4,
		section = markerFeaturesSection
	)
	default boolean includeRetreatInteractionRange()
	{
		return false;
	}

	@ConfigItem(
		keyName = "includeAttackRange",
		name = "Include attack range",
		description = "Whether the markers should have the attack range parameter",
		position = 5,
		section = markerFeaturesSection
	)
	default boolean includeAttackRange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeHuntRange",
		name = "Include hunt range",
		description = "Whether the markers should have the hunt range parameter",
		position = 6,
		section = markerFeaturesSection
	)
	default boolean includeHuntRange()
	{
		return false;
	}

	@ConfigItem(
		keyName = "includeInteractionRange",
		name = "Include interaction range",
		description = "Whether the markers should have the interaction range parameter",
		position = 7,
		section = markerFeaturesSection
	)
	default boolean includeInteractionRange()
	{
		return false;
	}

	@ConfigSection(
		name = "Default radiuses",
		description = "Default radius values",
		position = 8
	)
	String defaultRadiusSection = "defaultRadiusSection";

	@ConfigItem(
		keyName = "defaultRadiusWander",
		name = "Wander radius",
		description = "Default NPC wander range radius",
		position = 9,
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
		position = 10,
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
		position = 11,
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
		position = 12,
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
		position = 13,
		section = defaultRadiusSection
	)
	default int defaultRadiusInteraction()
	{
		return 1;
	}

	@ConfigSection(
		name = "Default colours",
		description = "Default radius colour values",
		position = 14
	)
	String defaultColourSection = "defaultColourSection";

	@Alpha
	@ConfigItem(
		keyName = "defaultColourSpawn",
		name = "Spawn point",
		description = "Default NPC spawn tile colour",
		position = 15,
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
		position = 16,
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
		position = 17,
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
		position = 18,
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
		position = 19,
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
		position = 20,
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
		position = 21,
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
		position = 22,
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
		position = 23
	)
	default int borderWidth()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "showMinimap",
		name = "Show on minimap",
		description = "Show radius markers on the minimap",
		position = 24
	)
	default boolean showMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "Show on world map",
		description = "Show radius markers on the world map",
		position = 25
	)
	default boolean showWorldMap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hideNavButton",
		name = "Hide side panel button",
		description = "Allows you to hide the side panel button to reduce clutter when not needing to modify Radius Markers",
		position = 26
	)
	default boolean hideNavButton()
	{
		return false;
	}
}
