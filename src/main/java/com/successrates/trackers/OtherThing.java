package com.successrates.trackers;

import com.successrates.SuccessRatesSkill;
import com.successrates.SuccessRatesTracker;

public class OtherThing extends SuccessRatesTracker
{
	@Override
	public SuccessRatesSkill getSkill()
	{
		return SuccessRatesSkill.CUSTOM;
	}
}
