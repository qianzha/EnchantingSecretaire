package top.ma6jia.qianzha.enchantnote.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class ENoteCapability {
    @CapabilityInject(IEnchantKeeper.class)
    public static Capability<IEnchantKeeper> ENCHANT_KEEPER_CAPABILITY;
}
