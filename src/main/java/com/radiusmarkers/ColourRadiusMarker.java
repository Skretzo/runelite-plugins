package com.radiusmarkers;

import java.awt.Color;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

/**
 * Used to denote marked tiles, radiuses and their colours.
 * Note: This is not used for serialization of radius markers; see {@link RadiusMarker}
 */
@Getter
@Setter
@EqualsAndHashCode
class ColourRadiusMarker
{
	private WorldPoint worldPoint;

	private String name;
	private boolean visible;

	private final int z;

	private Color spawnColour;
	private boolean spawnVisible;

	private int wanderRadius;
	private Color wanderColour;
	private boolean wanderVisible;

	private int retreatRadius;
	private Color retreatColour;
	private boolean retreatVisible;

	private int aggroRadius;
	private Color aggroColour;
	private boolean aggroVisible;

	ColourRadiusMarker(RadiusMarker radiusMarker, WorldPoint worldPoint)
	{
		this.name = radiusMarker.getName();
		this.visible = radiusMarker.isVisible();

		this.z = radiusMarker.getZ();
		this.spawnColour = radiusMarker.getSpawnColour();
		this.spawnVisible = radiusMarker.isSpawnVisible();

		this.wanderRadius = radiusMarker.getWanderRadius();
		this.wanderColour = radiusMarker.getWanderColour();
		this.wanderVisible = radiusMarker.isWanderVisible();

		this.retreatRadius = radiusMarker.getRetreatRadius();
		this.retreatColour = radiusMarker.getRetreatColour();
		this.retreatVisible = radiusMarker.isRetreatVisible();

		this.aggroRadius = radiusMarker.getAggroRadius();
		this.aggroColour = radiusMarker.getAggroColour();
		this.aggroVisible = radiusMarker.isAggroVisible();

		this.worldPoint = worldPoint;
	}
}
