package top.ma6jia.qianzha.enchantnote.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.*

object ENoteCommonConfig {
    @JvmStatic
    val COMMON_CONFIG: ForgeConfigSpec

    @JvmStatic
    val ENCHANTED_DISABLED: BooleanValue // 附魔已附魔物品
    @JvmStatic
    val ENCHANT_MULTIPLIER: DoubleValue // 附魔消耗经验乘数，参考铁砧机制合并物品或书的稀有度消耗

    @JvmStatic
    val ENCHANT_COST_LIMIT: IntValue // 附魔消耗等级上限（超过则生存模式无法附魔）
    @JvmStatic
    val PRIOR_WORK_PENALTY: BooleanValue // 启用铁砧机制的累积惩罚
    @JvmStatic
    val PENALTY_START_NUM: IntValue // 物品附魔数量大于等于该值，则开始增加累积惩罚

    init {
        val builder = Builder()
        builder.push("general")

        ENCHANTED_DISABLED = builder.comment("Is disable enchanting enchanted item?")
            .define("enchant_enchanted", false)

        ENCHANT_MULTIPLIER = builder.comment(
            "Enchantment cost: `level * max(1, rarityBaseCost * theMultiplier)`\n" +
                    "As combining enchantments at an anvil, this multiplier is 1 from item, and 0.5 from book"
        )
            .defineInRange("enchant_multiplier", 0.5, 0.0, Double.MAX_VALUE)

        ENCHANT_COST_LIMIT =
            builder.comment("The cost of enchanting must be smaller than this value in survival. [MAX_VALUE: 2147483647]")
                .defineInRange("enchant_cost_limit", 40, 0, Int.MAX_VALUE)

        PRIOR_WORK_PENALTY = builder.comment("Is enable getting prior work penalty as anvil do?")
            .define("prior_work_penalty", true)

        PENALTY_START_NUM = builder
            .comment("Getting PRIOR_WORK_PENALTY if it is equal or greater than this value that the number of enchantments at the item.")
            .defineInRange("penalty_start_num", 1, 0, Int.MAX_VALUE)

        builder.pop()
        COMMON_CONFIG = builder.build()
    }
}