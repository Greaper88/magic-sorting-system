# mudwork: minecraft:mud_bricks
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:mud_bricks"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:mud_bricks"}},distance=..128,limit=1,sort=random]
return run function mss:sort_mud
