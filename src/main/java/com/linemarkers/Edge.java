package com.linemarkers;

import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@AllArgsConstructor
public enum Edge
{
	WEST(0, 0, 0, 1),
	EAST(1, 0, 0, 1),
	SOUTH(0, 0, 1, 0),
	NORTH(0, 1, 1, 0);

	private final int x;
	private final int y;
	private final int dx;
	private final int dy;

	private static final Edge[] EDGES = values();

	public Edge next()
	{
		return EDGES[(this.ordinal() + 1) % EDGES.length];
	}

	public static WorldPoint start(Line line)
	{
		return line.getLocation().dx(line.getEdge().x).dy(line.getEdge().y);
	}

	public static WorldPoint end(Line line)
	{
		return start(line).dx(line.getEdge().dx).dy(line.getEdge().dy);
	}

	@Override
	public String toString()
	{
		return name().charAt(0) + name().substring(1).toLowerCase().replaceAll("_", " ");
	}
}
