# misc: minecraft:carrot_on_a_stick
execute if entity @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:carrot_on_a_stick"}},distance=..128,limit=1] run return run teleport @s @e[type=#mss:item_frames,nbt={Item:{id:"minecraft:carrot_on_a_stick"}},distance=..128,limit=1,sort=random]
return run teleport @s @p[distance=0..]
