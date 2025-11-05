#!/bin/bash
# Script de test du mod RSA Minecraft

echo "🔧 Script de test du mod CYBERCOM RSA"
echo "======================================"
echo ""

# 1. Vérifier le conteneur distrobox
echo "1️⃣  Vérification du conteneur distrobox..."
if distrobox list | grep -q "tumbleweed.*Up"; then
    echo "✅ Conteneur distrobox tumbleweed actif"
else
    echo "⚠️  Redémarrage du conteneur distrobox..."
    distrobox stop tumbleweed 2>/dev/null
    distrobox enter tumbleweed -- echo "Conteneur OK"
fi
echo ""

# 2. Nettoyer et compiler
echo "2️⃣  Compilation du projet..."
./gradlew clean build --no-daemon

if [ $? -eq 0 ]; then
    echo "✅ BUILD SUCCESSFUL"
else
    echo "❌ BUILD FAILED"
    exit 1
fi
echo ""

# 3. Vérifier les fichiers générés
echo "3️⃣  Fichiers générés:"
ls -lh build/libs/*.jar 2>/dev/null
echo ""

# 4. Afficher les informations du mod
echo "4️⃣  Informations du mod:"
echo "   📦 Fichier: build/libs/cybercom-1.0.0.jar"
echo "   🎮 Version Minecraft: 1.21.8"
echo "   🧵 Loader: Fabric"
echo ""

# 5. Instructions
echo "5️⃣  Pour tester le mod:"
echo "   Option A - Client:"
echo "     ./gradlew runClient"
echo ""
echo "   Option B - Serveur:"
echo "     ./gradlew runServer"
echo ""
echo "   Option C - Installation manuelle:"
echo "     cp build/libs/cybercom-1.0.0.jar ~/.minecraft/mods/"
echo ""

# 6. Commandes disponibles
echo "6️⃣  Commandes RSA disponibles en jeu:"
echo "   /rsa generate <inf> <lg>    - Générer une clé"
echo "   /rsa encode <M> <n> <e>     - Encoder un message"
echo "   /rsa decode <Y> <n> <d>     - Décoder un message"
echo ""

# 7. Exemple d'utilisation
echo "7️⃣  Exemple rapide:"
echo "   /rsa generate 100 50"
echo "   (vous obtenez p=127 q=139 e=103)"
echo ""
echo "   /rsa encode 42 17653 103"
echo "   (vous obtenez encoded=12345)"
echo ""
echo "   /rsa decode 12345 17653 9567"
echo "   (vous obtenez decoded=42)"
echo ""

echo "✅ Tout est prêt! Bon jeu! 🎮🔐"

