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

        String publicKey = player.getAttached(ModDataComponents.PUBLIC_KEY);
        if (publicKey == null) {
            throw new RuntimeException("Player has no public key");
        }

        // Use the player's name as the book title
        String playerName = player.getName().getString();
        return encodeBookWithKey(bookStack, publicKey, playerName);
    }

    /**
     * Encode a written book's content using a specific public key.
     */
    public static ItemStack encodeBookWithKey(ItemStack bookStack, String publicKey) {
        return encodeBookWithKey(bookStack, publicKey, "[ENCRYPTED]");
    }

    /**
     * Encode a written book's content using a specific public key with a custom title.
     */
    public static ItemStack encodeBookWithKey(ItemStack bookStack, String publicKey, String title) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK && bookStack.getItem() != Items.WRITABLE_BOOK) {
            throw new IllegalArgumentException("Item must be a book");
        }

        // Extract book content
        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException("Book has no content");
        }

        // Maximum characters per page in Minecraft (conservative estimate)
        final int MAX_PAGE_LENGTH = 266;

        // Encode each page and split into multiple pages if needed
        List<RawFilteredPair<Text>> encodedPages = new ArrayList<>();
        for (RawFilteredPair<Text> page : content.pages()) {
            String pageText = page.raw().getString();
            String encodedText = RSAMessage.encodeMessageWithKey(publicKey, pageText);

            // Split encoded text into chunks that fit in a page
            int start = 0;
            while (start < encodedText.length()) {
                int end = Math.min(start + MAX_PAGE_LENGTH, encodedText.length());
                String chunk = encodedText.substring(start, end);
                encodedPages.add(RawFilteredPair.of(Text.literal(chunk)));
                start = end;
            }
        }

        // Create new book with encoded content
        ItemStack encodedBook = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent encodedContent = new WrittenBookContentComponent(
                RawFilteredPair.of(title),
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

        String privateKey = player.getAttached(ModDataComponents.PRIVATE_KEY);
        if (privateKey == null) {
            throw new RuntimeException("Player has no private key");
        }

        return decodeBookWithKey(bookStack, privateKey);
    }

    /**
     * Decode an encrypted book using a specific private key.
     */
    public static ItemStack decodeBookWithKey(ItemStack bookStack, String privateKey) {
        if (bookStack.getItem() != Items.WRITTEN_BOOK) {
            throw new IllegalArgumentException("Item must be a written book");
        }

        // Extract book content
        WrittenBookContentComponent content = bookStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
        if (content == null) {
            throw new IllegalArgumentException("Book has no content");
        }

        // Concatenate all pages to reconstruct the full encoded text
        StringBuilder fullEncodedText = new StringBuilder();
        for (RawFilteredPair<Text> page : content.pages()) {
            fullEncodedText.append(page.raw().getString());
        }

        // Decode the full text
        String decodedText = RSAMessage.decodeMessageWithKey(privateKey, fullEncodedText.toString());

        // Split decoded text into pages (max ~200 characters per page for readability)
        List<RawFilteredPair<Text>> decodedPages = new ArrayList<>();
        final int MAX_PAGE_LENGTH = 200;
        int start = 0;
        while (start < decodedText.length()) {
            int end = Math.min(start + MAX_PAGE_LENGTH, decodedText.length());
            String pageChunk = decodedText.substring(start, end);
            decodedPages.add(RawFilteredPair.of(Text.literal(pageChunk)));
            start = end;
        }

        // If no pages, add at least one empty page
        if (decodedPages.isEmpty()) {
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
