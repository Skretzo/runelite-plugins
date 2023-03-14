package com.linemarkers;

import java.awt.Color;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
@EqualsAndHashCode
class Line
{
	Color colour;
	Edge edge;
	double width;
	@Setter(AccessLevel.NONE)
	WorldPoint location;

	Line(LineMarkerConfig config, WorldPoint location)
	{
		colour = config.defaultColour();
		edge = config.defaultEdge();
		width = config.defaultWidth();
		this.location = location;
	}
}
