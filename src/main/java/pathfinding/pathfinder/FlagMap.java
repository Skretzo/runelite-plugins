package pathfinding.pathfinder;

import static net.runelite.api.Constants.MAX_Z;
import static net.runelite.api.Constants.REGION_SIZE;
import static pathfinding.PathfindingPlugin.FLAG_COUNT;
import static pathfinding.PathfindingPlugin.FLAG_MULTIPLIER;
import pathfinding.Util;

public class FlagMap
{
	private final int minX;
	private final int minY;
	private final int maxX;
	private final int maxY;
	protected final boolean[] flags;

	public FlagMap(int regionX, int regionY)
	{
		this(regionX, regionY, Util.defaultCollisionMap());
	}

	public FlagMap(int regionX, int regionY, boolean[] flags)
	{
		minX = regionX * REGION_SIZE;
		minY = regionY * REGION_SIZE;
		maxX = minX + REGION_SIZE - 1;
		maxY = minY + REGION_SIZE - 1;
		this.flags = flags;
	}

	public boolean get(int x, int y, int z, int flag, int wallIndex)
	{
		if (x < minX || x > maxX || y < minY || y > maxY || z < 0 || z > MAX_Z - 1)
		{
			return false;
		}

		return flags[index(x, y, z, flag, wallIndex)];
	}

	private int index(int x, int y, int z, int flag, int wallIndex)
	{
		if (x < minX || x > maxX || y < minY || y > maxY || z < 0 || z > MAX_Z - 1 || flag < 0 || flag > FLAG_COUNT - 1)
		{
			throw new IndexOutOfBoundsException(x + " " + y + " " + z);
		}

		return index(x, y, z, flag, wallIndex, minX, minY);
	}

	public static int index(int x, int y, int z, int flag, int wallIndex, int minX, int minY)
	{
		return (z * REGION_SIZE * REGION_SIZE + (y - minY) * REGION_SIZE + (x - minX)) * FLAG_COUNT * FLAG_MULTIPLIER + FLAG_COUNT * wallIndex + flag;
	}
}
