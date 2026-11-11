# concrete: minecraft:gray_concrete_powder
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:gray_concrete_powder"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:gray_concrete_powder"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
