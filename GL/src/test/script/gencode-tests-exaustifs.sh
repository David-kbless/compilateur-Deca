#! /bin/sh

# Auteur : David & Ousmane
# Version initiale : 26/12/2025

# tests de la partie Etape C


# ce test consiste a compiler des fichier nom_fichier.deca en nom_fichier.ass
# puis les execute avec ima,
# enfin compare leur resultat avec le contenue des fichier nom_fichier.res (qui contient la reponse juste assigé manuellement par nous)


PATH=./src/main/bin:"$PATH"


for f in src/test/deca/codegen/valid/*/*/*.deca; do
    [ -f "$f" ] || continue
    
    # on creer des variables pour faciliter la manipulation des fichiers en jeu
    ass="${f%.deca}.ass"
    res="${f%.deca}.res"
    tmp="${f%.deca}.tmp"  #  j'ai rajoute ce fichier pour stocker les logs afin de s'assurer que c'est pas
                      # les valeur attendu qui sont fausses
                      # @David, n'hesite pas de faire la meme chose si ça te va


    # Phase de compilation du fic
    decac "$f" > "$tmp" 2>&1
    if [ $? -ne 0 ]; then
        echo "Compilation échouée pour $f"
        cat "$tmp"
        continue
    fi

    # on verifie aussi que  que le fichier .ass a été généré
    if [ ! -f "$ass" ]; then
        echo "Aucun fichier $ass généré"
        continue
    fi

    # on exec avec ima
    ima "$ass" > "$tmp" 2>&1
    if [ $? -ne 0 ]; then
        echo "Erreur à l'exécution de $ass"
        cat "$tmp"
        continue
    fi

    # on compare enfin  avec le résultat attendu
    if [ -f "$res" ]; then
        diff -q "$tmp" "$res" > /dev/null
        if [ $? -eq 0 ]; then
            echo "Test OK : sortie conforme à $res"
        else
            echo "Test KO : sortie différente de $res"
            echo "Différences :"
            diff "$tmp" "$res"
            exit 1
        fi
    else
        echo "Aucun fichier $res pour comparaison, sortie dans $tmp"
    fi
done

# Nettoyage des fichiers temporaires
rm -f src/test/deca/codegen/valid/*/*/*.tmp


echo " ======================================================================================================================="

# maintenant testons les fichier avec erreur d'execution : 

for f in src/test/deca/codegen/invalid/*.deca; do

    ass="${f%.deca}.ass"


    # Phase de compilation du fic
    decac "$f" 
    if [ $? -ne 0 ]; then
        echo "Compilation échouée pour $f"
        continue
    fi

    # on verifie aussi que  que le fichier .ass a été généré
    if [ ! -f "$ass" ]; then
        echo "Aucun fichier $ass généré"
        continue
    fi

    # on exec avec ima
    ima "$ass" > /dev/null
    if [ $? -ne 0 ]; then
        echo "Erreur d'execution attendu pour -> $ass"
    else
        echo "Erreur NON CAPTUREE dans  -> $ass"
        exit 1
    fi

done

echo 
echo 

echo "Tous les tests de CodeGen Deca-Sans-Objet sont [PASSED]."
echo "@Ousmane :  Bravo Equipe GL54 !! ."
echo 

