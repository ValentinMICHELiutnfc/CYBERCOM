package com.cybercom;

import java.util.Calendar;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;


public class RSAMessage {

    public static void initkeys(PlayerEntity player){
        long[] choixCle = RSA.choixCle(1,100);
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
}
