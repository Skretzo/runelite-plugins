package com.chatsuccessrates;

import com.chatsuccessrates.trackers.BendingNails;
import com.chatsuccessrates.trackers.CatchingPetfish;
import com.chatsuccessrates.trackers.ChoppingCanoeStation;
import com.chatsuccessrates.trackers.CustomConfig;
import com.chatsuccessrates.trackers.FiringPottery;
import com.chatsuccessrates.trackers.LightingLogs;
import com.chatsuccessrates.trackers.MiningRock;
import com.chatsuccessrates.trackers.PickingLock;
import com.chatsuccessrates.trackers.Pickpocketing;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.ItemID;

@RequiredArgsConstructor
public enum ChatSuccessRatesAction
{
	// Construction
	BENDING_BRONZE_NAILS(new BendingNails(ItemID.NAILS_BRONZE)),
	BENDING_IRON_NAILS(new BendingNails(ItemID.NAILS_IRON)),
	BENDING_STEEL_NAILS(new BendingNails(ItemID.NAILS)),
	BENDING_BLACK_NAILS(new BendingNails(ItemID.NAILS_BLACK)),
	BENDING_MITHRIL_NAILS(new BendingNails(ItemID.NAILS_MITHRIL)),
	BENDING_ADAMANTITE_NAILS(new BendingNails(ItemID.NAILS_ADAMANT)),
	BENDING_RUNE_NAILS(new BendingNails(ItemID.NAILS_RUNE)),

	// Crafting
	FIRING_BOWL(new FiringPottery("bowl")),
	FIRING_PIE_DISH(new FiringPottery("pie dish")),
	FIRING_POT(new FiringPottery("pot")),

	// Firemaking
	LIGHTING_LOGS(new LightingLogs()),

	// Fishing
	CATCHING_BLUEFISH(new CatchingPetfish(CatchingPetfish.TYPE_BLUE)),
	CATCHING_GREENFISH(new CatchingPetfish(CatchingPetfish.TYPE_GREEN)),
	CATCHING_SPINEFISH(new CatchingPetfish(CatchingPetfish.TYPE_SPINE)),

	// Mining
	MINING_ADAMANTITE(new MiningRock("adamantite")),
	MINING_BLURITE(new MiningRock("blurite")),
	MINING_CLAY(new MiningRock("clay")),
	MINING_COAL(new MiningRock("coal")),
	MINING_COPPER(new MiningRock("copper")),
	MINING_GOLD(new MiningRock("gold")),
	MINING_IRON(new MiningRock("iron")),
	MINING_MITHRIL(new MiningRock("mithril")),
	MINING_RUNITE(new MiningRock("runite")),
	MINING_SILVER(new MiningRock("silver")),
	MINING_TIN(new MiningRock("tin")),

	// Thieving
	LOCKPICKING_PORT_SARIM_JAIL(new PickingLock("prison door", "door", 9565)),
	PICKPOCKETING_ELF(new Pickpocketing("elf")),
	PICKPOCKETING_GNOME(new Pickpocketing("gnome")),
	PICKPOCKETING_HERO(new Pickpocketing("hero")),
	PICKPOCKETING_KNIGHT(new Pickpocketing("knight")),
	PICKPOCKETING_MAN(new Pickpocketing("man")),
	PICKPOCKETING_MASTER_FARMER(new Pickpocketing("Master Farmer")),
	PICKPOCKETING_PALADIN(new Pickpocketing("paladin")),
	PICKPOCKETING_TZHAAR_HUR(new Pickpocketing("TzHaar-Hur")),
	PICKPOCKETING_VYRE(new Pickpocketing("vyre")),
	PICKPOCKETING_WARRIOR(new Pickpocketing("warrior")),
	PICKPOCKETING_WOMAN(new Pickpocketing("woman")),

	// Woodcutting
	CANOE_STATION_CHOPPING_BRONZE(new ChoppingCanoeStation("bronze", AnimationID.HUMAN_WOODCUTTING_BRONZE_AXE)),
	CANOE_STATION_CHOPPING_IRON(new ChoppingCanoeStation("iron", AnimationID.HUMAN_WOODCUTTING_IRON_AXE)),
	CANOE_STATION_CHOPPING_STEEL(new ChoppingCanoeStation("steel", AnimationID.HUMAN_WOODCUTTING_STEEL_AXE)),
	CANOE_STATION_CHOPPING_BLACK(new ChoppingCanoeStation("black", AnimationID.HUMAN_WOODCUTTING_BLACK_AXE)),
	CANOE_STATION_CHOPPING_MITHRIL(new ChoppingCanoeStation("mithril", AnimationID.HUMAN_WOODCUTTING_MITHRIL_AXE)),
	CANOE_STATION_CHOPPING_ADAMANT(new ChoppingCanoeStation("adamant", AnimationID.HUMAN_WOODCUTTING_ADAMANT_AXE)),
	CANOE_STATION_CHOPPING_RUNE(new ChoppingCanoeStation("rune", AnimationID.HUMAN_WOODCUTTING_RUNE_AXE)),

	// Other
	CUSTOM_CONFIG(new CustomConfig());

	@Getter
	private final ChatSuccessRatesTracker tracker;
}
