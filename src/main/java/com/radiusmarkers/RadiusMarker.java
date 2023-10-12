package com.radiusmarkers;

import java.awt.Color;
import lombok.Value;

/**
 * Used for serialization of radius markers.
 */
@Value
class RadiusMarker
{
	long id;

	String name;
	boolean visible;
	boolean collapsed;

	int z;

	int spawnX;
	int spawnY;
	Color spawnColour;
	boolean spawnVisible;

	int wanderRadius;
	Color wanderColour;
	boolean wanderVisible;

	int maxRadius;
	Color maxColour;
	boolean maxVisible;

	Color aggressionColour;
	boolean aggressionVisible;

	Color retreatInteractionColour;
	boolean retreatInteractionVisible;

	int npcId;

	int attackRadius;
	Color attackColour;
	AttackType attackType;
	boolean attackVisible;

	int huntRadius;
	Color huntColour;
	boolean huntVisible;

	int interactionRadius;
	Color interactionColour;
	boolean interactionVisible;

	public static boolean isInvalid(RadiusMarker marker)
	{
		return marker == null
			|| marker.id <= 0
			|| marker.name == null
			|| marker.spawnColour == null
			|| marker.wanderColour == null
			|| marker.maxColour == null
			|| marker.aggressionColour == null
			|| marker.retreatInteractionColour == null
			|| marker.attackColour == null
			|| marker.attackType == null
			|| marker.huntColour == null
			|| marker.interactionColour == null;
	}
}
