package com.cybercom;

import java.math.BigInteger;
import net.minecraft.entity.player.PlayerEntity;


public class RSAMessage {

    /**
     * Initialize RSA keys for a player with 512-bit key strength
     */
    public static void initkeys(PlayerEntity player){
        // Generate 512-bit RSA key pair
        BigInteger[] choixCle = RSA.choixCle(1024);
        if(choixCle == null) throw new RuntimeException("Couldn't get the keys");
        BigInteger p = choixCle[0];
        BigInteger q = choixCle[1];
        BigInteger e = choixCle[2];

        BigInteger[] public_key = RSA.clePublique(p, q, e);
        if(public_key == null) throw new RuntimeException("Couldn't generate the public_keys");
        BigInteger[] private_key = RSA.clePrivee(p, q, e);
        if(private_key == null) throw new RuntimeException("Couldn't generate the private_keys");

        // Store as "n,e" and "n,d" strings
        String publicKeyStr = public_key[0].toString() + "," + public_key[1].toString();
        String privateKeyStr = private_key[0].toString() + "," + private_key[1].toString();

        player.setAttached(ModDataComponents.PUBLIC_KEY, publicKeyStr);
        player.setAttached(ModDataComponents.PRIVATE_KEY, privateKeyStr);
    }

    /**
     * Parse a key string "n,e" into BigInteger array [n, e]
     */
    private static BigInteger[] parseKey(String keyStr) {
        if (keyStr == null || keyStr.isEmpty()) return null;
        String[] parts = keyStr.split(",");
        if (parts.length != 2) return null;
        return new BigInteger[] {
            new BigInteger(parts[0]),
            new BigInteger(parts[1])
        };
    }

    /**
     * Encode a message using a public key stored as string
     */
    public static String encodeMessageWithKey(String publicKeyStr, String message) {
        BigInteger[] publicKey = parseKey(publicKeyStr);
        if(publicKey == null) throw new RuntimeException("Public key null or invalid");

        StringBuilder encoded = new StringBuilder();
        BigInteger n = publicKey[0];
        BigInteger e = publicKey[1];

        // Calculate fixed width based on n
        int width = n.subtract(BigInteger.ONE).toString().length();

        for(int i = 0; i < message.length(); i++) {
            int charCode = (int) message.charAt(i);
            BigInteger encryptedChar = RSA.codageRSA(BigInteger.valueOf(charCode), n, e);

            if (encryptedChar.equals(BigInteger.valueOf(-1))) {
                throw new RuntimeException("Encryption failed for character: " + message.charAt(i));
            }

            // zero-pad to fixed width and append (no separators)
            String block = String.format("%0" + width + "d", encryptedChar);
            encoded.append(block);
        }

        return encoded.toString();
    }

    /**
     * Decode a message using a private key stored as string
     */
    public static String decodeMessageWithKey(String privateKeyStr, String encryptedMessage) {
        BigInteger[] privateKey = parseKey(privateKeyStr);
        if(privateKey == null) throw new RuntimeException("Private key null or invalid");

        BigInteger n = privateKey[0];
        BigInteger d = privateKey[1];
        int width = n.subtract(BigInteger.ONE).toString().length();

        String trimmed = encryptedMessage.trim();
        if (trimmed.length() % width != 0) {
            throw new RuntimeException("Encrypted message length is not a multiple of block width");
        }

        StringBuilder decoded = new StringBuilder();

        for (int i = 0; i < trimmed.length(); i += width) {
            String block = trimmed.substring(i, i + width);
            if (block.isEmpty()) continue;
            try {
                BigInteger encryptedValue = new BigInteger(block);
                BigInteger decryptedValue = RSA.decodageRSA(encryptedValue, n, d);

                if (decryptedValue.equals(BigInteger.valueOf(-1))) {
                    throw new RuntimeException("Decryption failed");
                }

                decoded.append((char) decryptedValue.intValue());
            } catch(NumberFormatException e) {
                throw new RuntimeException("Invalid encrypted block: " + block);
            }
        }

        return decoded.toString();
    }
}
