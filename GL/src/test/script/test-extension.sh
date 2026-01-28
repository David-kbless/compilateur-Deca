#!/bin/sh

# Auteur : Gemini
# Date   : 23/01/2026
#
# Description:
# Ce script teste la génération de code pour les fichiers d'extension Deca.
# Il compile chaque fichier .deca du répertoire src/test/deca/extension,
# exécute le code assembleur .ass généré avec l'interpréteur IMA,
# et compare la sortie avec le contenu du fichier .res correspondant.

# Met le chemin vers decac dans le PATH
PATH=./src/main/bin:"$PATH"

# Répertoire contenant les tests d'extension
TEST_DIR="src/test/deca/extension"

echo "================================================="
echo "  Tests de Génération de Code pour les Extensions "
echo "================================================="
echo ""

# Variable pour suivre les erreurs
has_error=0

# Boucle sur tous les fichiers .deca dans le répertoire d'extension
for f in "$TEST_DIR"/*.deca; do
    # S'il n'y a pas de fichiers .deca, on passe
    [ -f "$f" ] || continue
    
    echo "--- Traitement de $f ---"

    # Définition des noms de fichiers
    ass_file="${f%.deca}.ass"
    res_file="${f%.deca}.res"
    tmp_file="${f%.deca}.tmp"

    # 1. Compilation du fichier .deca
    decac "$f" 2> "$tmp_file"
    if [ $? -ne 0 ]; then
        echo "ERREUR: La compilation de $f a échoué."
        cat "$tmp_file"
        has_error=1
        continue
    fi

    # 2. Vérification de la création du fichier .ass
    if [ ! -f "$ass_file" ]; then
        echo "ERREUR: Le fichier assembleur $ass_file n'a pas été généré."
        has_error=1
        continue
    fi

    # 3. Exécution du code assembleur avec IMA
    ima "$ass_file" > "$tmp_file" 2>&1
    if [ $? -ne 0 ]; then
        echo "ERREUR: L'exécution de $ass_file a échoué."
        cat "$tmp_file"
        has_error=1
        continue
    fi

    # 4. Comparaison avec le résultat attendu (.res)
    if [ ! -f "$res_file" ]; then
        echo "AVERTISSEMENT: Aucun fichier de résultat $res_file trouvé pour ce test."
        echo "Sortie obtenue :"
        cat "$tmp_file"
    else
        diff -q "$tmp_file" "$res_file" > /dev/null
        if [ $? -eq 0 ]; then
            echo "SUCCES: La sortie est conforme à $res_file."
        else
            echo "ECHEC: La sortie est différente de $res_file."
            echo "Différences :"
            diff --unified "$res_file" "$tmp_file"
            has_error=1
        fi
    fi
    
    # Nettoyage du fichier temporaire
    rm -f "$tmp_file"
    echo ""
done

# Bilan final
echo "================================================="
if [ "$has_error" -eq 0 ]; then
    echo "Tous les tests d'extension ont réussi ! Bravo !"
    exit 0
else
    echo "Certains tests d'extension ont échoué."
    exit 1
fi
