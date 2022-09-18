package com.damagetracker;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import static net.runelite.api.MenuAction.MENU_ACTION_DEPRIORITIZE_OFFSET;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.StringUtils;

@PluginDescriptor(
	name = "Damage Tracker",
	description = "Track and display damage distributions",
	tags = {"track", "display", "damage", "dealt", "distribution"}
)
public class DamageTrackerPlugin extends Plugin
{
	protected static final List<Integer> WEAPON_STANCE_WIDGET_CHILD_IDS = Arrays.asList(4, 8, 12, 16, 21, 26);
	private static final Map<Integer, Integer> SKILL_INDICES = new HashMap<>();
	private static final String CONFIG_KEY = "tracker_";
	private static final String CONFIG_GROUP = DamageTracker.class.getSimpleName().toLowerCase();
	private static final String DEFAULT_TRACKER_NAME = "Tracker ";
	private static final String UPDATE_TRACKER = "Update tracker";

	static
	{
		int i = 1;
		List<Integer> indices = Arrays.asList(0, 2, 1, 4, 5, 6, 20, 22, 3, 16, 15, 17, 12, 9, 18, 21, 14, 13, 10, 7, 11, 8, 19);
		for (int index : indices)
		{
			SKILL_INDICES.put(i++, index);
		}
	}

	@Getter(AccessLevel.PACKAGE)
	private final List<DamageTracker> trackers = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	@Setter(AccessLevel.PACKAGE)
	private DamageTrackerPanel renameTracker = null;
	private DamageTrackerPluginPanel pluginPanel;
	private NavigationButton navigationButton;

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private DamageTrackerConditionsOverlay conditionsOverlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private Gson gson;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp()
	{
		pluginPanel = new DamageTrackerPluginPanel(this);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/panel_icon.png");

		navigationButton = NavigationButton.builder()
			.tooltip(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(DamageTracker.class.getSimpleName()), " "))
			.icon(icon)
			.priority(5)
			.panel(pluginPanel)
			.build();

		loadTrackers();

		overlayManager.add(conditionsOverlay);
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(conditionsOverlay);
		clientToolbar.removeNavigation(navigationButton);

		pluginPanel = null;
		navigationButton = null;
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		boolean isOwnDamage = event.getHitsplat().isMine();
		int damage = event.getHitsplat().getAmount();
		int targetId = event.getActor() instanceof NPC ? ((NPC) event.getActor()).getId() : -2;
		String targetName = event.getActor().getName();

