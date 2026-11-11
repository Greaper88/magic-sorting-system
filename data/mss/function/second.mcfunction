# Loose items on lapis, with gold two blocks below the item.
execute as @e[type=minecraft:item] at @s if block ~ ~-1 ~ minecraft:lapis_block if block ~ ~-2 ~ minecraft:gold_block run function mss:sort
