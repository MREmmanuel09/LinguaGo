"""F1.3 — A1 completo de ES→FR (150 lecciones nuevas, 15 bloques)."""

from __future__ import annotations

import importlib.util
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json"
COURSE_ID = "es-fr"
NEXT_LEVEL = "es-fr-lv-a2"

SPEC = importlib.util.spec_from_file_location("f1_1", ROOT / "tools/content/f1_1_a1_es_en.py")
F1_1 = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(F1_1)


def L(title: str, topic: str, items: list[tuple[str, str]]) -> dict:
    return {"title": title, "topic": topic, "items": items}


FR_BLOCKS = [
    (
        "Premiers pas",
        [
            L("Salutations", "les salutations", [("hola", "bonjour"), ("adiós", "au revoir"), ("gracias", "merci")]),
            L("Nombres", "les nombres", [("uno", "un"), ("dos", "deux"), ("tres", "trois")]),
            L("Couleurs", "les couleurs", [("rojo", "rouge"), ("azul", "bleue"), ("verde", "verte")]),
            L("En classe", "les objets de classe", [("un libro", "un livre"), ("una silla", "une chaise"), ("un lápiz", "un crayon")]),
            L("La famille", "la famille", [("un hermano", "un frère"), ("una hermana", "une sœur"), ("un hijo", "un fils")]),
            L("Professions", "les professions", [("profesor", "professeur"), ("médico", "médecin"), ("estudiante", "étudiant")]),
            L("Animaux de compagnie", "les animaux de compagnie", [("un perro", "un chien"), ("un gato", "un chat"), ("un pájaro", "un oiseau")]),
            L("Nourriture", "la nourriture", [("la pizza", "la pizza"), ("la fruta", "les fruits"), ("el pan", "le pain")]),
            L("Lieux", "les lieux", [("el parque", "le parc"), ("la escuela", "l'école"), ("el hospital", "l'hôpital")]),
            L("Descriptions", "les descriptions", [("el coche", "la voiture"), ("la casa", "la maison"), ("grande", "grande")]),
        ],
    ),
    (
        "Ma journée",
        [
            L("Ma routine", "la routine quotidienne", [("me levanto", "je me réveille"), ("desayuno", "je prends le petit-déjeuner"), ("me acuesto", "je me couche")]),
            L("Les heures", "les heures", [("la una", "une heure"), ("las dos", "deux heures"), ("las tres", "trois heures")]),
            L("La maison", "les pièces", [("la cocina", "la cuisine"), ("el baño", "la salle de bain"), ("el salón", "le salon")]),
            L("Objets de la maison", "les objets de la maison", [("una cama", "un lit"), ("una mesa", "une table"), ("una ventana", "une fenêtre")]),
            L("Le petit-déjeuner", "le petit-déjeuner", [("un huevo", "un œuf"), ("pan", "du pain"), ("una manzana", "une pomme")]),
            L("Boissons", "les boissons", [("agua", "de l'eau"), ("café", "du café"), ("leche", "du lait")]),
            L("Tâches", "les tâches ménagères", [("cocinar", "cuisiner"), ("limpiar", "nettoyer"), ("lavar", "laver")]),
            L("Temps libre", "le temps libre", [("leer", "lire"), ("bailar", "danser"), ("cantar", "chanter")]),
            L("Achats", "les achats", [("una camisa", "une chemise"), ("unos zapatos", "des chaussures"), ("un sombrero", "un chapeau")]),
            L("La ville", "la ville", [("el parque", "le parc"), ("el museo", "le musée"), ("bonito", "beau")]),
        ],
    ),
    (
        "Personnes",
        [
            L("Plus de famille", "la famille", [("un padre", "un père"), ("una madre", "une mère"), ("un abuelo", "un grand-père")]),
            L("Amis", "les amis", [("un amigo", "un ami"), ("una amiga", "une amie"), ("un vecino", "un voisin")]),
            L("Âges", "les âges", [("diez", "dix"), ("veinte", "vingt"), ("treinta", "trente")]),
            L("Description physique", "la description physique", [("el pelo", "le cheveu"), ("el brazo", "le bras"), ("largo", "long")]),
            L("Caractère", "le caractère", [("el profesor", "le professeur"), ("el estudiante", "l'étudiant"), ("simpático", "sympathique")]),
            L("Vêtements", "les vêtements", [("una camisa", "une chemise"), ("un pantalón", "un pantalon"), ("un abrigo", "un manteau")]),
            L("Travail", "les métiers", [("cocinero", "cuisinier"), ("conductor", "chauffeur"), ("dependiente", "vendeur")]),
            L("Le bureau", "le bureau", [("la oficina", "le bureau"), ("la tienda", "le magasin"), ("el taller", "l'atelier")]),
            L("L'école", "l'école", [("una mesa", "une table"), ("un ordenador", "un ordinateur"), ("un cuaderno", "un cahier")]),
            L("Révision", "la révision", [("un libro", "un livre"), ("una mochila", "un sac à dos"), ("un bolígrafo", "un stylo")]),
        ],
    ),
    (
        "Nourriture",
        [
            L("Fruits", "les fruits", [("una manzana", "une pomme"), ("un plátano", "une banane"), ("una naranja", "une orange")]),
            L("Légumes", "les légumes", [("una zanahoria", "une carotte"), ("un tomate", "une tomate"), ("una cebolla", "un oignon")]),
            L("Boissons", "les boissons", [("agua", "de l'eau"), ("zumo", "du jus"), ("té", "du thé")]),
            L("Le petit-déjeuner", "le petit-déjeuner", [("pan", "du pain"), ("un huevo", "un œuf"), ("cereales", "des céréales")]),
            L("Le déjeuner", "le déjeuner", [("arroz", "du riz"), ("pollo", "du poulet"), ("pescado", "du poisson")]),
            L("Au restaurant", "le restaurant", [("una mesa", "une table"), ("un menú", "un menu"), ("la cuenta", "l'addition")]),
            L("Goûts", "les goûts", [("el chocolate", "le chocolat"), ("el queso", "le fromage"), ("la sopa", "la soupe")]),
            L("Au marché", "le marché", [("una manzana", "une pomme"), ("un tomate", "une tomate"), ("una botella de agua", "une bouteille d'eau")]),
            L("Prix", "les prix", [("cinco euros", "cinq euros"), ("diez euros", "dix euros"), ("veinte euros", "vingt euros")]),
            L("Révision", "la révision", [("el restaurante", "le restaurant"), ("el menú", "le menu"), ("caro", "cher")]),
        ],
    ),
    (
        "Maison et ville",
        [
            L("Pièces", "les pièces", [("el dormitorio", "la chambre"), ("la cocina", "la cuisine"), ("el baño", "la salle de bain")]),
            L("Meubles", "les meubles", [("una cama", "un lit"), ("una silla", "une chaise"), ("un sofá", "un canapé")]),
            L("La rue", "la rue", [("la calle", "la rue"), ("la plaza", "la place"), ("el supermercado", "le supermarché")]),
            L("Directions", "les directions", [("a la izquierda", "à gauche"), ("a la derecha", "à droite"), ("todo recto", "tout droit")]),
            L("Transport", "le transport", [("en autobús", "en bus"), ("en tren", "en train"), ("en bicicleta", "à vélo")]),
            L("La gare", "la gare", [("la estación", "la gare"), ("el aeropuerto", "l'aéroport"), ("el hotel", "l'hôtel")]),
            L("Le magasin", "le magasin", [("pan", "du pain"), ("leche", "du lait"), ("una camisa", "une chemise")]),
            L("Le quartier", "le quartier", [("el barrio", "le quartier"), ("el parque", "le parc"), ("tranquilo", "tranquille")]),
            L("Ma maison", "ma maison", [("una casa", "une maison"), ("un piso", "un appartement"), ("una habitación", "une chambre")]),
            L("Révision", "la révision", [("una llave", "une clé"), ("un mapa", "une carte"), ("un billete", "un billet")]),
        ],
    ),
    (
        "Santé",
        [
            L("Le corps", "le corps", [("cabeza", "la tête"), ("mano", "la main"), ("pie", "le pied")]),
            L("Symptômes", "les symptômes", [("dolor de cabeza", "mal à la tête"), ("dolor de estómago", "mal au ventre"), ("fiebre", "de la fièvre")]),
            L("Chez le médecin", "le médecin", [("una cita", "un rendez-vous"), ("un médico", "un médecin"), ("ayuda", "de l'aide")]),
            L("La pharmacie", "la pharmacie", [("medicina", "des médicaments"), ("una venda", "un bandage"), ("pastillas", "des comprimés")]),
            L("Habitudes saines", "les habitudes saines", [("caminar", "marcher"), ("descansar", "se reposer"), ("beber agua", "boire de l'eau")]),
            L("Le sport", "le sport", [("al fútbol", "au football"), ("al tenis", "au tennis"), ("al baloncesto", "au basket")]),
            L("Dormir", "le repos", [("dormir", "dormir"), ("soñar", "rêver"), ("respirar", "respirer")]),
            L("La salle de sport", "la salle de sport", [("el gimnasio", "la salle de sport"), ("la piscina", "la piscine"), ("el parque", "le parc")]),
            L("Urgences", "les urgences", [("un médico", "un médecin"), ("una ambulancia", "une ambulance"), ("ayuda", "aide")]),
            L("Révision", "la révision", [("el médico", "le médecin"), ("el hospital", "l'hôpital"), ("bueno", "bon")]),
        ],
    ),
    (
        "École et travail",
        [
            L("Matières", "les matières", [("la historia", "l'histoire"), ("la ciencia", "la science"), ("el arte", "l'art")]),
            L("En classe", "les objets de classe", [("un libro", "un livre"), ("un cuaderno", "un cahier"), ("una regla", "une règle")]),
            L("L'école", "l'école", [("la escuela", "l'école"), ("la biblioteca", "la bibliothèque"), ("el aula", "la salle de classe")]),
            L("Professions", "les professions", [("maestro", "professeur"), ("enfermero", "infirmier"), ("ingeniero", "ingénieur")]),
            L("Le bureau", "le bureau", [("la oficina", "le bureau"), ("la reunión", "la réunion"), ("el escritorio", "l'espace de travail")]),
            L("Tâches", "les tâches", [("escribir", "écrire"), ("leer", "lire"), ("hablar", "parler")]),
            L("L'emploi du temps", "l'emploi du temps", [("las ocho", "huit heures"), ("las diez", "dix heures"), ("las doce", "douze heures")]),
            L("Outils", "les outils", [("un ordenador", "un ordinateur"), ("un teléfono", "un téléphone"), ("una carpeta", "un dossier")]),
            L("Le travail", "le travail", [("trabajo", "je travaille"), ("estudio", "j'étudie"), ("descanso", "je me repose")]),
            L("Révision", "la révision", [("el trabajo", "le travail"), ("el proyecto", "le projet"), ("importante", "important")]),
        ],
    ),
    (
        "Achats",
        [
            L("Vêtements", "les vêtements", [("una camisa", "une chemise"), ("un vestido", "une robe"), ("unos zapatos", "des chaussures")]),
            L("Couleurs", "les couleurs", [("rojo", "rouge"), ("azul", "bleue"), ("negro", "noire")]),
            L("Tailles", "les tailles", [("una talla pequeña", "une petite taille"), ("una talla mediana", "une taille moyenne"), ("una talla grande", "une grande taille")]),
            L("Le magasin", "le magasin", [("la tienda", "le magasin"), ("el centro comercial", "le centre commercial"), ("el mercado", "le marché")]),
            L("Prix", "les prix", [("cinco euros", "cinq euros"), ("quince euros", "quinze euros"), ("cincuenta euros", "cinquante euros")]),
            L("Payer", "payer", [("pagar", "payer"), ("ahorrar", "économiser"), ("gastar", "dépenser")]),
            L("Cadeaux", "les cadeaux", [("un regalo", "un cadeau"), ("flores", "des fleurs"), ("una tarjeta", "une carte")]),
            L("Goûts", "les goûts", [("la ropa", "les vêtements"), ("el color azul", "la couleur bleue"), ("el algodón", "le coton")]),
            L("Au marché", "le marché", [("una manzana", "une pomme"), ("pan", "du pain"), ("queso", "du fromage")]),
            L("Révision", "la révision", [("el mercado", "le marché"), ("el centro comercial", "le centre commercial"), ("grande", "grand")]),
        ],
    ),
    (
        "Voyages",
        [
            L("Transport", "le transport", [("en avión", "en avion"), ("en tren", "en train"), ("en coche", "en voiture")]),
            L("La gare", "la gare", [("la estación", "la gare"), ("el aeropuerto", "l'aéroport"), ("la parada", "l'arrêt")]),
            L("L'hôtel", "l'hôtel", [("una habitación", "une chambre"), ("una llave", "une clé"), ("un mapa", "une carte")]),
            L("Les bagages", "les bagages", [("una maleta", "une valise"), ("un pasaporte", "un passeport"), ("un billete", "un billet")]),
            L("Directions", "les directions", [("a la izquierda", "à gauche"), ("a la derecha", "à droite"), ("todo recto", "tout droit")]),
            L("Vacances", "les vacances", [("viajar", "voyager"), ("nadar", "nager"), ("visitar", "visiter")]),
            L("Le temps", "le temps", [("soleado", "il fait beau"), ("nublado", "il y a des nuages"), ("lluvioso", "il pleut")]),
            L("La plage", "la plage", [("la playa", "la plage"), ("el mar", "la mer"), ("la montaña", "la montagne")]),
            L("Achats de voyage", "les achats de voyage", [("un recuerdo", "un souvenir"), ("agua", "de l'eau"), ("un helado", "une glace")]),
            L("Révision", "la révision", [("el viaje", "le voyage"), ("el hotel", "l'hôtel"), ("bonito", "beau")]),
        ],
    ),
    (
        "Temps libre",
        [
            L("Passe-temps", "les passe-temps", [("leer", "lire"), ("pintar", "peindre"), ("cocinar", "cuisiner")]),
            L("Musique", "la musique", [("la música", "de la musique"), ("el rock", "du rock"), ("el pop", "de la pop")]),
            L("Cinéma", "le cinéma", [("una película", "un film"), ("una serie", "une série"), ("un documental", "un documentaire")]),
            L("Sport", "le sport", [("al fútbol", "au football"), ("al tenis", "au tennis"), ("al baloncesto", "au basket")]),
            L("Invitations", "les invitations", [("salir", "sortir"), ("cenar", "dîner"), ("ver una película", "regarder un film")]),
            L("La ville", "la ville", [("el cine", "le cinéma"), ("el museo", "le musée"), ("el restaurante", "le restaurant")]),
            L("Goûts", "les goûts", [("la música", "la musique"), ("el deporte", "le sport"), ("la lectura", "la lecture")]),
            L("Instruments", "les instruments", [("tocar la guitarra", "jouer de la guitare"), ("cantar", "chanter"), ("bailar", "danser")]),
            L("Le week-end", "le week-end", [("descanso", "je me repose"), ("salgo", "je sors"), ("leo", "je lis")]),
            L("Révision", "la révision", [("el libro", "le livre"), ("el juego", "le jeu"), ("divertido", "amusant")]),
        ],
    ),
    (
        "Nature",
        [
            L("Animaux", "les animaux", [("un caballo", "un cheval"), ("una vaca", "une vache"), ("una oveja", "un mouton")]),
            L("Animaux sauvages", "les animaux sauvages", [("un león", "un lion"), ("un elefante", "un éléphant"), ("un mono", "un singe")]),
            L("Plantes", "les plantes", [("una flor", "une fleur"), ("un árbol", "un arbre"), ("una planta", "une plante")]),
            L("Le temps", "le temps", [("calor", "il fait chaud"), ("frío", "il fait froid"), ("viento", "il y a du vent")]),
            L("Les saisons", "les saisons", [("primavera", "au printemps"), ("verano", "en été"), ("otoño", "en automne")]),
            L("La campagne", "la campagne", [("el campo", "la campagne"), ("el río", "le fleuve"), ("el bosque", "la forêt")]),
            L("La montagne", "la montagne", [("la montaña", "la montagne"), ("el lago", "le lac"), ("el sendero", "le sentier")]),
            L("Actions", "les actions", [("caminar", "marcher"), ("nadar", "nager"), ("correr", "courir")]),
            L("L'environnement", "l'environnement", [("reciclar", "recycler"), ("ahorrar agua", "économiser l'eau"), ("cuidar el planeta", "protéger la planète")]),
            L("Révision", "la révision", [("el río", "le fleuve"), ("el parque", "le parc"), ("bonito", "beau")]),
        ],
    ),
    (
        "Technologie",
        [
            L("Le téléphone", "le téléphone", [("un teléfono", "un téléphone"), ("una tableta", "une tablette"), ("un cargador", "un chargeur")]),
            L("L'ordinateur", "l'ordinateur", [("un ordenador", "un ordinateur"), ("un teclado", "un clavier"), ("una pantalla", "un écran")]),
            L("Internet", "internet", [("navegar", "naviguer"), ("descargar", "télécharger"), ("buscar", "chercher")]),
            L("Messages", "les messages", [("un mensaje", "un message"), ("una foto", "une photo"), ("un correo", "un e-mail")]),
            L("Photos", "les photos", [("hacer fotos", "prendre des photos"), ("ver vídeos", "regarder des vidéos"), ("escuchar música", "écouter de la musique")]),
            L("Applications", "les applications", [("una aplicación", "une application"), ("una cuenta", "un compte"), ("una contraseña", "un mot de passe")]),
            L("Sécurité", "la sécurité", [("una contraseña", "un mot de passe"), ("ayuda", "aide"), ("privacidad", "la confidentialité")]),
            L("Le réseau", "le réseau", [("la red", "le réseau"), ("la nube", "le cloud"), ("el sitio web", "le site web")]),
            L("Problèmes", "les problèmes", [("un problema", "un problème"), ("una pregunta", "une question"), ("una solución", "une solution")]),
            L("Révision", "la révision", [("el teléfono", "le téléphone"), ("el ordenador", "l'ordinateur"), ("nuevo", "nouveau")]),
        ],
    ),
    (
        "Culture",
        [
            L("Fêtes", "les fêtes", [("feliz cumpleaños", "bon anniversaire"), ("feliz año nuevo", "bonne année"), ("felices fiestas", "bonnes fêtes")]),
            L("Anniversaires", "les anniversaires", [("un pastel", "un gâteau"), ("un regalo", "un cadeau"), ("una tarjeta", "une carte")]),
            L("Traditions", "les traditions", [("la música", "la musique"), ("la comida", "la nourriture"), ("la familia", "la famille")]),
            L("Invitations", "les invitations", [("venir a la fiesta", "venir à la fête"), ("traer un regalo", "apporter un cadeau"), ("celebrar juntos", "fêter ensemble")]),
            L("La musique", "la musique", [("la música tradicional", "de la musique traditionnelle"), ("el flamenco", "du flamenco"), ("el tango", "du tango")]),
            L("L'art", "l'art", [("el museo", "le musée"), ("la galería", "la galerie"), ("el teatro", "le théâtre")]),
            L("Cuisine typique", "la cuisine typique", [("una paella", "une paella"), ("un taco", "un taco"), ("una arepa", "une arepa")]),
            L("Cadeaux", "les cadeaux", [("flores", "des fleurs"), ("chocolate", "du chocolat"), ("un libro", "un livre")]),
            L("Le cinéma", "le cinéma", [("una película", "un film"), ("un documental", "un documentaire"), ("una comedia", "une comédie")]),
            L("Révision", "la révision", [("el festival", "le festival"), ("el concierto", "le concert"), ("popular", "populaire")]),
        ],
    ),
    (
        "Révision et projets",
        [
            L("Révision de routine", "la routine", [("me levanto", "je me réveille"), ("trabajo", "je travaille"), ("me acuesto", "je me couche")]),
            L("Révision de nourriture", "la nourriture", [("una manzana", "une pomme"), ("arroz", "du riz"), ("pollo", "du poulet")]),
            L("Révision de lieux", "les lieux", [("el parque", "le parc"), ("la escuela", "l'école"), ("el supermercado", "le supermarché")]),
            L("Révision d'achats", "les achats", [("una camisa", "une chemise"), ("pan", "du pain"), ("un regalo", "un cadeau")]),
            L("Révision de voyages", "les voyages", [("en tren", "en train"), ("en avión", "en avion"), ("en coche", "en voiture")]),
            L("Projets", "les projets", [("viajar", "voyager"), ("estudiar", "étudier"), ("trabajar", "travailler")]),
            L("Invitations", "les invitations", [("salir", "sortir"), ("cenar", "dîner"), ("ver una película", "regarder un film")]),
            L("Descriptions", "les descriptions", [("el proyecto", "le projet"), ("el plan", "le plan"), ("interesante", "intéressant")]),
            L("Problèmes et solutions", "les problèmes", [("un problema", "un problème"), ("una pregunta", "une question"), ("una solución", "une solution")]),
            L("Évaluation", "la révision", [("un libro", "un livre"), ("una mochila", "un sac à dos"), ("un bolígrafo", "un stylo")]),
        ],
    ),
    (
        "Consolidation A1",
        [
            L("Présentations", "les présentations", [("Me llamo Ana", "Je m'appelle Ana"), ("Soy de España", "Je viens d'Espagne"), ("Tengo veinte años", "J'ai vingt ans")]),
            L("Ma famille", "la famille", [("Tengo un hermano", "J'ai un frère"), ("Mi madre es profesora", "Ma mère est professeure"), ("Mi padre trabaja", "Mon père travaille")]),
            L("Ma routine", "la routine", [("Me levanto a las siete", "Je me réveille à sept heures"), ("Desayuno a las ocho", "Je prends le petit-déjeuner à huit heures"), ("Me acuesto a las diez", "Je me couche à dix heures")]),
            L("Ma maison", "la maison", [("Vivo en un piso", "Je vis dans un appartement"), ("Hay tres habitaciones", "Il y a trois chambres"), ("Mi cocina es pequeña", "Ma cuisine est petite")]),
            L("Ma ville", "la ville", [("Vivo en Madrid", "Je vis à Madrid"), ("Hay un parque", "Il y a un parc"), ("El transporte es bueno", "Le transport est bon")]),
            L("Nourriture", "la nourriture", [("Me gusta la pizza", "J'aime la pizza"), ("Como fruta", "Je mange des fruits"), ("Bebo agua", "Je bois de l'eau")]),
            L("Travail", "le travail", [("Trabajo en una oficina", "Je travaille dans un bureau"), ("Mi jefe es simpático", "Mon chef est sympathique"), ("Tengo una reunión", "J'ai une réunion")]),
            L("Voyages", "les voyages", [("Viajo en tren", "Je voyage en train"), ("El hotel es bonito", "L'hôtel est beau"), ("Tengo una maleta", "J'ai une valise")]),
            L("Temps libre", "le temps libre", [("Me gusta leer", "J'aime lire"), ("Veo una película", "Je regarde un film"), ("Salgo con amigos", "Je sors avec des amis")]),
            L("Évaluation finale", "la révision", [("Hablo inglés", "Je parle anglais"), ("Aprendo portugués", "J'apprends le portugais"), ("Quiero viajar", "Je veux voyager")]),
        ],
    ),
]


