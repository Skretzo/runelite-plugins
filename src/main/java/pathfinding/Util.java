package pathfinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.Tile;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldPoint;
import static pathfinding.PathfindingPlugin.BLOCKED;
import static pathfinding.PathfindingPlugin.COLLISION_MAP_DIR;
import static pathfinding.PathfindingPlugin.COLLISION_MAP_FILE_DELIMITER;
import static pathfinding.PathfindingPlugin.COLLISION_MAP_FILE_EXTENSION;
import static pathfinding.PathfindingPlugin.FLAG_COUNT;
import static pathfinding.PathfindingPlugin.FLAG_MULTIPLIER;
import static pathfinding.PathfindingPlugin.FLAGS;
import static pathfinding.PathfindingPlugin.WALL_INFO;
import static pathfinding.PathfindingPlugin.WALLS;
import static net.runelite.api.Constants.REGION_SIZE;
import static net.runelite.api.Constants.MAX_Z;
import pathfinding.pathfinder.CollisionMap;
import pathfinding.pathfinder.FlagMap;
import pathfinding.pathfinder.SplitFlagMap;

public class Util
{
	public static CollisionMap loadCollisionMap()
	{
		Map<SplitFlagMap.Position, boolean[]> compressedRegion = new HashMap<>();
		compressedRegion.put(new SplitFlagMap.Position(50, 50), defaultCollisionMap()); // Lumbridge Castle

		CollisionMap map = new CollisionMap(compressedRegion);

		if (!COLLISION_MAP_DIR.exists())
		{
			COLLISION_MAP_DIR.mkdirs();
		}

		for (File file : COLLISION_MAP_DIR.listFiles())
		{
			if (file.isDirectory() || !file.getName().endsWith(COLLISION_MAP_FILE_EXTENSION))
			{
				continue;
			}

			String[] region_indices = file.getName().substring(0,
				file.getName().indexOf(COLLISION_MAP_FILE_EXTENSION)).split(COLLISION_MAP_FILE_DELIMITER);

			if (region_indices.length != 2)
			{
				continue;
			}

			int regionX;
			int regionY;
			try
			{
				regionX = Integer.parseInt(region_indices[0]);
				regionY = Integer.parseInt(region_indices[1]);
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			boolean[] collisionMap = defaultCollisionMap();

			try (InflaterInputStream stream = new InflaterInputStream(new FileInputStream(new File(
				COLLISION_MAP_DIR, regionX + COLLISION_MAP_FILE_DELIMITER + regionY + COLLISION_MAP_FILE_EXTENSION))))
			{
				int b;
				int i = 0;
				while ((b = stream.read()) != -1)
				{
					collisionMap[i++] = b == 1;
				}
			}
			catch (IOException e)
			{
				continue;
			}

			map.add(regionX, regionY, collisionMap);
		}

		return map;
	}

	public static Map<WorldPoint, List<WorldPoint>> loadTransports()
	{
		Map<WorldPoint, List<WorldPoint>> transports = new HashMap<>();

		try
		{
			String s = new String(Util.readAllBytes(PathfindingPlugin.class.getResourceAsStream("transports.txt")), StandardCharsets.UTF_8);
			Scanner scanner = new Scanner(s);
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine();

				if (line.startsWith("#") || line.isEmpty())
				{
					continue;
				}

				String[] l = line.split(" ");
				WorldPoint a = new WorldPoint(Integer.parseInt(l[0]), Integer.parseInt(l[1]), Integer.parseInt(l[2]));
				WorldPoint b = new WorldPoint(Integer.parseInt(l[3]), Integer.parseInt(l[4]), Integer.parseInt(l[5]));
				transports.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return transports;
	}

	public static void addCollisionMap(CollisionMap map, Client client) // todo: something is wrong when re-generating collision map, it does not update
	{
		if (client.getCollisionMaps() == null)
		{
			return;
		}

		CollisionData[] collisionData = client.getCollisionMaps();
		Tile[][][] tiles = client.getScene().getTiles();

		// Scene corners, but accounted for some scuffed edge collision data
		WorldPoint sceneBottomLeft = tiles[0][0][0].getWorldLocation().dx(1).dy(1);
		WorldPoint sceneTopRight = tiles[0][tiles[0].length - 1][tiles[0][0].length - 1].getWorldLocation().dx(-5).dy(-5);

		for (int id : client.getMapRegions())
		{
			int regionX = (id >> 8);
			int regionY = (id % REGION_SIZE);
			int regionMinX = regionX * REGION_SIZE;
			int regionMinY = regionY * REGION_SIZE;

			// System.out.println(regionX + ", " + regionY + "    minX = " + regionMinX + ", minY = " + regionMinY); // todo: remove

			/*
			int regionMaxX = regionMinX + 64 - 1;
			int regionMaxY = regionMinY + 64 - 1;

			if (regionMinX < sceneBottomLeft.getX() || regionMinY < sceneBottomLeft.getY() ||
				regionMaxX > sceneTopRight.getX() || regionMaxY > sceneTopRight.getY())
			{
				continue;
			}
			*/

			boolean[] collisionMap = map.get(regionX, regionY);

			if (collisionMap == null)
			{
				collisionMap = defaultCollisionMap();
			}

			for (int z = 0; z < MAX_Z; z++)
			{
				for (Tile[] tileRow : tiles[z])
				{
					for (Tile tile : tileRow)
					{
						if (tile == null || tile.getWorldLocation().getRegionID() != id)
						{
							continue;
						}

						int x = tile.getWorldLocation().getX();
						int y = tile.getWorldLocation().getY();

						if (x < sceneBottomLeft.getX() || y < sceneBottomLeft.getY() ||
							x > sceneTopRight.getX() || y > sceneTopRight.getY())
						{
							continue;
						}

						int data = collisionData[z].getFlags()[tile.getSceneLocation().getX()][tile.getSceneLocation().getY()];
						Set<MovementFlag> movementFlags = MovementFlag.getSetFlags(data);

						boolean blocked = movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_OBJECT) ||
							movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FULL) ||
							movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR) ||
							movementFlags.contains(MovementFlag.BLOCK_MOVEMENT_FLOOR_DECORATION);

						// int idx = (z * REGION_SIZE * REGION_SIZE + (y - regionMinY) * REGION_SIZE + (x - regionMinX)) * FLAG_COUNT * FLAG_MULTIPLIER;

						if (blocked)
						{
							collisionMap[FlagMap.index(x, y, z, BLOCKED, WALLS[0], regionMinX, regionMinY)] = true; // idx + FLAG_COUNT * 0 + BLOCKED
							for (int j = 0; j < WALL_INFO.length; j++)
							{
								collisionMap[FlagMap.index(x, y, z, FLAGS[j], WALLS[0], regionMinX, regionMinY)] = true; // idx + FLAG_COUNT * 0 + FLAGS[j]
							}
							continue;
						}

						if (tile.getWallObject() == null)
						{
							continue;
						}

						WallObject wall = tile.getWallObject();

						// do something with doors here. Maybe use the name/target "Door" and/or option "Open"?

						int[] orientations = new int[] { wall.getOrientationA(), wall.getOrientationB() };
						for (int i = 0; i < orientations.length; i++)
						{
							int o = orientations[i];
							int wallIndex = WALLS[i];
							for (int j = 0; j < WALL_INFO.length; j++)
							{
								if (o == WALL_INFO[j])
								{
									collisionMap[FlagMap.index(x, y, z, FLAGS[j], wallIndex, regionMinX, regionMinY)] = true; // idx + FLAG_COUNT * wallIndex + FLAGS[j]
									break;
								}
							}
						}
					}
				}
			}

			map.add(regionX, regionY, collisionMap);

			if (!COLLISION_MAP_DIR.exists())
			{
				COLLISION_MAP_DIR.mkdirs();
			}

			try (DeflaterOutputStream stream = new DeflaterOutputStream(new FileOutputStream(new File(
				COLLISION_MAP_DIR, regionX + COLLISION_MAP_FILE_DELIMITER + regionY + COLLISION_MAP_FILE_EXTENSION))))
			{
				for (boolean b : collisionMap)
				{
					stream.write(b ? 1 : 0);
				}
				System.out.println("id = " + id + "   " + regionX + ", " + regionY + "   file created !"); // todo: remove
			}
			catch (IOException e)
			{
			}
		}
	}

	public static boolean[] defaultCollisionMap()
	{
		return new boolean[MAX_Z * REGION_SIZE * REGION_SIZE * FLAG_COUNT * FLAG_MULTIPLIER];
	}

	private static byte[] readAllBytes(InputStream in) throws IOException
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		int read;
		while ((read = in.read(buffer, 0, buffer.length)) != -1)
		{
			result.write(buffer, 0, read);
		}

		return result.toByteArray();
	}
}
