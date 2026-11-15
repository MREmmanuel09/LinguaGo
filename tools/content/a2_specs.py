"""Currículo A2 de LinguaGo (F2).

Cada bloque contiene 10 lecciones y cada lección tres pares de vocabulario
(es, en, fr). Los patrones de frase viven en `a2_factory.py`; las lecciones
que necesitan una estructura propia declaran `sentences` explícitas.
"""

from __future__ import annotations


def L(
    title: str,
    topic: str,
    can_do: str,
    grammar: list[str],
    pattern: str,
    items: list[tuple[str, str, str]],
    sentences: list[tuple[str, str, str]] | None = None,
) -> dict:
    spec = {
        "title": title,
        "topic": topic,
        "can_do": can_do,
        "grammar": grammar,
        "pattern": pattern,
        "items": items,
    }
    if sentences is not None:
        spec["sentences"] = sentences
    return spec


BLOCKS: list[tuple[str, list[dict]]] = [
    (
        "Recuerdos y pasado",
        [
            L(
                "Repaso: mi rutina",
                "la rutina diaria",
                "Puedo describir mi rutina y decir a qué hora hago cada cosa.",
                ["present-simple", "adverbs-frequency"],
                "routine",
                [
                    (
                        "me levanto a las siete",
                        "get up at seven",
                        "je me lève à sept heures",
                    ),
                    (
                        "desayuno con mi familia",
                        "have breakfast with my family",
                        "je prends le petit-déjeuner avec ma famille",
                    ),
                    (
                        "trabajo desde casa",
                        "work from home",
                        "je travaille depuis la maison",
                    ),
                ],
            ),
            L(
                "Un día ocupado",
                "un día de trabajo",
                "Puedo contar cómo es un día normal y en qué momento ocurre cada cosa.",
                ["present-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "Por la mañana voy al trabajo en autobús.",
                        "In the morning I go to work by bus.",
                        "Le matin, je vais au travail en bus.",
                    ),
                    (
                        "Por la tarde tengo una reunión.",
                        "In the afternoon I have a meeting.",
                        "L'après-midi, j'ai une réunion.",
                    ),
                    (
                        "Por la noche ceno con mis amigos.",
                        "In the evening I have dinner with my friends.",
                        "Le soir, je dîne avec mes amis.",
                    ),
                ],
            ),
            L(
                "Verbos de rutina",
                "los objetos de la rutina",
                "Puedo nombrar objetos y acciones que necesito cada día.",
                ["present-simple"],
                "need",
                [
                    ("un despertador", "an alarm clock", "un réveil"),
                    ("un billete de autobús", "a bus ticket", "un ticket de bus"),
                    ("una ducha caliente", "a hot shower", "une douche chaude"),
                ],
            ),
            L(
                "Pasado simple regular",
                "las acciones de ayer",
                "Puedo contar acciones terminadas que hice ayer.",
                ["past-simple"],
                "past",
                [
                    (
                        "visité a mi familia",
                        "visited my family",
                        "j'ai visité ma famille",
                    ),
                    (
                        "caminé por el parque",
                        "walked in the park",
                        "je me suis promené dans le parc",
                    ),
                    (
                        "cené en un restaurante",
                        "had dinner in a restaurant",
                        "j'ai dîné dans un restaurant",
                    ),
                ],
            ),
            L(
                "Pasado simple irregular",
                "los recuerdos del fin de semana",
                "Puedo usar verbos irregulares para contar recuerdos.",
                ["past-irregular"],
                "past",
                [
                    ("fui al mercado", "went to the market", "je suis allé au marché"),
                    ("hice la compra", "did the shopping", "j'ai fait les courses"),
                    ("vi a mis amigos", "saw my friends", "j'ai vu mes amis"),
                ],
            ),
            L(
                "Contar el fin de semana",
                "el fin de semana",
                "Puedo contar lo que hice el fin de semana.",
                ["past-simple", "past-irregular"],
                "weekend",
                [
                    (
                        "jugué al fútbol con mis amigos",
                        "played football with my friends",
                        "j'ai joué au football avec mes amis",
                    ),
                    (
                        "leí un libro en casa",
                        "read a book at home",
                        "j'ai lu un livre à la maison",
                    ),
                    (
                        "escuché música en el parque",
                        "listened to music in the park",
                        "j'ai écouté de la musique au parc",
                    ),
                ],
            ),
            L(
                "Escuchar: un viaje corto",
                "un viaje corto",
                "Puedo entender y describir cómo fue un viaje.",
                ["past-simple", "adjectives"],
                "trip",
                [
                    ("fue tranquilo", "was quiet", "était tranquille"),
                    ("fue interesante", "was interesting", "était intéressant"),
                    ("fue un poco largo", "was a bit long", "était un peu long"),
                ],
            ),
            L(
                "Escribir una postal",
                "una postal de vacaciones",
                "Puedo escribir una postal sencilla desde un lugar de vacaciones.",
                ["present-simple", "prepositions"],
                "postcard",
                [
                    ("en la playa", "at the beach", "à la plage"),
                    ("en un hotel pequeño", "in a small hotel", "dans un petit hôtel"),
                    ("con mis amigos", "with my friends", "avec mes amis"),
                ],
            ),
            L(
                "Integración: ayer y hoy",
                "los cambios en mi vida",
                "Puedo comparar lo que hacía antes con lo que hago ahora.",
                ["used-to", "present-simple"],
                "phrase",
                [
                    (
                        "Antes iba al trabajo en autobús; ahora voy en bicicleta.",
                        "I used to go to work by bus; now I cycle.",
                        "Avant, j'allais au travail en bus ; maintenant, je vais à vélo.",
                    ),
                    (
                        "Antes comía en casa; ahora como en la oficina.",
                        "I used to eat at home; now I eat at the office.",
                        "Avant, je mangeais à la maison ; maintenant, je mange au bureau.",
                    ),
                    (
                        "Antes veía la televisión; ahora leo un libro.",
                        "I used to watch TV; now I read a book.",
                        "Avant, je regardais la télévision ; maintenant, je lis un livre.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi semana",
                "mi semana",
                "Puedo contar lo que hice durante la semana.",
                ["past-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "El lunes empecé un curso de inglés.",
                        "On Monday I started an English course.",
                        "Lundi, j'ai commencé un cours d'anglais.",
                    ),
                    (
                        "El miércoles visité a mi familia.",
                        "On Wednesday I visited my family.",
                        "Mercredi, j'ai rendu visite à ma famille.",
                    ),
                    (
                        "El viernes terminé un proyecto.",
                        "On Friday I finished a project.",
                        "Vendredi, j'ai terminé un projet.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Viajes y transporte",
        [
            L(
                "Repaso: mi último viaje",
                "mi último viaje",
                "Puedo contar las etapas de mi último viaje.",
                ["past-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "El verano pasado viajé a Lisboa.",
                        "Last summer I travelled to Lisbon.",
                        "L'été dernier, j'ai voyagé à Lisbonne.",
                    ),
                    (
                        "Pasé cinco días en un hotel pequeño.",
                        "I spent five days in a small hotel.",
                        "J'ai passé cinq jours dans un petit hôtel.",
                    ),
                    (
                        "Visité el centro y probé la comida local.",
                        "I visited the centre and tried the local food.",
                        "J'ai visité le centre et goûté la cuisine locale.",
                    ),
                ],
            ),
            L(
                "En la estación",
                "la estación de tren",
                "Puedo preguntar horarios y comprar billetes en la estación.",
                ["present-simple", "questions"],
                "phrase",
                [
                    (
                        "¿A qué hora sale el tren?",
                        "What time does the train leave?",
                        "À quelle heure part le train ?",
                    ),
                    (
                        "El andén está al final del pasillo.",
                        "The platform is at the end of the corridor.",
                        "Le quai est au bout du couloir.",
                    ),
                    (
                        "Necesito comprar un billete de ida y vuelta.",
                        "I need to buy a return ticket.",
                        "J'ai besoin d'acheter un billet aller-retour.",
                    ),
                ],
            ),
            L(
                "Alojamiento y billetes",
                "el alojamiento",
                "Puedo pedir alojamiento y billetes para un viaje.",
                ["quantity", "questions"],
                "need",
                [
                    ("una habitación doble", "a double room", "une chambre double"),
                    (
                        "una reserva para dos noches",
                        "a reservation for two nights",
                        "une réservation pour deux nuits",
                    ),
                    ("un billete de tren", "a train ticket", "un billet de train"),
                ],
            ),
            L(
                "Pasado continuo en un viaje",
                "acciones en desarrollo",
                "Puedo describir qué estaba pasando en un momento del pasado.",
                ["past-continuous"],
                "phrase",
                [
                    (
                        "Estaba esperando el tren cuando llegó Ana.",
                        "I was waiting for the train when Ana arrived.",
                        "J'attendais le train quand Ana est arrivée.",
                    ),
                    (
                        "Estábamos hablando cuando sonó el teléfono.",
                        "We were talking when the phone rang.",
                        "Nous parlions quand le téléphone a sonné.",
                    ),
                    (
                        "Estaba lloviendo cuando salimos del hotel.",
                        "It was raining when we left the hotel.",
                        "Il pleuvait quand nous sommes sortis de l'hôtel.",
                    ),
                ],
            ),
            L(
                "Pasado simple y continuo",
                "una anécdota de viaje",
                "Puedo combinar el pasado continuo y el simple para contar una anécdota.",
                ["past-simple-vs-continuous"],
                "phrase",
                [
                    (
                        "Mientras esperaba, vi un anuncio.",
                        "While I was waiting, I saw an announcement.",
                        "Pendant que j'attendais, j'ai vu une annonce.",
                    ),
                    (
                        "Cuando llegamos, el hotel estaba cerrado.",
                        "When we arrived, the hotel was closed.",
                        "Quand nous sommes arrivés, l'hôtel était fermé.",
                    ),
                    (
                        "Mientras dormía, perdí el billete.",
                        "While I was sleeping, I lost the ticket.",
                        "Pendant que je dormais, j'ai perdu le billet.",
                    ),
                ],
            ),
            L(
                "Cambiar un billete",
                "cambiar un billete",
                "Puedo pedir cortésmente un cambio de billete o de asiento.",
                ["polite-requests"],
                "polite",
                [
                    ("cambiar mi billete", "change my ticket", "changer mon billet"),
                    ("reservar otro asiento", "book another seat", "réserver une autre place"),
                    ("llamarme por teléfono", "call me", "m'appeler"),
                ],
            ),
            L(
                "Anuncio en el aeropuerto",
                "un anuncio del aeropuerto",
                "Puedo entender anuncios sencillos en un aeropuerto.",
                ["present-simple", "questions"],
                "phrase",
                [
                    (
                        "El vuelo a Roma sale a las nueve.",
                        "The flight to Rome leaves at nine.",
                        "Le vol de Rome part à neuf heures.",
                    ),
                    (
                        "Los pasajeros deben ir a la puerta doce.",
                        "Passengers must go to gate twelve.",
                        "Les passagers doivent aller à la porte douze.",
                    ),
                    (
                        "El vuelo lleva veinte minutos de retraso.",
                        "The flight is twenty minutes late.",
                        "Le vol a vingt minutes de retard.",
                    ),
                ],
            ),
            L(
                "Contar un viaje",
                "un viaje inolvidable",
                "Puedo contar experiencias de un viaje inolvidable.",
                ["past-simple", "past-irregular"],
                "past",
                [
                    (
                        "viajé en tren a la costa",
                        "travelled by train to the coast",
                        "j'ai voyagé en train jusqu'à la côte",
                    ),
                    (
                        "conocí a gente muy amable",
                        "met very friendly people",
                        "j'ai rencontré des gens très gentils",
                    ),
                    (
                        "probé un plato típico",
                        "tried a typical dish",
                        "j'ai goûté un plat typique",
                    ),
                ],
            ),
            L(
                "Integración: incidencias",
                "las incidencias de un viaje",
                "Puedo resolver incidencias habituales durante un viaje.",
                ["past-simple-vs-continuous", "polite-requests"],
                "phrase",
                [
                    (
                        "Perdí mi maleta y fui a reclamarla.",
                        "I lost my suitcase and went to report it.",
                        "J'ai perdu ma valise et je suis allé la signaler.",
                    ),
                    (
                        "El vuelo se canceló y cambié el billete.",
                        "The flight was cancelled and I changed my ticket.",
                        "Le vol a été annulé et j'ai changé mon billet.",
                    ),
                    (
                        "El hotel estaba lleno y busqué otro.",
                        "The hotel was full and I looked for another one.",
                        "L'hôtel était complet et j'en ai cherché un autre.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi último viaje",
                "mi último viaje",
                "Puedo narrar mi último viaje con detalle y orden.",
                ["past-simple", "past-continuous"],
                "phrase",
                [
                    (
                        "El año pasado viajé a México con mi familia.",
                        "Last year I travelled to Mexico with my family.",
                        "L'année dernière, j'ai voyagé au Mexique avec ma famille.",
                    ),
                    (
                        "Mientras visitábamos la ciudad, empezó a llover.",
                        "While we were visiting the city, it started to rain.",
                        "Pendant que nous visitions la ville, il a commencé à pleuvoir.",
                    ),
                    (
                        "Volvimos a casa cansados pero contentos.",
                        "We came home tired but happy.",
                        "Nous sommes rentrés fatigués mais heureux.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Planes y futuro",
        [
            L(
                "Repaso: viajes",
                "los viajes recientes",
                "Puedo recordar y contar un viaje reciente.",
                ["past-simple", "past-irregular"],
                "phrase",
                [
                    (
                        "El verano pasado visité a mis primos.",
                        "Last summer I visited my cousins.",
                        "L'été dernier, j'ai visité mes cousins.",
                    ),
                    (
                        "Hicimos una excursión a la montaña.",
                        "We went on a trip to the mountains.",
                        "Nous avons fait une excursion à la montagne.",
                    ),
                    (
                        "Lo pasamos muy bien.",
                        "We had a great time.",
                        "Nous nous sommes bien amusés.",
                    ),
                ],
            ),
            L(
                "Planes para el fin de semana",
                "los planes del fin de semana",
                "Puedo hablar de mis planes para el fin de semana.",
                ["going-to"],
                "going_to",
                [
                    (
                        "visitar a mis abuelos",
                        "visit my grandparents",
                        "rendre visite à mes grands-parents",
                    ),
                    ("ir a la playa", "go to the beach", "aller à la plage"),
                    ("quedarme en casa", "stay at home", "rester à la maison"),
                ],
            ),
            L(
                "Actividades y quedadas",
                "las quedadas con amigos",
                "Puedo hablar de citas y actividades con amigos.",
                ["present-continuous-future"],
                "phrase",
                [
                    (
                        "El viernes quedo con Ana.",
                        "On Friday I'm meeting Ana.",
                        "Vendredi, je retrouve Ana.",
                    ),
                    (
                        "El sábado juego al tenis.",
                        "On Saturday I'm playing tennis.",
                        "Samedi, je joue au tennis.",
                    ),
                    (
                        "El domingo ceno fuera.",
                        "On Sunday I'm having dinner out.",
                        "Dimanche, je dîne dehors.",
                    ),
                ],
            ),
            L(
                "Going to",
                "los propósitos",
                "Puedo expresar intenciones y propósitos con going to.",
                ["going-to"],
                "going_to",
                [
                    ("estudiar inglés", "study English", "étudier l'anglais"),
                    ("hacer deporte", "do sport", "faire du sport"),
                    ("leer más libros", "read more books", "lire plus de livres"),
                ],
            ),
            L(
                "Presente continuo para el futuro",
                "las citas cerradas",
                "Puedo usar el presente continuo para citas y planes cerrados.",
                ["present-continuous-future"],
                "phrase",
                [
                    (
                        "El viernes veo a Ana.",
                        "On Friday I'm seeing Ana.",
                        "Vendredi, je vois Ana.",
                    ),
                    (
                        "El sábado juego al tenis.",
                        "On Saturday I'm playing tennis.",
                        "Samedi, je joue au tennis.",
                    ),
                    (
                        "El domingo ceno con mi familia.",
                        "On Sunday I'm having dinner with my family.",
                        "Dimanche, je dîne avec ma famille.",
                    ),
                ],
            ),
            L(
                "Invitar y aceptar",
                "las invitaciones",
                "Puedo invitar a alguien y aceptar o rechazar un plan.",
                ["polite-requests", "questions"],
                "phrase",
                [
                    (
                        "¿Quieres venir al cine conmigo?",
                        "Do you want to come to the cinema with me?",
                        "Tu veux venir au cinéma avec moi ?",
                    ),
                    (
                        "Me encantaría, gracias.",
                        "I'd love to, thanks.",
                        "J'aimerais beaucoup, merci.",
                    ),
                    (
                        "Lo siento, no puedo.",
                        "Sorry, I can't.",
                        "Désolé, je ne peux pas.",
                    ),
                ],
            ),
            L(
                "Escuchar planes",
                "los planes de un amigo",
                "Puedo entender los planes de otra persona.",
                ["going-to", "time-expressions"],
                "phrase",
                [
                    (
                        "El sábado por la mañana voy al mercado.",
                        "On Saturday morning I'm going to the market.",
                        "Samedi matin, je vais au marché.",
                    ),
                    (
                        "Por la tarde voy a visitar a mis padres.",
                        "In the afternoon I'm going to visit my parents.",
                        "L'après-midi, je vais rendre visite à mes parents.",
                    ),
                    (
                        "Por la noche voy a descansar en casa.",
                        "In the evening I'm going to rest at home.",
                        "Le soir, je vais me reposer à la maison.",
                    ),
                ],
            ),
            L(
                "Escribir un plan",
                "mi plan para el fin de semana",
                "Puedo escribir un plan sencillo para el fin de semana.",
                ["going-to"],
                "phrase",
                [
                    (
                        "Este fin de semana voy a ordenar mi casa.",
                        "This weekend I'm going to tidy my house.",
                        "Ce week-end, je vais ranger ma maison.",
                    ),
                    (
                        "El sábado voy a hacer la compra.",
                        "On Saturday I'm going to do the shopping.",
                        "Samedi, je vais faire les courses.",
                    ),
                    (
                        "El domingo voy a cocinar para mis amigos.",
                        "On Sunday I'm going to cook for my friends.",
                        "Dimanche, je vais cuisiner pour mes amis.",
                    ),
                ],
            ),
            L(
                "Integración: agenda",
                "mi agenda",
                "Puedo describir mi agenda y mis compromisos.",
                ["present-continuous-future", "time-expressions"],
                "phrase",
                [
                    (
                        "El lunes tengo una reunión a las diez.",
                        "On Monday I have a meeting at ten.",
                        "Lundi, j'ai une réunion à dix heures.",
                    ),
                    (
                        "El miércoles voy a ver a mi médico.",
                        "On Wednesday I'm going to see my doctor.",
                        "Mercredi, je vais voir mon médecin.",
                    ),
                    (
                        "El viernes voy a cenar con Ana.",
                        "On Friday I'm going to have dinner with Ana.",
                        "Vendredi, je vais dîner avec Ana.",
                    ),
                ],
            ),
            L(
                "Evaluación: mis planes",
                "mis planes de futuro",
                "Puedo hablar de mis planes para este año.",
                ["going-to", "will"],
                "phrase",
                [
                    (
                        "Este año voy a aprender a cocinar.",
                        "This year I'm going to learn to cook.",
                        "Cette année, je vais apprendre à cuisiner.",
                    ),
                    (
                        "En verano voy a viajar a Portugal.",
                        "In summer I'm going to travel to Portugal.",
                        "En été, je vais voyager au Portugal.",
                    ),
                    (
                        "También voy a leer más en inglés.",
                        "I'm also going to read more in English.",
                        "Je vais aussi lire davantage en anglais.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Ciudad y comparaciones",
        [
            L(
                "Repaso: planes",
                "los planes recientes",
                "Puedo recordar planes y citas de la semana.",
                ["going-to", "time-expressions"],
                "phrase",
                [
                    (
                        "El sábado voy a quedar con mis amigos.",
                        "On Saturday I'm going to meet my friends.",
                        "Samedi, je vais retrouver mes amis.",
                    ),
                    (
                        "El domingo voy a descansar en casa.",
                        "On Sunday I'm going to rest at home.",
                        "Dimanche, je vais me reposer à la maison.",
                    ),
                    (
                        "También voy a estudiar un poco.",
                        "I'm also going to study a little.",
                        "Je vais aussi étudier un peu.",
                    ),
                ],
            ),
            L(
                "Mi barrio",
                "mi barrio",
                "Puedo describir mi barrio y lo que hay cerca.",
                ["present-simple", "prepositions"],
                "phrase",
                [
                    (
                        "Mi barrio es tranquilo y tiene un parque.",
                        "My neighbourhood is quiet and has a park.",
                        "Mon quartier est calme et il y a un parc.",
                    ),
                    (
                        "Hay una panadería y una farmacia cerca.",
                        "There is a bakery and a pharmacy nearby.",
                        "Il y a une boulangerie et une pharmacie à proximité.",
                    ),
                    (
                        "Me gusta vivir aquí porque todo está cerca.",
                        "I like living here because everything is close.",
                        "J'aime vivre ici parce que tout est proche.",
                    ),
                ],
            ),
            L(
                "Servicios y lugares",
                "los servicios del barrio",
                "Puedo preguntar dónde están los servicios del barrio.",
                ["questions", "prepositions"],
                "question",
                [
                    ("la farmacia", "the pharmacy", "la pharmacie"),
                    ("la estación de tren", "the train station", "la gare"),
                    ("el supermercado", "the supermarket", "le supermarché"),
                ],
            ),
            L(
                "Comparativos",
                "las comparaciones",
                "Puedo comparar personas, lugares y cosas.",
                ["comparatives"],
                "phrase",
                [
                    (
                        "El tren es más rápido que el autobús.",
                        "The train is faster than the bus.",
                        "Le train est plus rapide que le bus.",
                    ),
                    (
                        "Mi barrio es más tranquilo que el centro.",
                        "My neighbourhood is quieter than the centre.",
                        "Mon quartier est plus calme que le centre.",
                    ),
                    (
                        "Este piso es menos caro que el otro.",
                        "This flat is less expensive than the other one.",
                        "Cet appartement est moins cher que l'autre.",
                    ),
                ],
            ),
            L(
                "Superlativos",
                "los superlativos",
                "Puedo destacar lo mejor y lo más característico de un lugar.",
                ["superlatives"],
                "phrase",
                [
                    (
                        "Es el restaurante más antiguo de la ciudad.",
                        "It is the oldest restaurant in the city.",
                        "C'est le restaurant le plus ancien de la ville.",
                    ),
                    (
                        "Es la mejor panadería del barrio.",
                        "It is the best bakery in the neighbourhood.",
                        "C'est la meilleure boulangerie du quartier.",
                    ),
                    (
                        "Es el parque más grande de la zona.",
                        "It is the biggest park in the area.",
                        "C'est le plus grand parc du quartier.",
                    ),
                ],
            ),
            L(
                "Elegir un barrio",
                "elegir dónde vivir",
                "Puedo comparar barrios y justificar una elección.",
                ["comparatives", "agree-disagree"],
                "phrase",
                [
                    (
                        "Creo que el centro es más divertido.",
                        "I think the centre is more fun.",
                        "Je pense que le centre est plus amusant.",
                    ),
                    (
                        "Pero mi barrio es más tranquilo.",
                        "But my neighbourhood is quieter.",
                        "Mais mon quartier est plus calme.",
                    ),
                    (
                        "Estoy de acuerdo: prefiero la tranquilidad.",
                        "I agree: I prefer the peace and quiet.",
                        "Je suis d'accord : je préfère le calme.",
                    ),
                ],
            ),
            L(
                "Anuncio inmobiliario",
                "un anuncio de piso",
                "Puedo entender y describir un anuncio de alquiler.",
                ["present-simple", "adjectives"],
                "phrase",
                [
                    (
                        "Piso luminoso de dos habitaciones.",
                        "Bright two-bedroom flat.",
                        "Appartement lumineux de deux chambres.",
                    ),
                    (
                        "Está cerca del metro y de los comercios.",
                        "It is close to the metro and shops.",
                        "Il est proche du métro et des commerces.",
                    ),
                    (
                        "El alquiler incluye agua y calefacción.",
                        "The rent includes water and heating.",
                        "Le loyer comprend l'eau et le chauffage.",
                    ),
                ],
            ),
            L(
                "Describir y comparar",
                "mi ciudad",
                "Puedo describir mi ciudad y compararla con otra.",
                ["comparatives", "superlatives"],
                "phrase",
                [
                    (
                        "Mi ciudad es más pequeña que Madrid.",
                        "My city is smaller than Madrid.",
                        "Ma ville est plus petite que Madrid.",
                    ),
                    (
                        "Pero tiene mejores transportes que antes.",
                        "But it has better transport than before.",
                        "Mais elle a de meilleurs transports qu'avant.",
                    ),
                    (
                        "Es la ciudad más cómoda para vivir.",
                        "It is the most comfortable city to live in.",
                        "C'est la ville la plus agréable à vivre.",
                    ),
                ],
            ),
            L(
                "Integración: mudanza",
                "una mudanza",
                "Puedo explicar una mudanza y comparar las opciones.",
                ["comparatives", "going-to"],
                "phrase",
                [
                    (
                        "Voy a mudarme a un barrio más tranquilo.",
                        "I'm going to move to a quieter neighbourhood.",
                        "Je vais déménager dans un quartier plus calme.",
                    ),
                    (
                        "El alquiler es más barato que el actual.",
                        "The rent is cheaper than the current one.",
                        "Le loyer est moins cher que l'actuel.",
                    ),
                    (
                        "Es la mejor decisión para mi familia.",
                        "It is the best decision for my family.",
                        "C'est la meilleure décision pour ma famille.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi ciudad ideal",
                "mi ciudad ideal",
                "Puedo describir y justificar cómo sería mi ciudad ideal.",
                ["comparatives", "superlatives"],
                "phrase",
                [
                    (
                        "Mi ciudad ideal es tranquila y tiene espacios verdes.",
                        "My ideal city is quiet and has green spaces.",
                        "Ma ville idéale est calme et a des espaces verts.",
                    ),
                    (
                        "La gente es más amable que en las ciudades grandes.",
                        "People are friendlier than in big cities.",
                        "Les gens sont plus sympathiques que dans les grandes villes.",
                    ),
                    (
                        "Es el mejor lugar para vivir.",
                        "It is the best place to live.",
                        "C'est le meilleur endroit pour vivre.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Salud y bienestar",
        [
            L(
                "Repaso: mi ciudad",
                "mi ciudad",
                "Puedo describir mi ciudad y compararla con otras.",
                ["comparatives", "superlatives"],
                "phrase",
                [
                    (
                        "Mi ciudad es tranquila y tiene un parque.",
                        "My city is quiet and has a park.",
                        "Ma ville est calme et il y a un parc.",
                    ),
                    (
                        "El transporte público es más barato que el coche.",
                        "Public transport is cheaper than the car.",
                        "Les transports publics sont moins chers que la voiture.",
                    ),
                    (
                        "Me gustaría vivir en un barrio así.",
                        "I would like to live in a neighbourhood like that.",
                        "J'aimerais vivre dans un quartier comme celui-là.",
                    ),
                ],
            ),
            L(
                "Síntomas y estados",
                "los síntomas",
                "Puedo describir síntomas y decir cómo me siento.",
                ["present-simple"],
                "phrase",
                [
                    (
                        "Tengo dolor de cabeza.",
                        "I have a headache.",
                        "J'ai mal à la tête.",
                    ),
                    (
                        "Me duele la garganta.",
                        "I have a sore throat.",
                        "J'ai mal à la gorge.",
                    ),
                    (
                        "Estoy muy cansado.",
                        "I am very tired.",
                        "Je suis très fatigué.",
                    ),
                ],
            ),
            L(
                "Remedios y farmacia",
                "la farmacia",
                "Puedo pedir remedios sencillos en la farmacia.",
                ["quantity"],
                "need",
                [
                    ("un analgésico", "a painkiller", "un antidouleur"),
                    (
                        "un jarabe para la tos",
                        "a cough syrup",
                        "un sirop pour la toux",
                    ),
                    ("una caja de tiritas", "a box of plasters", "une boîte de pansements"),
                ],
            ),
            L(
                "Should / shouldn't",
                "los consejos de salud",
                "Puedo dar consejos con should y shouldn't.",
                ["should"],
                "should",
                [
                    ("descansar más", "rest more", "te reposer davantage"),
                    ("beber más agua", "drink more water", "boire plus d'eau"),
                    ("dormir ocho horas", "sleep eight hours", "dormir huit heures"),
                ],
            ),
            L(
                "Have to / don't have to",
                "las obligaciones de salud",
                "Puedo expresar obligaciones y ausencia de obligación.",
                ["have-to"],
                "have_to",
                [
                    ("tomar la medicina", "take the medicine", "prendre le médicament"),
                    ("ir al médico", "go to the doctor", "aller chez le médecin"),
                    ("hacer ejercicio", "do exercise", "faire de l'exercice"),
                ],
            ),
            L(
                "En la consulta médica",
                "la consulta médica",
                "Puedo explicar un problema de salud en la consulta.",
                ["present-perfect", "questions"],
                "phrase",
                [
                    (
                        "Me duele el estómago desde ayer.",
                        "My stomach has hurt since yesterday.",
                        "J'ai mal au ventre depuis hier.",
                    ),
                    (
                        "¿Puedo tomar este medicamento?",
                        "Can I take this medicine?",
                        "Puis-je prendre ce médicament ?",
                    ),
                    (
                        "Tiene que descansar y beber agua.",
                        "You have to rest and drink water.",
                        "Vous devez vous reposer et boire de l'eau.",
                    ),
                ],
            ),
            L(
                "Consejos de salud",
                "los hábitos saludables",
                "Puedo dar consejos para llevar una vida más sana.",
                ["should"],
                "phrase",
                [
                    (
                        "Deberías caminar treinta minutos al día.",
                        "You should walk thirty minutes a day.",
                        "Tu devrais marcher trente minutes par jour.",
                    ),
                    (
                        "No deberías comer tanto azúcar.",
                        "You shouldn't eat so much sugar.",
                        "Tu ne devrais pas manger autant de sucre.",
                    ),
                    (
                        "Deberías dormir mejor.",
                        "You should sleep better.",
                        "Tu devrais mieux dormir.",
                    ),
                ],
            ),
            L(
                "Hábitos saludables",
                "mi rutina saludable",
                "Puedo describir mis hábitos saludables.",
                ["present-simple", "adverbs-frequency"],
                "phrase",
                [
                    (
                        "Desayuno fruta y cereales.",
                        "I have fruit and cereal for breakfast.",
                        "Je mange des fruits et des céréales au petit-déjeuner.",
                    ),
                    (
                        "Camino al trabajo todos los días.",
                        "I walk to work every day.",
                        "Je vais au travail à pied tous les jours.",
                    ),
                    (
                        "Duermo ocho horas cada noche.",
                        "I sleep eight hours every night.",
                        "Je dors huit heures chaque nuit.",
                    ),
                ],
            ),
            L(
                "Integración: plan de bienestar",
                "un plan de bienestar",
                "Puedo diseñar un plan sencillo para sentirme mejor.",
                ["going-to", "should"],
                "phrase",
                [
                    (
                        "Voy a hacer ejercicio tres veces por semana.",
                        "I'm going to exercise three times a week.",
                        "Je vais faire de l'exercice trois fois par semaine.",
                    ),
                    (
                        "También voy a beber más agua.",
                        "I'm also going to drink more water.",
                        "Je vais aussi boire plus d'eau.",
                    ),
                    (
                        "Y voy a dormir mejor.",
                        "And I'm going to sleep better.",
                        "Et je vais mieux dormir.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi salud",
                "mi salud",
                "Puedo hablar de mi salud y de mis propósitos.",
                ["present-perfect", "have-to"],
                "phrase",
                [
                    (
                        "Me siento mejor que antes.",
                        "I feel better than before.",
                        "Je me sens mieux qu'avant.",
                    ),
                    (
                        "He empezado a caminar cada día.",
                        "I have started walking every day.",
                        "J'ai commencé à marcher chaque jour.",
                    ),
                    (
                        "Todavía tengo que descansar más.",
                        "I still have to rest more.",
                        "Je dois encore me reposer davantage.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Compras y servicios",
        [
            L(
                "Repaso: salud",
                "los hábitos saludables",
                "Puedo recordar mis hábitos y mis propósitos de salud.",
                ["present-perfect", "present-simple"],
                "phrase",
                [
                    (
                        "He empezado a hacer ejercicio.",
                        "I have started exercising.",
                        "J'ai commencé à faire de l'exercice.",
                    ),
                    (
                        "Como más fruta y bebo más agua.",
                        "I eat more fruit and drink more water.",
                        "Je mange plus de fruits et je bois plus d'eau.",
                    ),
                    (
                        "Me siento mucho mejor.",
                        "I feel much better.",
                        "Je me sens beaucoup mieux.",
                    ),
                ],
            ),
            L(
                "En la tienda",
                "las compras de ropa",
                "Puedo preguntar precios y tallas en una tienda.",
                ["questions", "polite-requests"],
                "phrase",
                [
                    (
                        "¿Cuánto cuesta esta camiseta?",
                        "How much is this T-shirt?",
                        "Combien coûte ce T-shirt ?",
                    ),
                    (
                        "¿La tiene en talla mediana?",
                        "Do you have it in medium?",
                        "Vous l'avez en taille moyenne ?",
                    ),
                    (
                        "Me la llevo, gracias.",
                        "I'll take it, thanks.",
                        "Je la prends, merci.",
                    ),
                ],
            ),
            L(
                "Ropa y tallas",
                "la ropa",
                "Puedo pedir ropa y describir tallas y colores.",
                ["quantity", "adjectives"],
                "need",
                [
                    ("una camiseta azul", "a blue T-shirt", "un T-shirt bleu"),
                    ("unos pantalones negros", "black trousers", "un pantalon noir"),
                    ("una chaqueta ligera", "a light jacket", "une veste légère"),
                ],
            ),
            L(
                "Too much / too many",
                "las cantidades excesivas",
                "Puedo hablar de cantidades excesivas con too much y too many.",
                ["too-much"],
                "too_much",
                [
                    (
                        "tráfico en el centro",
                        "traffic in the centre",
                        "de trafic dans le centre",
                    ),
                    ("ruido por la noche", "noise at night", "de bruit la nuit"),
                    (
                        "contaminación en la ciudad",
                        "pollution in the city",
                        "de pollution dans la ville",
                    ),
                ],
            ),
            L(
                "Enough / not enough",
                "la cantidad suficiente",
                "Puedo expresar si hay cantidad suficiente o no.",
                ["enough"],
                "enough",
                [
                    (
                        "tiempo para descansar",
                        "time to rest",
                        "de temps pour se reposer",
                    ),
                    (
                        "dinero para un regalo",
                        "money for a present",
                        "d'argent pour un cadeau",
                    ),
                    (
                        "espacio en la maleta",
                        "space in the suitcase",
                        "de place dans la valise",
                    ),
                ],
            ),
            L(
                "Cambiar o devolver",
                "los cambios y devoluciones",
                "Puedo pedir un cambio o una devolución con cortesía.",
                ["polite-requests"],
                "polite",
                [
                    (
                        "cambiar esta camiseta",
                        "change this T-shirt",
                        "changer ce T-shirt",
                    ),
                    (
                        "devolver estos zapatos",
                        "return these shoes",
                        "rendre ces chaussures",
                    ),
                    ("darme un recibo", "give me a receipt", "me donner un reçu"),
                ],
            ),
            L(
                "Atención al cliente",
                "la atención al cliente",
                "Puedo explicar un problema a atención al cliente.",
                ["present-perfect", "polite-requests"],
                "phrase",
                [
                    (
                        "Hola, compré este producto ayer.",
                        "Hello, I bought this product yesterday.",
                        "Bonjour, j'ai acheté ce produit hier.",
                    ),
                    (
                        "No funciona y quiero cambiarlo.",
                        "It doesn't work and I want to change it.",
                        "Il ne fonctionne pas et je veux l'échanger.",
                    ),
                    (
                        "¿Puede ayudarme, por favor?",
                        "Can you help me, please?",
                        "Pouvez-vous m'aider, s'il vous plaît ?",
                    ),
                ],
            ),
            L(
                "Escribir una reclamación",
                "una reclamación",
                "Puedo escribir una reclamación sencilla.",
                ["present-perfect", "polite-requests"],
                "phrase",
                [
                    (
                        "Escribo para reclamar un pedido defectuoso.",
                        "I am writing to complain about a faulty order.",
                        "J'écris pour me plaindre d'une commande défectueuse.",
                    ),
                    (
                        "El producto llegó roto y faltan piezas.",
                        "The product arrived broken and parts are missing.",
                        "Le produit est arrivé cassé et il manque des pièces.",
                    ),
                    (
                        "Solicito un reemplazo o un reembolso.",
                        "I request a replacement or a refund.",
                        "Je demande un remplacement ou un remboursement.",
                    ),
                ],
            ),
            L(
                "Integración: compra online",
                "una compra online",
                "Puedo contar una compra online y resolver un problema.",
                ["past-simple", "polite-requests"],
                "phrase",
                [
                    (
                        "Compré una mochila en una tienda online.",
                        "I bought a backpack in an online shop.",
                        "J'ai acheté un sac à dos dans une boutique en ligne.",
                    ),
                    (
                        "La talla no era la correcta.",
                        "The size was not right.",
                        "La taille n'était pas la bonne.",
                    ),
                    (
                        "La devolví y pedí otra.",
                        "I returned it and ordered another one.",
                        "Je l'ai renvoyé et j'en ai commandé un autre.",
                    ),
                ],
            ),
            L(
                "Evaluación: una compra",
                "una compra reciente",
                "Puedo contar una compra y valorar el servicio.",
                ["past-simple", "adjectives"],
                "phrase",
                [
                    (
                        "Fui a la tienda a cambiar una camiseta.",
                        "I went to the shop to change a T-shirt.",
                        "Je suis allé au magasin pour changer un T-shirt.",
                    ),
                    (
                        "El dependiente fue muy amable.",
                        "The shop assistant was very kind.",
                        "Le vendeur a été très aimable.",
                    ),
                    (
                        "Al final compré una chaqueta más barata.",
                        "In the end I bought a cheaper jacket.",
                        "Finalement, j'ai acheté une veste moins chère.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Trabajo y estudios",
        [
            L(
                "Repaso: compras",
                "una compra reciente",
                "Puedo recordar una compra y una reclamación.",
                ["past-simple", "polite-requests"],
                "phrase",
                [
                    (
                        "Ayer fui a la tienda a cambiar una camiseta.",
                        "Yesterday I went to the shop to change a T-shirt.",
                        "Hier, je suis allé au magasin pour changer un T-shirt.",
                    ),
                    (
                        "El dependiente me ayudó mucho.",
                        "The shop assistant helped me a lot.",
                        "Le vendeur m'a beaucoup aidé.",
                    ),
                    (
                        "Al final compré una chaqueta.",
                        "In the end I bought a jacket.",
                        "Finalement, j'ai acheté une veste.",
                    ),
                ],
            ),
            L(
                "Mi trabajo y mis estudios",
                "el trabajo y los estudios",
                "Puedo describir mi trabajo o mis estudios.",
                ["present-simple"],
                "phrase",
                [
                    (
                        "Trabajo en una oficina pequeña.",
                        "I work in a small office.",
                        "Je travaille dans un petit bureau.",
                    ),
                    (
                        "Estudio inglés dos veces por semana.",
                        "I study English twice a week.",
                        "J'étudie l'anglais deux fois par semaine.",
                    ),
                    (
                        "Quiero mejorar mi currículum.",
                        "I want to improve my CV.",
                        "Je veux améliorer mon CV.",
                    ),
                ],
            ),
            L(
                "Tareas y responsabilidades",
                "las tareas del trabajo",
                "Puedo hablar de mis tareas y responsabilidades.",
                ["have-to"],
                "have_to",
                [
                    ("responder correos", "answer emails", "répondre aux e-mails"),
                    ("preparar reuniones", "prepare meetings", "préparer des réunions"),
                    ("hablar con clientes", "talk to clients", "parler aux clients"),
                ],
            ),
            L(
                "Present perfect",
                "las experiencias profesionales",
                "Puedo hablar de experiencias con el presente perfecto.",
                ["present-perfect"],
                "present_perfect",
                [
                    (
                        "trabajado en tres países",
                        "worked in three countries",
                        "travaillé dans trois pays",
                    ),
                    (
                        "estudiado inglés durante años",
                        "studied English for years",
                        "étudié l'anglais pendant des années",
                    ),
                    (
                        "aprendido a usar una nueva herramienta",
                        "learned to use a new tool",
                        "appris à utiliser un nouvel outil",
                    ),
                ],
            ),
            L(
                "For y since",
                "la duración",
                "Puedo expresar duración y punto de inicio con for y since.",
                ["for-since"],
                "phrase",
                [
                    (
                        "Vivo en Madrid desde 2019.",
                        "I have lived in Madrid since 2019.",
                        "Je vis à Madrid depuis 2019.",
                    ),
                    (
                        "Trabajo aquí desde hace tres años.",
                        "I have worked here for three years.",
                        "Je travaille ici depuis trois ans.",
                    ),
                    (
                        "Estudio inglés desde el año pasado.",
                        "I have studied English since last year.",
                        "J'étudie l'anglais depuis l'année dernière.",
                    ),
                ],
            ),
            L(
                "Entrevista",
                "una entrevista de trabajo",
                "Puedo responder preguntas sencillas en una entrevista.",
                ["present-perfect", "questions"],
                "phrase",
                [
                    (
                        "¿Cuál es su experiencia profesional?",
                        "What is your professional experience?",
                        "Quelle est votre expérience professionnelle ?",
                    ),
                    (
                        "He trabajado en atención al cliente.",
                        "I have worked in customer service.",
                        "J'ai travaillé dans le service client.",
                    ),
                    (
                        "¿Por qué quiere trabajar con nosotros?",
                        "Why do you want to work with us?",
                        "Pourquoi voulez-vous travailler avec nous ?",
                    ),
                ],
            ),
            L(
                "Oferta de trabajo",
                "una oferta de trabajo",
                "Puedo entender una oferta de trabajo sencilla.",
                ["present-simple", "questions"],
                "phrase",
                [
                    (
                        "La empresa busca un asistente administrativo.",
                        "The company is looking for an administrative assistant.",
                        "L'entreprise recherche un assistant administratif.",
                    ),
                    (
                        "El puesto es a tiempo completo.",
                        "The position is full-time.",
                        "Le poste est à temps plein.",
                    ),
                    (
                        "Ofrecen formación y horario flexible.",
                        "They offer training and flexible hours.",
                        "Ils offrent une formation et des horaires flexibles.",
                    ),
                ],
            ),
            L(
                "Correo profesional",
                "un correo profesional",
                "Puedo escribir un correo profesional breve.",
                ["present-simple", "polite-requests"],
                "phrase",
                [
                    (
                        "Escribo para solicitar una entrevista.",
                        "I am writing to request an interview.",
                        "J'écris pour demander un entretien.",
                    ),
                    (
                        "Adjunto mi currículum y una carta de presentación.",
                        "I attach my CV and a cover letter.",
                        "Je joins mon CV et une lettre de motivation.",
                    ),
                    (
                        "Quedo a su disposición para ampliar la información.",
                        "I am available to provide further information.",
                        "Je reste à votre disposition pour plus d'informations.",
                    ),
                ],
            ),
            L(
                "Integración: trayectoria",
                "mi trayectoria profesional",
                "Puedo contar mi trayectoria profesional.",
                ["past-simple", "present-perfect"],
                "phrase",
                [
                    (
                        "Empecé como becario en una empresa pequeña.",
                        "I started as an intern in a small company.",
                        "J'ai commencé comme stagiaire dans une petite entreprise.",
                    ),
                    (
                        "Después trabajé en varios proyectos internacionales.",
                        "Then I worked on several international projects.",
                        "Ensuite, j'ai travaillé sur plusieurs projets internationaux.",
                    ),
                    (
                        "Ahora quiero asumir más responsabilidades.",
                        "Now I want to take on more responsibilities.",
                        "Maintenant, je veux assumer plus de responsabilités.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi perfil",
                "mi perfil profesional",
                "Puedo presentar mi perfil y mis objetivos.",
                ["present-perfect", "for-since"],
                "phrase",
                [
                    (
                        "Tengo experiencia en atención al cliente.",
                        "I have experience in customer service.",
                        "J'ai de l'expérience dans le service client.",
                    ),
                    (
                        "He trabajado en equipo durante cinco años.",
                        "I have worked in a team for five years.",
                        "J'ai travaillé en équipe pendant cinq ans.",
                    ),
                    (
                        "Me gustaría aprender nuevas habilidades.",
                        "I would like to learn new skills.",
                        "J'aimerais apprendre de nouvelles compétences.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Casa y convivencia",
        [
            L(
                "Repaso: trabajo",
                "la experiencia profesional",
                "Puedo recordar mi experiencia y mis objetivos profesionales.",
                ["present-perfect", "for-since"],
                "phrase",
                [
                    (
                        "He trabajado en tres países.",
                        "I have worked in three countries.",
                        "J'ai travaillé dans trois pays.",
                    ),
                    (
                        "Tengo experiencia en atención al cliente.",
                        "I have experience in customer service.",
                        "J'ai de l'expérience dans le service client.",
                    ),
                    (
                        "Quiero aprender nuevas habilidades.",
                        "I want to learn new skills.",
                        "Je veux apprendre de nouvelles compétences.",
                    ),
                ],
            ),
            L(
                "Mi casa y sus habitaciones",
                "mi casa",
                "Puedo describir mi casa y sus habitaciones.",
                ["present-simple", "prepositions"],
                "phrase",
                [
                    (
                        "Vivo en un piso pequeño con dos habitaciones.",
                        "I live in a small flat with two bedrooms.",
                        "Je vis dans un petit appartement avec deux chambres.",
                    ),
                    (
                        "La cocina es grande y luminosa.",
                        "The kitchen is big and bright.",
                        "La cuisine est grande et lumineuse.",
                    ),
                    (
                        "Hay un balcón con vistas a la calle.",
                        "There is a balcony with views of the street.",
                        "Il y a un balcon avec vue sur la rue.",
                    ),
                ],
            ),
            L(
                "Muebles y tareas",
                "los muebles y las tareas",
                "Puedo nombrar muebles y tareas de la casa.",
                ["quantity"],
                "need",
                [
                    (
                        "una lavadora",
                        "a washing machine",
                        "une machine à laver",
                    ),
                    ("una mesa grande", "a big table", "une grande table"),
                    ("un armario nuevo", "a new wardrobe", "une nouvelle armoire"),
                ],
            ),
            L(
                "Must / mustn't",
                "las normas de la casa",
                "Puedo expresar obligaciones y prohibiciones en casa.",
                ["must"],
                "must",
                [
                    (
                        "respetar el silencio por la noche",
                        "keep quiet at night",
                        "respecter le silence la nuit",
                    ),
                    (
                        "limpiar la cocina después de cocinar",
                        "clean the kitchen after cooking",
                        "nettoyer la cuisine après avoir cuisiné",
                    ),
                    (
                        "cerrar la puerta con llave",
                        "lock the door",
                        "fermer la porte à clé",
                    ),
                ],
            ),
            L(
                "Have to / allowed to",
                "las obligaciones de la casa",
                "Puedo hablar de obligaciones y normas de convivencia.",
                ["have-to"],
                "have_to",
                [
                    (
                        "pagar el alquiler el primer día",
                        "pay the rent on the first day",
                        "payer le loyer le premier jour",
                    ),
                    (
                        "sacar la basura los martes",
                        "take out the rubbish on Tuesdays",
                        "sortir les poubelles le mardi",
                    ),
                    (
                        "compartir los gastos",
                        "share the expenses",
                        "partager les dépenses",
                    ),
                ],
            ),
            L(
                "Normas de convivencia",
                "la convivencia",
                "Puedo explicar las normas de convivencia de mi casa.",
                ["must", "have-to"],
                "phrase",
                [
                    (
                        "En casa tenemos que respetar el descanso.",
                        "At home we have to respect each other's rest.",
                        "À la maison, nous devons respecter le repos.",
                    ),
                    (
                        "No se puede fumar dentro.",
                        "You can't smoke inside.",
                        "On ne peut pas fumer à l'intérieur.",
                    ),
                    (
                        "Cada uno limpia su habitación.",
                        "Everyone cleans their own room.",
                        "Chacun nettoie sa chambre.",
                    ),
                ],
            ),
            L(
                "Anuncio de piso",
                "un anuncio de alquiler",
                "Puedo entender un anuncio de alquiler.",
                ["present-simple", "adjectives"],
                "phrase",
                [
                    (
                        "Se alquila piso de dos habitaciones.",
                        "Two-bedroom flat for rent.",
                        "Appartement de deux chambres à louer.",
                    ),
                    (
                        "Está amueblado y tiene ascensor.",
                        "It is furnished and has a lift.",
                        "Il est meublé et dispose d'un ascenseur.",
                    ),
                    (
                        "El alquiler es de ochocientos euros al mes.",
                        "The rent is eight hundred euros a month.",
                        "Le loyer est de huit cents euros par mois.",
                    ),
                ],
            ),
            L(
                "Negociar tareas",
                "repartir las tareas",
                "Puedo negociar y repartir las tareas de casa.",
                ["polite-requests", "questions"],
                "phrase",
                [
                    (
                        "¿Podrías encargarte de la compra esta semana?",
                        "Could you do the shopping this week?",
                        "Pourrais-tu faire les courses cette semaine ?",
                    ),
                    (
                        "Yo puedo limpiar el baño.",
                        "I can clean the bathroom.",
                        "Je peux nettoyer la salle de bain.",
                    ),
                    (
                        "¿Te parece bien si cambiamos las tareas?",
                        "Is it okay if we swap the chores?",
                        "Ça te va si on échange les tâches ?",
                    ),
                ],
            ),
            L(
                "Integración: contrato",
                "un contrato de alquiler",
                "Puedo revisar las condiciones básicas de un contrato.",
                ["imperatives", "must"],
                "phrase",
                [
                    (
                        "Antes de firmar, lee las condiciones del contrato.",
                        "Before signing, read the contract terms.",
                        "Avant de signer, lis les conditions du contrat.",
                    ),
                    (
                        "Comprueba la duración y la fianza.",
                        "Check the duration and the deposit.",
                        "Vérifie la durée et le dépôt de garantie.",
                    ),
                    (
                        "Guarda una copia firmada.",
                        "Keep a signed copy.",
                        "Garde une copie signée.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi casa ideal",
                "mi casa ideal",
                "Puedo describir cómo sería mi casa ideal.",
                ["present-simple", "comparatives"],
                "phrase",
                [
                    (
                        "Mi casa ideal es luminosa y tiene un jardín.",
                        "My ideal house is bright and has a garden.",
                        "Ma maison idéale est lumineuse et a un jardin.",
                    ),
                    (
                        "Me gustaría vivir cerca del centro.",
                        "I would like to live near the centre.",
                        "J'aimerais vivre près du centre.",
                    ),
                    (
                        "Pero también necesito un espacio tranquilo.",
                        "But I also need a quiet space.",
                        "Mais j'ai aussi besoin d'un espace calme.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Comida y restaurantes",
        [
            L(
                "Repaso: casa",
                "mi casa",
                "Puedo recordar cómo es mi casa y mi barrio.",
                ["present-simple", "prepositions"],
                "phrase",
                [
                    (
                        "Vivo en un piso pequeño con dos habitaciones.",
                        "I live in a small flat with two bedrooms.",
                        "Je vis dans un petit appartement avec deux chambres.",
                    ),
                    (
                        "La cocina es grande y luminosa.",
                        "The kitchen is big and bright.",
                        "La cuisine est grande et lumineuse.",
                    ),
                    (
                        "Me gusta pasar tiempo en casa.",
                        "I like spending time at home.",
                        "J'aime passer du temps à la maison.",
                    ),
                ],
            ),
            L(
                "En el mercado",
                "el mercado",
                "Puedo comprar alimentos en el mercado.",
                ["quantity"],
                "need",
                [
                    (
                        "un kilo de tomates",
                        "a kilo of tomatoes",
                        "un kilo de tomates",
                    ),
                    ("una barra de pan", "a loaf of bread", "une baguette de pain"),
                    ("un trozo de queso", "a piece of cheese", "un morceau de fromage"),
                ],
            ),
            L(
                "Ingredientes y platos",
                "los ingredientes",
                "Puedo nombrar ingredientes y hablar de platos.",
                ["quantity", "countable-uncountable"],
                "need",
                [
                    ("arroz", "rice", "du riz"),
                    ("pollo", "chicken", "du poulet"),
                    ("verduras frescas", "fresh vegetables", "des légumes frais"),
                ],
            ),
            L(
                "Countable / uncountable",
                "los contables e incontables",
                "Puedo distinguir sustantivos contables e incontables.",
                ["countable-uncountable"],
                "phrase",
                [
                    (
                        "Hay mucha agua en la botella.",
                        "There is a lot of water in the bottle.",
                        "Il y a beaucoup d'eau dans la bouteille.",
                    ),
                    (
                        "Necesito tres manzanas para la receta.",
                        "I need three apples for the recipe.",
                        "J'ai besoin de trois pommes pour la recette.",
                    ),
                    (
                        "No queda mucha leche.",
                        "There isn't much milk left.",
                        "Il ne reste pas beaucoup de lait.",
                    ),
                ],
            ),
            L(
                "Some / any / much / many",
                "las cantidades",
                "Puedo usar some, any, much y many.",
                ["some-any", "quantity"],
                "phrase",
                [
                    (
                        "Hay fruta en la nevera.",
                        "There is some fruit in the fridge.",
                        "Il y a des fruits dans le frigo.",
                    ),
                    (
                        "No hay leche.",
                        "There isn't any milk.",
                        "Il n'y a pas de lait.",
                    ),
                    (
                        "¿Tienes azúcar?",
                        "Do you have any sugar?",
                        "Tu as du sucre ?",
                    ),
                ],
            ),
            L(
                "Pedir en un restaurante",
                "el restaurante",
                "Puedo pedir en un restaurante con cortesía.",
                ["polite-requests", "questions"],
                "phrase",
                [
                    (
                        "¿Tienen una mesa para dos?",
                        "Do you have a table for two?",
                        "Avez-vous une table pour deux ?",
                    ),
                    (
                        "Quisiera el menú del día.",
                        "I would like the menu of the day.",
                        "Je voudrais le menu du jour.",
                    ),
                    (
                        "¿Nos trae la cuenta, por favor?",
                        "Could you bring us the bill, please?",
                        "Pourriez-vous nous apporter l'addition, s'il vous plaît ?",
                    ),
                ],
            ),
            L(
                "Reseña de un restaurante",
                "una reseña gastronómica",
                "Puedo describir y valorar un restaurante.",
                ["past-simple", "adjectives"],
                "phrase",
                [
                    (
                        "El restaurante es pequeño pero acogedor.",
                        "The restaurant is small but cosy.",
                        "Le restaurant est petit mais accueillant.",
                    ),
                    (
                        "La comida estaba deliciosa y el servicio fue rápido.",
                        "The food was delicious and the service was fast.",
                        "La nourriture était délicieuse et le service rapide.",
                    ),
                    (
                        "Volvería sin dudarlo.",
                        "I would go back without hesitation.",
                        "J'y retournerais sans hésiter.",
                    ),
                ],
            ),
            L(
                "Escribir una receta",
                "una receta",
                "Puedo escribir los pasos de una receta sencilla.",
                ["imperatives", "countable-uncountable"],
                "phrase",
                [
                    (
                        "Primero corta las verduras.",
                        "First, cut the vegetables.",
                        "D'abord, coupe les légumes.",
                    ),
                    (
                        "Después añade el arroz y el agua.",
                        "Then add the rice and the water.",
                        "Ensuite, ajoute le riz et l'eau.",
                    ),
                    (
                        "Cocina todo durante veinte minutos.",
                        "Cook everything for twenty minutes.",
                        "Fais cuire le tout pendant vingt minutes.",
                    ),
                ],
            ),
            L(
                "Integración: organizar una cena",
                "una cena con amigos",
                "Puedo organizar una cena y explicar lo que necesito.",
                ["going-to", "quantity"],
                "phrase",
                [
                    (
                        "Voy a organizar una cena el sábado.",
                        "I'm going to organise a dinner on Saturday.",
                        "Je vais organiser un dîner samedi.",
                    ),
                    (
                        "Necesito comprar comida para seis personas.",
                        "I need to buy food for six people.",
                        "J'ai besoin d'acheter de la nourriture pour six personnes.",
                    ),
                    (
                        "También voy a preparar una tarta.",
                        "I'm also going to make a cake.",
                        "Je vais aussi préparer un gâteau.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi comida favorita",
                "mi comida favorita",
                "Puedo hablar de mi comida favorita y de su preparación.",
                ["present-simple", "countable-uncountable"],
                "phrase",
                [
                    (
                        "Mi comida favorita es la paella.",
                        "My favourite food is paella.",
                        "Mon plat préféré est la paella.",
                    ),
                    (
                        "Me gusta porque tiene arroz, marisco y verduras.",
                        "I like it because it has rice, seafood and vegetables.",
                        "J'aime parce qu'il a du riz et des légumes.",
                    ),
                    (
                        "Mi familia la prepara los domingos.",
                        "My family makes it on Sundays.",
                        "Ma famille la prépare le dimanche.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Ocio y medios",
        [
            L(
                "Repaso: comida",
                "la comida",
                "Puedo recordar vocabulario de comida y restaurantes.",
                ["present-simple", "countable-uncountable"],
                "phrase",
                [
                    (
                        "Mi comida favorita es la paella.",
                        "My favourite food is paella.",
                        "Mon plat préféré est la paella.",
                    ),
                    (
                        "Me gusta porque tiene arroz y verduras.",
                        "I like it because it has rice and vegetables.",
                        "J'aime parce qu'il contient du riz et des légumes.",
                    ),
                    (
                        "Mi familia la prepara los domingos.",
                        "My family makes it on Sundays.",
                        "Ma famille la prépare le dimanche.",
                    ),
                ],
            ),
            L(
                "Series y películas favoritas",
                "las series y las películas",
                "Puedo hablar de series y películas que he visto.",
                ["past-simple", "present-perfect"],
                "phrase",
                [
                    (
                        "Anoche vi una serie muy buena.",
                        "Last night I watched a very good series.",
                        "Hier soir, j'ai regardé une très bonne série.",
                    ),
                    (
                        "La historia era interesante y los actores eran buenos.",
                        "The story was interesting and the actors were good.",
                        "L'histoire était intéressante et les acteurs étaient bons.",
                    ),
                    (
                        "Terminé dos capítulos en una hora.",
                        "I finished two episodes in an hour.",
                        "J'ai terminé deux épisodes en une heure.",
                    ),
                ],
            ),
            L(
                "Géneros y opiniones",
                "los géneros",
                "Puedo opinar sobre géneros de series y películas.",
                ["opinions", "adjectives"],
                "opinion",
                [
                    ("interesante", "interesting", "intéressant"),
                    ("aburrida", "boring", "ennuyeuse"),
                    ("emocionante", "exciting", "passionnante"),
                ],
            ),
            L(
                "Pasado continuo en pantalla",
                "acciones en desarrollo",
                "Puedo describir qué estaba pasando en el pasado.",
                ["past-continuous"],
                "phrase",
                [
                    (
                        "Estaba viendo una película cuando sonó el teléfono.",
                        "I was watching a film when the phone rang.",
                        "Je regardais un film quand le téléphone a sonné.",
                    ),
                    (
                        "Estábamos hablando cuando empezó la serie.",
                        "We were talking when the series started.",
                        "Nous parlions quand la série a commencé.",
                    ),
                    (
                        "Estaba leyendo cuando se fue la luz.",
                        "I was reading when the power went out.",
                        "Je lisais quand l'électricité est coupée.",
                    ),
                ],
            ),
            L(
                "While / when",
                "las acciones simultáneas",
                "Puedo conectar acciones con while y when.",
                ["past-simple-vs-continuous"],
                "phrase",
                [
                    (
                        "Mientras veía la serie, mi hermano estudiaba.",
                        "While I was watching the series, my brother was studying.",
                        "Pendant que je regardais la série, mon frère étudiait.",
                    ),
                    (
                        "Cuando llegué a casa, la película ya había empezado.",
                        "When I got home, the film had already started.",
                        "Quand je suis arrivé à la maison, le film avait déjà commencé.",
                    ),
                    (
                        "Mientras cenábamos, hablamos de la película.",
                        "While we were having dinner, we talked about the film.",
                        "Pendant que nous dînions, nous avons parlé du film.",
                    ),
                ],
            ),
            L(
                "Recomendar una serie o película",
                "recomendar una serie",
                "Puedo recomendar una serie y justificar por qué.",
                ["present-simple", "imperatives"],
                "phrase",
                [
                    (
                        "Te recomiendo esta serie.",
                        "I recommend this series to you.",
                        "Je te recommande cette série.",
                    ),
                    (
                        "Es muy entretenida y los capítulos son cortos.",
                        "It is very entertaining and the episodes are short.",
                        "Elle est très divertissante et les épisodes sont courts.",
                    ),
                    (
                        "Puedes verla en la plataforma.",
                        "You can watch it on the platform.",
                        "Tu peux la regarder sur la plateforme.",
                    ),
                ],
            ),
            L(
                "Crítica de una película",
                "una crítica de cine",
                "Puedo escribir una crítica breve de una película.",
                ["present-simple", "adjectives"],
                "phrase",
                [
                    (
                        "La película cuenta una historia real.",
                        "The film tells a true story.",
                        "Le film raconte une histoire vraie.",
                    ),
                    (
                        "Los actores están convincentes.",
                        "The actors are convincing.",
                        "Les acteurs sont convaincants.",
                    ),
                    (
                        "El final es sorprendente.",
                        "The ending is surprising.",
                        "La fin est surprenante.",
                    ),
                ],
            ),
            L(
                "Escribir una reseña",
                "una reseña de una película",
                "Puedo escribir una reseña personal de una película.",
                ["past-simple", "opinions"],
                "phrase",
                [
                    (
                        "Vi una película el fin de semana.",
                        "I watched a film at the weekend.",
                        "J'ai regardé un film le week-end.",
                    ),
                    (
                        "Me gustó mucho la fotografía.",
                        "I really liked the photography.",
                        "J'ai beaucoup aimé la photographie.",
                    ),
                    (
                        "Se la recomiendo a quien busque una historia emocionante.",
                        "I recommend it to anyone looking for an exciting story.",
                        "Je le recommande à ceux qui cherchent une histoire passionnante.",
                    ),
                ],
            ),
            L(
                "Integración: plan de ocio",
                "un plan de ocio",
                "Puedo organizar un plan de ocio para el fin de semana.",
                ["going-to", "present-continuous-future"],
                "phrase",
                [
                    (
                        "El viernes voy a ver una serie en casa.",
                        "On Friday I'm going to watch a series at home.",
                        "Vendredi, je vais regarder une série à la maison.",
                    ),
                    (
                        "El sábado voy al cine con mis amigos.",
                        "On Saturday I'm going to the cinema with my friends.",
                        "Samedi, je vais au cinéma avec mes amis.",
                    ),
                    (
                        "El domingo voy a leer y descansar.",
                        "On Sunday I'm going to read and rest.",
                        "Dimanche, je vais lire et me reposer.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi serie favorita",
                "mi serie favorita",
                "Puedo describir mi serie favorita y explicar por qué me gusta.",
                ["present-simple", "opinions"],
                "phrase",
                [
                    (
                        "Mi serie favorita es una comedia.",
                        "My favourite series is a comedy.",
                        "Ma série préférée est une comédie.",
                    ),
                    (
                        "Los personajes son divertidos y la historia es ligera.",
                        "The characters are funny and the story is light.",
                        "Les personnages sont drôles et l'histoire est légère.",
                    ),
                    (
                        "La veo cuando necesito desconectar.",
                        "I watch it when I need to switch off.",
                        "Je la regarde quand j'ai besoin de déconnecter.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Medio ambiente",
        [
            L(
                "Repaso: ocio",
                "el tiempo libre",
                "Puedo recordar mis planes de ocio y mis gustos.",
                ["past-simple", "opinions"],
                "phrase",
                [
                    (
                        "El viernes vi una serie en casa.",
                        "On Friday I watched a series at home.",
                        "Vendredi, j'ai regardé une série à la maison.",
                    ),
                    (
                        "El sábado fui al cine con mis amigos.",
                        "On Saturday I went to the cinema with my friends.",
                        "Samedi, je suis allé au cinéma avec mes amis.",
                    ),
                    (
                        "El domingo descansé y leí un libro.",
                        "On Sunday I rested and read a book.",
                        "Dimanche, je me suis reposé et j'ai lu un livre.",
                    ),
                ],
            ),
            L(
                "Problemas ambientales",
                "los problemas ambientales",
                "Puedo describir problemas ambientales de mi entorno.",
                ["present-simple", "too-much"],
                "phrase",
                [
                    (
                        "Hay demasiada contaminación en la ciudad.",
                        "There is too much pollution in the city.",
                        "Il y a trop de pollution dans la ville.",
                    ),
                    (
                        "Se desperdicia mucha agua.",
                        "A lot of water is wasted.",
                        "Beaucoup d'eau est gaspillée.",
                    ),
                    (
                        "Muchos plásticos terminan en el mar.",
                        "A lot of plastic ends up in the sea.",
                        "Beaucoup de plastique finit dans la mer.",
                    ),
                ],
            ),
            L(
                "Acciones sostenibles",
                "las acciones sostenibles",
                "Puedo recomendar acciones sostenibles.",
                ["should"],
                "should",
                [
                    (
                        "reciclar el plástico",
                        "recycle plastic",
                        "recycler le plastique",
                    ),
                    (
                        "usar el transporte público",
                        "use public transport",
                        "utiliser les transports publics",
                    ),
                    ("ahorrar agua", "save water", "économiser l'eau"),
                ],
            ),
            L(
                "First conditional",
                "las condiciones ambientales",
                "Puedo expresar condiciones reales y sus consecuencias.",
                ["first-conditional"],
                "phrase",
                [
                    (
                        "Si reciclamos, reducimos los residuos.",
                        "If we recycle, we reduce waste.",
                        "Si nous recyclons, nous réduisons les déchets.",
                    ),
                    (
                        "Si usas el transporte público, contaminas menos.",
                        "If you use public transport, you pollute less.",
                        "Si tu utilises les transports publics, tu pollues moins.",
                    ),
                    (
                        "Si ahorramos agua, protegemos el planeta.",
                        "If we save water, we protect the planet.",
                        "Si nous économisons l'eau, nous protégeons la planète.",
                    ),
                ],
            ),
            L(
                "Will / won't",
                "las predicciones ambientales",
                "Puedo hacer predicciones sobre el futuro del medio ambiente.",
                ["will"],
                "phrase",
                [
                    (
                        "El año que viene habrá más carriles bici.",
                        "Next year there will be more bike lanes.",
                        "L'année prochaine, il y aura plus de pistes cyclables.",
                    ),
                    (
                        "Las ciudades serán más limpias.",
                        "Cities will be cleaner.",
                        "Les villes seront plus propres.",
                    ),
                    (
                        "La gente usará menos coches.",
                        "People will use fewer cars.",
                        "Les gens utiliseront moins de voitures.",
                    ),
                ],
            ),
            L(
                "Debate: reciclar",
                "el reciclaje",
                "Puedo dar mi opinión y responder a otra opinión.",
                ["agree-disagree", "should"],
                "phrase",
                [
                    (
                        "Creo que reciclar es importante.",
                        "I think recycling is important.",
                        "Je pense que le recyclage est important.",
                    ),
                    (
                        "Estoy de acuerdo, pero necesitamos más contenedores.",
                        "I agree, but we need more bins.",
                        "Je suis d'accord, mais nous avons besoin de plus de poubelles.",
                    ),
                    (
                        "También deberíamos reducir el plástico.",
                        "We should also reduce plastic.",
                        "Nous devrions aussi réduire le plastique.",
                    ),
                ],
            ),
            L(
                "Noticia ambiental",
                "una noticia ambiental",
                "Puedo entender y resumir una noticia ambiental.",
                ["present-perfect", "will"],
                "phrase",
                [
                    (
                        "El ayuntamiento ha anunciado un nuevo plan verde.",
                        "The city council has announced a new green plan.",
                        "La mairie a annoncé un nouveau plan vert.",
                    ),
                    (
                        "Se plantarán mil árboles en los parques.",
                        "A thousand trees will be planted in the parks.",
                        "Mille arbres seront plantés dans les parcs.",
                    ),
                    (
                        "El objetivo es reducir la contaminación.",
                        "The goal is to reduce pollution.",
                        "L'objectif est de réduire la pollution.",
                    ),
                ],
            ),
            L(
                "Propuesta sostenible",
                "una propuesta sostenible",
                "Puedo presentar una propuesta sostenible para mi comunidad.",
                ["first-conditional", "will"],
                "phrase",
                [
                    (
                        "Propongo crear un huerto comunitario.",
                        "I propose creating a community garden.",
                        "Je propose de créer un jardin communautaire.",
                    ),
                    (
                        "Podemos compartir herramientas y semillas.",
                        "We can share tools and seeds.",
                        "Nous pouvons partager des outils et des graines.",
                    ),
                    (
                        "Así aprenderemos a cultivar nuestros alimentos.",
                        "In this way we will learn to grow our own food.",
                        "Ainsi, nous apprendrons à cultiver nos propres aliments.",
                    ),
                ],
            ),
            L(
                "Integración: mi huella",
                "mi huella ambiental",
                "Puedo explicar cómo reducir mi huella ambiental.",
                ["going-to", "first-conditional"],
                "phrase",
                [
                    (
                        "Voy a reducir el consumo de plástico.",
                        "I'm going to reduce plastic consumption.",
                        "Je vais réduire la consommation de plastique.",
                    ),
                    (
                        "También voy a caminar más y usar menos el coche.",
                        "I'm also going to walk more and use the car less.",
                        "Je vais aussi marcher plus et utiliser moins la voiture.",
                    ),
                    (
                        "Si todos hacemos algo, el cambio será posible.",
                        "If everyone does something, change will be possible.",
                        "Si tout le monde fait quelque chose, le changement sera possible.",
                    ),
                ],
            ),
            L(
                "Evaluación: plan verde",
                "mi plan verde",
                "Puedo presentar mi plan verde personal.",
                ["will", "first-conditional"],
                "phrase",
                [
                    (
                        "Mi plan verde empieza en casa.",
                        "My green plan starts at home.",
                        "Mon plan vert commence à la maison.",
                    ),
                    (
                        "Separaré los residuos y ahorraré agua.",
                        "I will separate waste and save water.",
                        "Je séparerai les déchets et j'économiserai l'eau.",
                    ),
                    (
                        "También usaré bolsas reutilizables.",
                        "I will also use reusable bags.",
                        "J'utiliserai aussi des sacs réutilisables.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Relaciones y opiniones",
        [
            L(
                "Repaso: medio ambiente",
                "el medio ambiente",
                "Puedo recordar acciones y propuestas ambientales.",
                ["present-perfect", "first-conditional"],
                "phrase",
                [
                    (
                        "He reducido el consumo de plástico.",
                        "I have reduced plastic consumption.",
                        "J'ai réduit la consommation de plastique.",
                    ),
                    (
                        "También camino más y uso menos el coche.",
                        "I also walk more and use the car less.",
                        "Je marche aussi plus et j'utilise moins la voiture.",
                    ),
                    (
                        "Si todos hacemos algo, el cambio será posible.",
                        "If everyone does something, change will be possible.",
                        "Si tout le monde fait quelque chose, le changement sera possible.",
                    ),
                ],
            ),
            L(
                "Amistades y familia",
                "las amistades y la familia",
                "Puedo describir a mis amigos y a mi familia.",
                ["present-simple", "past-simple"],
                "phrase",
                [
                    (
                        "Mi mejor amiga se llama Laura.",
                        "My best friend's name is Laura.",
                        "Ma meilleure amie s'appelle Laura.",
                    ),
                    (
                        "Nos conocimos en la universidad.",
                        "We met at university.",
                        "Nous nous sommes rencontrés à l'université.",
                    ),
                    (
                        "Siempre nos apoyamos cuando tenemos problemas.",
                        "We always support each other when we have problems.",
                        "Nous nous soutenons toujours quand nous avons des problèmes.",
                    ),
                ],
            ),
            L(
                "Rasgos y emociones",
                "los rasgos y las emociones",
                "Puedo describir el carácter y las emociones.",
                ["adjectives", "present-simple"],
                "phrase",
                [
                    (
                        "Mi hermano es muy divertido.",
                        "My brother is very funny.",
                        "Mon frère est très drôle.",
                    ),
                    (
                        "Mi madre es paciente y generosa.",
                        "My mother is patient and generous.",
                        "Ma mère est patiente et généreuse.",
                    ),
                    (
                        "A veces me siento nervioso antes de hablar en público.",
                        "Sometimes I feel nervous before speaking in public.",
                        "Parfois, je me sens nerveux avant de parler en public.",
                    ),
                ],
            ),
            L(
                "Verbo + infinitivo o gerundio",
                "los patrones verbales",
                "Puedo usar verbos seguidos de infinitivo o gerundio.",
                ["verb-patterns"],
                "phrase",
                [
                    (
                        "Quiero aprender a cocinar mejor.",
                        "I want to learn to cook better.",
                        "Je veux apprendre à mieux cuisiner.",
                    ),
                    (
                        "Me gusta pasar tiempo con mi familia.",
                        "I like spending time with my family.",
                        "J'aime passer du temps avec ma famille.",
                    ),
                    (
                        "Prefiero quedarme en casa los viernes.",
                        "I prefer staying at home on Fridays.",
                        "Je préfère rester à la maison le vendredi.",
                    ),
                ],
            ),
            L(
                "Oraciones de relativo",
                "las oraciones de relativo",
                "Puedo añadir información sobre personas, cosas y lugares.",
                ["relative-clauses"],
                "phrase",
                [
                    (
                        "La persona que me ayudó es mi vecina.",
                        "The person who helped me is my neighbour.",
                        "La personne qui m'a aidé est ma voisine.",
                    ),
                    (
                        "El libro que estoy leyendo es muy interesante.",
                        "The book I am reading is very interesting.",
                        "Le livre que je lis est très intéressant.",
                    ),
                    (
                        "El lugar donde nos conocimos es un parque.",
                        "The place where we met is a park.",
                        "Le lieu où nous nous sommes rencontrés est un parc.",
                    ),
                ],
            ),
            L(
                "Acuerdo y desacuerdo",
                "las opiniones",
                "Puedo expresar acuerdo y desacuerdo con respeto.",
                ["agree-disagree"],
                "phrase",
                [
                    (
                        "Estoy de acuerdo con tu opinión.",
                        "I agree with your opinion.",
                        "Je suis d'accord avec ton opinion.",
                    ),
                    (
                        "No estoy de acuerdo con esa idea.",
                        "I don't agree with that idea.",
                        "Je ne suis pas d'accord avec cette idée.",
                    ),
                    (
                        "Creo que hay otra solución.",
                        "I think there is another solution.",
                        "Je pense qu'il y a une autre solution.",
                    ),
                ],
            ),
            L(
                "Conversación difícil",
                "una conversación difícil",
                "Puedo mantener una conversación difícil con calma.",
                ["polite-requests", "agree-disagree"],
                "phrase",
                [
                    (
                        "Necesito hablar contigo de algo importante.",
                        "I need to talk to you about something important.",
                        "J'ai besoin de te parler de quelque chose d'important.",
                    ),
                    (
                        "Entiendo cómo te sientes y quiero ayudar.",
                        "I understand how you feel and I want to help.",
                        "Je comprends ce que tu ressens et je veux aider.",
                    ),
                    (
                        "¿Podemos buscar una solución juntos?",
                        "Can we find a solution together?",
                        "Pouvons-nous trouver une solution ensemble ?",
                    ),
                ],
            ),
            L(
                "Carta de opinión",
                "una carta de opinión",
                "Puedo escribir una carta de opinión breve.",
                ["opinions", "polite-requests"],
                "phrase",
                [
                    (
                        "Escribo para expresar mi opinión sobre el tema.",
                        "I am writing to express my opinion on the topic.",
                        "J'écris pour exprimer mon opinion sur le sujet.",
                    ),
                    (
                        "Creo que la educación es fundamental.",
                        "I think education is essential.",
                        "Je pense que l'éducation est essentielle.",
                    ),
                    (
                        "Por eso pido más inversión en las escuelas.",
                        "That is why I ask for more investment in schools.",
                        "C'est pourquoi je demande plus d'investissement dans les écoles.",
                    ),
                ],
            ),
            L(
                "Integración: resolver un conflicto",
                "resolver un conflicto",
                "Puedo contar y resolver un conflicto interpersonal.",
                ["past-simple", "agree-disagree"],
                "phrase",
                [
                    (
                        "Tuvimos un malentendido y discutimos.",
                        "We had a misunderstanding and argued.",
                        "Nous avons eu un malentendu et nous nous sommes disputés.",
                    ),
                    (
                        "Después hablamos con calma y pedimos perdón.",
                        "Then we talked calmly and apologised.",
                        "Ensuite, nous avons parlé calmement et nous nous sommes excusés.",
                    ),
                    (
                        "Ahora nuestra relación es más fuerte.",
                        "Now our relationship is stronger.",
                        "Maintenant, notre relation est plus forte.",
                    ),
                ],
            ),
            L(
                "Evaluación: mis relaciones",
                "mis relaciones",
                "Puedo reflexionar sobre mis relaciones y lo que valoro.",
                ["present-simple", "relative-clauses"],
                "phrase",
                [
                    (
                        "Las relaciones importantes necesitan tiempo y respeto.",
                        "Important relationships need time and respect.",
                        "Les relations importantes ont besoin de temps et de respect.",
                    ),
                    (
                        "Aprendo de las personas que me rodean.",
                        "I learn from the people around me.",
                        "J'apprends des personnes qui m'entourent.",
                    ),
                    (
                        "Intento escuchar antes de opinar.",
                        "I try to listen before giving my opinion.",
                        "J'essaie d'écouter avant de donner mon avis.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Cultura y celebraciones",
        [
            L(
                "Repaso: relaciones",
                "las relaciones",
                "Puedo recordar cómo describir relaciones y opiniones.",
                ["present-simple", "agree-disagree"],
                "phrase",
                [
                    (
                        "Hablo con mi familia todos los días.",
                        "I talk to my family every day.",
                        "Je parle à ma famille tous les jours.",
                    ),
                    (
                        "Quedo con mis amigos los fines de semana.",
                        "I meet my friends at the weekend.",
                        "Je retrouve mes amis le week-end.",
                    ),
                    (
                        "Intento escuchar antes de opinar.",
                        "I try to listen before giving my opinion.",
                        "J'essaie d'écouter avant de donner mon avis.",
                    ),
                ],
            ),
            L(
                "Fiestas y tradiciones",
                "las fiestas y tradiciones",
                "Puedo describir una fiesta tradicional de mi entorno.",
                ["present-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "En mi pueblo celebramos una fiesta en agosto.",
                        "In my town we celebrate a festival in August.",
                        "Dans mon village, nous célébrons une fête en août.",
                    ),
                    (
                        "Hay música, comida y bailes tradicionales.",
                        "There is music, food and traditional dancing.",
                        "Il y a de la musique et des danses traditionnelles.",
                    ),
                    (
                        "Toda la familia participa.",
                        "The whole family takes part.",
                        "Toute la famille participe.",
                    ),
                ],
            ),
            L(
                "Comida y música",
                "la comida y la música",
                "Puedo hablar de comida y música en una celebración.",
                ["quantity"],
                "need",
                [
                    (
                        "una tarta de cumpleaños",
                        "a birthday cake",
                        "un gâteau d'anniversaire",
                    ),
                    ("música en directo", "live music", "de la musique live"),
                    ("fuegos artificiales", "fireworks", "des feux d'artifice"),
                ],
            ),
            L(
                "Present perfect vs past simple",
                "las experiencias y los recuerdos",
                "Puedo distinguir experiencias y momentos concretos del pasado.",
                ["present-perfect", "past-simple"],
                "phrase",
                [
                    (
                        "He visitado muchas fiestas este año.",
                        "I have visited many festivals this year.",
                        "J'ai visité beaucoup de fêtes cette année.",
                    ),
                    (
                        "El año pasado fui a una boda muy bonita.",
                        "Last year I went to a very beautiful wedding.",
                        "L'année dernière, je suis allé à un très beau mariage.",
                    ),
                    (
                        "Nunca he probado ese plato.",
                        "I have never tried that dish.",
                        "Je n'ai jamais goûté ce plat.",
                    ),
                ],
            ),
            L(
                "Used to",
                "los hábitos del pasado",
                "Puedo hablar de hábitos que ya no tengo.",
                ["used-to"],
                "used_to",
                [
                    (
                        "jugaba a las cartas con mis abuelos",
                        "played cards with my grandparents",
                        "je jouais aux cartes avec mes grands-parents",
                    ),
                    (
                        "íbamos a la playa cada verano",
                        "went to the beach every summer",
                        "nous allions à la plage chaque été",
                    ),
                    (
                        "celebrábamos mi cumpleaños en el jardín",
                        "celebrated my birthday in the garden",
                        "nous fêtions mon anniversaire dans le jardin",
                    ),
                ],
            ),
            L(
                "Invitar a una fiesta",
                "una invitación",
                "Puedo invitar a alguien a una fiesta y dar detalles.",
                ["polite-requests", "questions"],
                "phrase",
                [
                    (
                        "¿Quieres venir a mi fiesta el sábado?",
                        "Do you want to come to my party on Saturday?",
                        "Tu veux venir à ma fête samedi ?",
                    ),
                    (
                        "Empieza a las ocho en mi casa.",
                        "It starts at eight at my house.",
                        "Ça commence à huit heures chez moi.",
                    ),
                    (
                        "Puedes traer a quien quieras.",
                        "You can bring whoever you want.",
                        "Tu peux amener qui tu veux.",
                    ),
                ],
            ),
            L(
                "Programa cultural",
                "un programa cultural",
                "Puedo entender un programa cultural sencillo.",
                ["present-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "El programa incluye un concierto y una exposición.",
                        "The programme includes a concert and an exhibition.",
                        "Le programme comprend un concert et une exposition.",
                    ),
                    (
                        "La entrada es gratuita para estudiantes.",
                        "Entry is free for students.",
                        "L'entrée est gratuite pour les étudiants.",
                    ),
                    (
                        "El evento termina a las diez.",
                        "The event ends at ten.",
                        "L'événement se termine à dix heures.",
                    ),
                ],
            ),
            L(
                "Describir una tradición",
                "una tradición",
                "Puedo describir una tradición y explicar su significado.",
                ["present-simple", "relative-clauses"],
                "phrase",
                [
                    (
                        "Cada primavera celebramos la llegada del buen tiempo.",
                        "Every spring we celebrate the arrival of good weather.",
                        "Chaque printemps, nous célébrons l'arrivée du beau temps.",
                    ),
                    (
                        "Las familias se reúnen y comparten comida.",
                        "Families gather and share food.",
                        "Les familles se réunissent et partagent de la nourriture.",
                    ),
                    (
                        "Es una tradición que pasa de generación en generación.",
                        "It is a tradition passed down from generation to generation.",
                        "C'est une tradition transmise de génération en génération.",
                    ),
                ],
            ),
            L(
                "Integración: organizar una fiesta",
                "organizar una fiesta",
                "Puedo organizar una fiesta y explicar los preparativos.",
                ["going-to", "quantity"],
                "phrase",
                [
                    (
                        "Voy a organizar una fiesta sorpresa.",
                        "I'm going to organise a surprise party.",
                        "Je vais organiser une fête surprise.",
                    ),
                    (
                        "Necesito comprar bebidas y decoración.",
                        "I need to buy drinks and decorations.",
                        "J'ai besoin d'acheter des boissons et des décorations.",
                    ),
                    (
                        "También voy a preparar una lista de música.",
                        "I'm also going to prepare a music playlist.",
                        "Je vais aussi préparer une playlist.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi celebración",
                "mi celebración favorita",
                "Puedo describir mi celebración favorita y sus costumbres.",
                ["present-simple", "present-perfect"],
                "phrase",
                [
                    (
                        "Mi celebración favorita es la Nochevieja.",
                        "My favourite celebration is New Year's Eve.",
                        "Ma fête préférée est la Saint-Sylvestre.",
                    ),
                    (
                        "Cenamos en familia y vemos los fuegos artificiales.",
                        "We have dinner as a family and watch the fireworks.",
                        "Nous dînons en famille et regardons les feux d'artifice.",
                    ),
                    (
                        "Siempre brindamos por el nuevo año.",
                        "We always toast to the new year.",
                        "Nous portons toujours un toast à la nouvelle année.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Tecnología y comunicación",
        [
            L(
                "Repaso: cultura",
                "las fiestas y tradiciones",
                "Puedo recordar tradiciones y experiencias culturales.",
                ["past-simple", "present-perfect"],
                "phrase",
                [
                    (
                        "El año pasado fui a una fiesta tradicional.",
                        "Last year I went to a traditional festival.",
                        "L'année dernière, je suis allé à une fête traditionnelle.",
                    ),
                    (
                        "Probé comida típica y escuché música en directo.",
                        "I tried typical food and listened to live music.",
                        "J'ai goûté de la cuisine typique et écouté de la musique live.",
                    ),
                    (
                        "Me encantó la experiencia.",
                        "I loved the experience.",
                        "J'ai adoré l'expérience.",
                    ),
                ],
            ),
            L(
                "Dispositivos y apps",
                "la tecnología",
                "Puedo nombrar dispositivos y aplicaciones que uso.",
                ["quantity"],
                "need",
                [
                    ("un cargador", "a charger", "un chargeur"),
                    (
                        "una aplicación de mensajes",
                        "a messaging app",
                        "une application de messagerie",
                    ),
                    ("unos auriculares", "headphones", "un casque"),
                ],
            ),
            L(
                "Uso de las redes sociales",
                "las redes sociales",
                "Puedo hablar de cómo uso las redes sociales.",
                ["present-simple", "adverbs-frequency"],
                "phrase",
                [
                    (
                        "Uso las redes sociales para hablar con amigos.",
                        "I use social media to talk to friends.",
                        "J'utilise les réseaux sociaux pour parler à mes amis.",
                    ),
                    (
                        "Comparto fotos y vídeos de mis viajes.",
                        "I share photos and videos of my trips.",
                        "Je partage des photos et des vidéos de mes voyages.",
                    ),
                    (
                        "Intento no pasar demasiado tiempo en el móvil.",
                        "I try not to spend too much time on my phone.",
                        "J'essaie de ne pas passer trop de temps sur mon téléphone.",
                    ),
                ],
            ),
            L(
                "May / might",
                "la posibilidad",
                "Puedo expresar posibilidad con may y might.",
                ["may-might"],
                "phrase",
                [
                    (
                        "Quizá vaya al concierto el sábado.",
                        "Maybe I will go to the concert on Saturday.",
                        "Peut-être que j'irai au concert samedi.",
                    ),
                    (
                        "Puede que llueva por la tarde.",
                        "It might rain in the afternoon.",
                        "Il se peut qu'il pleuve l'après-midi.",
                    ),
                    (
                        "Quizá visite a mis primos el domingo.",
                        "Maybe I will visit my cousins on Sunday.",
                        "Peut-être que je rendrai visite à mes cousins dimanche.",
                    ),
                ],
            ),
            L(
                "Consejos tecnológicos",
                "los consejos tecnológicos",
                "Puedo dar consejos sobre el uso de la tecnología.",
                ["should"],
                "should",
                [
                    (
                        "actualizar el sistema",
                        "update the system",
                        "mettre à jour le système",
                    ),
                    (
                        "hacer copias de seguridad",
                        "make backups",
                        "faire des sauvegardes",
                    ),
                    (
                        "usar contraseñas seguras",
                        "use strong passwords",
                        "utiliser des mots de passe sécurisés",
                    ),
                ],
            ),
            L(
                "Problemas técnicos",
                "los problemas técnicos",
                "Puedo explicar un problema técnico y pedir ayuda.",
                ["polite-requests", "questions"],
                "phrase",
                [
                    (
                        "¿Podrías ayudarme con este problema?",
                        "Could you help me with this problem?",
                        "Pourrais-tu m'aider avec ce problème ?",
                    ),
                    (
                        "Mi ordenador no se enciende.",
                        "My computer won't turn on.",
                        "Mon ordinateur ne s'allume pas.",
                    ),
                    (
                        "¿Puedes revisar la conexión?",
                        "Can you check the connection?",
                        "Peux-tu vérifier la connexion ?",
                    ),
                ],
            ),
            L(
                "Artículo sobre pantallas",
                "el tiempo de pantalla",
                "Puedo entender y resumir un artículo sobre pantallas.",
                ["present-simple", "should"],
                "phrase",
                [
                    (
                        "Pasamos muchas horas delante de las pantallas.",
                        "We spend many hours in front of screens.",
                        "Nous passons beaucoup d'heures devant les écrans.",
                    ),
                    (
                        "Los expertos recomiendan hacer pausas.",
                        "Experts recommend taking breaks.",
                        "Les experts recommandent de faire des pauses.",
                    ),
                    (
                        "También aconsejan no usar el móvil antes de dormir.",
                        "They also advise not using the phone before sleeping.",
                        "Ils conseillent aussi de ne pas utiliser le téléphone avant de dormir.",
                    ),
                ],
            ),
            L(
                "Netiqueta",
                "la netiqueta",
                "Puedo explicar normas básicas de comunicación digital.",
                ["imperatives", "should"],
                "phrase",
                [
                    (
                        "Escribe mensajes claros y respetuosos.",
                        "Write clear and respectful messages.",
                        "Écris des messages clairs et respectueux.",
                    ),
                    (
                        "No compartas información personal sin permiso.",
                        "Don't share personal information without permission.",
                        "Ne partage pas d'informations personnelles sans permission.",
                    ),
                    (
                        "Responde cuando puedas, sin prisas.",
                        "Reply when you can, without rushing.",
                        "Réponds quand tu peux, sans te presser.",
                    ),
                ],
            ),
            L(
                "Integración: desconectar",
                "desconectar de la tecnología",
                "Puedo proponer un plan para desconectar de las pantallas.",
                ["going-to", "should"],
                "phrase",
                [
                    (
                        "Voy a desconectar el móvil durante la cena.",
                        "I'm going to switch off my phone during dinner.",
                        "Je vais éteindre mon téléphone pendant le dîner.",
                    ),
                    (
                        "También voy a hacer una caminata sin pantallas.",
                        "I'm also going to go for a walk without screens.",
                        "Je vais aussi faire une promenade sans écrans.",
                    ),
                    (
                        "Quiero pasar más tiempo con mi familia.",
                        "I want to spend more time with my family.",
                        "Je veux passer plus de temps avec ma famille.",
                    ),
                ],
            ),
            L(
                "Evaluación: mi vida digital",
                "mi vida digital",
                "Puedo reflexionar sobre mi relación con la tecnología.",
                ["present-simple", "should"],
                "phrase",
                [
                    (
                        "Uso la tecnología para aprender y comunicarme.",
                        "I use technology to learn and communicate.",
                        "J'utilise la technologie pour apprendre et communiquer.",
                    ),
                    (
                        "Pero intento mantener un equilibrio.",
                        "But I try to keep a balance.",
                        "Mais j'essaie de garder un équilibre.",
                    ),
                    (
                        "Lo más importante es sentirme bien.",
                        "The most important thing is to feel good.",
                        "Le plus important est de me sentir bien.",
                    ),
                ],
            ),
        ],
    ),
    (
        "Repaso y proyecto final",
        [
            L(
                "Repaso de hitos A2",
                "los hitos de A2",
                "Puedo resumir lo que he aprendido en el nivel A2.",
                ["present-perfect", "going-to"],
                "phrase",
                [
                    (
                        "He aprendido a contar historias en pasado.",
                        "I have learned to tell stories in the past.",
                        "J'ai appris à raconter des histoires au passé.",
                    ),
                    (
                        "También puedo hablar de planes y comparar opciones.",
                        "I can also talk about plans and compare options.",
                        "Je peux aussi parler de projets et comparer des options.",
                    ),
                    (
                        "Todavía quiero practicar más la conversación.",
                        "I still want to practise conversation more.",
                        "Je veux encore pratiquer davantage la conversation.",
                    ),
                ],
            ),
            L(
                "Mi historia personal",
                "mi historia personal",
                "Puedo contar los hitos importantes de mi vida.",
                ["past-simple", "time-expressions"],
                "phrase",
                [
                    (
                        "Nací en una ciudad pequeña.",
                        "I was born in a small city.",
                        "Je suis né dans une petite ville.",
                    ),
                    (
                        "Crecí con mis padres y mis dos hermanos.",
                        "I grew up with my parents and my two brothers.",
                        "J'ai grandi avec mes parents et mes deux frères.",
                    ),
                    (
                        "A los dieciocho años me mudé a Madrid.",
                        "At eighteen I moved to Madrid.",
                        "À dix-huit ans, j'ai déménagé à Madrid.",
                    ),
                ],
            ),
            L(
                "Planes y sueños",
                "mis planes y sueños",
                "Puedo hablar de mis sueños y proyectos de futuro.",
                ["going-to", "will"],
                "phrase",
                [
                    (
                        "Me gustaría hablar inglés con fluidez.",
                        "I would like to speak English fluently.",
                        "J'aimerais parler anglais couramment.",
                    ),
                    (
                        "También quiero viajar y conocer otras culturas.",
                        "I also want to travel and learn about other cultures.",
                        "Je veux aussi voyager et découvrir d'autres cultures.",
                    ),
                    (
                        "En el futuro me gustaría trabajar en el extranjero.",
                        "In the future I would like to work abroad.",
                        "À l'avenir, j'aimerais travailler à l'étranger.",
                    ),
                ],
            ),
            L(
                "Comparar opciones",
                "comparar opciones",
                "Puedo comparar opciones y justificar una decisión.",
                ["comparatives", "superlatives"],
                "phrase",
                [
                    (
                        "Vivir en el centro es más caro pero más cómodo.",
                        "Living in the centre is more expensive but more convenient.",
                        "Vivre dans le centre est plus cher mais plus pratique.",
                    ),
                    (
                        "El transporte público es más barato que el coche.",
                        "Public transport is cheaper than the car.",
                        "Les transports publics sont moins chers que la voiture.",
                    ),
                    (
                        "Creo que la mejor opción es compartir piso.",
                        "I think the best option is to share a flat.",
                        "Je pense que la meilleure option est de partager un appartement.",
                    ),
                ],
            ),
            L(
                "Consejos y salud",
                "los consejos de salud",
                "Puedo dar consejos y hablar de hábitos saludables.",
                ["should", "have-to"],
                "phrase",
                [
                    (
                        "Deberías descansar y beber más agua.",
                        "You should rest and drink more water.",
                        "Tu devrais te reposer et boire plus d'eau.",
                    ),
                    (
                        "No deberías trabajar tantas horas.",
                        "You shouldn't work so many hours.",
                        "Tu ne devrais pas travailler autant d'heures.",
                    ),
                    (
                        "Si tienes dolor, ve al médico.",
                        "If you are in pain, go to the doctor.",
                        "Si tu as mal, va chez le médecin.",
                    ),
                ],
            ),
            L(
                "Un viaje imaginario",
                "un viaje imaginario",
                "Puedo imaginar y describir un viaje soñado.",
                ["first-conditional", "will"],
                "phrase",
                [
                    (
                        "Si pudiera, viajaría a Japón el próximo año.",
                        "If I could, I would travel to Japan next year.",
                        "Si je pouvais, je voyagerais au Japon l'année prochaine.",
                    ),
                    (
                        "Me gustaría ver los cerezos en flor.",
                        "I would like to see the cherry blossoms.",
                        "J'aimerais voir les cerisiers en fleurs.",
                    ),
                    (
                        "También probaría la comida típica.",
                        "I would also try the typical food.",
                        "Je goûterais aussi la cuisine typique.",
                    ),
                ],
            ),
            L(
                "Entrevista de trabajo",
                "una entrevista de trabajo",
                "Puedo participar en una entrevista de trabajo sencilla.",
                ["present-perfect", "questions"],
                "phrase",
                [
                    (
                        "Cuénteme un poco sobre su experiencia.",
                        "Tell me a little about your experience.",
                        "Parlez-moi un peu de votre expérience.",
                    ),
                    (
                        "He trabajado en equipo y he aprendido rápido.",
                        "I have worked in a team and learned quickly.",
                        "J'ai travaillé en équipe et appris rapidement.",
                    ),
                    (
                        "¿Cuáles son sus puntos fuertes?",
                        "What are your strengths?",
                        "Quels sont vos points forts ?",
                    ),
                ],
            ),
            L(
                "Debate de opiniones",
                "un debate",
                "Puedo debatir y responder a opiniones distintas.",
                ["agree-disagree", "first-conditional"],
                "phrase",
                [
                    (
                        "Creo que el transporte público debería ser gratuito.",
                        "I think public transport should be free.",
                        "Je pense que les transports publics devraient être gratuits.",
                    ),
                    (
                        "No estoy de acuerdo: hay que invertir en otros servicios.",
                        "I don't agree: we need to invest in other services.",
                        "Je ne suis pas d'accord : il faut investir dans d'autres services.",
                    ),
                    (
                        "Podemos buscar un equilibrio.",
                        "We can find a balance.",
                        "Nous pouvons trouver un équilibre.",
                    ),
                ],
            ),
            L(
                "Proyecto final: presentación",
                "la presentación final",
                "Puedo presentar lo que he aprendido y mis próximos objetivos.",
                ["present-perfect", "going-to"],
                "phrase",
                [
                    (
                        "Para terminar, voy a resumir lo que he aprendido.",
                        "To finish, I'm going to summarise what I have learned.",
                        "Pour terminer, je vais résumer ce que j'ai appris.",
                    ),
                    (
                        "He mejorado mi vocabulario y mi pronunciación.",
                        "I have improved my vocabulary and pronunciation.",
                        "J'ai amélioré mon vocabulaire et ma prononciation.",
                    ),
                    (
                        "Me siento más seguro al hablar en inglés.",
                        "I feel more confident speaking English.",
                        "Je me sens plus confiant en parlant anglais.",
                    ),
                ],
            ),
            L(
                "Evaluación A2",
                "la evaluación final",
                "Puedo evaluar mi progreso y fijar mi próximo objetivo.",
                ["present-perfect", "will"],
                "phrase",
                [
                    (
                        "He completado el nivel A2 con éxito.",
                        "I have completed level A2 successfully.",
                        "J'ai terminé le niveau A2 avec succès.",
                    ),
                    (
                        "Puedo describir experiencias, planes y opiniones.",
                        "I can describe experiences, plans and opinions.",
                        "Je peux décrire des expériences, des projets et des opinions.",
                    ),
                    (
                        "Mi próximo objetivo es el nivel B1.",
                        "My next goal is level B1.",
                        "Mon prochain objectif est le niveau B1.",
                    ),
                ],
            ),
        ],
    ),
]

CAPSTONE_FR: list[dict] = [
    L(
        "Diálogo final: una conversación",
        "una conversación cotidiana",
        "Puedo mantener una conversación sencilla y responder a preguntas.",
        ["questions", "present-simple"],
        "phrase",
        [
            (
                "¿Qué haces los fines de semana?",
                "What do you do at the weekend?",
                "Que fais-tu le week-end ?",
            ),
            (
                "Normalmente quedo con amigos y voy al cine.",
                "I usually meet friends and go to the cinema.",
                "D'habitude, je retrouve des amis et je vais au cinéma.",
            ),
            (
                "¿Te gustaría venir con nosotros?",
                "Would you like to come with us?",
                "Est-ce que tu voudrais venir avec nous ?",
            ),
        ],
    ),
    L(
        "Autoevaluación y metas",
        "mis metas de aprendizaje",
        "Puedo hablar de lo que he aprendido y de mis próximos objetivos.",
        ["present-perfect", "going-to"],
        "phrase",
        [
            (
                "He aprendido a contar historias en pasado.",
                "I have learned to tell stories in the past.",
                "J'ai appris à raconter des histoires au passé.",
            ),
            (
                "Todavía necesito practicar la pronunciación.",
                "I still need to practise pronunciation.",
                "J'ai encore besoin de pratiquer la prononciation.",
            ),
            (
                "El próximo mes voy a leer un libro en inglés.",
                "Next month I'm going to read a book in English.",
                "Le mois prochain, je vais lire un livre en anglais.",
            ),
        ],
    ),
]
