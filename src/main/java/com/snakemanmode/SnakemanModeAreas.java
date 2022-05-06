package com.snakemanmode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.runelite.api.Constants;
import net.runelite.api.coords.WorldArea;

public class SnakemanModeAreas
{
	public static final List<Integer> FREE_TO_PLAY = new ArrayList<>();
	public static final List<Integer> FRUIT_AREA = new ArrayList<>();
	public static final WorldArea[] WHITELISTED_AREA = new WorldArea[]
	{
		new WorldArea(3008, 3008, 128, 128, 0),
		new WorldArea(3136, 3072, 64, 64, 0),
		new WorldArea(3072, 9472, 64, 64, 0),
		new WorldArea(1600, 6016, 192, 192, 0),
		new WorldArea(1664, 12480, 64, 64, 0)
	};

	static
	{
		resourceToChunks(FREE_TO_PLAY, "free-to-play_area.txt");
		resourceToChunks(FRUIT_AREA, "fruit_area.txt");
	}

	private static void resourceToChunks(List<Integer> chunks, String path)
	{
		chunks.clear();

		Scanner scanner = new Scanner(SnakemanModePlugin.class.getResourceAsStream(path));
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();

			if (line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}

			if (line.contains(","))
			{
				String[] parts = line.replace(",", "").split(" ");

				if (parts.length != 2)
				{
					continue;
				}

				int i = Integer.parseInt(parts[0]);
				int j = Integer.parseInt(parts[1]);
				regionToChunks(chunks, i, j);
			}
			else
			{
				String[] parts = line.split(" ");

				for (String s : parts)
				{
					chunks.add(Integer.parseInt(s));
				}
			}
		}
	}

	private static void regionToChunks(List<Integer> ids, int i, int j)
	{
		for (int y = 0; y < Constants.CHUNK_SIZE; y++)
		{
			for (int x = 0; x < Constants.CHUNK_SIZE; x++)
			{
				ids.add(SnakemanModeChunk.getId(0, i, j, x, y));
			}
		}
	}
}
