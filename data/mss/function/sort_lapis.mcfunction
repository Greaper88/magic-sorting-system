# lapis: minecraft:lapis_lazuli
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:lapis_lazuli"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:lapis_lazuli"}},distance=..128,limit=1,sort=random]
return run function mss:sort_precious
