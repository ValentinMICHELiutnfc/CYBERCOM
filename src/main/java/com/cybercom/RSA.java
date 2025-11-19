package com.cybercom;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Implémentation RSA avec BigInteger pour supporter des clés de 512 bits ou plus.
 */
public class RSA {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Calcule le Plus Grand Commun Diviseur (PGCD) de deux nombres.
     */
    public static BigInteger PGCD(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    /**
     * Calcule l'inverse modulaire d de e modulo m.
     * Retourne null si aucun inverse n'est trouvé.
     */
    public static BigInteger inverseModulaire(BigInteger e, BigInteger m) {
        try {
            return e.modInverse(m);
        } catch (ArithmeticException ex) {
            return null;
        }
    }

    /**
     * Vérifie si un nombre n est premier (test probabiliste).
     */
    public static boolean estPremier(BigInteger n) {
        if (n.compareTo(BigInteger.TWO) < 0) {
            return false;
        }
        // Test de primalité probabiliste (certainty = 100 donne une très haute confiance)
        return n.isProbablePrime(100);
    }

    /**
     * Génère un nombre premier aléatoire de bitLength bits.
     */
    public static BigInteger genererPremier(int bitLength) {
        return BigInteger.probablePrime(bitLength, random);
    }

    /**
     * Trouve un nombre e premier avec m (PGCD(e, m) == 1).
     * Utilise typiquement e = 65537 (standard RSA) si possible.
     */
    public static BigInteger trouverE(BigInteger m) {
        // Essayer d'abord 65537 (standard pour RSA)
        BigInteger e = BigInteger.valueOf(65537);
        if (e.compareTo(m) < 0 && PGCD(e, m).equals(BigInteger.ONE)) {
            return e;
        }

        // Sinon, chercher un autre e premier avec m
        e = BigInteger.valueOf(3);
        while (e.compareTo(m) < 0) {
            if (PGCD(e, m).equals(BigInteger.ONE)) {
                return e;
            }
            e = e.add(BigInteger.TWO);
        }

        return null;
    }

    /**
     * Crée un triplet (p, q, e) pour les clés RSA avec une taille de clé spécifiée.
     * Retourne un tableau [p, q, e] ou null en cas d'échec.
     */
    public static BigInteger[] choixCle(int bitLength) {
        // Génère deux nombres premiers de bitLength/2 bits chacun
        BigInteger p = genererPremier(bitLength / 2);
        BigInteger q = genererPremier(bitLength / 2);

        // S'assurer que p != q
        while (p.equals(q)) {
            q = genererPremier(bitLength / 2);
        }

        // Calcul de phi(n) = (p-1) * (q-1)
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Trouver e premier avec phi
        BigInteger e = trouverE(phi);

        if (e == null) {
            System.out.println("Impossible de trouver un e premier avec phi(n)");
            return null;
        }

        return new BigInteger[] { p, q, e };
    }

    /**
     * Version compatible avec l'ancienne interface (avec long).
     * Génère une clé RSA avec des nombres premiers dans la plage donnée.
     * ATTENTION: Cette méthode est limitée par la taille des long.
     */
    public static long[] choixCle(long inf, long lg) {
        // Pour compatibilité, on génère une clé de 128 bits
        BigInteger[] key = choixCle(128);
        if (key == null) return null;

        try {
            return new long[] {
                key[0].longValueExact(),
                key[1].longValueExact(),
                key[2].longValueExact()
            };
        } catch (ArithmeticException e) {
            System.out.println("Les valeurs générées sont trop grandes pour des long");
            return null;
        }
    }

    /**
     * Calcule la clé publique (n, e) à partir de (p, q, e).
     * Retourne un tableau [n, e].
     */
    public static BigInteger[] clePublique(BigInteger p, BigInteger q, BigInteger e) {
        BigInteger n = p.multiply(q);
        return new BigInteger[] { n, e };
    }

    /**
     * Version long pour compatibilité.
     */
    public static long[] clePublique(long p, long q, long e) {
        long n = p * q;
        return new long[] { n, e };
    }

    /**
     * Calcule la clé privée (n, d) à partir de (p, q, e).
     * Retourne un tableau [n, d].
     */
    public static BigInteger[] clePrivee(BigInteger p, BigInteger q, BigInteger e) {
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger n = p.multiply(q);
        BigInteger d = inverseModulaire(e, phi);

        if (d == null) {
            return null;
        }

        return new BigInteger[] { n, d };
    }

    /**
     * Version long pour compatibilité.
     */
    public static long[] clePrivee(long p, long q, long e) {
        long m = (p - 1) * (q - 1);
        long n = p * q;

        // Utiliser BigInteger pour calculer l'inverse
        BigInteger bE = BigInteger.valueOf(e);
        BigInteger bM = BigInteger.valueOf(m);
        BigInteger bD = inverseModulaire(bE, bM);

        if (bD == null) {
            return null;
        }

        try {
            long d = bD.longValueExact();
            return new long[] { n, d };
        } catch (ArithmeticException ex) {
            return null;
        }
    }

    /**
     * Code un message M avec la clé publique (n, e).
     */
    public static BigInteger codageRSA(BigInteger M, BigInteger n, BigInteger e) {
        if (M.compareTo(n) >= 0) {
            System.out.println("Le message à coder doit être inférieur à n.");
            return BigInteger.valueOf(-1);
        }

        return M.modPow(e, n);
    }

    /**
     * Version long pour compatibilité.
     */
    public static long codageRSA(long M, long n, long e) {
        BigInteger bM = BigInteger.valueOf(M);
        BigInteger bN = BigInteger.valueOf(n);
        BigInteger bE = BigInteger.valueOf(e);

        BigInteger result = codageRSA(bM, bN, bE);

        if (result.equals(BigInteger.valueOf(-1))) {
            return -1;
        }

        try {
            return result.longValueExact();
        } catch (ArithmeticException ex) {
            System.out.println("Le résultat du codage est trop grand pour un long");
            return -1;
        }
    }

    /**
     * Décode un message M avec la clé privée (n, d).
     */
    public static BigInteger decodageRSA(BigInteger M, BigInteger n, BigInteger d) {
        if (M.compareTo(n) >= 0) {
            System.out.println("Le message à décoder doit être inférieur à n.");
            return BigInteger.valueOf(-1);
        }

        return M.modPow(d, n);
    }

    /**
     * Version long pour compatibilité.
     */
    public static long decodageRSA(long M, long n, long d) {
        BigInteger bM = BigInteger.valueOf(M);
        BigInteger bN = BigInteger.valueOf(n);
        BigInteger bD = BigInteger.valueOf(d);

        BigInteger result = decodageRSA(bM, bN, bD);

        if (result.equals(BigInteger.valueOf(-1))) {
            return -1;
        }

        try {
            return result.longValueExact();
        } catch (ArithmeticException ex) {
            System.out.println("Le résultat du décodage est trop grand pour un long");
            return -1;
        }
    }

    /**
     * Exponentiation modulaire (legacy - utilise maintenant modPow de BigInteger).
     */
    public static long expoModulaire(long a, long n, long m) {
        BigInteger ba = BigInteger.valueOf(a);
        BigInteger bn = BigInteger.valueOf(n);
        BigInteger bm = BigInteger.valueOf(m);

        return ba.modPow(bn, bm).longValue();
    }
}

