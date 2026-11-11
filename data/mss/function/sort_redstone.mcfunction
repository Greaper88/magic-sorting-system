# redstone: minecraft:redstone
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:redstone"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:redstone"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
