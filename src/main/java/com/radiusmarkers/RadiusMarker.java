package com.radiusmarkers;

import java.awt.Color;
import lombok.Value;

/**
 * Used for serialization of radius markers.
 */
@Value
class RadiusMarker
{
	String name;
	boolean visible;
	boolean collapsed;

	int z;

	int spawnRegionX;
	int spawnRegionY;
	Color spawnColour;
	boolean spawnVisible;

	int wanderRadius;
	Color wanderColour;
	boolean wanderVisible;

	int retreatRadius;
	Color retreatColour;
	boolean retreatVisible;

	int maxRadius;
	Color maxColour;
	boolean maxVisible;
}
