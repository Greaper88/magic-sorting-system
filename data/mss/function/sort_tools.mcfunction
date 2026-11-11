# tools: minecraft:wooden_pickaxe
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:wooden_pickaxe"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:wooden_pickaxe"}},distance=..128,limit=1,sort=random]
return run function mss:sort_misc
