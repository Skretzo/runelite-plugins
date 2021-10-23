package com.successrates;

import com.successrates.trackers.BendingNails;
import com.successrates.trackers.FiringBowl;
import com.successrates.trackers.FiringPieDish;
import com.successrates.trackers.FiringPot;
import com.successrates.trackers.LightingLogs;
import com.successrates.trackers.OtherThing;
import lombok.Getter;

public enum SuccessRatesAction
{
	// Construction
	BENDING_NAILS(new BendingNails()),

	// Crafting
	FIRING_BOWL(new FiringBowl()),
	FIRING_PIE_DISH(new FiringPieDish()),
	FIRING_POT(new FiringPot()),

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
