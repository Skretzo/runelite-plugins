package com.npcstackreorderer;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "NPC stack reorderer",
	description = "Reorders stacks of NPCs that contain invisible NPCs" +
		"<br>from showing the 5 \"oldest\" to instead show the 5 \"newest\"." +
		"<br>The \"newest\" NPC is the last npc to spawn or enter the scene.",
	tags = {"stacking", "reordering", "hide", "hiding", "invert", "reverse"}
)
public class NpcStackReordererPlugin extends Plugin
{
	/**
	 * The maximum number of NPCs that the client will draw and show in the
	 * right-click menu if at least three NPCs are bigger than 1x1 and all the
	 * NPCs are overlapping with at least one tile. The 6th, 7th, ... , Nth NPCs
	 * will be invisible, and the order is from oldest (1st) to newest (Nth) NPC
	 * that the client first encountered when iterating the scene. This is the
	 * same order as client.getNpcs(). Players and 1x1 NPCs contribute 
	 * separately to the limit as a binary count (0 or 1). Unlike 1x1 NPCs
	 * the players are prioritized and cannot become invisible. This limit
	 * is presumably in the client to improve performance by not having to
	 * render thousands of NPCs at once. It is a problem for niche accounts
	 * without access to aoe attacks to tag healers during the zuk fight
	 */
	private static final int BIG_NPC_STACK_DRAW_LIMIT = 5;

	@Inject
	private Client client;

	@Inject
	private Hooks hooks;

	@Inject
	private NpcStackReordererConfig config;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	private final Map<WorldPoint, List<NpcInfo>> overlapActors = new HashMap<>(100);
	private final Set<WorldPoint> overlapStacks = new HashSet<>(30);

	@Provides
	NpcStackReordererConfig providesConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NpcStackReordererConfig.class);
	}

	@Override
	protected void startUp()
	{
		hooks.registerRenderableDrawListener(drawListener);
	}

	@Override
	protected void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		overlapActors.clear();
		overlapStacks.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		WorldView worldView = client.getTopLevelWorldView();

		overlapActors.clear();
		overlapStacks.clear();

		for (Player player : worldView.players())
		{
			List<NpcInfo> tileOverlapActors = overlapActors.getOrDefault(player.getWorldLocation(), new ArrayList<>(10));
			tileOverlapActors.add(new NpcInfo(player));
			overlapActors.put(player.getWorldLocation(), tileOverlapActors);
		}

		for (NPC npc : worldView.npcs())
		{
			WorldPoint npcWorldLocation = npc.getWorldLocation();
			if (npcWorldLocation == null)
			{
				continue;
			}

			int x = npcWorldLocation.getX();
			int y = npcWorldLocation.getY();
			int z = npcWorldLocation.getPlane();

			NPCComposition npcComposition = npc.getTransformedComposition();
			if (npcComposition == null)
			{
				continue;
			}

			int size = npcComposition.getSize();

			for (int i = 0; i < size; i++)
			{
				for (int j = 0; j < size; j++)
				{
					WorldPoint tileWorldLocation = new WorldPoint(x + i, y + j, z);
					List<NpcInfo> tileOverlapActors = overlapActors.getOrDefault(tileWorldLocation, new ArrayList<>(10));
					tileOverlapActors.add(new NpcInfo(npc, size));
					overlapActors.put(tileWorldLocation, tileOverlapActors);
					if (tileOverlapActors.size() > BIG_NPC_STACK_DRAW_LIMIT) // TODO: && atLeastOneBigNpc(tileOverlapActors))
					{
						overlapStacks.add(tileWorldLocation);
					}
				}
			}
		}
	}

	/*
	private boolean atLeastOneBigNpc(List<Actor> tileOverlapActors)
	{
		for (Actor actor : tileOverlapActors)
		{
			if (actor instanceof NPC)
			{
				NPCComposition npcComposition = ((NPC) actor).getTransformedComposition();
				if (npcComposition != null && npcComposition.getSize() > 1)
				{
					return true;
				}
			}
		}
		return false;
	}
	*/

	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof NPC)
		{
			NPC npc = (NPC) renderable;
			for (WorldPoint worldLocation : overlapStacks)
			{
				List<NpcInfo> actors = overlapActors.get(worldLocation);
				int numberOfBiggerThan1x1Npcs = 0;
				int binaryNumberOf1x1NpcsAmongFirst5 = 0; // 0 or 1
				int binaryNumberOf1x1NpcsPostFirst5 = 0; // 0 or 1
				int binaryNumberOfPlayers = 0; // 0 or 1
				int i = 0;
				for (NpcInfo actor : actors)
				{
					if (actor.isNpc())
					{
						if (!actor.isSize1x1())
						{
							numberOfBiggerThan1x1Npcs++;
						}
						else if (binaryNumberOf1x1NpcsAmongFirst5 == 0
							&& (i < BIG_NPC_STACK_DRAW_LIMIT))
							//|| (?) < BIG_NPC_STACK_DRAW_LIMIT))
						{
							binaryNumberOf1x1NpcsAmongFirst5++;
						}
					}
					else if (binaryNumberOfPlayers == 0) // not NPC so is Player
					{
						binaryNumberOfPlayers++;
					}
					i++;
				}
				int numberOfHideNpcs = (numberOfBiggerThan1x1Npcs + binaryNumberOfPlayers +
					binaryNumberOf1x1NpcsAmongFirst5 - BIG_NPC_STACK_DRAW_LIMIT);
				i = 0;
				if (numberOfHideNpcs > 0)
				{
					for (NpcInfo actor : actors)
					{
						if (actor.isNpc())
						{
							NPC hideNpc = (NPC) actor.getActor();
							if (i < numberOfHideNpcs && npc.equals(hideNpc))
							{
								return false;
							}
							i++;
						}
					}
				}
			}
		}
		//else if (renderable instanceof Player) // TODO

		if (renderable instanceof NPC)
		{
			NPC hideActor = (NPC) renderable;
			for (WorldPoint worldLocation : overlapStacks)
			{
				List<NpcInfo> actors = overlapActors.get(worldLocation);
				for (int i = actors.size() - BIG_NPC_STACK_DRAW_LIMIT - 1; i >= 0; i--)
				{
					NpcInfo actor = actors.get(i);
					if (hideActor.equals(actor.getActor()) && (config.hidePlayers() || !(actor instanceof Player)))
					{
						// TODO: this fails for and will hide 1N1 but should hide nothing
						// [1N1 1N2 1N3 1N4 1N5 1N6] -> hide nothing
						return false;
					}
				}
			}
		}
		/*
		else if (renderable instanceof Player)
		{
			Player player = (Player) renderable;
			for (WorldPoint worldLocation : overlapStacks)
			{
				List<NpcInfo> actors = overlapActors.get(worldLocation);
				// TODO
			}
		}
		*/

		return true;
	}
}
