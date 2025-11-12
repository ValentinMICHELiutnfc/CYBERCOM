package com.cybercom;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModDataComponents {
    private static final Codec<long[]> LONG_PAIR_CODEC = Codec.LONG.listOf().xmap(
            list -> new long[]{list.get(0), list.get(1)}, // decode
            arr -> List.of(arr[0], arr[1])                // encode
    );

    // Use AttachmentType for entity data (players), not ComponentType (which is for items)
    public static final AttachmentType<long[]> PUBLIC_KEY = AttachmentRegistry.<long[]>builder()
            .persistent(LONG_PAIR_CODEC)
            .buildAndRegister(Identifier.of(CYBERCOM.MOD_ID, "public_key"));

    public static final AttachmentType<long[]> PRIVATE_KEY = AttachmentRegistry.<long[]>builder()
            .persistent(LONG_PAIR_CODEC)
            .buildAndRegister(Identifier.of(CYBERCOM.MOD_ID, "private_key"));

    public static void register(){
        CYBERCOM.LOGGER.info("Registered attachment types: PUBLIC_KEY={}, PRIVATE_KEY={}", PUBLIC_KEY, PRIVATE_KEY);
    }
}
