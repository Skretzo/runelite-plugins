package com.chatsuccessrates;

import lombok.Getter;
import net.runelite.api.Skill;

public enum ChatSuccessRatesSkill
{
	CUSTOM(Skill.OVERALL),
	AGILITY(Skill.AGILITY),
	ATTACK(Skill.ATTACK),
	CONSTRUCTION(Skill.CONSTRUCTION),
	COOKING(Skill.COOKING),
	CRAFTING(Skill.CRAFTING),
	DEFENCE(Skill.DEFENCE),
	FARMING(Skill.FARMING),
	FIREMAKING(Skill.FIREMAKING),
	FISHING(Skill.FISHING),
	FLETCHING(Skill.FLETCHING),
	HERBLORE(Skill.HERBLORE),
	HITPOINTS(Skill.HITPOINTS),
	HUNTER(Skill.HUNTER),
	MAGIC(Skill.MAGIC),
	MINING(Skill.MINING),
	PRAYER(Skill.PRAYER),
	RANGED(Skill.RANGED),
	RUNECRAFT(Skill.RUNECRAFT),
	SLAYER(Skill.SLAYER),
	SMITHING(Skill.SMITHING),
	STRENGTH(Skill.STRENGTH),
	THIEVING(Skill.THIEVING),
	WOODCUTTING(Skill.WOODCUTTING);

	@Getter
	private final Skill skill;

	ChatSuccessRatesSkill(Skill skill)
	{
		this.skill = skill;
	}

	public static ChatSuccessRatesSkill from(Skill skill)
	{
		for (ChatSuccessRatesSkill chatSuccessRatesSkill : ChatSuccessRatesSkill.values())
		{
			if (chatSuccessRatesSkill.skill.equals(skill))
			{
				return chatSuccessRatesSkill;
			}
		}
		return CUSTOM;
	}
}
