package pathfinding.pathfinder;

import java.util.Map;
import static pathfinding.PathfindingPlugin.BLOCKED;
import static pathfinding.PathfindingPlugin.EAST;
import static pathfinding.PathfindingPlugin.NORTH;
import static pathfinding.PathfindingPlugin.NORTH_EAST;
import static pathfinding.PathfindingPlugin.NORTH_WEST;
import static pathfinding.PathfindingPlugin.SOUTH;
import static pathfinding.PathfindingPlugin.SOUTH_EAST;
import static pathfinding.PathfindingPlugin.SOUTH_WEST;
import static pathfinding.PathfindingPlugin.WALL_1;
import static pathfinding.PathfindingPlugin.WALL_2;
import static pathfinding.PathfindingPlugin.WEST;

public class CollisionMap extends SplitFlagMap
{
	public CollisionMap(Map<Position, boolean[]> compressedRegions)
	{
		super(compressedRegions);
	}

	public boolean n(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, SOUTH, WALL_1) && !get(x, y, z, SOUTH, WALL_2) &&
			!get(x, y - 1, z, NORTH, WALL_1) && !get(x, y - 1, z, NORTH, WALL_2);
	}

	public boolean s(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, NORTH, WALL_1) && !get(x, y, z, NORTH, WALL_2) &&
			!get(x, y + 1, z, SOUTH, WALL_1) && !get(x, y + 1, z, SOUTH, WALL_2);
	}

	public boolean e(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, WEST, WALL_1) && !get(x, y, z, WEST, WALL_2) &&
			!get(x - 1, y, z, EAST, WALL_1) && !get(x - 1, y, z, EAST, WALL_2);
	}

	public boolean w(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, EAST, WALL_1) && !get(x, y, z, EAST, WALL_2) &&
			!get(x + 1, y, z, WEST, WALL_1) && !get(x + 1, y, z, WEST, WALL_2);
	}

	public boolean ne(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, WEST, WALL_1) && !get(x, y, z, WEST, WALL_2) &&
			!get(x, y, z, SOUTH, WALL_1) && !get(x, y, z, SOUTH, WALL_2) &&
			!get(x, y, z, SOUTH_WEST, WALL_1) && !get(x, y, z, SOUTH_WEST, WALL_2) &&
			!get(x - 1, y, z, EAST, WALL_1) && !get(x - 1, y, z, EAST, WALL_1) &&
			!get(x - 1, y, z, SOUTH, WALL_1) && !get(x - 1, y, z, SOUTH, WALL_2) &&
			!get(x, y - 1, z, WEST, WALL_1) && !get(x, y - 1, z, WEST, WALL_2) &&
			!get(x, y - 1, z, NORTH, WALL_1) && !get(x, y - 1, z, NORTH, WALL_2) &&
			!get(x - 1, y - 1, z, EAST, WALL_1) && !get(x - 1, y - 1, z, EAST, WALL_2) &&
			!get(x - 1, y - 1, z, NORTH, WALL_1) && !get(x - 1, y - 1, z, NORTH, WALL_2) &&
			!get(x - 1, y - 1, z, NORTH_EAST, WALL_1) && !get(x - 1, y - 1, z, NORTH_EAST, WALL_2);
	}

	public boolean nw(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, EAST, WALL_1) && !get(x, y, z, EAST, WALL_2) &&
			!get(x, y, z, SOUTH, WALL_1) && !get(x, y, z, SOUTH, WALL_2) &&
			!get(x, y, z, SOUTH_EAST, WALL_1) && !get(x, y, z, SOUTH_EAST, WALL_2) &&
			!get(x + 1, y, z, WEST, WALL_1) && !get(x + 1, y, z, WEST, WALL_2) &&
			!get(x + 1, y, z, SOUTH, WALL_1) && !get(x + 1, y, z, SOUTH, WALL_2) &&
			!get(x, y - 1, z, EAST, WALL_1) && !get(x, y - 1, z, EAST, WALL_2) &&
			!get(x, y - 1, z, NORTH, WALL_1) && !get(x, y - 1, z, NORTH, WALL_2) &&
			!get(x + 1, y - 1, z, WEST, WALL_1) && !get(x + 1, y - 1, z, WEST, WALL_2) &&
			!get(x + 1, y - 1, z, NORTH, WALL_1) && !get(x + 1, y - 1, z, NORTH, WALL_2) &&
			!get(x + 1, y - 1, z, NORTH_WEST, WALL_1) && !get(x + 1, y - 1, z, NORTH_WEST, WALL_2);
	}

	public boolean se(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, WEST, WALL_1) && !get(x, y, z, WEST, WALL_2) &&
			!get(x, y, z, NORTH, WALL_1) && !get(x, y, z, NORTH, WALL_2) &&
			!get(x, y, z, NORTH_WEST, WALL_1) && !get(x, y, z, NORTH_WEST, WALL_2) &&
			!get(x - 1, y, z, EAST, WALL_1) && !get(x - 1, y, z, EAST, WALL_2) &&
			!get(x - 1, y, z, NORTH, WALL_1) && !get(x - 1, y, z, NORTH, WALL_2) &&
			!get(x, y + 1, z, WEST, WALL_1) && !get(x, y + 1, z, WEST, WALL_2) &&
			!get(x, y + 1, z, SOUTH, WALL_1) && !get(x, y + 1, z, SOUTH, WALL_2) &&
			!get(x - 1, y + 1, z, EAST, WALL_1) && !get(x - 1, y + 1, z, EAST, WALL_2) &&
			!get(x - 1, y + 1, z, SOUTH, WALL_1) && !get(x - 1, y + 1, z, SOUTH, WALL_2) &&
			!get(x - 1, y + 1, z, SOUTH_EAST, WALL_1) && !get(x - 1, y + 1, z, SOUTH_EAST, WALL_2);
	}

	public boolean sw(int x, int y, int z)
	{
		return !get(x, y, z, BLOCKED, WALL_1) &&
			!get(x, y, z, EAST, WALL_1) && !get(x, y, z, EAST, WALL_2) &&
			!get(x, y, z, NORTH, WALL_1) && !get(x, y, z, NORTH, WALL_2) &&
			!get(x, y, z, NORTH_EAST, WALL_1) && !get(x, y, z, NORTH_EAST, WALL_2) &&
			!get(x + 1, y, z, WEST, WALL_1) && !get(x + 1, y, z, WEST, WALL_2) &&
			!get(x + 1, y, z, NORTH, WALL_1) && !get(x + 1, y, z, NORTH, WALL_2) &&
			!get(x, y + 1, z, EAST, WALL_1) && !get(x, y + 1, z, EAST, WALL_2) &&
			!get(x, y + 1, z, SOUTH, WALL_1) && !get(x, y + 1, z, SOUTH, WALL_2) &&
			!get(x + 1, y + 1, z, WEST, WALL_1) && !get(x + 1, y + 1, z, WEST, WALL_2) &&
			!get(x + 1, y + 1, z, SOUTH, WALL_1) && !get(x + 1, y + 1, z, SOUTH, WALL_2) &&
			!get(x + 1, y + 1, z, SOUTH_WEST, WALL_1) && !get(x + 1, y + 1, z, SOUTH_WEST, WALL_2);
	}
}