def tokens(text: str) -> list[str]:
    return re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text)


def cap(text: str) -> str:
    return text[:1].upper() + text[1:] if text else text


def a_contract(phrase: str) -> str:
    if phrase.startswith("le "):
        return "au " + phrase[3:]
    if phrase.startswith("la "):
        return "à la " + phrase[3:]
    if phrase.startswith("l'"):
        return "à " + phrase
    if phrase.startswith("les "):
        return "aux " + phrase[4:]
    return "à " + phrase


def de_contract(phrase: str) -> str:
    if phrase.startswith("un "):
        return "d'un " + phrase[3:]
    if phrase.startswith("une "):
        return "d'une " + phrase[4:]
    if phrase.startswith("le "):
        return "du " + phrase[3:]
    if phrase.startswith("la "):
        return "de la " + phrase[3:]
    if phrase.startswith("l'"):
        return "de " + phrase
    if phrase.startswith("les "):
        return "des " + phrase[4:]
    if phrase[:1].lower() in "aeiouéèêàâîïôûù":
        return "d'" + phrase
    return "de " + phrase


def article(word: str) -> str:
    return "an" if word[:1].lower() in "aeiou" else "a"


def fr_sentences(pattern: str, items: list[tuple[str, str]]) -> list[str]:
    fr = [item[1] for item in items]
    if pattern == "say":
        return [f"Je dis {fr[0]}", f"Je dis {fr[1]}", f"Je dis {fr[2]}"]
    if pattern == "number":
        def number(value: str) -> str:
            if value == "un":
                return "J'ai un livre"
            return f"J'ai {value} livres"
        return [number(fr[0]), number(fr[1]), number(fr[2])]
    if pattern == "age":
        return [f"J'ai {fr[0]} ans", f"J'ai {fr[1]} ans", f"J'ai {fr[2]} ans"]
    if pattern == "color":
        return [f"La voiture est {fr[0]}", f"La voiture est {fr[1]}", f"La voiture est {fr[2]}"]
    if pattern == "classroom":
        return [f"C'est {fr[0]}", f"C'est {fr[1]}", f"C'est {fr[2]}"]
    if pattern == "family":
        return [f"J'ai {fr[0]}", f"J'ai {fr[1]}", f"J'ai {fr[2]}"]
    if pattern == "job":
        return [f"Je suis {fr[0]}", f"Je suis {fr[1]}", f"Je suis {fr[2]}"]
    if pattern == "have":
        return [f"J'ai {fr[0]}", f"Je veux {fr[1]}", f"J'ai besoin {de_contract(fr[2])}"]
    if pattern == "like":
        return [f"J'aime {fr[0]}", f"J'aime {fr[1]}", f"J'aime {fr[2]}"]
    if pattern == "place":
        return [f"Je suis {a_contract(fr[0])}", f"Je suis {a_contract(fr[1])}", f"Je suis {a_contract(fr[2])}"]
    if pattern == "adjective":
        return [f"{fr[0]} est {fr[2]}", f"{fr[1]} est {fr[2]}", f"J'aime {fr[0]}"]
    if pattern == "eat":
        return [f"Je mange {fr[0]}", f"Je mange {fr[1]}", f"Je mange {fr[2]}"]
    if pattern == "drink":
        return [f"Je bois {fr[0]}", f"Je bois {fr[1]}", f"Je bois {fr[2]}"]
    if pattern == "want":
        return [f"Je veux {fr[0]}", f"Je veux {fr[1]}", f"Je veux {fr[2]}"]
    if pattern == "need":
        return [f"J'ai besoin {de_contract(fr[0])}", f"J'ai besoin {de_contract(fr[1])}", f"J'ai besoin {de_contract(fr[2])}"]
    if pattern == "buy":
        return [f"J'achète {fr[0]}", f"J'achète {fr[1]}", f"J'achète {fr[2]}"]
    if pattern == "see":
        return [f"Je vois {fr[0]}", f"Je vois {fr[1]}", f"Je vois {fr[2]}"]
    if pattern == "verb":
        return [f"Je peux {fr[0]}", f"Je veux {fr[1]}", f"Je dois {fr[2]}"]
    if pattern == "hobby":
        return [f"J'aime {fr[0]}", f"J'aime {fr[1]}", f"J'aime {fr[2]}"]
    if pattern == "routine":
        return [f"{cap(fr[0])} à sept heures", f"{cap(fr[1])} à huit heures", f"{cap(fr[2])} à dix heures"]
    if pattern == "health":
        return [f"J'ai {fr[0]}", f"J'ai {fr[1]}", f"J'ai {fr[2]}"]
    if pattern == "body":
        return [f"Touche {fr[0]}", f"Touche {fr[1]}", f"Touche {fr[2]}"]
    if pattern == "transport":
        return [f"Je vais {fr[0]}", f"Je vais {fr[1]}", f"Je vais {fr[2]}"]
    if pattern == "direction":
        return [f"Va {fr[0]}", f"Va {fr[1]}", f"Va {fr[2]}"]
    if pattern == "sport":
        return [f"Je joue {fr[0]}", f"Je joue {fr[1]}", f"Je joue {fr[2]}"]
    if pattern == "music":
        return [f"J'écoute {fr[0]}", f"J'écoute {fr[1]}", f"J'écoute {fr[2]}"]
    if pattern == "movie":
        return [f"Je regarde {fr[0]}", f"Je regarde {fr[1]}", f"Je regarde {fr[2]}"]
    if pattern == "invitation":
        return [f"Tu veux {fr[0]} ?", f"Tu veux {fr[1]} ?", f"Tu veux {fr[2]} ?"]
    if pattern == "time":
        return [f"Il est {fr[0]}", f"Il est {fr[1]}", f"Il est {fr[2]}"]
    if pattern == "price":
        return [f"Ça coûte {fr[0]}", f"Ça coûte {fr[1]}", f"Ça coûte {fr[2]}"]
    if pattern == "weather":
        return [f"Aujourd'hui {fr[0]}", f"Aujourd'hui {fr[1]}", f"Aujourd'hui {fr[2]}"]
    if pattern == "weather2":
        return [f"Aujourd'hui {fr[0]}", f"Aujourd'hui {fr[1]}", f"Aujourd'hui {fr[2]}"]
    if pattern == "season":
        return [f"{cap(fr[0])} il fait beau", f"{cap(fr[1])} il fait beau", f"{cap(fr[2])} il fait beau"]
    if pattern == "message":
        return [f"J'envoie {fr[0]}", f"J'envoie {fr[1]}", f"J'envoie {fr[2]}"]
    if pattern == "problem":
        return [f"J'ai {fr[0]}", f"J'ai {fr[1]}", f"J'ai {fr[2]}"]
    if pattern == "want_verb":
        return [f"Je veux {fr[0]}", f"Je veux {fr[1]}", f"Je veux {fr[2]}"]
    if pattern == "weekend":
        return [f"{cap(fr[0])} le samedi", f"{cap(fr[1])} le samedi", f"{cap(fr[2])} le samedi"]
    if pattern == "phrase":
        return [cap(fr[0]), cap(fr[1]), cap(fr[2])]
    raise ValueError(f"Patrón desconocido: {pattern}")


