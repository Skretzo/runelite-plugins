package com.successrates.trackers;

import com.successrates.SuccessRatesTracker;
import net.runelite.api.Skill;

public class OtherThing extends SuccessRatesTracker
{
	@Override
	public Skill getSkill()
	{
		return Skill.OVERALL;
	}
}
