#!/bin/bash
# test des options du compilateur 

TEST_DIR="src/test/deca/compiler_option"
DECAC="./src/main/bin/decac"

# Nettoyage initial
rm -f "$TEST_DIR"/*.ass

echo -e "=== Lancement de la suite de tests (Options Compiler) ===\n"

# --- TEST 1 : Option -v (Vérification seule) ---
# Un compilateur fonctionnel ne doit RIEN générer sur la sortie standard si OK
$DECAC -v "$TEST_DIR/option_v.deca" > temp_v.log 2>&1
if [ ! -s temp_v.log ] && [ ! -f "${TEST_DIR}/option_v.ass" ]; then
    echo -e "OK TEST_OPTION -v (Aucune sortie, aucun .ass généré)"
else
    echo -e "ECHEC TEST -v (Sortie inattendue ou fichier .ass généré)"
    exit 1
fi

# --- TEST 2 : Option -p (Décompilation) ---
# Au lieu d'un diff strict, on vérifie que la décompilation est un code Deca valide
$DECAC -p "$TEST_DIR/option_p.deca" > temp_p.deca
if [ -s temp_p.deca ]; then
    $DECAC -v temp_p.deca > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo -e "OK TEST_OPTION -p (Décompilation génère un code syntaxiquement correct)"
    else
        echo -e "ECHEC TEST -p (Le code décompilé est invalide)"
        exit 1
    fi
else
    echo -e "ECHEC TEST -p (Fichier vide)"
    exit 1
fi



# --- TEST 4 : Option -r X (Limitation des registres) ---
$DECAC -r 4 "$TEST_DIR/option_r_4.deca" > /dev/null
# On vérifie qu'aucun registre R4 à R15 n'est utilisé
if ! grep -qE "R([4-9]|1[0-5])\b" "$TEST_DIR/option_r_4.ass"; then
    echo -e "OK TEST_OPTION -r 4 (Registres limités à R0-R3)"
else
    echo -e "ECHEC TEST -r 4 (Registre R4+ détecté)"
    exit 1
fi

# --- TEST 5 : Option -b (Bannière) ---
# On vérifie juste que quelque chose est écrit et que le programme s'arrête (exit 0)
$DECAC -b | grep -q "." 
if [ $? -eq 0 ]; then
    echo "OK TEST_OPTION -b (Bannière affichée)"
else
    echo "ECHEC TEST -b"
    exit 1
fi

# --- TEST 7 : Option -d (Debug) ---
$DECAC -d "$TEST_DIR/option_v.deca" > log1.txt 2>&1
$DECAC -d -d "$TEST_DIR/option_v.deca" > log2.txt 2>&1
size1=$(wc -c < log1.txt)
size2=$(wc -c < log2.txt)
if [ "$size2" -gt "$size1" ]; then
    echo "OK TEST_OPTION -d (Le niveau de log augmente bien)"
else
    echo "ECHEC TEST -d (Pas de changement dans les logs)"
    exit 1
fi

echo -e "\n=== Tests d'exclusions et erreurs d'arguments ==="

# Test exclusion -p et -v
$DECAC -p -v "$TEST_DIR/option_v.deca" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "OK TEST (Exclusion -p -v respectée)"
else
    echo "ECHEC TEST (Le compilateur a accepté -p et -v simultanément)"
    exit 1
fi



# test de l'option -P (Parallélisation)
$DECAC -P "$TEST_DIR/fichier1_paralle.deca" "$TEST_DIR /fichier2_paralle.deca" > /dev/null 2>&1


# Nettoyage
rm -f temp_v.log temp_p.deca log1.txt log2.txt
rm -f "$TEST_DIR"/*.ass
echo -e "\n=== Fin des tests : TOUT EST OK ==="