package pathfinding;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("pathfinding")
public interface PathfindingConfig extends Config
{
	@ConfigItem(
		keyName = "avoidWilderness",
		name = "Avoid wilderness",
		description = "Whether the wilderness should be avoided if possible (otherwise, will suggest using wilderness lever to travel from Edgeville to Ardougne)",
		position = 0
	)
	default boolean avoidWilderness()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawMap",
		name = "Draw path on world map",
		description = "Whether the path should be drawn on the world map",
		position = 1
	)
	default boolean drawMap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawMinimap",
		name = "Draw path on minimap",
		description = "Whether the path should be drawn on the minimap",
		position = 2
	)
	default boolean drawMinimap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTiles",
		name = "Draw path on tiles",
		description = "Whether the path should be drawn on the game tiles",
		position = 3
	)
	default boolean drawTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drawTransports",
		name = "Draw transports",
		description = "Whether transports should be drawn",
		position = 4
	)
	default boolean drawTransports()
	{
		return false;
	}

	@ConfigItem(
		keyName = "drawCollisionMap",
		name = "Draw collision map",
		description = "Whether the collision map should be drawn",
		position = 5
	)
	default boolean drawCollisionMap()
	{
		return false;
	}

	@ConfigItem(
		keyName = "cancelInstead",
		name = "Cancel instead of recalculating",
		description = "Whether the path should be cancelled rather than recalculated when the distance limit is exceeded",
		position = 6
	)
	default boolean cancelInstead()
	{
		return false;
	}

	@Range(
		min = 1
	)
	@ConfigItem(
		keyName = "recalculateDistance",
		name = "Recalculate distance",
		description = "Distance from the path the player should be for it to be recalculated",
		position = 7
	)
	default int recalculateDistance()
	{
		return 10;
	}

	@Range(
		min = -1,
		max = 50
	)
	@ConfigItem(
		keyName = "finishDistance",
		name = "Finish distance",
		description = "Distance from the target tile at which the path should be ended (-1 for never)",
		position = 8
	)
	default int reachedDistance()
	{
		return 5;
	}

	@ConfigItem(
		keyName = "showTileCounter",
		name = "Show tile counter",
		description = "Whether to display the number of tiles travelled, number of tiles remaining or disable counting",
		position = 9
	)
	default TileCounter showTileCounter()
	{
		return TileCounter.DISABLED;
	}

	@ConfigSection(
		name = "Colours",
		description = "Colours for the path, transports and collision map",
		position = 10
	)
	String sectionColours = "sectionColours";

	@Alpha
	@ConfigItem(
		keyName = "colourPath",
		name = "Path",
		description = "Colour of the path tiles on the world map, minimap and in the game scene",
		position = 11,
		section = sectionColours
	)
	default Color colourPath()
	{
		return new Color(255, 0, 0);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourPathCalculating",
		name = "Calculating",
		description = "Colour of the path tiles while the pathfinding calculation is in progress",
		position = 12,
		section = sectionColours
	)
	default Color colourPathCalculating()
	{
		return new Color(0, 0, 255);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourTransports",
		name = "Transports",
		description = "Colour of the transport tiles",
		position = 13,
		section = sectionColours
	)
	default Color colourTransports()
	{
		return new Color(0, 255, 0, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "colourCollisionMap",
		name = "Collision map",
		description = "Colour of the collision map tiles",
		position = 14,
		section = sectionColours
	)
	default Color colourCollisionMap()
	{
		return new Color(0, 128, 255, 128);
	}

	@ConfigItem(
		keyName = "showInfoOverlay",
		name = "Show info overlay",
		description = "Displays hover info about wall object orientation(s) and collision flag(s)",
		position = 15
	)
	default boolean showInfoOverlay() // todo: remove, only for testing
	{
		return true;
	}
}
