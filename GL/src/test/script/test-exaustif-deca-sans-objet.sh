#! /bin/sh

# Auteur : Ousmane
# Version initiale : 26/12/2025



# ce test consiste a compiler le fichier  test_complet_sans_objet.deca en test_complet_sans_objet.ass
# puis l' execute avec ima,
# enfin compare leur resultat avec le contenue des fichier test_complet_sans_objet.res (qui contient la reponse juste assigé manuellement par nous)


PATH=./src/main/bin:"$PATH"



    ass="src/test/deca/presentation-client/test_complet_sans_objet.ass"
    f="src/test/deca/presentation-client/test_complet_sans_objet.deca"

    
    # Phase de compilation du fic
    src/main/bin/decac "$f" 
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
    ima "$ass" < src/test/deca/presentation-client/input.txt  > /dev/null
    if [ $? -ne 0 ]; then
        echo "Erreur d'execution attendu pour -> $ass"
    else
        echo "Erreur NON CAPTUREE dans  -> $ass"
        exit 1
    fi