def safe_distractors(correct: list[str], candidates: list[list[str]]) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    for fallback in ["très", "aujourd'hui", "ici", "maintenant", "aussi", "toujours"]:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def build_lesson(block: int, index: int, spec: dict) -> dict:
    lesson_id = f"{COURSE_ID}-a1-u{block:02d}-l{index:02d}"
    es_spec = F1_1.BLOCKS[block - 1][1][index - 1]
    pattern = es_spec["pattern"]
    es_items = es_spec["items"]
    fr_items = spec["items"]
    if pattern == "weather":
        es_sentences = ["Hoy está soleado", "Hoy está nublado", "Hoy está lluvioso"]
    elif pattern == "weather2":
        es_sentences = ["Hoy hace calor", "Hoy hace frío", "Hoy hace viento"]
    else:
        es_pairs = F1_1.sentences_for(pattern, es_items)
        es_sentences = [pair[0] for pair in es_pairs]
    fr_sentences_text = fr_sentences(pattern, fr_items)
    fr_tokens = [tokens(text) for text in fr_sentences_text]
    first_es, first_fr = fr_items[0]
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": f"Puedo usar frases sencillas sobre {spec['topic']} en francés.",
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en francés.",
            ],
            "sections": [
                {
                    "heading": "Mots et phrases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las palabras nuevas.",
                    "examples": [
                        {"source": es_sentences[0], "target": fr_sentences_text[0]},
                        {"source": es_sentences[1], "target": fr_sentences_text[1]},
                    ],
                }
            ],
        },
        "glossary": [{"term": fr, "translations": [es]} for es, fr in fr_items],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}» en francés?",
                "options": [fr for _, fr in fr_items],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_es}» se dice «{first_fr}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": es_sentences[0],
                "correctTokens": fr_tokens[0],
                "distractorTokens": safe_distractors(fr_tokens[0], [fr_tokens[1], fr_tokens[2]]),
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"El orden correcto es «{' '.join(fr_tokens[0])}».",
                },
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (fr_tokens[1][1:] + fr_tokens[1][:1]) if len(fr_tokens[1]) > 1 else fr_tokens[1],
                "answerTokens": fr_tokens[1],
                "sourcePhrase": es_sentences[1],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{' '.join(fr_tokens[1])}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [{"left": es, "right": fr} for es, fr in fr_items],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": fr_sentences_text[2],
                "options": fr_sentences_text,
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "comprehension",
                    "explanation": f"El audio dice «{fr_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": fr_sentences_text[2],
                "transcription": fr_sentences_text[2],
                "feedbackOnError": {
                    "type": "spelling",
                    "explanation": f"Escribe exactamente «{fr_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": fr_sentences_text[2],
                "audioText": fr_sentences_text[2],
                "feedbackOnError": {
                    "type": "pronunciation",
                    "explanation": f"Repite «{fr_sentences_text[2]}» con claridad.",
                },
                "feedbackOnSuccess": {"explanation": "La frase se entiende y mantiene el ritmo."},
            },
        ],
    }


