# NPC stack reorderer

Reorders NPC stacks with invisible NPCs from showing the oldest NPCs to instead show the newest NPCs.

## Info
Only 5 NPCs of size bigger than 1x1 can be displayed at once in a stack when they all have at least one tile in common. The 6th, 7th, ... , Nth NPCs will turn invisible, even in the right-click menu. Apart from some exceptions, the NPCs are ordered from the oldest to newest. This is the order in which they were transmitted to the client and first appeared in the render viewport. Recently respawned NPCs are the newest. Ties are resolved in a first-in-first-out order for all 8x8 map chunks/zones in the current render viewport around the player starting from south-west and ending at north-east.

Some NPCs are purposely implemented to always be on top. This is the case for certain bosses and many non-combat NPCs that you can talk to. This plugin is currently not able to detect and account for NPCs that are always on top, but this should be a rare occurence.

Players contribute to the limit of only displaying 5 NPCs bigger than 1x1 at a time, but players are always prioritized over NPCs and will thus never become invisible.

1x1 NPCs also contribute to the limit of only displaying 5 NPCs bigger than 1x1 at a time, but they only contribute if they are among the 5 oldest NPCs in the stack and their contribution is only binary. If at least one 1x1 NPC is among the 5 oldest NPCs in the stack then 1x1 NPCs not among the 5 oldest NPCs will be shown in the right-click menu. However, if there are no 1x1 NPCs among the 5 oldest NPCs in the stack then 1x1 NPCs not among the 5 oldest NPCs will not be shown in the right click menu and instead turn invisible.

This plugin uses entity hiding to hide enough old NPCs in the stack to show the 5 newest NPCs in the stack.

## Alternatives
- Use AoE attacks like chinchompas or barrage to target invisible NPCs via visible NPCs. The invisible NPCs remain invisible, even while in combat.
- Use entity hider with config option `Hide attackers` enabled to hide visible NPCs, which allows invisible NPCs to turn visible until they also become attackers. When later disabling the entity hider or the config option `Hide attackers` the previously visible non-attacker NPCs will turn invisible.