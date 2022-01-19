package pathfinding.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import pathfinding.PathfindingConfig;
import pathfinding.Util;

public class Pathfinder implements Runnable
{
	private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3523, 448, 448, 0);
	private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);

	@Getter
	private final CollisionMap map;

	@Getter
	private final Map<WorldPoint, List<WorldPoint>> transports;

	private final Client client;
	private final PathfindingConfig config;

	private final List<Node> boundary = new LinkedList<>();
	private final Set<WorldPoint> visited = new HashSet<>();

	private Node start;
	private Node nearest;

	@Getter
	@Setter
	private WorldPoint target;

	@Getter
	private List<WorldPoint> path;

	@Getter
	private boolean calculating;

	public Pathfinder(CollisionMap map, Map<WorldPoint, List<WorldPoint>> transports, Client client, PathfindingConfig config)
	{
		this.map = map;
		this.transports = transports;
		this.client = client;
		this.config = config;
	}

	public void setStart(WorldPoint start)
	{
		this.start = new Node(start, null);
	}

	public void start()
	{
		new Thread(this).start();
	}

	public void clear()
	{
		start = null;
		target = null;
		path = null;
		nearest = null;
		calculating = false;
		boundary.clear();
		visited.clear();
	}

	public void addCollisionMap()
	{
		Util.addCollisionMap(map, client);
	}

	public List<WorldPoint> getCurrentPath()
	{
		return nearest == null ? null : nearest.getPath();
	}

	public void updatePath()
	{
		if (path == null)
		{
			return;
		}

		if (isNearTarget())
		{
			clear();
		}

		if (!isNearPath())
		{
			if (config.cancelInstead())
			{
				clear();
			}
			else if (client.getLocalPlayer() != null)
			{
				setStart(client.getLocalPlayer().getWorldLocation());
				start();
			}
		}
	}

	@Override
	public void run()
	{
		path = find();
	}

	private List<WorldPoint> find()
	{
		path = null;
		nearest = null;
		calculating = true;
		boundary.clear();
		visited.clear();

		if (client.getLocalPlayer() == null || target == null)
		{
			return null;
		}

		if (start == null || start.position == null)
		{
			setStart(client.getLocalPlayer().getWorldLocation());
		}

		boundary.add(start);

		int bestDistance = Integer.MAX_VALUE;

		while (!boundary.isEmpty())
		{
			Node node = boundary.remove(0);

			if (target == null || node == null)
			{
				return null;
			}

			if (target.equals(node.position))
			{
				calculating = false;
				return node.getPath();
			}

			int distance = Math.max(Math.abs(node.position.getX() - target.getX()), Math.abs(node.position.getY() - target.getY()));
			if (nearest == null || distance < bestDistance)
			{
				nearest = node;
				bestDistance = distance;
			}

			if (visited.size() >= Integer.MAX_VALUE) // todo: find something better
			{
				calculating = false;
				return nearest.getPath();
			}

			addNeighbors(node);
		}

		calculating = false;

		if (nearest != null)
		{
			return nearest.getPath();
		}

		return null;
	}

	private void addNeighbors(Node node)
	{
		int x = node.position.getX();
		int y = node.position.getY();
		int z = node.position.getPlane();

		if (map.w(x - 1, y, z))
		{
			addNeighbor(node, new WorldPoint(x - 1, y, z));
		}

		if (map.e(x + 1, y, z))
		{
			addNeighbor(node, new WorldPoint(x + 1, y, z));
		}

		if (map.s(x, y - 1, z))
		{
			addNeighbor(node, new WorldPoint(x, y - 1, z));
		}

		if (map.n(x, y + 1, z))
		{
			addNeighbor(node, new WorldPoint(x, y + 1, z));
		}

		if (map.sw(x - 1, y - 1, z))
		{
			addNeighbor(node, new WorldPoint(x - 1, y - 1, z));
		}

		if (map.se(x + 1, y - 1, z))
		{
			addNeighbor(node, new WorldPoint(x + 1, y - 1, z));
		}

		if (map.nw(x - 1, y + 1, z))
		{
			addNeighbor(node, new WorldPoint(x - 1, y + 1, z));
		}

		if (map.ne(x + 1, y + 1, z))
		{
			addNeighbor(node, new WorldPoint(x + 1, y + 1, z));
		}

		for (WorldPoint transport : transports.getOrDefault(node.position, new ArrayList<>()))
		{
			addNeighbor(node, transport);
		}
	}

	private void addNeighbor(Node node, WorldPoint neighbor)
	{
		if (config.avoidWilderness() && isInWilderness(neighbor) && !isInWilderness(node.position) && !isInWilderness(target))
		{
			return;
		}

		if (!visited.add(neighbor))
		{
			return;
		}

		boundary.add(new Node(neighbor, node));
	}

	private boolean isInWilderness(WorldPoint p)
	{
		return WILDERNESS_ABOVE_GROUND.distanceTo(p) == 0 || WILDERNESS_UNDERGROUND.distanceTo(p) == 0;
	}

	private boolean isNearPath()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}
		for (WorldPoint point : path)
		{
			if (client.getLocalPlayer().getWorldLocation().distanceTo(point) < config.recalculateDistance())
			{
				return true;
			}
		}
		return false;
	}

	private boolean isNearTarget()
	{
		return target != null && client.getLocalPlayer() != null &&
			client.getLocalPlayer().getWorldLocation().distanceTo(target) < config.reachedDistance();
	}

	private static class Node
	{
		public final WorldPoint position;
		public final Node previous;

		public Node(WorldPoint position, Node previous)
		{
			this.position = position;
			this.previous = previous;
		}

		public List<WorldPoint> getPath()
		{
			List<WorldPoint> path = new LinkedList<>();
			Node node = this;

			while (node != null)
			{
				path.add(0, node.position);
				node = node.previous;
			}

			return new ArrayList<>(path);
		}
	}
}
