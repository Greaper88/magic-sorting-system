# Magic Sorting System -- Minecraft Java Edition 26.3
# Executes as the dropped item, positioned at its controller.
# Return after dispatch so one item can only take one sorting path.
execute unless score #mss_cooldown mss_cooldown matches 1 run playsound minecraft:entity.illusioner.mirror_move block @a[distance=..5] ~ ~ ~ 1.0 1.0
execute unless score #mss_cooldown mss_cooldown matches 1 run particle minecraft:portal ~ ~ ~ 1 1 1 1 100
scoreboard players set #mss_cooldown mss_cooldown 1

execute if items entity @s contents #mss:group/dirt run return run function mss:sort_dirt
execute if items entity @s contents #mss:group/mud run return run function mss:sort_mud
execute if items entity @s contents #mss:group/mudwork run return run function mss:sort_mudwork
execute if items entity @s contents #mss:group/gravel run return run function mss:sort_gravel
execute if items entity @s contents #mss:group/sand run return run function mss:sort_sand
execute if items entity @s contents #mss:group/sandstone run return run function mss:sort_sandstone
execute if items entity @s contents #mss:group/wood run return run function mss:sort_wood
execute if items entity @s contents #mss:group/woodwork run return run function mss:sort_woodwork
execute if items entity @s contents #mss:group/office run return run function mss:sort_office
execute if items entity @s contents #mss:group/stone run return run function mss:sort_stone
execute if items entity @s contents #mss:group/stonework run return run function mss:sort_stonework
execute if items entity @s contents #mss:group/coal run return run function mss:sort_coal
execute if items entity @s contents #mss:group/tools run return run function mss:sort_tools
execute if items entity @s contents #mss:group/storage run return run function mss:sort_storage
execute if items entity @s contents #mss:group/armor run return run function mss:sort_armor
execute if items entity @s contents #mss:group/rails run return run function mss:sort_rails
execute if items entity @s contents #mss:group/plants run return run function mss:sort_plants
execute if items entity @s contents #mss:group/flowers run return run function mss:sort_flowers
execute if items entity @s contents #mss:group/food run return run function mss:sort_food
execute if items entity @s contents #mss:group/raw run return run function mss:sort_raw
execute if items entity @s contents #mss:group/mobs run return run function mss:sort_mobs
execute if items entity @s contents #mss:group/precious run return run function mss:sort_precious
execute if items entity @s contents #mss:group/lapis run return run function mss:sort_lapis
execute if items entity @s contents #mss:group/magic run return run function mss:sort_magic
execute if items entity @s contents #mss:group/potions run return run function mss:sort_potions
execute if items entity @s contents #mss:group/books run return run function mss:sort_books
execute if items entity @s contents #mss:group/wool run return run function mss:sort_wool
execute if items entity @s contents #mss:group/concrete run return run function mss:sort_concrete
execute if items entity @s contents #mss:group/terracotta run return run function mss:sort_terracotta
execute if items entity @s contents #mss:group/glass run return run function mss:sort_glass
execute if items entity @s contents #mss:group/ice run return run function mss:sort_ice
execute if items entity @s contents #mss:group/ocean run return run function mss:sort_ocean
execute if items entity @s contents #mss:group/coral run return run function mss:sort_coral
execute if items entity @s contents #mss:group/prismarine run return run function mss:sort_prismarine
execute if items entity @s contents #mss:group/ink run return run function mss:sort_ink
execute if items entity @s contents #mss:group/nether run return run function mss:sort_nether
execute if items entity @s contents #mss:group/smelt run return run function mss:sort_smelt
execute if items entity @s contents #mss:group/ores run return run function mss:sort_ores
execute if items entity @s contents #mss:group/copper run return run function mss:sort_copper
execute if items entity @s contents #mss:group/redstone run return run function mss:sort_redstone
execute if items entity @s contents #mss:group/end run return run function mss:sort_end
execute if items entity @s contents #mss:group/music run return run function mss:sort_music
return run function mss:sort_misc
