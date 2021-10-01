# Radius Markers
![Icon](icon.png)

## Info
![Example usage of radius markers in-game](https://user-images.githubusercontent.com/53493631/135122278-a9649fa5-e567-4ac3-86ba-6f14a544113c.png)  
*Radius marker for a lonely chicken north-east of Lumbridge east farm*

A radius marker is a way to display the spawn point, wander range, retreat range and/or max range of an NPC in the game scene. You place down a marker by clicking the ![plus icon](../radius-markers/src/main/resources/com/radiusmarkers/add_icon.png) icon in the plugin panel while being logged in-game. This will place the marker at the feet of your character, but the location can easily be adjusted in the panel later. This plugin does **NOT** automatically detect the radius regions for you. This is how you can determine those:
- Spawn point
  - Observe the NPC respawn 1 or more times. The [NPC Indicators](https://github.com/runelite/runelite/wiki/NPC-Indicators) plugin can be helpful for this.  
  ![animation_spawn_point](https://user-images.githubusercontent.com/53493631/134697466-45f9882f-92f2-4ed1-913c-58f722c088d9.gif)
- Wander range
  - Observe the NPC pathfind freely around without being blocked. Alternatively bring the NPC to the max range line, step outside the max range line and observe where the NPC's first successful path back to the wander range ends.  
  ![animation_wander_range](https://user-images.githubusercontent.com/53493631/134697674-613b397b-7587-48aa-a4d0-8770f1b52a6b.gif)
- Retreat range
  - Also known as: *fleeing range*.
  - Attack the NPC from outside its max range and observe where it is pathing to.  
  ![animation_retreat_range](https://user-images.githubusercontent.com/53493631/134697790-86aaebe2-1209-49ff-97be-ac44d90c2294.gif)
- Max range
  - Also known as: *aggression range*.
  - Let the NPC attack you and bring it step-by-step away from its spawn point until it stops attacking you.  
  ![animation_max_range](https://user-images.githubusercontent.com/53493631/134697819-1e7b5be2-76a1-4265-ae32-a0e4489abf6c.gif)

## To-do
- Hunt range
- Attack range

## Panel
![Example of radius marker in the plugin panel](https://user-images.githubusercontent.com/53493631/135182247-5cd2dbe6-25f6-4e11-ba64-1896dbc373e7.png)

## Config options
- Default radiuses
  - Wander radius: `5`
  - Retreat radius: `7`
  - Max radius: `8`
- Default colours
  - Spawn point: ![#FF00FFFF](https://via.placeholder.com/15/00FFFF/000000?text=+) `#FF00FFFF`
  - Wander range: ![#FFFFFF00](https://via.placeholder.com/15/FFFF00/000000?text=+) `#FFFFFF00`
  - Retreat range: ![#FFFF00FF](https://via.placeholder.com/15/FF00FF/000000?text=+) `#FFFF00FF`
  - Max range: ![#FFFF0000](https://via.placeholder.com/15/FF0000/000000?text=+) `#FFFF0000`
- Border width: `3`
- Show on minimap: ✅ `true`
- Show on world map: ✅ `true`
