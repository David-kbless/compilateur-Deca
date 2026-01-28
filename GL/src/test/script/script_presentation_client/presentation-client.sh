#!/bin/bash

clear;

echo
echo

# chemin ver le fihchier test pour le suivi en question 
TEST_FILE="./../deca/presentation-client/test_complet_sans_objet.deca"

# on verif l'existence du fichier avant de commencer
if [ ! -f "$TEST_FILE" ]; then
    echo "Erreur : Le fichier $TEST_FILE est introuvable."
    exit 1
fi

echo "========================================================="
echo "   INTERPRETEUR DE TEST POUR LE SUIVI 2 AVEC LE CLIENT M. FRERE MARCO"
echo "   (Tapez 'z' pour quitter le script)"
echo "========================================================="

while true; do
    echo ""
    read -p "Entrez les options pour decac (ex: -n, -p, -v, -r X) ou 'z' : " OPTION

    # on verif la condition d'arret
    if [ "$OPTION" = "z" ]; then
        echo "Arrêt du script demandé. Au revoir !"
        break
    fi

    echo "Exécution : src/main/bin/decac $OPTION $TEST_FILE"
    
    if [ "$OPTION" = "" ]; then
        ./../../main/bin/decac  "$TEST_FILE"
    else
        ./../../main/bin/decac $OPTION "$TEST_FILE"

    fi


    if [ $? -eq 0 ]; then
        echo "-> Compilation réussie."
        
        # on lancee le test immediatement
        read -p "exécution avec ima les gars ? (y/n) : " EXEC
        if [ "$EXEC" = "y" ]; then
            ass_file="${TEST_FILE%.deca}.ass"
            if [ -f "$ass_file" ]; then
                echo "--- Début de l'exécution du code ASM ---"
                ima "$ass_file"
                echo "--- Fin de l'exécution ---"
            else
                echo "Erreur : Fichier .ass introuvable."
            fi
        fi
    else
        echo "-> Erreur lors de la compilation."
    fi
    
    echo "---------------------------------------------------------"
done