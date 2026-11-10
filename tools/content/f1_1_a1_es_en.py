"""F1.1 — A1 completo de ES→EN (150 lecciones nuevas, 15 bloques)."""

from __future__ import annotations

import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-en/src/main/assets/courses/es-en/course.json"
COURSE_ID = "es-en"
NEXT_LEVEL = "es-en-lv-a2"


def L(title: str, topic: str, pattern: str, items: list[tuple[str, str]]) -> dict:
    return {"title": title, "topic": topic, "pattern": pattern, "items": items}


BLOCKS = [
    (
        "Primeros pasos",
        [
            L("Saludos", "los saludos", "say", [("hola", "hello"), ("adiós", "goodbye"), ("gracias", "thank you")]),
            L("Números", "los números", "number", [("uno", "one"), ("dos", "two"), ("tres", "three")]),
            L("Colores", "los colores", "color", [("rojo", "red"), ("azul", "blue"), ("verde", "green")]),
            L("En clase", "los objetos de clase", "classroom", [("un libro", "a book"), ("una silla", "a chair"), ("un lápiz", "a pencil")]),
            L("La familia", "la familia", "family", [("un hermano", "a brother"), ("una hermana", "a sister"), ("un hijo", "a son")]),
            L("Profesiones", "las profesiones", "job", [("profesor", "teacher"), ("médico", "doctor"), ("estudiante", "student")]),
            L("Mascotas", "las mascotas", "have", [("un perro", "a dog"), ("un gato", "a cat"), ("un pájaro", "a bird")]),
            L("Comida", "la comida", "like", [("la pizza", "pizza"), ("la fruta", "fruit"), ("el pan", "bread")]),
            L("Lugares", "los lugares", "place", [("el parque", "the park"), ("la escuela", "the school"), ("el hospital", "the hospital")]),
            L("Descripciones", "las descripciones", "adjective", [("el coche", "the car"), ("la casa", "the house"), ("grande", "big")]),
        ],
    ),
    (
        "Mi día",
        [
            L("Mi rutina", "la rutina diaria", "routine", [("me levanto", "wake up"), ("desayuno", "have breakfast"), ("me acuesto", "go to bed")]),
            L("Las horas", "las horas", "time", [("la una", "one o'clock"), ("las dos", "two o'clock"), ("las tres", "three o'clock")]),
            L("La casa", "las habitaciones", "place", [("la cocina", "the kitchen"), ("el baño", "the bathroom"), ("el salón", "the living room")]),
            L("Objetos de casa", "los objetos de casa", "classroom", [("una cama", "a bed"), ("una mesa", "a table"), ("una ventana", "a window")]),
            L("El desayuno", "el desayuno", "eat", [("un huevo", "an egg"), ("pan", "bread"), ("una manzana", "an apple")]),
            L("Bebidas", "las bebidas", "drink", [("agua", "water"), ("café", "coffee"), ("leche", "milk")]),
            L("Tareas", "las tareas de casa", "verb", [("cocinar", "cook"), ("limpiar", "clean"), ("lavar", "wash")]),
            L("Tiempo libre", "el tiempo libre", "hobby", [("leer", "reading"), ("bailar", "dancing"), ("cantar", "singing")]),
            L("Compras", "las compras", "want", [("una camisa", "a shirt"), ("unos zapatos", "shoes"), ("un sombrero", "a hat")]),
            L("La ciudad", "la ciudad", "adjective", [("el parque", "the park"), ("el museo", "the museum"), ("bonito", "nice")]),
        ],
    ),
    (
        "Personas",
        [
            L("Más familia", "la familia", "family", [("un padre", "a father"), ("una madre", "a mother"), ("un abuelo", "a grandfather")]),
            L("Amigos", "los amigos", "family", [("un amigo", "a friend"), ("una amiga", "a female friend"), ("un vecino", "a neighbour")]),
            L("Edades", "las edades", "age", [("diez", "ten"), ("veinte", "twenty"), ("treinta", "thirty")]),
            L("Descripción física", "la descripción física", "adjective", [("el pelo", "the hair"), ("el brazo", "the arm"), ("largo", "long")]),
            L("Carácter", "el carácter", "adjective", [("el profesor", "the teacher"), ("el estudiante", "the student"), ("simpático", "nice")]),
            L("Ropa", "la ropa", "have", [("una camisa", "a shirt"), ("un pantalón", "trousers"), ("un abrigo", "a coat")]),
            L("Trabajo", "los oficios", "job", [("cocinero", "cook"), ("conductor", "driver"), ("dependiente", "shop assistant")]),
            L("La oficina", "la oficina", "place", [("la oficina", "the office"), ("la tienda", "the shop"), ("el taller", "the workshop")]),
            L("El colegio", "el colegio", "classroom", [("una mesa", "a table"), ("un ordenador", "a computer"), ("un cuaderno", "a notebook")]),
            L("Repaso", "el repaso", "have", [("un libro", "a book"), ("una mochila", "a backpack"), ("un bolígrafo", "a pen")]),
        ],
    ),
    (
        "Comida",
        [
            L("Frutas", "las frutas", "eat", [("una manzana", "an apple"), ("un plátano", "a banana"), ("una naranja", "an orange")]),
            L("Verduras", "las verduras", "eat", [("una zanahoria", "a carrot"), ("un tomate", "a tomato"), ("una cebolla", "an onion")]),
            L("Bebidas", "las bebidas", "drink", [("agua", "water"), ("zumo", "juice"), ("té", "tea")]),
            L("El desayuno", "el desayuno", "eat", [("pan", "bread"), ("un huevo", "an egg"), ("cereales", "cereal")]),
            L("El almuerzo", "el almuerzo", "eat", [("arroz", "rice"), ("pollo", "chicken"), ("pescado", "fish")]),
            L("En el restaurante", "el restaurante", "want", [("una mesa", "a table"), ("un menú", "a menu"), ("la cuenta", "the bill")]),
            L("Gustos", "los gustos", "like", [("el chocolate", "chocolate"), ("el queso", "cheese"), ("la sopa", "soup")]),
            L("En el mercado", "el mercado", "buy", [("una manzana", "an apple"), ("un tomate", "a tomato"), ("una botella de agua", "a bottle of water")]),
            L("Precios", "los precios", "price", [("cinco euros", "five euros"), ("diez euros", "ten euros"), ("veinte euros", "twenty euros")]),
            L("Repaso", "el repaso", "adjective", [("el restaurante", "the restaurant"), ("el menú", "the menu"), ("caro", "expensive")]),
        ],
    ),
    (
        "Casa y ciudad",
        [
            L("Habitaciones", "las habitaciones", "place", [("el dormitorio", "the bedroom"), ("la cocina", "the kitchen"), ("el baño", "the bathroom")]),
            L("Muebles", "los muebles", "have", [("una cama", "a bed"), ("una silla", "a chair"), ("un sofá", "a sofa")]),
            L("La calle", "la calle", "place", [("la calle", "the street"), ("la plaza", "the square"), ("el supermercado", "the supermarket")]),
            L("Direcciones", "las direcciones", "direction", [("a la izquierda", "left"), ("a la derecha", "right"), ("todo recto", "straight")]),
            L("Transporte", "el transporte", "transport", [("en autobús", "by bus"), ("en tren", "by train"), ("en bicicleta", "by bike")]),
            L("La estación", "la estación", "place", [("la estación", "the station"), ("el aeropuerto", "the airport"), ("el hotel", "the hotel")]),
            L("La tienda", "la tienda", "buy", [("pan", "bread"), ("leche", "milk"), ("una camisa", "a shirt")]),
            L("El barrio", "el barrio", "adjective", [("el barrio", "the neighbourhood"), ("el parque", "the park"), ("tranquilo", "quiet")]),
            L("Mi casa", "mi casa", "classroom", [("una casa", "a house"), ("un piso", "a flat"), ("una habitación", "a room")]),
            L("Repaso", "el repaso", "have", [("una llave", "a key"), ("un mapa", "a map"), ("un billete", "a ticket")]),
        ],
    ),
    (
        "Salud",
        [
            L("El cuerpo", "el cuerpo", "body", [("cabeza", "head"), ("mano", "hand"), ("pie", "foot")]),
            L("Síntomas", "los síntomas", "health", [("dolor de cabeza", "a headache"), ("dolor de estómago", "a stomachache"), ("fiebre", "a fever")]),
            L("En el médico", "el médico", "want", [("una cita", "an appointment"), ("un médico", "a doctor"), ("ayuda", "help")]),
            L("La farmacia", "la farmacia", "buy", [("medicina", "medicine"), ("una venda", "a bandage"), ("pastillas", "pills")]),
            L("Hábitos sanos", "los hábitos sanos", "verb", [("caminar", "walk"), ("descansar", "rest"), ("beber agua", "drink water")]),
            L("El deporte", "el deporte", "sport", [("al fútbol", "football"), ("al tenis", "tennis"), ("al baloncesto", "basketball")]),
            L("Dormir", "el descanso", "verb", [("dormir", "sleep"), ("soñar", "dream"), ("respirar", "breathe")]),
            L("El gimnasio", "el gimnasio", "place", [("el gimnasio", "the gym"), ("la piscina", "the pool"), ("el parque", "the park")]),
            L("Emergencias", "las emergencias", "need", [("un médico", "a doctor"), ("una ambulancia", "an ambulance"), ("ayuda", "help")]),
            L("Repaso", "el repaso", "adjective", [("el médico", "the doctor"), ("el hospital", "the hospital"), ("bueno", "good")]),
        ],
    ),
    (
        "Escuela y trabajo",
        [
            L("Asignaturas", "las asignaturas", "like", [("la historia", "history"), ("la ciencia", "science"), ("el arte", "art")]),
            L("En clase", "los objetos de clase", "classroom", [("un libro", "a book"), ("un cuaderno", "a notebook"), ("una regla", "a ruler")]),
            L("La escuela", "la escuela", "place", [("la escuela", "the school"), ("la biblioteca", "the library"), ("el aula", "the classroom")]),
            L("Profesiones", "las profesiones", "job", [("maestro", "teacher"), ("enfermero", "nurse"), ("ingeniero", "engineer")]),
            L("La oficina", "la oficina", "place", [("la oficina", "the office"), ("la reunión", "the meeting"), ("el escritorio", "the desk")]),
            L("Tareas", "las tareas", "verb", [("escribir", "write"), ("leer", "read"), ("hablar", "speak")]),
            L("El horario", "el horario", "time", [("las ocho", "eight o'clock"), ("las diez", "ten o'clock"), ("las doce", "twelve o'clock")]),
            L("Herramientas", "las herramientas", "have", [("un ordenador", "a computer"), ("un teléfono", "a phone"), ("una carpeta", "a folder")]),
            L("El trabajo", "el trabajo", "routine", [("trabajo", "work"), ("estudio", "study"), ("descanso", "rest")]),
            L("Repaso", "el repaso", "adjective", [("el trabajo", "the work"), ("el proyecto", "the project"), ("importante", "important")]),
        ],
    ),
    (
        "Compras",
        [
            L("Ropa", "la ropa", "buy", [("una camisa", "a shirt"), ("un vestido", "a dress"), ("unos zapatos", "shoes")]),
            L("Colores", "los colores", "color", [("rojo", "red"), ("azul", "blue"), ("negro", "black")]),
            L("Tallas", "las tallas", "want", [("una talla pequeña", "a small size"), ("una talla mediana", "a medium size"), ("una talla grande", "a large size")]),
            L("La tienda", "la tienda", "place", [("la tienda", "the shop"), ("el centro comercial", "the shopping centre"), ("el mercado", "the market")]),
            L("Precios", "los precios", "price", [("cinco euros", "five euros"), ("quince euros", "fifteen euros"), ("cincuenta euros", "fifty euros")]),
            L("Pagar", "pagar", "verb", [("pagar", "pay"), ("ahorrar", "save"), ("gastar", "spend")]),
            L("Regalos", "los regalos", "buy", [("un regalo", "a gift"), ("flores", "flowers"), ("una tarjeta", "a card")]),
            L("Gustos", "los gustos", "like", [("la ropa", "clothes"), ("el color azul", "the colour blue"), ("el algodón", "cotton")]),
            L("En el mercado", "el mercado", "buy", [("una manzana", "an apple"), ("pan", "bread"), ("queso", "cheese")]),
            L("Repaso", "el repaso", "adjective", [("el mercado", "the market"), ("el centro comercial", "the shopping centre"), ("grande", "big")]),
        ],
    ),
    (
        "Viajes",
        [
            L("Transporte", "el transporte", "transport", [("en avión", "by plane"), ("en tren", "by train"), ("en coche", "by car")]),
            L("La estación", "la estación", "place", [("la estación", "the station"), ("el aeropuerto", "the airport"), ("la parada", "the stop")]),
            L("El hotel", "el hotel", "want", [("una habitación", "a room"), ("una llave", "a key"), ("un mapa", "a map")]),
            L("El equipaje", "el equipaje", "have", [("una maleta", "a suitcase"), ("un pasaporte", "a passport"), ("un billete", "a ticket")]),
            L("Direcciones", "las direcciones", "direction", [("a la izquierda", "left"), ("a la derecha", "right"), ("todo recto", "straight")]),
            L("Vacaciones", "las vacaciones", "verb", [("viajar", "travel"), ("nadar", "swim"), ("visitar", "visit")]),
            L("El tiempo", "el tiempo", "weather", [("soleado", "sunny"), ("nublado", "cloudy"), ("lluvioso", "rainy")]),
            L("La playa", "la playa", "place", [("la playa", "the beach"), ("el mar", "the sea"), ("la montaña", "the mountain")]),
            L("Compras de viaje", "las compras de viaje", "buy", [("un recuerdo", "a souvenir"), ("agua", "water"), ("un helado", "an ice cream")]),
            L("Repaso", "el repaso", "adjective", [("el viaje", "the trip"), ("el hotel", "the hotel"), ("bonito", "nice")]),
        ],
    ),
    (
        "Tiempo libre",
        [
            L("Pasatiempos", "los pasatiempos", "hobby", [("leer", "reading"), ("pintar", "painting"), ("cocinar", "cooking")]),
            L("Música", "la música", "music", [("la música", "music"), ("el rock", "rock"), ("el pop", "pop")]),
            L("Cine", "el cine", "movie", [("una película", "a film"), ("una serie", "a series"), ("un documental", "a documentary")]),
            L("Deporte", "el deporte", "sport", [("al fútbol", "football"), ("al tenis", "tennis"), ("al baloncesto", "basketball")]),
            L("Invitaciones", "las invitaciones", "invitation", [("salir", "go out"), ("cenar", "have dinner"), ("ver una película", "watch a film")]),
            L("La ciudad", "la ciudad", "place", [("el cine", "the cinema"), ("el museo", "the museum"), ("el restaurante", "the restaurant")]),
            L("Gustos", "los gustos", "like", [("la música", "music"), ("el deporte", "sport"), ("la lectura", "reading")]),
            L("Instrumentos", "los instrumentos", "verb", [("tocar la guitarra", "play the guitar"), ("cantar", "sing"), ("bailar", "dance")]),
            L("El fin de semana", "el fin de semana", "weekend", [("descanso", "rest"), ("salgo", "go out"), ("leo", "read")]),
            L("Repaso", "el repaso", "adjective", [("el libro", "the book"), ("el juego", "the game"), ("divertido", "fun")]),
        ],
    ),
    (
        "Naturaleza",
        [
            L("Animales", "los animales", "have", [("un caballo", "a horse"), ("una vaca", "a cow"), ("una oveja", "a sheep")]),
            L("Animales salvajes", "los animales salvajes", "see", [("un león", "a lion"), ("un elefante", "an elephant"), ("un mono", "a monkey")]),
            L("Plantas", "las plantas", "have", [("una flor", "a flower"), ("un árbol", "a tree"), ("una planta", "a plant")]),
            L("El tiempo", "el tiempo", "weather2", [("calor", "hot"), ("frío", "cold"), ("viento", "windy")]),
            L("Las estaciones", "las estaciones", "season", [("primavera", "spring"), ("verano", "summer"), ("otoño", "autumn")]),
            L("El campo", "el campo", "place", [("el campo", "the countryside"), ("el río", "the river"), ("el bosque", "the forest")]),
            L("La montaña", "la montaña", "place", [("la montaña", "the mountain"), ("el lago", "the lake"), ("el sendero", "the path")]),
            L("Acciones", "las acciones", "verb", [("caminar", "walk"), ("nadar", "swim"), ("correr", "run")]),
            L("El medio ambiente", "el medio ambiente", "verb", [("reciclar", "recycle"), ("ahorrar agua", "save water"), ("cuidar el planeta", "care for the planet")]),
            L("Repaso", "el repaso", "adjective", [("el árbol", "the tree"), ("el río", "the river"), ("bonito", "nice")]),
        ],
    ),
    (
        "Tecnología",
        [
            L("El teléfono", "el teléfono", "have", [("un teléfono", "a phone"), ("una tableta", "a tablet"), ("un cargador", "a charger")]),
            L("El ordenador", "el ordenador", "classroom", [("un ordenador", "a computer"), ("un teclado", "a keyboard"), ("una pantalla", "a screen")]),
            L("Internet", "internet", "verb", [("navegar", "browse"), ("descargar", "download"), ("buscar", "search")]),
            L("Mensajes", "los mensajes", "message", [("un mensaje", "a message"), ("una foto", "a photo"), ("un correo", "an email")]),
            L("Fotos", "las fotos", "verb", [("hacer fotos", "take photos"), ("ver vídeos", "watch videos"), ("escuchar música", "listen to music")]),
            L("Aplicaciones", "las aplicaciones", "want", [("una aplicación", "an app"), ("una cuenta", "an account"), ("una contraseña", "a password")]),
            L("Seguridad", "la seguridad", "need", [("una contraseña", "a password"), ("ayuda", "help"), ("privacidad", "privacy")]),
            L("La red", "la red", "place", [("la red", "the network"), ("la nube", "the cloud"), ("el sitio web", "the website")]),
            L("Problemas", "los problemas", "problem", [("un problema", "a problem"), ("una pregunta", "a question"), ("una solución", "a solution")]),
            L("Repaso", "el repaso", "adjective", [("el teléfono", "the phone"), ("el ordenador", "the computer"), ("nuevo", "new")]),
        ],
    ),
    (
        "Cultura",
        [
            L("Fiestas", "las fiestas", "say", [("feliz cumpleaños", "happy birthday"), ("feliz año nuevo", "happy new year"), ("felices fiestas", "happy holidays")]),
            L("Cumpleaños", "los cumpleaños", "have", [("un pastel", "a cake"), ("un regalo", "a gift"), ("una tarjeta", "a card")]),
            L("Tradiciones", "las tradiciones", "like", [("la música", "music"), ("la comida", "food"), ("la familia", "family")]),
            L("Invitaciones", "las invitaciones", "invitation", [("venir a la fiesta", "come to the party"), ("traer un regalo", "bring a gift"), ("celebrar juntos", "celebrate together")]),
            L("La música", "la música", "music", [("la música tradicional", "traditional music"), ("el flamenco", "flamenco"), ("el tango", "tango")]),
            L("El arte", "el arte", "place", [("el museo", "the museum"), ("la galería", "the gallery"), ("el teatro", "the theatre")]),
            L("Comida típica", "la comida típica", "eat", [("una paella", "a paella"), ("un taco", "a taco"), ("una arepa", "an arepa")]),
            L("Regalos", "los regalos", "buy", [("flores", "flowers"), ("chocolate", "chocolate"), ("un libro", "a book")]),
            L("El cine", "el cine", "movie", [("una película", "a film"), ("un documental", "a documentary"), ("una comedia", "a comedy")]),
            L("Repaso", "el repaso", "adjective", [("el festival", "the festival"), ("el concierto", "the concert"), ("popular", "popular")]),
        ],
    ),
    (
        "Repaso y proyectos",
        [
            L("Repaso de rutina", "la rutina", "routine", [("me levanto", "wake up"), ("trabajo", "work"), ("me acuesto", "go to bed")]),
            L("Repaso de comida", "la comida", "eat", [("una manzana", "an apple"), ("arroz", "rice"), ("pollo", "chicken")]),
            L("Repaso de lugares", "los lugares", "place", [("el parque", "the park"), ("la escuela", "the school"), ("el supermercado", "the supermarket")]),
            L("Repaso de compras", "las compras", "buy", [("una camisa", "a shirt"), ("pan", "bread"), ("un regalo", "a gift")]),
            L("Repaso de viajes", "los viajes", "transport", [("en tren", "by train"), ("en avión", "by plane"), ("en coche", "by car")]),
            L("Planes", "los planes", "want_verb", [("viajar", "travel"), ("estudiar", "study"), ("trabajar", "work")]),
            L("Invitaciones", "las invitaciones", "invitation", [("salir", "go out"), ("cenar", "have dinner"), ("ver una película", "watch a film")]),
            L("Descripciones", "las descripciones", "adjective", [("el proyecto", "the project"), ("el plan", "the plan"), ("interesante", "interesting")]),
            L("Problemas y soluciones", "los problemas", "problem", [("un problema", "a problem"), ("una pregunta", "a question"), ("una solución", "a solution")]),
            L("Evaluación", "el repaso", "have", [("un libro", "a book"), ("una mochila", "a backpack"), ("un bolígrafo", "a pen")]),
        ],
    ),
    (
        "Consolidación A1",
        [
            L("Presentaciones", "las presentaciones", "phrase", [("Me llamo Ana", "My name is Ana"), ("Soy de España", "I am from Spain"), ("Tengo veinte años", "I am twenty years old")]),
            L("Mi familia", "la familia", "phrase", [("Tengo un hermano", "I have a brother"), ("Mi madre es profesora", "My mother is a teacher"), ("Mi padre trabaja", "My father works")]),
            L("Mi rutina", "la rutina", "phrase", [("Me levanto a las siete", "I wake up at seven"), ("Desayuno a las ocho", "I have breakfast at eight"), ("Me acuesto a las diez", "I go to bed at ten")]),
            L("Mi casa", "la casa", "phrase", [("Vivo en un piso", "I live in a flat"), ("Hay tres habitaciones", "There are three rooms"), ("Mi cocina es pequeña", "My kitchen is small")]),
            L("Mi ciudad", "la ciudad", "phrase", [("Vivo en Madrid", "I live in Madrid"), ("Hay un parque", "There is a park"), ("El transporte es bueno", "The transport is good")]),
            L("Comida", "la comida", "phrase", [("Me gusta la pizza", "I like pizza"), ("Como fruta", "I eat fruit"), ("Bebo agua", "I drink water")]),
            L("Trabajo", "el trabajo", "phrase", [("Trabajo en una oficina", "I work in an office"), ("Mi jefe es simpático", "My boss is nice"), ("Tengo una reunión", "I have a meeting")]),
            L("Viajes", "los viajes", "phrase", [("Viajo en tren", "I travel by train"), ("El hotel es bonito", "The hotel is nice"), ("Tengo una maleta", "I have a suitcase")]),
            L("Tiempo libre", "el tiempo libre", "phrase", [("Me gusta leer", "I like reading"), ("Veo una película", "I watch a film"), ("Salgo con amigos", "I go out with friends")]),
            L("Evaluación final", "el repaso", "phrase", [("Hablo inglés", "I speak English"), ("Aprendo portugués", "I learn Portuguese"), ("Quiero viajar", "I want to travel")]),
        ],
    ),
]


