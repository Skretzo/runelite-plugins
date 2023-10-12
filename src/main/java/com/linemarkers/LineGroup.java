package com.linemarkers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import net.runelite.api.Client;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class LineGroup
{
	@Setter(AccessLevel.NONE)
	long id;
	String name;
	boolean visible;
	boolean collapsed;
	@Setter(AccessLevel.NONE)
	List<Line> lines;

	LineGroup(String name, Line line)
	{
		this(System.currentTimeMillis(), name, true, false, new ArrayList<>(Arrays.asList(line)));
	}

	public static List<LineGroup> instances(Client client, List<LineGroup> groups)
	{
		List<LineGroup> markers = new ArrayList<>();

		for (LineGroup group : groups)
		{
			List<Line> lines = new ArrayList<>(group.getLines());
			for (Line line : group.getLines())
			{
				lines.addAll(Line.instances(client, line));
			}
			markers.add(new LineGroup(group.id, group.name, group.visible, group.collapsed, lines));
		}

		return markers;
	}

	public static boolean isInvalid(LineGroup group)
	{
		return group == null || group.id <= 0 || group.name == null || group.lines == null;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
