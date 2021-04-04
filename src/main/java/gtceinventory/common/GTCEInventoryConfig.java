package gtceinventory.common;

import gtceinventory.GTCEInventory;
import net.minecraftforge.common.config.Config;

@Config(modid = GTCEInventory.MODID)
public class GTCEInventoryConfig {

    @Config.Comment("The energy consumed per network per second. Default: 1")
    public static int energyPerNetwork = 1;

    @Config.Comment("The energy consumed per pipe per second. Default: 0")
    public static int energyPerPipe = 0;

    @Config.Comment("The energy consumed per operation. Default: 0")
    public static int energyPerOperation = 0;

    @Config.Comment("The energy consumed per item. Default: 1")
    public static int energyPerItem = 1;
}
