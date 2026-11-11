# Unrestricted @e selection finds loaded items across dimensions.
scoreboard players add #mss_tick mss_tick 1
execute if score #mss_tick mss_tick matches 1 run function mss:second
execute if score #mss_tick mss_tick matches 2 if score #mss_cooldown mss_cooldown matches 1 run function mss:cleanup
execute if score #mss_tick mss_tick matches 20.. run scoreboard players set #mss_tick mss_tick 0
