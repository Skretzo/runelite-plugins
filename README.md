# Radius Markers
![Icon](icon.png)

## Info
![Image of example usage of radius markers in-game](https://user-images.githubusercontent.com/53493631/133250776-cdd7ec14-da0f-45e6-b564-a670bc7b96b4.png)  
*Radius marker for a lonely chicken north-east of Lumbridge east farm*

A radius marker is a way to display the spawn point, wander range, retreat range and/or aggro range of an NPC in the game scene. You place down a marker by clicking the ![plus icon](../radius-markers/src/main/resources/com/radiusmarkers/add_icon.png) icon in the plugin panel while being logged in-game. This will place the marker at the feet of your character, but the location can easily be adjusted in the panel later. This plugin does **NOT** automatically detect the radius regions for you. This is how you can determine those:
- Spawn point
  - Observe the NPC respawn 1 or more times. The [NPC Indicators](https://github.com/runelite/runelite/wiki/NPC-Indicators) plugin can be helpful for this.  
  ![animation_spawn_point](https://user-images.githubusercontent.com/53493631/134697466-45f9882f-92f2-4ed1-913c-58f722c088d9.gif)
- Wander range
  - Observe the NPC pathfind freely around without being blocked. Alternatively bring the NPC to the aggro line, step outside the aggro line and observe where the NPC's first successful path back to the wander range ends.  
  ![animation_wander_range](https://user-images.githubusercontent.com/53493631/134697674-613b397b-7587-48aa-a4d0-8770f1b52a6b.gif)
- Retreat range
  - Attack the NPC from outside its aggro range and observe where it is pathing to.  
  ![animation_retreat_range](https://user-images.githubusercontent.com/53493631/134697790-86aaebe2-1209-49ff-97be-ac44d90c2294.gif)
- Aggro range
  - Let the NPC attack you and bring it step-by-step away from its spawn point until it stops attacking you.  
  ![animation_aggro_range](https://user-images.githubusercontent.com/53493631/134697819-1e7b5be2-76a1-4265-ae32-a0e4489abf6c.gif)

## To-do
- Hunt range
- Attack range

## Config options
- Default radiuses
  - Wander radius: 5
  - Retreat radius: 7
  - Aggro radius: 8
- Default colours
  - Spawn point: ![#FF00FFFF](https://via.placeholder.com/15/00FFFF/000000?text=+) `#FF00FFFF`
  - Wander range: ![#FFFFFF00](https://via.placeholder.com/15/FFFF00/000000?text=+) `#FFFFFF00`
  - Retreat range: ![#FFFF00FF](https://via.placeholder.com/15/FF00FF/000000?text=+) `#FFFF00FF`
  - Aggro range: ![#FFFF0000](https://via.placeholder.com/15/FF0000/000000?text=+) `#FFFF0000`
- Border width: 3
