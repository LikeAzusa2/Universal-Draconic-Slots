package com.likeazusa2.universaldraconicslots.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UDSHostUpgradeData(
        boolean enabled,
        UDSTier techTier,
        int gridWidth,
        int gridHeight,
        boolean opEnabled,
        UDSTier opTier
) {
    // 持久化到数据组件时使用的结构；字段顺序需要和 record 构造参数保持一致。
    public static final Codec<UDSHostUpgradeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(UDSHostUpgradeData::enabled),
            UDSTier.CODEC.fieldOf("tech_tier").forGetter(UDSHostUpgradeData::techTier),
            Codec.INT.fieldOf("grid_width").forGetter(UDSHostUpgradeData::gridWidth),
            Codec.INT.fieldOf("grid_height").forGetter(UDSHostUpgradeData::gridHeight),
            Codec.BOOL.fieldOf("op_enabled").forGetter(UDSHostUpgradeData::opEnabled),
            UDSTier.CODEC.fieldOf("op_tier").forGetter(UDSHostUpgradeData::opTier)
    ).apply(instance, UDSHostUpgradeData::new));

    // 客户端界面和同步逻辑复用同一份字段顺序，避免出现本地看到的升级状态与服务端不一致。
    public static final StreamCodec<ByteBuf, UDSHostUpgradeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UDSHostUpgradeData::enabled,
            UDSTier.STREAM_CODEC, UDSHostUpgradeData::techTier,
            ByteBufCodecs.VAR_INT, UDSHostUpgradeData::gridWidth,
            ByteBufCodecs.VAR_INT, UDSHostUpgradeData::gridHeight,
            ByteBufCodecs.BOOL, UDSHostUpgradeData::opEnabled,
            UDSTier.STREAM_CODEC, UDSHostUpgradeData::opTier,
            UDSHostUpgradeData::new
    );

    public static final UDSHostUpgradeData DISABLED = new UDSHostUpgradeData(false, UDSTier.WYVERN, 0, 0, false, UDSTier.WYVERN);

    public UDSHostUpgradeData {
        // 统一在数据入口钳制网格尺寸，避免旧存档或错误同步把非法尺寸带进宿主解析流程。
        gridWidth = Math.max(0, Math.min(16, gridWidth));
        gridHeight = Math.max(0, Math.min(16, gridHeight));
    }

    public boolean hasHost() {
        return enabled && gridWidth > 0 && gridHeight > 0;
    }
}
