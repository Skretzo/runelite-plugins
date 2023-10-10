package com.chatsuccessrates;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Skill;

@RequiredArgsConstructor
public enum ChatSuccessRatesSkill
{
	CUSTOM(null),
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
}
