package me.axieum.mcmod.oneswing.event;

import me.axieum.mcmod.oneswing.Config;
import me.axieum.mcmod.oneswing.api.CachedBreakSpeed;
import me.axieum.mcmod.oneswing.util.TreeUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class EventChop
{
    private static ConcurrentHashMap<CachedBreakSpeed, Float> cached_speeds = new ConcurrentHashMap<>();

    /**
     * Determine the breaking speed for the current log being chopped by an
     * axe.
     *
     * @param event break speed event
     */
    @SubscribeEvent
    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event)
    {
        if (!isLogChoppedByAxeAndStanding(event.getState(), event.getPlayer()))
            return;

        // Set new break speed determined by tree height
        // NB: We only compute this if not already
        final float speed = cached_speeds.computeIfAbsent(new CachedBreakSpeed(event), k -> {
            final int height = TreeUtils.getHeight(event.getPlayer().world, k.getPos());
            if (height < 1) return event.getOriginalSpeed(); // Not a tree
            final double modifier = Config.AXE_SPEED_MODIFIER.get();
            return (float) (event.getOriginalSpeed() / (height * modifier));
        });

        event.setNewSpeed(speed);
    }

    /**
     * Attempt to cascade break event to all tree logs.
     *
     * @param event block break event
     */
    @SubscribeEvent
    public static void onPlayerBreakLog(BlockEvent.BreakEvent event)
    {
        if (!isLogChoppedByAxeAndStanding(event.getState(), event.getPlayer()))
            return;

        IWorld world = event.getWorld();
        BlockPos pos = event.getPos();

        // Clear the cached tree break speed now that we're done with it
        for (CachedBreakSpeed cbs : cached_speeds.keySet())
            if (cbs.getPos().equals(pos))
                cached_speeds.remove(cbs);

        // Cascade breaking to all connected logs given it is a tree
        // NB: Spreading across ticks allows us to implement a delay between
        // breaking blocks
        final List<BlockPos> blocks = TreeUtils.getLogs(world, pos, Config.SIZE_THRESHOLD.get());
        if (blocks != null)
            MinecraftForge.EVENT_BUS.register(new Object()
            {
                final long delay = Config.DELAY.get();
                long elapsed = 0;
                int index = 0;

                @SubscribeEvent
                public void onTick(TickEvent.WorldTickEvent e)
                {
                    if (elapsed++ < delay) return;
                    elapsed = 0;

                    if (index < blocks.size())
                        world.destroyBlock(blocks.get(index++), true);
                    else
                        MinecraftForge.EVENT_BUS.unregister(this);
                }
            });
    }

    /**
     * Check whether the block is being chopped with an axe by a non-sneaking
     * player.
     *
     * @param state  block state
     * @param player player responsible
     * @return true if the block is a log and the item is of type axe
     */
    private static boolean isLogChoppedByAxeAndStanding(BlockState state, PlayerEntity player)
    {
        // Is it a log being chopped by an axe?
        return state.isIn(BlockTags.LOGS) &&
               !player.isSneaking() &&
               player.getHeldItemMainhand()
                     .getToolTypes()
                     .contains(ToolType.AXE);
    }
}
