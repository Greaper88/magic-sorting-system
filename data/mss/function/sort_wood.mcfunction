# wood: minecraft:oak_log
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:oak_log"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:oak_log"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
