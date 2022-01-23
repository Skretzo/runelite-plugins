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
}