		boolean update = false;
		for (DamageTracker tracker : trackers)
		{
			if (tracker.shouldUpdate(isOwnDamage, targetName, targetId, client))
			{
				update = true;
				SwingUtilities.invokeLater(() ->
				{
					tracker.update(damage);
					saveTracker(tracker);
				});
			}
		}
		boolean finalUpdate = update;
		SwingUtilities.invokeLater(() ->
		{
			if (finalUpdate)
			{
				pluginPanel.update();
			}
		});
	}

	@Subscribe
	public void onMenuEntryAdded(final MenuEntryAdded event)
	{
		if (renameTracker == null)
		{
			return;
		}

		int type = event.getType();

		if (type >= MENU_ACTION_DEPRIORITIZE_OFFSET)
		{
			type -= MENU_ACTION_DEPRIORITIZE_OFFSET;
		}

		final MenuAction menuAction = MenuAction.of(type);
		final int groupId = WidgetInfo.TO_GROUP(event.getActionParam1());
		final int childId = WidgetInfo.TO_CHILD(event.getActionParam1());

		if (MenuAction.EXAMINE_NPC.equals(menuAction))
		{
			createMenuEntry(event, this::updateTrackerNpcInfo);
		}
		else if (groupId == WidgetID.EQUIPMENT_GROUP_ID &&
				(childId == 1 || (childId >= 15 && childId <= 25 && "Remove".equals(event.getOption()))))
		{
			createMenuEntry(event, e -> updateTrackerEquipmentInfo(childId));
		}
		else if (groupId == WidgetID.PRAYER_GROUP_ID && (childId >= 5 && childId <= 33))
		{
			createMenuEntry(event, e -> updateTrackerPrayerInfo(event.getTarget()));
		}
		else if (groupId == WidgetID.COMBAT_GROUP_ID && WEAPON_STANCE_WIDGET_CHILD_IDS.contains(childId))
		{
			createMenuEntry(event, e -> updateTrackerAttackStyleInfo(childId));
		}
		else if (groupId == WidgetID.SKILLS_GROUP_ID && (childId >= 1 && childId <= 23))
		{
			createMenuEntry(event, e -> updateTrackerSkillLevelInfo(childId));
		}
	}

	private void createMenuEntry(MenuEntryAdded event, Consumer<MenuEntry> callback)
	{
		client.createMenuEntry(-1)
			.setOption(UPDATE_TRACKER)
			.setTarget(event.getTarget())
			.setParam0(event.getActionParam0())
			.setParam1(event.getActionParam1())
			.setIdentifier(event.getIdentifier())
			.setType(MenuAction.RUNELITE)
			.onClick(callback);
	}

	private void updateTrackerNpcInfo(MenuEntry entry)
	{
		final NPC npc = client.getCachedNPCs()[entry.getIdentifier()];

		if (npc == null || npc.getName() == null || renameTracker == null)
		{
			return;
		}

		renameTracker.setTrackerText(npc.getName());
		renameTracker.setTargetInfo(npc.getName() + "#" + npc.getId());

		saveTracker(renameTracker.getTracker());
	}

	private void updateTrackerEquipmentInfo(int childId)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		Map<Integer, Integer> equipmentIds = renameTracker.getTracker().getEquipmentIds();
		if (equipment == null)
		{
			if (childId == 1)
			{
				equipmentIds.clear();
			}
			return;
		}

		int childIdEnd = childId + 1;

		if (childId == 1)
		{
			childId = 15;
			childIdEnd = 26;
		}

		for (int i = childId; i < childIdEnd; i++)
		{
			int slot = widgetChildIdToEquipmentSlot(i);
			Integer currentId = equipmentIds.get(slot);
			Integer id = (equipment.getItem(slot) != null) ? equipment.getItem(slot).getId() : null;
			if (id == null || id.equals(currentId))
			{
				equipmentIds.remove(slot);
			}
			else
			{
				equipmentIds.put(slot, id);
			}
		}

		saveTracker(renameTracker.getTracker());
	}

	private void updateTrackerPrayerInfo(String prayerName)
	{
		Integer prayerIndex = prayerNameToPrayerIndex(prayerName);
		if (prayerIndex >= 0 && !renameTracker.getTracker().getPrayerIds().contains(prayerIndex))
		{
			renameTracker.getTracker().getPrayerIds().add(prayerIndex);
		}
		else
		{
			renameTracker.getTracker().getPrayerIds().remove(prayerIndex);
		}
		saveTracker(renameTracker.getTracker());
	}

	private void updateTrackerAttackStyleInfo(int childId)
	{
		Integer attackStyle = widgetChildIdToAttackStyleIndex(childId);
		Integer currentAttackStyle = renameTracker.getTracker().getAttackStyle();
		renameTracker.getTracker().setAttackStyle(attackStyle.equals(currentAttackStyle) ? null : attackStyle);
		saveTracker(renameTracker.getTracker());
	}

	private void updateTrackerSkillLevelInfo(int childId)
	{
		Map<Integer, Integer> skills = renameTracker.getTracker().getSkillLevels();

		int skillIndex = widgetChildIdToSkillIndex(childId);
		if (skills.containsKey(skillIndex))
		{
			skills.remove(skillIndex);
		}
		else
		{
			skills.put(skillIndex, client.getBoostedSkillLevel(Skill.values()[skillIndex]));
		}

		saveTracker(renameTracker.getTracker());
	}

	public int widgetChildIdToSkillIndex(int childId)
	{
		// 1 -> 0, 2 -> 2, 3 -> 1, 4 -> 4, 5 -> 5, 6 -> 6, 7 -> 20, 8 -> 22, 9 -> 3, 10 -> 16, 11 -> 15, 12 -> 17
		// 13 -> 12, 14 -> 9, 15 -> 18, 16 -> 21, 17 -> 14, 18 -> 13, 19 -> 10, 20 -> 7, 21 -> 11, 22 -> 8, 23 -> 19
		return SKILL_INDICES.get(Math.max(Math.min(childId, 23), 1));
	}

	public int widgetChildIdToAttackStyleIndex(int childId)
	{
		// 4 -> 0, 8 -> 1, 12 -> 2, 16 -> 3, 21 -> 4, 26 -> 4
		return Math.max(Math.min(childId, 20), 4) / 4 - 1;
	}

	public int prayerNameToPrayerIndex(String prayerName)
	{
		prayerName = Text.removeFormattingTags(prayerName).toUpperCase().replace(' ', '_');
		for (Prayer prayer : Prayer.values())
		{
			if (prayer.name().equals(prayerName))
			{
				return prayer.ordinal();
			}
		}
		return -1;
	}

	public int widgetChildIdToEquipmentSlot(int childId)
	{
		int slotOffset = 15; // helm, cape, amulet, weapon, body, shield
		if (childId >= 24) // ring, ammo
		{
			slotOffset = 12;
		}
		else if (childId >= 22) // gloves, boots
		{
			slotOffset = 13;
		}
		else if (childId == 21) // legs
		{
			slotOffset = 14;
		}
		return childId - slotOffset;
	}

	public DamageTracker addTracker()
	{
		DamageTracker tracker = new DamageTracker(this,
			new DamageTrackerData(
				Instant.now().toEpochMilli(),
				DEFAULT_TRACKER_NAME + (trackers.size() + 1),
				true,
				false,
				true,
				"",
				new HashMap<>(),
				new ArrayList<>(),
				null,
				new HashMap<>(),
				new HashMap<>()));
		trackers.add(tracker);
		return tracker;
	}

	public void removeTracker(DamageTracker tracker)
	{
		tracker.reset();
		trackers.remove(tracker);
		configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY + tracker.getId());
	}

	public void saveTracker(DamageTracker tracker)
	{
		DamageTrackerData trackerData = tracker.toDamageTrackerData();
		String json = gson.toJson(trackerData);
		if (!Strings.isNullOrEmpty(json))
		{
			configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY + trackerData.getId(), json);
		}
	}

	private void loadTrackers()
	{
		trackers.clear();

		List<String> keys = configManager.getConfigurationKeys(CONFIG_GROUP + "." + CONFIG_KEY);
		for (String key : keys)
		{
			String[] str = key.split("\\.", 2);
			if (str.length == 2)
			{
				String json = configManager.getConfiguration(str[0], str[1]);
				if (Strings.isNullOrEmpty(json))
				{
					configManager.unsetConfiguration(str[0], str[1]);
				}
				else
				{
					trackers.add(new DamageTracker(this, gson.fromJson(json, new TypeToken<DamageTrackerData>(){}.getType())));
				}
			}
		}

		pluginPanel.rebuild();
		pluginPanel.update();
	}
}
