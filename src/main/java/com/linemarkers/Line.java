package com.linemarkers;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Line
{
	Color colour;
	Edge edge;
	double width;
	@Setter(AccessLevel.NONE)
	WorldPoint location;

	Line(LineMarkerConfig config, WorldPoint location)
	{
		this(config.defaultColour(), config.defaultEdge(), config.defaultWidth(), location);
	}

	public static List<Line> instances(Client client, Line line)
	{
		return WorldPoint.toLocalInstance(client, line.location).stream().map(wp ->
			new Line(line.colour, line.edge, line.width, wp)).collect(Collectors.toList());
	}
}
