# sandstone: minecraft:sandstone
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:sandstone"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:sandstone"}},distance=..128,limit=1,sort=random]
return run function mss:sort_sand
