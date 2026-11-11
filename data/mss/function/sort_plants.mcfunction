# plants: minecraft:oak_sapling
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:oak_sapling"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:oak_sapling"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
