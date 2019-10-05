package me.axieum.mcmod.oneswing.api;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CachedBreakSpeed extends PlayerEvent.BreakSpeed
{
    private final ItemStack heldItem;

    public CachedBreakSpeed(BreakSpeed event)
    {
        super(event.getPlayer(), event.getState(), event.getOriginalSpeed(), event.getPos());
        heldItem = event.getPlayer().getHeldItemMainhand();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof CachedBreakSpeed)) return false;

        CachedBreakSpeed other = (CachedBreakSpeed) obj;

        return this.getPlayer().getGameProfile().getName().equals(other.getPlayer().getGameProfile().getName()) &&
               this.heldItem.getItem().equals(other.heldItem.getItem()) &&
               this.getPos().equals(other.getPos()) &&
               this.getState().getBlock().equals(other.getState().getBlock()) &&
               this.getOriginalSpeed() == other.getOriginalSpeed();
    }

    @Override
    public int hashCode()
    {
        //noinspection UnstableApiUsage
        return Hashing.md5()
                      .newHasher()
                      .putString(getPlayer().getGameProfile().getName(), Charsets.UTF_8)
                      .putString(heldItem.getItem().getName().getUnformattedComponentText(), Charsets.UTF_8)
                      .putLong(getPos().toLong())
                      .putString(getState().getBlock().toString(), Charsets.UTF_8)
                      .putFloat(getOriginalSpeed())
                      .hash()
                      .hashCode();
    }
}