def tokens(text: str) -> list[str]:
    return [word for word in re.split(r"[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ']+", text) if word]


def sentences_for(pattern: str, items: list[tuple[str, str]]) -> list[tuple[str, str]]:
    es = [item[0] for item in items]
    en = [item[1] for item in items]
    if pattern == "say":
        return [(f"Digo {es[0]}", f"I say {en[0]}"), (f"Digo {es[1]}", f"I say {en[1]}"), (f"No digo {es[2]}", f"I don't say {en[2]}")]
    if pattern == "number":
        def number_sentence(es_value: str, en_value: str, negative: bool = False) -> tuple[str, str]:
            if es_value == "uno":
                es_text = "No tengo un libro" if negative else "Tengo un libro"
                en_text = "I don't have one book" if negative else "I have one book"
                return es_text, en_text
            es_text = f"{'No tengo' if negative else 'Tengo'} {es_value} libros"
            en_text = (
                f"I don't have {en_value} books"
                if negative
                else f"I have {en_value} books"
            )
            return es_text, en_text
        return [
            number_sentence(es[0], en[0]),
            number_sentence(es[1], en[1]),
            number_sentence(es[2], en[2], negative=True),
        ]
    if pattern == "age":
        return [(f"Tengo {es[0]} años", f"I am {en[0]} years old"), (f"Tengo {es[1]} años", f"I am {en[1]} years old"), (f"Tengo {es[2]} años", f"I am {en[2]} years old")]
    if pattern == "color":
        return [(f"El coche es {es[0]}", f"The car is {en[0]}"), (f"El coche es {es[1]}", f"The car is {en[1]}"), (f"El coche no es {es[2]}", f"The car is not {en[2]}")]
    if pattern == "classroom":
        return [(f"Esto es {es[0]}", f"This is {en[0]}"), (f"Esto es {es[1]}", f"This is {en[1]}"), (f"Esto no es {es[2]}", f"This is not {en[2]}")]
    if pattern == "family":
        return [(f"Tengo {es[0]}", f"I have {en[0]}"), (f"Tengo {es[1]}", f"I have {en[1]}"), (f"No tengo {es[2]}", f"I don't have {en[2]}")]
    if pattern == "job":
        def article(word: str) -> str:
            return "an" if word[:1].lower() in "aeiou" else "a"
        return [
            (f"Soy {es[0]}", f"I am {article(en[0])} {en[0]}"),
            (f"Soy {es[1]}", f"I am {article(en[1])} {en[1]}"),
            (f"No soy {es[2]}", f"I am not {article(en[2])} {en[2]}"),
        ]
    if pattern == "have":
        return [(f"Tengo {es[0]}", f"I have {en[0]}"), (f"Quiero {es[1]}", f"I want {en[1]}"), (f"Necesito {es[2]}", f"I need {en[2]}")]
    if pattern == "like":
        return [(f"Me gusta {es[0]}", f"I like {en[0]}"), (f"Me gusta {es[1]}", f"I like {en[1]}"), (f"No me gusta {es[2]}", f"I don't like {en[2]}")]
    if pattern == "place":
        return [(f"Estoy en {es[0]}", f"I am in {en[0]}"), (f"Estoy en {es[1]}", f"I am in {en[1]}"), (f"No estoy en {es[2]}", f"I am not in {en[2]}")]
    if pattern == "adjective":
        return [(f"{es[0]} es {es[2]}", f"{en[0]} is {en[2]}"), (f"{es[1]} es {es[2]}", f"{en[1]} is {en[2]}"), (f"Me gusta {es[0]}", f"I like {en[0]}")]
    if pattern == "eat":
        return [(f"Como {es[0]}", f"I eat {en[0]}"), (f"Como {es[1]}", f"I eat {en[1]}"), (f"No como {es[2]}", f"I don't eat {en[2]}")]
    if pattern == "drink":
        return [(f"Bebo {es[0]}", f"I drink {en[0]}"), (f"Bebo {es[1]}", f"I drink {en[1]}"), (f"No bebo {es[2]}", f"I don't drink {en[2]}")]
    if pattern == "want":
        return [(f"Quiero {es[0]}", f"I want {en[0]}"), (f"Quiero {es[1]}", f"I want {en[1]}"), (f"No quiero {es[2]}", f"I don't want {en[2]}")]
    if pattern == "need":
        return [(f"Necesito {es[0]}", f"I need {en[0]}"), (f"Necesito {es[1]}", f"I need {en[1]}"), (f"No necesito {es[2]}", f"I don't need {en[2]}")]
    if pattern == "buy":
        return [(f"Compro {es[0]}", f"I buy {en[0]}"), (f"Compro {es[1]}", f"I buy {en[1]}"), (f"No compro {es[2]}", f"I don't buy {en[2]}")]
    if pattern == "see":
        return [(f"Veo {es[0]}", f"I see {en[0]}"), (f"Veo {es[1]}", f"I see {en[1]}"), (f"No veo {es[2]}", f"I don't see {en[2]}")]
    if pattern == "verb":
        return [(f"Puedo {es[0]}", f"I can {en[0]}"), (f"Quiero {es[1]}", f"I want to {en[1]}"), (f"Me gusta {es[2]}", f"I like to {en[2]}")]
    if pattern == "hobby":
        return [(f"Me gusta {es[0]}", f"I like {en[0]}"), (f"Me gusta {es[1]}", f"I like {en[1]}"), (f"No me gusta {es[2]}", f"I don't like {en[2]}")]
    if pattern == "routine":
        return [(f"{es[0]} a las siete", f"I {en[0]} at seven"), (f"{es[1]} a las ocho", f"I {en[1]} at eight"), (f"{es[2]} a las diez", f"I {en[2]} at ten")]
    if pattern == "health":
        return [(f"Tengo {es[0]}", f"I have {en[0]}"), (f"Tengo {es[1]}", f"I have {en[1]}"), (f"No tengo {es[2]}", f"I don't have {en[2]}")]
    if pattern == "body":
        return [(f"Toca tu {es[0]}", f"Touch your {en[0]}"), (f"Toca tu {es[1]}", f"Touch your {en[1]}"), (f"Toca tu {es[2]}", f"Touch your {en[2]}")]
    if pattern == "transport":
        return [(f"Voy {es[0]}", f"I go {en[0]}"), (f"Voy {es[1]}", f"I go {en[1]}"), (f"No voy {es[2]}", f"I don't go {en[2]}")]
    if pattern == "direction":
        return [(f"Ve {es[0]}", f"Go {en[0]}"), (f"Ve {es[1]}", f"Go {en[1]}"), (f"Ve {es[2]}", f"Go {en[2]}")]
    if pattern == "sport":
        return [(f"Juego {es[0]}", f"I play {en[0]}"), (f"Juego {es[1]}", f"I play {en[1]}"), (f"No juego {es[2]}", f"I don't play {en[2]}")]
    if pattern == "music":
        return [(f"Escucho {es[0]}", f"I listen to {en[0]}"), (f"Escucho {es[1]}", f"I listen to {en[1]}"), (f"No escucho {es[2]}", f"I don't listen to {en[2]}")]
    if pattern == "movie":
        return [(f"Veo {es[0]}", f"I watch {en[0]}"), (f"Veo {es[1]}", f"I watch {en[1]}"), (f"No veo {es[2]}", f"I don't watch {en[2]}")]
    if pattern == "invitation":
        return [(f"¿Quieres {es[0]}?", f"Do you want to {en[0]}?"), (f"¿Quieres {es[1]}?", f"Do you want to {en[1]}?"), (f"¿Quieres {es[2]}?", f"Do you want to {en[2]}?")]
    if pattern == "time":
        def time_sentence(es_value: str, en_value: str, negative: bool = False) -> tuple[str, str]:
            plural = es_value.startswith("las ")
            if negative:
                es_text = f"{'No son' if plural else 'No es'} {es_value}"
                en_text = f"It is not {en_value}"
            else:
                es_text = f"{'Son' if plural else 'Es'} {es_value}"
                en_text = f"It is {en_value}"
            return es_text, en_text
        return [
            time_sentence(es[0], en[0]),
            time_sentence(es[1], en[1]),
            time_sentence(es[2], en[2], negative=True),
        ]
    if pattern == "price":
        return [(f"Cuesta {es[0]}", f"It costs {en[0]}"), (f"Cuesta {es[1]}", f"It costs {en[1]}"), (f"No cuesta {es[2]}", f"It doesn't cost {en[2]}")]
    if pattern == "weather":
        return [(f"Hoy está {es[0]}", f"Today it is {en[0]}"), (f"Hoy está {es[1]}", f"Today it is {en[1]}"), (f"Hoy no está {es[2]}", f"Today it is not {en[2]}")]
    if pattern == "weather2":
        return [(f"Hoy hace {es[0]}", f"Today it is {en[0]}"), (f"Hoy hace {es[1]}", f"Today it is {en[1]}"), (f"Hoy no hace {es[2]}", f"Today it is not {en[2]}")]
    if pattern == "season":
        return [(f"En {es[0]} hace buen tiempo", f"In {en[0]} the weather is nice"), (f"En {es[1]} hace buen tiempo", f"In {en[1]} the weather is nice"), (f"En {es[2]} hace buen tiempo", f"In {en[2]} the weather is nice")]
    if pattern == "message":
        return [(f"Envío {es[0]}", f"I send {en[0]}"), (f"Envío {es[1]}", f"I send {en[1]}"), (f"No envío {es[2]}", f"I don't send {en[2]}")]
    if pattern == "problem":
        return [(f"Tengo {es[0]}", f"I have {en[0]}"), (f"Tengo {es[1]}", f"I have {en[1]}"), (f"No tengo {es[2]}", f"I don't have {en[2]}")]
    if pattern == "want_verb":
        return [(f"Quiero {es[0]}", f"I want to {en[0]}"), (f"Quiero {es[1]}", f"I want to {en[1]}"), (f"No quiero {es[2]}", f"I don't want to {en[2]}")]
    if pattern == "weekend":
        return [(f"{es[0]} el sábado", f"I {en[0]} on Saturday"), (f"{es[1]} el sábado", f"I {en[1]} on Saturday"), (f"{es[2]} el sábado", f"I {en[2]} on Saturday")]
    if pattern == "phrase":
        return [(es[0], en[0]), (es[1], en[1]), (es[2], en[2])]
    raise ValueError(f"Patrón desconocido: {pattern}")


