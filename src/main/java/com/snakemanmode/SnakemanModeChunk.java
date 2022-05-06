package com.snakemanmode;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import static net.runelite.api.Constants.CHUNK_SIZE;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

@EqualsAndHashCode
public class SnakemanModeChunk
{
	private final int z;
	private final int i;
	private final int j;
	private final int x;
	private final int y;

	SnakemanModeChunk(Client client, WorldPoint worldPoint)
	{
		if (client.isInInstancedRegion())
		{
			LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
			if (localPoint != null)
			{
				worldPoint = WorldPoint.fromLocalInstance(client, localPoint);
			}
		}
		z = worldPoint.getPlane();
		i = worldPoint.getX() >> 6;
		j = worldPoint.getY() >> 6;
		x = worldPoint.getRegionX() / CHUNK_SIZE;
		y = worldPoint.getRegionY() / CHUNK_SIZE;
	}

	SnakemanModeChunk(int id)
	{
		z = (id >> (32 - 9 - 2)) & (4 - 1);
		i = (id >> (32 - 9 - 2 - 7)) & (128 - 1);
		j = (id >> (32 - 9 - 2 - 7 - 8)) & (256 - 1);
		x = (id >> (32 - 9 - 2 - 7 - 8 - 3)) & (8 - 1);
		y = (id >> (32 - 9 - 2 - 7 - 8 - 3 - 3)) & (8 - 1);
	}

	int getId()
	{
		return getId(z, i, j, x, y);
	}

	static int getId(int z, int i, int j, int x, int y)
	{
		int Z = (z << (32 - 9 - 2));
		int I = (i << (32 - 9 - 2 - 7));
		int J = (j << (32 - 9 - 2 - 7 - 8));
		int X = (x << (32 - 9 - 2 - 7 - 8 - 3));
		int Y = (y << (32 - 9 - 2 - 7 - 8 - 3 - 3));
		return Z | I | J | X | Y;
	}

	int getSize()
	{
		return CHUNK_SIZE;
	}

	WorldPoint getBottomLeft()
	{
		return new WorldPoint((i << 6) + x * getSize(), (j << 6) + y * getSize(), z);
	}

	WorldPoint getCenter()
	{
		int offset = getSize() / 2 - (getSize() % 2 == 0 ? 1 : 0);
		return getBottomLeft().dx(offset).dy(offset);
	}

	List<SnakemanModeChunk> getNeighbourChunks(Client client)
	{
		List<SnakemanModeChunk> neighbours = new ArrayList<>();
		int offset = getSize();
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(-offset).dy(offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dy(offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(offset).dy(offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(-offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(-offset).dy(-offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dy(-offset)));
		neighbours.add(new SnakemanModeChunk(client, getBottomLeft().dx(offset).dy(-offset)));
		return neighbours;
	}
}
