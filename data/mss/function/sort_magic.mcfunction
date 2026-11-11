# magic: minecraft:glass_bottle
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:glass_bottle"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:glass_bottle"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