def safe_distractors(correct: list[str], candidates: list[list[str]]) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    for fallback in ["very", "today", "here", "now", "also", "too"]:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def build_lesson(block: int, index: int, spec: dict) -> dict:
    lesson_id = f"{COURSE_ID}-a1-u{block:02d}-l{index:02d}"
    items = spec["items"]
    sentences = sentences_for(spec["pattern"], items)
    en_sentences = [sentence[1] for sentence in sentences]
    s0_tokens = tokens(en_sentences[0])
    s1_tokens = tokens(en_sentences[1])
    s2_tokens = tokens(en_sentences[2])
    first_es, first_en = items[0]
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": f"Puedo usar frases sencillas sobre {spec['topic']}.",
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']}.",
            ],
            "sections": [
                {
                    "heading": "Palabras y frases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las palabras nuevas.",
                    "examples": [
                        {"source": sentences[0][0], "target": sentences[0][1]},
                        {"source": sentences[1][0], "target": sentences[1][1]},
                    ],
                }
            ],
        },
        "glossary": [
            {"term": en, "translations": [es]}
            for es, en in items
        ],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}»?",
                "options": [en for _, en in items],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_es}» se dice «{first_en}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": sentences[0][0],
                "correctTokens": s0_tokens,
                "distractorTokens": safe_distractors(s0_tokens, [s1_tokens, s2_tokens]),
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"El orden correcto es «{' '.join(s0_tokens)}».",
                },
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (s1_tokens[1:] + s1_tokens[:1]) if len(s1_tokens) > 1 else s1_tokens,
                "answerTokens": s1_tokens,
                "sourcePhrase": sentences[1][0],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{' '.join(s1_tokens)}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [{"left": es, "right": en} for es, en in items],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": en_sentences[2],
                "options": en_sentences,
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "comprehension",
                    "explanation": f"El audio dice «{en_sentences[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": en_sentences[2],
                "transcription": en_sentences[2],
                "feedbackOnError": {
                    "type": "spelling",
                    "explanation": f"Escribe exactamente «{en_sentences[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": en_sentences[2],
                "audioText": en_sentences[2],
                "feedbackOnError": {
                    "type": "pronunciation",
                    "explanation": f"Repite «{en_sentences[2]}» con claridad.",
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
        print("F1.1 ya existe; no se modifica.")
        return
    lessons = []
    units = []
    for block_number, (block_title, block_lessons) in enumerate(BLOCKS, start=1):
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
