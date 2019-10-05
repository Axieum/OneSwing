package me.axieum.mcmod.oneswing.util;

import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TreeUtils
{
    /**
     * Calculate the tree height for a given log belonging to a tree.
     *
     * @param world world of the tree
     * @param pos   block belonging to the tree
     * @return tree height or 0 if not a tree
     */
    public static int getHeight(IWorld world, BlockPos pos)
    {
        int height = 0, leaves = 0;

        // Keep moving up the logs, and count adjacent leaves
        while (world.getBlockState(pos).isIn(BlockTags.LOGS)) {
            height++;
            leaves += getLeafCountForLog(world, pos);
            pos = pos.up();
        }

        // If there's enough leaves (leaves touch sides of a log) it's a tree
        return leaves >= height / 6f ? height : 0;
    }

    /**
     * Retrieve a list of block positions that belong to a given tree.
     *
     * @param world     world of the tree
     * @param pos       block belonging to the tree
     * @param threshold maximum tree size before aborting
     * @return list of block positions or null if too many or not a tree
     */
    public static List<BlockPos> getLogs(IWorld world, BlockPos pos, int threshold)
    {
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> logs = new LinkedList<>(),
                leaves = new LinkedList<>();

        // Initialise the queue with the current position
        queue.add(pos);

        // Iterate adjacent blocks and curate a list of logs and leaves
        while (!queue.isEmpty()) {
            // Have we processed too many adjacent blocks?
            if (logs.size() > threshold)
                return null;

            BlockPos cur = queue.remove();

            // Have we seen this block before?
            if (leaves.contains(cur) || logs.contains(cur))
                continue;

            // Is this a leaf, log, or have we seen this before?
            BlockState state = world.getBlockState(cur);
            final boolean isLog = state.isIn(BlockTags.LOGS),
                    isLeaf = state.isIn(BlockTags.LEAVES);

            if (!(isLog || isLeaf))
                continue;

            // Is it a leaf? Don't check its neighbours
            if (isLeaf) {
                leaves.add(cur);
                continue;
            }

            // It's a log, remember and search its neighbours
            logs.add(cur);

            // Add adjacent blocks to queue
            for (int x = -1; x <= 1; x++)
                for (int y = 0; y <= 1; y++) // only above
                    for (int z = -1; z <= 1; z++)
                        if (!queue.contains(cur.add(x, y, z)))
                            queue.add(cur.add(x, y, z));
        }

        // Is this a tree? Leaves count are leaves attached to six faces of a log
        return leaves.size() >= logs.size() / 6f ? logs : null;
    }

    /**
     * Retrieve the number of leaves adjacent to a log.
     *
     * @param world world the log is in
     * @param pos   block position of the log
     * @return number of leaves adjacent to given log
     */
    public static int getLeafCountForLog(IWorld world, BlockPos pos)
    {
        int count = 0;

        BlockPos[] adjacents = new BlockPos[]{
                pos.up(),
                pos.down(),
                pos.north(),
                pos.east(),
                pos.south(),
                pos.west()
        };

        for (BlockPos adjacent : adjacents)
            if (world.getBlockState(adjacent).isIn(BlockTags.LEAVES))
                count++;

        return count;
    }
}
