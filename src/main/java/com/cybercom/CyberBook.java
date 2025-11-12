package com.cybercom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class CyberBook {

    /**
     * Encode a written book's content using a player's public key.
     * Returns a new ItemStack with the encoded book.
     */
    public static ItemStack encodeBook(ItemStack bookStack, PlayerEntity player) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK && bookStack.getItem() != Items.WRITABLE_BOOK) {
            throw new IllegalArgumentException("Item must be a book");
        }

        long[] publicKey = player.get(ModDataComponents.PUBLIC_KEY);
        if (publicKey == null) {
            throw new RuntimeException("Player has no public key");
        }

        return encodeBookWithKey(bookStack, publicKey);
    }

    /**
     * Encode a written book's content using a specific public key.
     */
    public static ItemStack encodeBookWithKey(ItemStack bookStack, long[] publicKey) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK && bookStack.getItem() != Items.WRITABLE_BOOK) {
            throw new IllegalArgumentException("Item must be a book");
        }

        // Extract book content
        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException("Book has no content");
        }

        // Encode each page
        List<RawFilteredPair<Text>> encodedPages = new ArrayList<>();
        for (RawFilteredPair<Text> page : content.pages()) {
            String pageText = page.raw().getString();
            String encodedText = RSAMessage.encodeMessageWithKey(publicKey, pageText);
            encodedPages.add(RawFilteredPair.of(Text.literal(encodedText)));
        }

        // Create new book with encoded content
        ItemStack encodedBook = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent encodedContent = new WrittenBookContentComponent(
                RawFilteredPair.of("[ENCRYPTED]"),
                content.author(),
                content.generation(),
                encodedPages,
                content.resolved()
        );
        encodedBook.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, encodedContent);

        return encodedBook;
    }

    /**
     * Decode an encrypted book using a player's private key.
     */
    public static ItemStack decodeBook(ItemStack bookStack, PlayerEntity player) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK) {
            throw new IllegalArgumentException("Item must be a written book");
        }

        long[] privateKey = player.get(ModDataComponents.PRIVATE_KEY);
        if (privateKey == null) {
            throw new RuntimeException("Player has no private key");
        }

        return decodeBookWithKey(bookStack, privateKey);
    }

    /**
     * Decode an encrypted book using a specific private key.
     */
    public static ItemStack decodeBookWithKey(ItemStack bookStack, long[] privateKey) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK) {
            throw new IllegalArgumentException("Item must be a written book");
        }

        // Extract book content
        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException("Book has no content");
        }

        // Decode each page
        List<RawFilteredPair<Text>> decodedPages = new ArrayList<>();
        for (RawFilteredPair<Text> page : content.pages()) {
            String encodedText = page.raw().getString();
            String decodedText = RSAMessage.decodeMessageWithKey(privateKey, encodedText);
            decodedPages.add(RawFilteredPair.of(Text.literal(decodedText)));
        }

        // Create new book with decoded content
        ItemStack decodedBook = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent decodedContent = new WrittenBookContentComponent(
                RawFilteredPair.of("[DECRYPTED]"),
                content.author(),
                content.generation(),
                decodedPages,
                content.resolved()
        );
        decodedBook.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, decodedContent);

        return decodedBook;
    }

    /**
     * Get the raw text content from all pages of a book.
     */
    public static String getBookText(ItemStack bookStack) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK && bookStack.getItem() != Items.WRITABLE_BOOK) {
            throw new IllegalArgumentException("Item must be a book");
        }

        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            return "";
        }

        StringBuilder fullText = new StringBuilder();
        for (RawFilteredPair<Text> page : content.pages()) {
            if (fullText.length() > 0) {
                fullText.append("\n");
            }
            fullText.append(page.raw().getString());
        }

        return fullText.toString();
    }
}