def self_check(lessons: list[dict]) -> None:
    assert len(lessons) == 150, f"se esperaban 150 lecciones, hay {len(lessons)}"
    seen_titles = set()
    for lesson in lessons:
        assert len(lesson["exercises"]) == 7, f"{lesson['id']}: se esperaban 7 ejercicios"
        assert lesson.get("grammarPoints") is None, f"{lesson['id']}: A1 no declara gramática"
        assert lesson["title"] not in seen_titles, f"título duplicado: {lesson['title']}"
        seen_titles.add(lesson["title"])
        for exercise in lesson["exercises"]:
            texts: list[str] = []
            if exercise["type"] == "Translate":
                texts.append(" ".join(exercise["correctTokens"]))
            elif exercise["type"] == "OrderWords":
                texts.append(" ".join(exercise["answerTokens"]))
            elif exercise["type"] == "ListenType":
                texts.append(exercise["transcription"])
            elif exercise["type"] == "SpeakRepeat":
                texts.append(exercise["targetPhrase"])
            elif exercise["type"] == "ListenChoose":
                texts.append(exercise["audioText"])
            for text in texts:
                assert len(text.split()) <= 8, f"{lesson['id']}/{exercise['id']}: frase larga ({text})"
            if exercise["type"] == "Translate":
                overlap = set(exercise["correctTokens"]).intersection(exercise["distractorTokens"])
                assert not overlap, f"{lesson['id']}: distractor repetido {overlap}"


