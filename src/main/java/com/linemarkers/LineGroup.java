package com.linemarkers;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
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
		id = System.currentTimeMillis();
		this.name = name;
		visible = true;
		collapsed = false;
		lines = new ArrayList<>();
		lines.add(line);
	}

	@Override
	public String toString()
	{
		return name;
	}
}
