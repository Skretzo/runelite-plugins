package com.linemarkers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldView;
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

	Line(LineMarkerPlugin plugin, WorldPoint location)
	{
		this(plugin.defaultColour, plugin.defaultEdge, plugin.defaultWidth, location);
	}

	public static List<Line> instances(Client client, Line line)
	{
		WorldView worldView = client.getTopLevelWorldView();
		if (!GameState.LOGGED_IN.equals(client.getGameState()) || worldView == null)
		{
			List<Line> lines = new ArrayList<>();
			lines.add(line);
			return lines;
		}
		return WorldPoint.toLocalInstance(worldView, line.location).stream().filter(Objects::nonNull).map(wp ->
			new Line(line.colour, line.edge, line.width, wp)).collect(Collectors.toList());
	}
}