def find_insert_bracket(text: str) -> int:
    level_match = re.search(r'\n[ \t]*\{\n[ \t]*"id": "' + re.escape(NEXT_LEVEL) + '"', text)
    if level_match is None:
        raise SystemExit(f"No se encontró el nivel {NEXT_LEVEL}")
    bracket = None
    for match in re.finditer(r"\n([ \t]*)\]", text[: level_match.start()]):
        bracket = match
    if bracket is None:
        raise SystemExit("No se encontró el cierre de units de A1")
    return bracket.start(), len(bracket.group(1)) + 2


def indent_block(text: str, spaces: int) -> str:
    prefix = " " * spaces
    return "\n".join(prefix + line for line in text.splitlines())


def main() -> None:
    text = COURSE.read_text(encoding="utf-8")
    if f"{COURSE_ID}-a1-u01" in text:
        print("F1.3 ya existe; no se modifica.")
        return
    lessons = []
    units = []
    for block_number, (block_title, block_lessons) in enumerate(FR_BLOCKS, start=1):
        block = [
            build_lesson(block_number, lesson_number, spec)
            for lesson_number, spec in enumerate(block_lessons, start=1)
        ]
        lessons.extend(block)
        units.append(
            {
                "id": f"{COURSE_ID}-a1-u{block_number:02d}",
                "title": f"A1 · {block_title}",
                "lessons": block,
            }
        )
    title_counts = Counter(lesson["title"] for lesson in lessons)
    for unit in units:
        block_title = unit["title"].removeprefix("A1 · ")
        for lesson in unit["lessons"]:
            if title_counts[lesson["title"]] > 1:
                lesson["title"] = f"{lesson['title']} · {block_title}"
                lesson["microLesson"]["title"] = lesson["title"]
    self_check(lessons)
    bracket_start, indent = find_insert_bracket(text)
    units_text = ",\n".join(
        indent_block(json.dumps(unit, ensure_ascii=False, indent=2), indent) for unit in units
    )
    text = text[:bracket_start] + ",\n" + units_text + text[bracket_start:]
    COURSE.write_text(text, encoding="utf-8", newline="\n")
    json.loads(COURSE.read_text(encoding="utf-8"))
    print(f"Insertados 15 bloques con {len(lessons)} lecciones y {len(lessons) * 7} ejercicios.")


if __name__ == "__main__":
    main()
