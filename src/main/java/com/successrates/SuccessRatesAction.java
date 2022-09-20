package com.successrates;

import com.successrates.trackers.BendingNails;
import com.successrates.trackers.FiringPottery;
import com.successrates.trackers.LightingLogs;
import com.successrates.trackers.OtherThing;
import lombok.Getter;

public enum SuccessRatesAction
{
	// Construction
	BENDING_NAILS(new BendingNails()),

	// Crafting
	FIRING_BOWL(new FiringPottery("bowl")),
	FIRING_PIE_DISH(new FiringPottery("pie dish")),
	FIRING_POT(new FiringPottery("pot")),

	// Firemaking
	LIGHTING_LOGS(new LightingLogs()),

	// Other
	OTHER_THING(new OtherThing());

	@Getter
	private final SuccessRatesTracker tracker;

	SuccessRatesAction(SuccessRatesTracker tracker)
	{
		this.tracker = tracker;
	}
}
