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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class ColourRadiusMarker implements Comparable<ColourRadiusMarker>
{
	static final int RETREAT_INTERACTION_RANGE = 11;

	private RadiusMarkerPanel panel;

	private long id;

	@EqualsAndHashCode.Include
	private String name;
	private boolean visible;
	private boolean collapsed;

	private final int z;

	private int spawnX;
	private int spawnY;
	private Color spawnColour;
	private boolean spawnVisible;

	private int wanderRadius;
	private Color wanderColour;
	private boolean wanderVisible;

	private int maxRadius;
	private Color maxColour;
	private boolean maxVisible;

	private int aggressionRadius;
	private Color aggressionColour;
	private boolean aggressionVisible;

	private int retreatInteractionRadius;
	private Color retreatInteractionColour;
	private boolean retreatInteractionVisible;

	private int npcId;

	private int attackRadius;
	private Color attackColour;
	private AttackType attackType;
	private boolean attackVisible;

	private int huntRadius;
	private Color huntColour;
	private boolean huntVisible;

	private int interactionRadius;
	private Color interactionColour;
	private RadiusOrigin interactionOrigin;
	private boolean interactionVisible;

	ColourRadiusMarker(RadiusMarker radiusMarker)
	{
		this.id = radiusMarker.getId();

		this.name = radiusMarker.getName();
		this.visible = radiusMarker.isVisible();
		this.collapsed = radiusMarker.isCollapsed();

		this.z = radiusMarker.getZ();
		this.spawnX = radiusMarker.getSpawnX();
		this.spawnY = radiusMarker.getSpawnY();
		this.spawnColour = radiusMarker.getSpawnColour();
		this.spawnVisible = radiusMarker.isSpawnVisible();

		this.wanderRadius = radiusMarker.getWanderRadius();
		this.wanderColour = radiusMarker.getWanderColour();
		this.wanderVisible = radiusMarker.isWanderVisible();

		this.maxRadius = radiusMarker.getMaxRadius();
		this.maxColour = radiusMarker.getMaxColour();
		this.maxVisible = radiusMarker.isMaxVisible();

		this.aggressionRadius = radiusMarker.getMaxRadius() + radiusMarker.getAttackRadius();
		this.aggressionColour = radiusMarker.getAggressionColour();
		this.aggressionVisible = radiusMarker.isAggressionVisible();

		this.retreatInteractionRadius = radiusMarker.getMaxRadius() + RETREAT_INTERACTION_RANGE;
		this.retreatInteractionColour = radiusMarker.getRetreatInteractionColour();
		this.retreatInteractionVisible = radiusMarker.isRetreatInteractionVisible();

		this.npcId = radiusMarker.getNpcId();

		this.attackRadius = radiusMarker.getAttackRadius();
		this.attackColour = radiusMarker.getAttackColour();
		this.attackType = radiusMarker.getAttackType();
		this.attackVisible = radiusMarker.isAttackVisible();

		this.huntRadius = radiusMarker.getHuntRadius();
		this.huntColour = radiusMarker.getHuntColour();
		this.huntVisible = radiusMarker.isHuntVisible();

		this.interactionRadius = radiusMarker.getInteractionRadius();
		this.interactionColour = radiusMarker.getInteractionColour();
		this.interactionOrigin = radiusMarker.getInteractionOrigin();
		this.interactionVisible = radiusMarker.isInteractionVisible();
	}

	public void setMaxRadius(int maxRadius)
	{
		this.maxRadius = maxRadius;
		aggressionRadius = maxRadius + attackRadius;
		retreatInteractionRadius = maxRadius + RETREAT_INTERACTION_RANGE;
	}

	public void setAttackRadius(int attackRadius)
	{
		this.attackRadius = attackRadius;
		aggressionRadius = maxRadius + attackRadius;
	}

	public WorldPoint getWorldPoint()
	{
		return new WorldPoint(spawnX, spawnY, z);
	}

	@Override
	public int compareTo(ColourRadiusMarker other)
	{
		return this.name.compareTo(other.name);
	}
}
