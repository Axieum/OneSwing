package me.axieum.mcmod.oneswing;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class Config
{
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;

    private static final String CATEGORY_AXES = "axes";
    public static ForgeConfigSpec.ConfigValue<List<String>> AXE_WHITELIST, AXE_BLACKLIST;

    private static final String CATEGORY_TREES = "trees";
    public static ForgeConfigSpec.LongValue DELAY;
    public static ForgeConfigSpec.IntValue SIZE_THRESHOLD;
    public static ForgeConfigSpec.ConfigValue<List<String>> BLOCK_WHITELIST, BLOCK_BLACKLIST;

    // Define configuration schema
    static {
        // AXES
        COMMON_BUILDER.comment("Axe configuration").push(CATEGORY_AXES);

        AXE_WHITELIST = COMMON_BUILDER.comment("Axes that are allowed to one swing at trees (empty = all allowed)")
                                      .define("whitelist", new ArrayList<>());

        AXE_BLACKLIST = COMMON_BUILDER.comment("Axes that are disallowed from swinging at trees")
                                      .define("blacklist", new ArrayList<>());

        COMMON_BUILDER.pop();

        // TREES
        COMMON_BUILDER.comment("Tree configuration").push(CATEGORY_TREES);

        DELAY = COMMON_BUILDER.comment("Delay between breaking logs (20 ticks = 1 second)")
                              .defineInRange("delay", 5, 1, Long.MAX_VALUE);

        SIZE_THRESHOLD = COMMON_BUILDER.comment("Maximum tree size threshold (log count)")
                                       .defineInRange("threshold", 256, 2, Integer.MAX_VALUE);

        BLOCK_WHITELIST = COMMON_BUILDER.comment(
                "Blocks that are allowed to be chopped in one swing (empty = all allowed)")
                                        .define("whitelist", new ArrayList<>());

        BLOCK_BLACKLIST = COMMON_BUILDER.comment("Blocks that are disallowed from being chopped in one swing")
                                        .define("blacklist", new ArrayList<>());

        COMMON_BUILDER.pop();

        // Publish config
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    /**
     * Load the configuration from file.
     *
     * @param spec configuration instance
     * @param path file to be used for loading and saving
     */
    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                                                                  .sync()
                                                                  .autosave()
                                                                  .writingMode(WritingMode.REPLACE)
                                                                  .build();

        configData.load();
        spec.setConfig(configData);
    }
}
