package com.cybercom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class RSA {

    public static final Random random = new Random();

    /**
     * Calcule le Plus Grand Commun Diviseur (PGCD) de deux nombres.
     */
    public static long PGCD(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Algorithme d'Euclide étendu.
     * Retourne un tableau [r, u, v] tel que r = a*u + b*v
     * (Non utilisée par le script final, mais présente dans ex1.sh)
     */
    public static long[] euclide(long a, long b) {
        long r0 = a;
        long r1 = b;
        long u0 = 1;
        long u1 = 0;
        long v0 = 0;
        long v1 = 1;

        while (r1 != 0) {
            long q = r0 / r1;
            long r = r0 - q * r1;
            long u = u0 - q * u1;
            long v = v0 - q * v1;

            r0 = r1;
            r1 = r;
            u0 = u1;
            u1 = u;
            v0 = v1;
            v1 = v;
        }
        return new long[] { r0, u0, v0 };
    }

    /**
     * Calcule l'inverse modulaire d de e modulo m.
     * Retourne d tel que e*d = 1 mod m.
     * Retourne -1 si aucun inverse n'est trouvé (équivalent de "echo """).
     */
    public static long inverseModulaire(long e, long m) {
        for (long d = 1; d < m; d++) {
            // Attention : e * d peut dépasser la capacité d'un long
            if ((e * d) % m == 1) {
                return d;
            }
        }
        return -1; // Pas d'inverse trouvé
    }

    /**
     * Vérifie si un nombre n est premier.
     */
    public static boolean estPremier(long n) {
        if (n < 2) {
            return false;
        }
        for (long i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Trouve un nombre premier aléatoire dans [inf, inf + lg].
     * Retourne -1 si aucun nombre premier n'est trouvé (équivalent de "echo """).
     */
    public static long premierAleatoire(long inf, long lg) {
        List<Long> premiers = new ArrayList<>();
        for (long n = inf; n <= inf + lg; n++) {
            if (estPremier(n)) {
                premiers.add(n);
            }
        }

        if (premiers.isEmpty()) {
            return -1;
        }

        int index = random.nextInt(premiers.size());
        return premiers.get(index);
    }

    /**
     * Trouve un nombre p aléatoire (entre 2 et n-1) qui est premier
     * ET premier avec n (PGCD(n, p) == 1).
     * (Basé sur la logique de ex1.sh)
     */
    public static long premierAleatoireAvec(long n) {
        if (n <= 3) {
            return -1; // Cas de bord qui causerait une boucle infinie
        }

        while (true) {
            // p = $(( RANDOM % (n - 2) + 2 ))
            long p = 2 + (Math.abs(random.nextLong()) % (n - 2));

            if (estPremier(p) && PGCD(n, p) == 1) {
                return p;
            }
        }
    }

    /**
     * Calcule (a^n) % m par exponentiation modulaire rapide (récursive).
     * ATTENTION : (half * half) ou (a * half * half) va déborder
     * très rapidement si les nombres sont grands.
     */
    public static long expoModulaire(long a, long n, long m) {
        if (n == 0) {
            return 1 % m;
        }
        if (n % 2 == 0) {
            long half = expoModulaire(a, n / 2, m);
            // Dépassement de capacité (overflow) très probable ici
            return (half * half) % m;
        } else {
            long half = expoModulaire(a, (n - 1) / 2, m);
            // Dépassement de capacité (overflow) très probable ici
            return (a * half * half) % m;
        }
    }

    /**
     * Crée un triplet (p, q, e) pour les clés RSA.
     * (Cette fonction vient de ex2.sh et utilise les fonctions de ex1)
     * Retourne un tableau [p, q, e] ou null en cas d'échec.
     */
    public static long[] choixCle(long inf, long lg) {
        long p = premierAleatoire(inf, lg);
        if (p == -1) {
            System.out.println("Impossible de trouver un nombre premier p dans l'intervalle [" + inf + ", " + (inf + lg) + "]");
            return null;
        }

        long q = -1;
        do {
            q = premierAleatoire(p + 1, lg);
            // On boucle tant que q est égal à p ou qu'on n'a pas trouvé de premier
        } while (q == p || q == -1);

        // ATTENTION : (p - 1) * (q - 1) peut déborder
        long m = (p - 1) * (q - 1); // phi(n)
        long e = premierAleatoireAvec(m);

        if (e == -1) {
            System.out.println("Impossible de trouver un e premier avec m=" + m);
            return null;
        }

        return new long[] { p, q, e };
    }

    /**
     * Calcule la clé publique (n, e) à partir de (p, q, e).
     * Retourne un tableau [n, e].
     */
    public static long[] clePublique(long p, long q, long e) {
        // ATTENTION : p * q peut déborder
        long n = p * q;
        return new long[] { n, e };
    }

    /**
     * Calcule la clé privée (n, d) à partir de (p, q, e).
     * Retourne un tableau [n, d].
     */
    public static long[] clePrivee(long p, long q, long e) {
        // ATTENTION : (p - 1) * (q - 1) et p * q peuvent déborder
        long m = (p - 1) * (q - 1);
        long n = p * q;
        long d = inverseModulaire(e, m);

        return new long[] { n, d };
    }

    /**
     * Code un message M avec la clé publique (n, e).
     * NOTE : Le test 'if (M < n)' est incorrect dans le script original
     * (il devrait être 'if (M >= n)'), mais il est reproduit ici
     * "exactement" comme demandé.
     */
    public static long codageRSA(long M, long n, long e) {
        if (M < n) {
            System.out.println("Le message à coder doit être inférieur à n.");
            return -1; // Signale l'erreur
        }

        long y = expoModulaire(M, e, n);
        return y;
    }

    /**
     * Décode un message M avec la clé privée (n, d).
     * NOTE : Le test 'if (M < n)' est incorrect, comme pour codageRSA.
     */
    public static long decodageRSA(long M, long n, long d) {
        if (M < n) {
            System.out.println("Le message à décoder doit être inférieur à n.");
            return -1; // Signale l'erreur
        }

        long x = expoModulaire(M, d, n);
        return x;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choisir une option :");
        System.out.println("1) Générer une clé RSA");
        System.out.println("2) Coder un message");
        System.out.println("3) Décoder un message");
        System.out.print("Option (1-3) : ");

        int option = scanner.nextInt();

        switch (option) {
            case 1:
                System.out.print("Entrez la borne inférieure (inf) : ");
                long inf = scanner.nextLong();
                System.out.print("Entrez la longueur de l'intervalle (lg) : ");
                long lg = scanner.nextLong();

                // Appel direct à la fonction statique
                long[] cle = choixCle(inf, lg);

                if (cle != null) {
                    // Le script bash affiche "p q e"
                    System.out.println(cle[0] + " " + cle[1] + " " + cle[2]);
                }
                break;
            case 2:
                System.out.print("Entrez le message à coder (entier x) : ");
                long x = scanner.nextLong();
                System.out.print("Entrez la clé publique (n e) : ");
                long n_pub = scanner.nextLong();
                long e_pub = scanner.nextLong();

                long y = codageRSA(x, n_pub, e_pub);

                if (y != -1) { // N'affiche rien si l'erreur a été signalée
                    System.out.println(y);
                }
                break;
            case 3:
                System.out.print("Entrez le message à décoder (entier y) : ");
                long y_dec = scanner.nextLong();
                System.out.print("Entrez la clé privée (n d) : ");
                long n_priv = scanner.nextLong();
                long d_priv = scanner.nextLong();

                long x_dec = decodageRSA(y_dec, n_priv, d_priv);

                if (x_dec != -1) { // N'affiche rien si l'erreur a été signalée
                    System.out.println(x_dec);
                }
                break;
            default:
                System.out.println("Option invalide.");
                break;
        }

        scanner.close();
    }
}
