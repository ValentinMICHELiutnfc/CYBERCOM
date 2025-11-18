package com.cybercom;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class CyberItem {
    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        // Create the item key.
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CYBERCOM.MOD_ID, name));

        // Create the item instance.
        Item item = itemFactory.apply(settings.registryKey(itemKey));

        // Register the item.
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }

    public static final Item CYBER_BOOK = register(
            "cyber_book",
            settings -> new net.minecraft.item.WritableBookItem(settings
                    .maxCount(1) // Only one book per stack
                    .fireproof() // Example: make it fireproof
            ),
            new Item.Settings()
    );

    public static final RegistryKey<net.minecraft.item.ItemGroup> CYBERCOM_ITEM_GROUP_KEY =
           RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(CYBERCOM.MOD_ID, "item_group"));
    public static final net.minecraft.item.ItemGroup CYBERCOM_ITEM_GROUP = net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
           .icon(() -> new net.minecraft.item.ItemStack(CYBER_BOOK))
           .displayName(net.minecraft.text.Text.translatable("itemGroup.cybercom"))
           .build();


    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, CYBERCOM_ITEM_GROUP_KEY, CYBERCOM_ITEM_GROUP);
        ItemGroupEvents.modifyEntriesEvent(CYBERCOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(CyberItem.CYBER_BOOK);
        });
    }
}
