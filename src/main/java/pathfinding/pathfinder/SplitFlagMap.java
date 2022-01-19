package pathfinding.pathfinder;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import static net.runelite.api.Constants.REGION_SIZE;

public abstract class SplitFlagMap
{
	private static final int MAXIMUM_SIZE = 20 * 1024 * 1024;
	private final LoadingCache<Position, FlagMap> regionMaps;

	public SplitFlagMap(Map<Position, boolean[]> compressedRegions)
	{
		regionMaps = CacheBuilder
			.newBuilder()
			.weigher((Weigher<Position, FlagMap>) (k, v) -> v.flags.length / 8)
			.maximumWeight(MAXIMUM_SIZE)
			.build(CacheLoader.from(position ->
			{
				boolean[] compressedRegion = compressedRegions.get(position);

				if (compressedRegion == null)
				{
					return new FlagMap(position.x, position.y);
				}

				return new FlagMap(position.x, position.y, compressedRegion);
			}));
	}

	public boolean get(int x, int y, int z, int flag, int wallIndex)
	{
		try
		{
			return regionMaps.get(new Position(x / REGION_SIZE, y / REGION_SIZE)).get(x, y, z, flag, wallIndex);
		}
		catch (ExecutionException e)
		{
			throw new UncheckedExecutionException(e);
		}
	}

	public boolean[] get(int regionX, int regionY)
	{
		FlagMap flagMap = regionMaps.getIfPresent(new Position(regionX, regionY));
		if (flagMap == null)
		{
			return null;
		}
		return flagMap.flags;
		/*
		try
		{
			FlagMap flagMap = regionMaps.get(new Position(regionX, regionY));
			if (flagMap == null)
			{
				return null;
			}
			return flagMap.flags;
		}
		catch (ExecutionException e)
		{
			return null;
		}
		*/
	}

	public void add(int regionX, int regionY, boolean[] collisionMap) // todo: investigate why 14 -> 27, idk why
	{
		long before = size();
		boolean already = get(regionX, regionY) != null;

		regionMaps.put(
			new Position(regionX, regionY),
			new FlagMap(regionX, regionY, collisionMap));

		if (already) // todo: already is a bad idea, think about re-generation of collision maps
		{
			System.out.println("size = " + before + " -> " + size() + "    " + regionX + ", " + regionY + "    already in regionMaps");
		}
		else
		{
			System.out.println("size = " + before + " -> " + size() + "    " + regionX + ", " + regionY + "    was added");
		}
	}

	public long size() // todo: remove
	{
		/*
		if (regionMaps.size() > 11)
		{
			for (Map.Entry<Position, FlagMap> ety : regionMaps.asMap().entrySet())
			{
				System.out.println(ety.getKey().x + ", " + ety.getKey().y);
			}
		}
		*/
		return regionMaps.size();
	}

	public static class Position
	{
		public final int x;
		public final int y;

		public Position(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object o)
		{
			return o instanceof Position && ((Position) o).x == x && ((Position) o).y == y;
		}

		@Override
		public int hashCode()
		{
			return x * 31 + y;
		}

		@Override
		public String toString()
		{
			return "(" + x + ", " + y + ")";
		}
	}
}
