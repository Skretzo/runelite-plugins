package xrio.worldlocation;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WorldLocationPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WorldLocationPlugin.class);
		RuneLite.main(args);
	}
}