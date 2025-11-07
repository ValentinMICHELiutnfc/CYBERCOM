package com.cybercom;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModDataComponents {
    private static final Codec<long[]> LONG_PAIR_CODEC = Codec.LONG.listOf().xmap(
            list -> new long[]{list.get(0), list.get(1)}, // decode
            arr -> List.of(arr[0], arr[1])                // encode
    );


    public static final ComponentType<long[]> PUBLIC_KEY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("mymod", "public_key"),
            ComponentType.<long[]>builder()
                    .codec(LONG_PAIR_CODEC)
                    .build()
    );

    public static final ComponentType<long[]> PRIVATE_KEY = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of("mymod", "private_key"),
            ComponentType.<long[]>builder()
                    .codec(LONG_PAIR_CODEC)
                    .build()
    );

    public static void register(){
    }
}
