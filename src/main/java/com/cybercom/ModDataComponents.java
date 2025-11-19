package com.cybercom;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;


public class ModDataComponents {
    // Store RSA keys as String to support BigInteger (format: "n,e" or "n,d")
    private static final Codec<String> STRING_CODEC = Codec.STRING;

    // Use AttachmentType for entity data (players), not ComponentType (which is for items)
    public static final AttachmentType<String> PUBLIC_KEY = AttachmentRegistry.<String>builder()
            .persistent(STRING_CODEC)
            .buildAndRegister(Identifier.of(CYBERCOM.MOD_ID, "public_key"));

    public static final AttachmentType<String> PRIVATE_KEY = AttachmentRegistry.<String>builder()
            .persistent(STRING_CODEC)
            .buildAndRegister(Identifier.of(CYBERCOM.MOD_ID, "private_key"));

    public static void register(){
        CYBERCOM.LOGGER.info("Registered attachment types: PUBLIC_KEY={}, PRIVATE_KEY={}", PUBLIC_KEY, PRIVATE_KEY);
    }
}
