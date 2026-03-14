package com.likeazusa2.universaldraconicslots.data;

import com.brandon3055.brandonscore.api.TechLevel;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum UDSTier implements StringRepresentable {
    WYVERN(TechLevel.WYVERN, 1_000L, 1_000L),
    DRACONIC(TechLevel.DRACONIC, 4_000L, 4_000L),
    CHAOTIC(TechLevel.CHAOTIC, 16_000L, 16_000L);

    public static final Codec<UDSTier> CODEC = StringRepresentable.fromEnum(UDSTier::values);
    public static final StreamCodec<ByteBuf, UDSTier> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> ByteBufCodecs.STRING_UTF8.encode(buf, value.getSerializedName()),
            buf -> byName(ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    private final TechLevel techLevel;
    private final long baseCapacity;
    private final long baseTransfer;

    UDSTier(TechLevel techLevel, long baseCapacity, long baseTransfer) {
        this.techLevel = techLevel;
        this.baseCapacity = baseCapacity;
        this.baseTransfer = baseTransfer;
    }

    public TechLevel techLevel() {
        return techLevel;
    }

    public long baseCapacity() {
        return baseCapacity;
    }

    public long baseTransfer() {
        return baseTransfer;
    }

    public boolean isAtLeast(UDSTier other) {
        return ordinal() >= other.ordinal();
    }

    public static UDSTier byName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (UDSTier value : values()) {
            if (value.getSerializedName().equals(normalized)) {
                return value;
            }
        }
        return WYVERN;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
