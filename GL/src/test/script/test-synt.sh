#!/bin/bash

# Auteur : David
# Version initiale : 11/01/2026


cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo "======================================"
echo " Tests de SYNTAXE Deca "
echo "======================================"

#################################
# Tests INVALIDES
#################################
echo ""
echo ">>> TESTS SYNTAXE INVALIDES"

test_synt_invalide () {
    if test_synt "$1" 2>&1 | grep -q -e ':[0-9][0-9]*:'
    then
        echo "Echec attendu sur $1."
    else
        echo "Succes inattendu sur $1."
        exit 1
    fi
}
echo ""
echo ">>> Tests sans objet"
for cas_de_test in src/test/deca/syntax/invalid/deca_sans_objet/*.deca
do
    test_synt_invalide "$cas_de_test"
done

echo ""
echo ">>> Tests avec objet"
for cas_de_test in src/test/deca/syntax/invalid/deca_complet/*.deca
do
    test_synt_invalide "$cas_de_test"
done


echo " =============================================================================="
#################################
# Tests VALIDES
#################################
echo ""
echo ">>> TESTS SYNTAXE VALIDES"


test_synt_valide () {
    if test_synt "$1" 2>&1 | grep -q -e ':[0-9][0-9]*:'
    then
        echo "Echec inattendu sur $1"
        exit 1
    else
        echo "Succes attendu sur $1"
    fi
}
echo ""
echo ">>> Tests sans objet"
for cas_de_test in src/test/deca/syntax/valid/deca_sans_objet/*.deca
do
    test_synt_valide "$cas_de_test"
done

echo ""
echo ">>> Tests avec objet"
for cas_de_test in src/test/deca/syntax/valid/deca_complet/*.deca
do
    test_synt_valide "$cas_de_test"
done


echo ""
echo "======================================"
echo " Tous les tests de syntaxe ont réussi "
echo "======================================"
echo "@David :  Bravo Equipe GL54 !! ."
echo 

exit 0