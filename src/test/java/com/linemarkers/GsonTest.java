package com.linemarkers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonTest
{
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	GsonTest()
	{
		LineGroup test = new LineGroup(null, null);
		test.id = 0;
		test.name = null;
		test.visible = false;
		test.collapsed = false;
		test.lines = null;

		// "" -> null
		test("", null);

		// "{}" -> null
		test("{}", null);

		// "[]" -> null
		test("[]", new ArrayList<>());

		// "[{}]" -> [null] // because of LineGroup::toString
		test("[{}]", new ArrayList<>(Arrays.asList(test)));

		// "[{id:0}]" -> [null] // because of LineGroup::toString
		test("[{id:0}]", new ArrayList<>(Arrays.asList(test)));

		// "[null]" -> [null] // because of null element
		test("[null]", new ArrayList<>(Arrays.asList(new LineGroup[]{null})));

		// null -> null
		test(null, null);
	}

	public void test(String text, List<LineGroup> expectedResult)
	{
		List<LineGroup> groups = null;
		try
		{
			groups = gson.fromJson(text, new TypeToken<ArrayList<LineGroup>>(){}.getType());
		}
		catch (IllegalStateException | JsonSyntaxException ignore)
		{
		}

		if (groups == null)
		{
			assert groups == expectedResult : "Expected output " + expectedResult + ", but got " + groups + " for input " + text;
		}
		else
		{
			if (!groups.isEmpty())
			{
				for (LineGroup group : groups)
				{
					assert LineGroup.isInvalid(group) : "Expected " + group + " to be an invalid line group for input " + text;
				}
			}
			assert groups.equals(expectedResult) : "Expected output " + expectedResult + ", but got " + groups + " for input " + text;
		}

	}

	public static void main(String[] args)
	{
		new GsonTest();
	}
}
