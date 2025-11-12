package com.cybercom;

import java.util.Calendar;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;


public class RSAMessage {

    public static void initkeys(PlayerEntity player){
        // Increase the search range so n = p*q is comfortably > 255
        // (not cryptographically secure, but avoids "message too long" for ASCII chars)
        long[] choixCle = RSA.choixCle(100,900);
        if(choixCle == null) throw new RuntimeException("Couldn't get the keys");
        long p = choixCle[0]; long q = choixCle[1]; long e = choixCle[2];
        long[] public_key = RSA.clePublique(p,q,e);
        if(public_key == null) throw new RuntimeException("Couldn't generate the public_keys");
        long[] private_key = RSA.clePrivee(p,q,e);
        if(private_key == null) throw new RuntimeException("Couldn't generate the private_keys");
        player.setComponent(ModDataComponents.PUBLIC_KEY,public_key);
        player.setComponent(ModDataComponents.PRIVATE_KEY,private_key);
    }

    public static long encodeMessage(PlayerEntity player, String message){
        long messageASCII = 0;
        long messageASCII_encoded = 0;
        long[] public_key = player.get(ModDataComponents.PUBLIC_KEY);
        for(int i = 0 ; i < message.length() ; i++){
            messageASCII += (long)((int) message.charAt(i));
        }
        if(public_key == null) throw new RuntimeException("Public key null");
        return RSA.codageRSA(messageASCII,public_key[0],public_key[1]);
    }


    public static String encodeMessageWithKey(long[] publicKey, String message) {
        if(publicKey == null) throw new RuntimeException("Public key null");
        StringBuilder encoded = new StringBuilder();

        long n = publicKey[0];
        if (n <= 0) throw new RuntimeException("Invalid public key modulus");
        int width = String.valueOf(n - 1).length(); // fixed width for each encrypted block

        for(int i = 0; i < message.length(); i++) {
            int charCode = (int) message.charAt(i);
            long encryptedChar = RSA.codageRSA(charCode, publicKey[0], publicKey[1]);

            // zero-pad to fixed width and append (no separators)
            String block = String.format("%0" + width + "d", encryptedChar);
            encoded.append(block);
        }

        return encoded.toString();
    }

    public static String decodeMessageWithKey(long[] privateKey, String encryptedMessage) {
        if(privateKey == null) throw new RuntimeException("Private key null");

        long n = privateKey[0];
        if (n <= 0) throw new RuntimeException("Invalid private key modulus");
        int width = String.valueOf(n - 1).length();

        String trimmed = encryptedMessage.trim();
        if (trimmed.length() % width != 0) {
            throw new RuntimeException("Encrypted message length is not a multiple of block width");
        }

        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < trimmed.length(); i += width) {
            String block = trimmed.substring(i, i + width);
            if (block.isEmpty()) continue;
            try {
                long encryptedValue = Long.parseLong(block);
                long decryptedValue = RSA.decodageRSA(encryptedValue, privateKey[0], privateKey[1]);
                decoded.append((char) decryptedValue);
            } catch(NumberFormatException e) {
                throw new RuntimeException("Invalid encrypted block: " + block);
            }
        }

        return decoded.toString();
    }
}
