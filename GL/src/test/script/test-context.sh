#!/bin/bash

# Version initiale : 11/01/2026


cd "$(dirname "$0")"/../../.. || exit 1

PATH=./src/test/script/launchers:"$PATH"

echo "======================================"
echo " Tests de CONTEXTE Deca "
echo "======================================"

#################################
# Tests INVALIDES
#################################
echo ""
echo ">>> TESTS CONTEXTUELS INVALIDES"

test_context_invalide () {
    if test_context "$1" 2>&1 | grep -q -e ':[0-9][0-9]*:'
    then
        echo "Echec attendu sur $1."
    else
        echo "Succes inattendu sur $1."
        exit 1
    fi
}
echo ""
echo ">>> Tests sans objet"
for cas_de_test in src/test/deca/context/invalid/deca_sans_objet/*.deca
do
    test_context_invalide "$cas_de_test"
done

echo ""
echo ">>> Tests avec objet"
for cas_de_test in src/test/deca/context/invalid/deca_complet/*.deca
do
    test_context_invalide "$cas_de_test"
done


echo " ====================================================================="
#################################
# Tests VALIDES
#################################
echo ""
echo ">>> TESTS CONTEXTUELS VALIDES"


test_context_valide () {
    if test_context "$1" 2>&1 | grep -q -e ':[0-9][0-9]*:'
    then
        echo "Echec inattendu sur $1"
        exit 1
    else
        echo "Succes attendu sur $1"
    fi
}
echo ""
echo ">>> Tests sans objet"
for cas_de_test in src/test/deca/context/valid/deca_sans_objet/*.deca
do
    test_context_valide "$cas_de_test"
done

echo ""
echo ">>> Tests avec objet"
for cas_de_test in src/test/deca/context/valid/deca_complet/*.deca
do
    test_context_valide "$cas_de_test"
done


echo ""
echo "======================================"
echo " Tous les tests de context ont réussi "
echo "======================================"
echo ""
echo 

exit 0