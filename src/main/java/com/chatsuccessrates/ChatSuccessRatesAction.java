package com.chatsuccessrates;

import com.chatsuccessrates.trackers.BendingNails;
import com.chatsuccessrates.trackers.CustomConfig;
import com.chatsuccessrates.trackers.FiringPottery;
import com.chatsuccessrates.trackers.LightingLogs;
import com.chatsuccessrates.trackers.Pickpocketing;
import lombok.Getter;
import net.runelite.api.ItemID;

public enum ChatSuccessRatesAction
{
	// Construction
	BENDING_BRONZE_NAILS(new BendingNails(ItemID.BRONZE_NAILS)),
	BENDING_IRON_NAILS(new BendingNails(ItemID.IRON_NAILS)),
	BENDING_STEEL_NAILS(new BendingNails(ItemID.STEEL_NAILS)),
	BENDING_BLACK_NAILS(new BendingNails(ItemID.BLACK_NAILS)),
	BENDING_MITHRIL_NAILS(new BendingNails(ItemID.MITHRIL_NAILS)),
	BENDING_ADAMANTITE_NAILS(new BendingNails(ItemID.ADAMANTITE_NAILS)),
	BENDING_RUNE_NAILS(new BendingNails(ItemID.RUNE_NAILS)),

	// Crafting
	FIRING_BOWL(new FiringPottery("bowl")),
	FIRING_PIE_DISH(new FiringPottery("pie dish")),
	FIRING_POT(new FiringPottery("pot")),

	// Firemaking
	LIGHTING_LOGS(new LightingLogs()),

	// Thieving
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

	// Other
	CUSTOM_CONFIG(new CustomConfig());

	@Getter
	private final ChatSuccessRatesTracker tracker;

	ChatSuccessRatesAction(ChatSuccessRatesTracker tracker)
	{
		this.tracker = tracker;
	}
}
