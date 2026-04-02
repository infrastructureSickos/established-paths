package com.infrastructuresickos.established_paths;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Handles foot-traffic block degradation.
 * Registered manually on the FORGE bus — do NOT add @Mod.EventBusSubscriber.
 *
 * Each server player tick: if the player moved since last tick, roll for block
 * degradation based on sprint/walk speed. Sneaking never triggers wear.
 * The lastPos map is cleaned on player logout and level unload to prevent leaks.
 */
public class PathEvents {

    // Ordered degradation chains. Each entry is [from, to].
    private static final Block[][] TRANSITIONS = {
        { Blocks.GRASS_BLOCK,       Blocks.DIRT           },
        { Blocks.DIRT,              Blocks.DIRT_PATH      },
        { Blocks.STONE,             Blocks.COBBLESTONE    },
        { Blocks.COBBLESTONE,       Blocks.GRAVEL         },
        { Blocks.COARSE_DIRT,       Blocks.GRAVEL         },
        { Blocks.STONE_BRICKS,      Blocks.CRACKED_STONE_BRICKS },
        { Blocks.MYCELIUM,          Blocks.DIRT           },
        { Blocks.ICE,               Blocks.PACKED_ICE     },
        { Blocks.PACKED_ICE,        Blocks.BLUE_ICE       },
        { Blocks.TUFF,              Blocks.GRAVEL         },
        { Blocks.MAGMA_BLOCK,       Blocks.COBBLED_DEEPSLATE },
    };

    private static final Map<Block, Block> TRANSITION_MAP = new HashMap<>();

    static {
        for (Block[] pair : TRANSITIONS) {
            TRANSITION_MAP.put(pair[0], pair[1]);
        }
    }

    private static final Random RANDOM = new Random();

    // Tracks the last known position of each player (by UUID) to detect movement.
    // Cleaned on logout and level unload — no leak.
    private final Map<UUID, BlockPos> lastPos = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        Level level = player.level();
        if (level.isClientSide()) return;
        if (player.isSleeping()) return;

        UUID id = player.getUUID();
        BlockPos current = player.blockPosition();
        BlockPos prev = lastPos.put(id, current);

        // No previous position recorded yet, or player hasn't moved
        if (prev == null || prev.equals(current)) return;
        // Sneaking never causes wear
        if (player.isCrouching()) return;

        double chance = player.isSprinting()
                ? EPConfig.INSTANCE.sprintChance.get()
                : EPConfig.INSTANCE.walkChance.get();

        if (RANDOM.nextDouble() >= chance) return;

        // Block directly below the player's feet
        BlockPos below = current.below();
        Block block = level.getBlockState(below).getBlock();
        Block result = TRANSITION_MAP.get(block);
        if (result == null) return;

        level.setBlockAndUpdate(below, result.defaultBlockState());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Fix: remove player entry to prevent the map growing forever
        lastPos.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        // Clear all entries when a level unloads (e.g. server stop / dimension unload)
        lastPos.clear();
    }
}
