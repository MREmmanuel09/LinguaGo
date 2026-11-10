"""F1.2 — A1 completo de ES→PT (150 lecciones nuevas, 15 bloques)."""

from __future__ import annotations

import importlib.util
import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSE = ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json"
COURSE_ID = "es-pt"
NEXT_LEVEL = "es-pt-lv-a2"

SPEC = importlib.util.spec_from_file_location("f1_1", ROOT / "tools/content/f1_1_a1_es_en.py")
F1_1 = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(F1_1)


def L(title: str, topic: str, pattern: str, items: list[tuple[str, str]]) -> dict:
    return {"title": title, "topic": topic, "pattern": pattern, "items": items}


BLOCKS = [
    (
        "Primeiros passos",
        [
            L("Saudações", "as saudações", "say", [("hola", "olá"), ("adiós", "tchau"), ("gracias", "obrigado")]),
            L("Números", "os números", "number", [("uno", "um"), ("dos", "dois"), ("tres", "três")]),
            L("Cores", "as cores", "color", [("rojo", "vermelho"), ("azul", "azul"), ("verde", "verde")]),
            L("Na sala de aula", "os objetos de sala de aula", "classroom", [("un libro", "um livro"), ("una silla", "uma cadeira"), ("un lápiz", "um lápis")]),
            L("A família", "a família", "family", [("un hermano", "um irmão"), ("una hermana", "uma irmã"), ("un hijo", "um filho")]),
            L("Profissões", "as profissões", "job", [("profesor", "professor"), ("médico", "médico"), ("estudiante", "estudante")]),
            L("Animais de estimação", "os animais de estimação", "have", [("un perro", "um cachorro"), ("un gato", "um gato"), ("un pájaro", "um pássaro")]),
            L("Comida", "a comida", "like", [("la pizza", "pizza"), ("la fruta", "fruta"), ("el pan", "pão")]),
            L("Lugares", "os lugares", "place", [("el parque", "o parque"), ("la escuela", "a escola"), ("el hospital", "o hospital")]),
            L("Descrições", "as descrições", "adjective", [("el coche", "o carro"), ("la casa", "a casa"), ("grande", "grande")]),
        ],
    ),
    (
        "Meu dia",
        [
            L("Minha rotina", "a rotina diária", "routine", [("me levanto", "eu acordo"), ("desayuno", "eu tomo café da manhã"), ("me acuesto", "eu me deito")]),
            L("As horas", "as horas", "time", [("la una", "uma hora"), ("las dos", "duas horas"), ("las tres", "três horas")]),
            L("A casa", "os cômodos", "place", [("la cocina", "a cozinha"), ("el baño", "o banheiro"), ("el salón", "a sala")]),
            L("Objetos da casa", "os objetos da casa", "classroom", [("una cama", "uma cama"), ("una mesa", "uma mesa"), ("una ventana", "uma janela")]),
            L("O café da manhã", "o café da manhã", "eat", [("un huevo", "um ovo"), ("pan", "pão"), ("una manzana", "uma maçã")]),
            L("Bebidas", "as bebidas", "drink", [("agua", "água"), ("café", "café"), ("leche", "leite")]),
            L("Tarefas", "as tarefas de casa", "verb", [("cocinar", "cozinhar"), ("limpiar", "limpar"), ("lavar", "lavar")]),
            L("Tempo livre", "o tempo livre", "hobby", [("leer", "ler"), ("bailar", "dançar"), ("cantar", "cantar")]),
            L("Compras", "as compras", "want", [("una camisa", "uma camisa"), ("unos zapatos", "sapatos"), ("un sombrero", "um chapéu")]),
            L("A cidade", "a cidade", "adjective", [("el parque", "o parque"), ("el museo", "o museu"), ("bonito", "bonito")]),
        ],
    ),
    (
        "Pessoas",
        [
            L("Mais família", "a família", "family", [("un padre", "um pai"), ("una madre", "uma mãe"), ("un abuelo", "um avô")]),
            L("Amigos", "os amigos", "family", [("un amigo", "um amigo"), ("una amiga", "uma amiga"), ("un vecino", "um vizinho")]),
            L("Idades", "as idades", "age", [("diez", "dez"), ("veinte", "vinte"), ("treinta", "trinta")]),
            L("Descrição física", "a descrição física", "adjective", [("el pelo", "o cabelo"), ("el brazo", "o braço"), ("largo", "longo")]),
            L("Caráter", "o caráter", "adjective", [("el profesor", "o professor"), ("el estudiante", "o estudante"), ("simpático", "simpático")]),
            L("Roupas", "as roupas", "have", [("una camisa", "uma camisa"), ("un pantalón", "uma calça"), ("un abrigo", "um casaco")]),
            L("Trabalho", "os ofícios", "job", [("cocinero", "cozinheiro"), ("conductor", "motorista"), ("dependiente", "vendedor")]),
            L("O escritório", "o escritório", "place", [("la oficina", "o escritório"), ("la tienda", "a loja"), ("el taller", "a oficina")]),
            L("A escola", "a escola", "classroom", [("una mesa", "uma mesa"), ("un ordenador", "um computador"), ("un cuaderno", "um caderno")]),
            L("Revisão", "a revisão", "have", [("un libro", "um livro"), ("una mochila", "uma mochila"), ("un bolígrafo", "uma caneta")]),
        ],
    ),
    (
        "Comida",
        [
            L("Frutas", "as frutas", "eat", [("una manzana", "uma maçã"), ("un plátano", "uma banana"), ("una naranja", "uma laranja")]),
            L("Verduras", "as verduras", "eat", [("una zanahoria", "uma cenoura"), ("un tomate", "um tomate"), ("una cebolla", "uma cebola")]),
            L("Bebidas", "as bebidas", "drink", [("agua", "água"), ("zumo", "suco"), ("té", "chá")]),
            L("O café da manhã", "o café da manhã", "eat", [("pan", "pão"), ("un huevo", "um ovo"), ("cereales", "cereais")]),
            L("O almoço", "o almoço", "eat", [("arroz", "arroz"), ("pollo", "frango"), ("pescado", "peixe")]),
            L("No restaurante", "o restaurante", "want", [("una mesa", "uma mesa"), ("un menú", "um menu"), ("la cuenta", "a conta")]),
            L("Gostos", "os gostos", "like", [("el chocolate", "chocolate"), ("el queso", "queijo"), ("la sopa", "sopa")]),
            L("No mercado", "o mercado", "buy", [("una manzana", "uma maçã"), ("un tomate", "um tomate"), ("una botella de agua", "uma garrafa de água")]),
            L("Preços", "os preços", "price", [("cinco euros", "cinco euros"), ("diez euros", "dez euros"), ("veinte euros", "vinte euros")]),
            L("Revisão", "a revisão", "adjective", [("el restaurante", "o restaurante"), ("el menú", "o menu"), ("caro", "caro")]),
        ],
    ),
    (
        "Casa e cidade",
        [
            L("Quartos", "os quartos", "place", [("el dormitorio", "o quarto"), ("la cocina", "a cozinha"), ("el baño", "o banheiro")]),
            L("Móveis", "os móveis", "have", [("una cama", "uma cama"), ("una silla", "uma cadeira"), ("un sofá", "um sofá")]),
            L("A rua", "a rua", "place", [("la calle", "a rua"), ("la plaza", "a praça"), ("el supermercado", "o supermercado")]),
            L("Direções", "as direções", "direction", [("a la izquierda", "à esquerda"), ("a la derecha", "à direita"), ("todo recto", "em frente")]),
            L("Transporte", "o transporte", "transport", [("en autobús", "de ônibus"), ("en tren", "de trem"), ("en bicicleta", "de bicicleta")]),
            L("A estação", "a estação", "place", [("la estación", "a estação"), ("el aeropuerto", "o aeroporto"), ("el hotel", "o hotel")]),
            L("A loja", "a loja", "buy", [("pan", "pão"), ("leche", "leite"), ("una camisa", "uma camisa")]),
            L("O bairro", "o bairro", "adjective", [("el barrio", "o bairro"), ("el parque", "o parque"), ("tranquilo", "tranquilo")]),
            L("Minha casa", "minha casa", "classroom", [("una casa", "uma casa"), ("un piso", "um apartamento"), ("una habitación", "um quarto")]),
            L("Revisão", "a revisão", "have", [("una llave", "uma chave"), ("un mapa", "um mapa"), ("un billete", "uma passagem")]),
        ],
    ),
    (
        "Saúde",
        [
            L("O corpo", "o corpo", "body", [("cabeza", "a cabeça"), ("mano", "a mão"), ("pie", "o pé")]),
            L("Sintomas", "os sintomas", "health", [("dolor de cabeza", "dor de cabeça"), ("dolor de estómago", "dor de estômago"), ("fiebre", "febre")]),
            L("No médico", "o médico", "want", [("una cita", "uma consulta"), ("un médico", "um médico"), ("ayuda", "ajuda")]),
            L("A farmácia", "a farmácia", "buy", [("medicina", "remédio"), ("una venda", "uma bandagem"), ("pastillas", "comprimidos")]),
            L("Hábitos saudáveis", "os hábitos saudáveis", "verb", [("caminar", "caminhar"), ("descansar", "descansar"), ("beber agua", "beber água")]),
            L("O esporte", "o esporte", "sport", [("al fútbol", "futebol"), ("al tenis", "tênis"), ("al baloncesto", "basquete")]),
            L("Dormir", "o descanso", "verb", [("dormir", "dormir"), ("soñar", "sonhar"), ("respirar", "respirar")]),
            L("A academia", "a academia", "place", [("el gimnasio", "a academia"), ("la piscina", "a piscina"), ("el parque", "o parque")]),
            L("Emergências", "as emergências", "need", [("un médico", "um médico"), ("una ambulancia", "uma ambulância"), ("ayuda", "ajuda")]),
            L("Revisão", "a revisão", "adjective", [("el médico", "o médico"), ("el hospital", "o hospital"), ("bueno", "bom")]),
        ],
    ),
    (
        "Escola e trabalho",
        [
            L("Matérias", "as matérias", "like", [("la historia", "história"), ("la ciencia", "ciência"), ("el arte", "arte")]),
            L("Na sala de aula", "os objetos de sala de aula", "classroom", [("un libro", "um livro"), ("un cuaderno", "um caderno"), ("una regla", "uma régua")]),
            L("A escola", "a escola", "place", [("la escuela", "a escola"), ("la biblioteca", "a biblioteca"), ("el aula", "a sala de aula")]),
            L("Profissões", "as profissões", "job", [("maestro", "professor"), ("enfermero", "enfermeiro"), ("ingeniero", "engenheiro")]),
            L("O escritório", "o escritório", "place", [("la oficina", "o escritório"), ("la reunión", "a reunião"), ("el escritorio", "a mesa")]),
            L("Tarefas", "as tarefas", "verb", [("escribir", "escrever"), ("leer", "ler"), ("hablar", "falar")]),
            L("O horário", "o horário", "time", [("las ocho", "oito horas"), ("las diez", "dez horas"), ("las doce", "doze horas")]),
            L("Ferramentas", "as ferramentas", "have", [("un ordenador", "um computador"), ("un teléfono", "um celular"), ("una carpeta", "uma pasta")]),
            L("O trabalho", "o trabalho", "routine", [("trabajo", "eu trabalho"), ("estudio", "eu estudo"), ("descanso", "eu descanso")]),
            L("Revisão", "a revisão", "adjective", [("el trabajo", "o trabalho"), ("el proyecto", "o projeto"), ("importante", "importante")]),
        ],
    ),
    (
        "Compras",
        [
            L("Roupas", "as roupas", "buy", [("una camisa", "uma camisa"), ("un vestido", "um vestido"), ("unos zapatos", "sapatos")]),
            L("Cores", "as cores", "color", [("rojo", "vermelho"), ("azul", "azul"), ("negro", "preto")]),
            L("Tamanhos", "os tamanhos", "want", [("una talla pequeña", "um tamanho pequeno"), ("una talla mediana", "um tamanho médio"), ("una talla grande", "um tamanho grande")]),
            L("A loja", "a loja", "place", [("la tienda", "a loja"), ("el centro comercial", "o shopping"), ("el mercado", "o mercado")]),
            L("Preços", "os preços", "price", [("cinco euros", "cinco euros"), ("quince euros", "quinze euros"), ("cincuenta euros", "cinquenta euros")]),
            L("Pagar", "pagar", "verb", [("pagar", "pagar"), ("ahorrar", "economizar"), ("gastar", "gastar")]),
            L("Presentes", "os presentes", "buy", [("un regalo", "um presente"), ("flores", "flores"), ("una tarjeta", "um cartão")]),
            L("Gostos", "os gostos", "like", [("la ropa", "roupa"), ("el color azul", "a cor azul"), ("el algodón", "algodão")]),
            L("No mercado", "o mercado", "buy", [("una manzana", "uma maçã"), ("pan", "pão"), ("queso", "queijo")]),
            L("Revisão", "a revisão", "adjective", [("el mercado", "o mercado"), ("el centro comercial", "o shopping"), ("grande", "grande")]),
        ],
    ),
    (
        "Viagens",
        [
            L("Transporte", "o transporte", "transport", [("en avión", "de avião"), ("en tren", "de trem"), ("en coche", "de carro")]),
            L("A estação", "a estação", "place", [("la estación", "a estação"), ("el aeropuerto", "o aeroporto"), ("la parada", "a parada")]),
            L("O hotel", "o hotel", "want", [("una habitación", "um quarto"), ("una llave", "uma chave"), ("un mapa", "um mapa")]),
            L("A bagagem", "a bagagem", "have", [("una maleta", "uma mala"), ("un pasaporte", "um passaporte"), ("un billete", "uma passagem")]),
            L("Direções", "as direções", "direction", [("a la izquierda", "à esquerda"), ("a la derecha", "à direita"), ("todo recto", "em frente")]),
            L("Férias", "as férias", "verb", [("viajar", "viajar"), ("nadar", "nadar"), ("visitar", "visitar")]),
            L("O tempo", "o tempo", "weather", [("soleado", "ensolarado"), ("nublado", "nublado"), ("lluvioso", "chuvoso")]),
            L("A praia", "a praia", "place", [("la playa", "a praia"), ("el mar", "o mar"), ("la montaña", "a montanha")]),
            L("Compras de viagem", "as compras de viagem", "buy", [("un recuerdo", "uma lembrança"), ("agua", "água"), ("un helado", "um sorvete")]),
            L("Revisão", "a revisão", "adjective", [("el viaje", "a viagem"), ("el hotel", "o hotel"), ("bonito", "bonito")]),
        ],
    ),
    (
        "Tempo livre",
        [
            L("Passatempos", "os passatempos", "hobby", [("leer", "ler"), ("pintar", "pintar"), ("cocinar", "cozinhar")]),
            L("Música", "a música", "music", [("la música", "música"), ("el rock", "rock"), ("el pop", "pop")]),
            L("Cinema", "o cinema", "movie", [("una película", "um filme"), ("una serie", "uma série"), ("un documental", "um documentário")]),
            L("Esporte", "o esporte", "sport", [("al fútbol", "futebol"), ("al tenis", "tênis"), ("al baloncesto", "basquete")]),
            L("Convites", "os convites", "invitation", [("salir", "sair"), ("cenar", "jantar"), ("ver una película", "ver um filme")]),
            L("A cidade", "a cidade", "place", [("el cine", "o cinema"), ("el museo", "o museu"), ("el restaurante", "o restaurante")]),
            L("Gostos", "os gostos", "like", [("la música", "música"), ("el deporte", "esporte"), ("la lectura", "leitura")]),
            L("Instrumentos", "os instrumentos", "verb", [("tocar la guitarra", "tocar guitarra"), ("cantar", "cantar"), ("bailar", "dançar")]),
            L("O fim de semana", "o fim de semana", "weekend", [("descanso", "eu descanso"), ("salgo", "eu saio"), ("leo", "eu leio")]),
            L("Revisão", "a revisão", "adjective", [("el libro", "o livro"), ("el juego", "o jogo"), ("divertido", "divertido")]),
        ],
    ),
    (
        "Natureza",
        [
            L("Animais", "os animais", "have", [("un caballo", "um cavalo"), ("una vaca", "uma vaca"), ("una oveja", "uma ovelha")]),
            L("Animais selvagens", "os animais selvagens", "see", [("un león", "um leão"), ("un elefante", "um elefante"), ("un mono", "um macaco")]),
            L("Plantas", "as plantas", "have", [("una flor", "uma flor"), ("un árbol", "uma árvore"), ("una planta", "uma planta")]),
            L("O tempo", "o tempo", "weather_pt", [("sol", "sol"), ("lluvia", "chuva"), ("viento", "vento")]),
            L("As estações", "as estações", "season", [("primavera", "a primavera"), ("verano", "o verão"), ("otoño", "o outono")]),
            L("O campo", "o campo", "place", [("el campo", "o campo"), ("el río", "o rio"), ("el bosque", "a floresta")]),
            L("A montanha", "a montanha", "place", [("la montaña", "a montanha"), ("el lago", "o lago"), ("el sendero", "a trilha")]),
            L("Ações", "as ações", "verb", [("caminar", "caminhar"), ("nadar", "nadar"), ("correr", "correr")]),
            L("O meio ambiente", "o meio ambiente", "verb", [("reciclar", "reciclar"), ("ahorrar agua", "economizar água"), ("cuidar el planeta", "cuidar do planeta")]),
            L("Revisão", "a revisão", "adjective", [("el río", "o rio"), ("el parque", "o parque"), ("bonito", "bonito")]),
        ],
    ),
    (
        "Tecnologia",
        [
            L("O celular", "o celular", "have", [("un teléfono", "um celular"), ("una tableta", "um tablet"), ("un cargador", "um carregador")]),
            L("O computador", "o computador", "classroom", [("un ordenador", "um computador"), ("un teclado", "um teclado"), ("una pantalla", "uma tela")]),
            L("Internet", "internet", "verb", [("navegar", "navegar"), ("descargar", "baixar"), ("buscar", "pesquisar")]),
            L("Mensagens", "as mensagens", "message", [("un mensaje", "uma mensagem"), ("una foto", "uma foto"), ("un correo", "um e-mail")]),
            L("Fotos", "as fotos", "verb", [("hacer fotos", "tirar fotos"), ("ver vídeos", "ver vídeos"), ("escuchar música", "escutar música")]),
            L("Aplicativos", "os aplicativos", "want", [("una aplicación", "um aplicativo"), ("una cuenta", "uma conta"), ("una contraseña", "uma senha")]),
            L("Segurança", "a segurança", "need", [("una contraseña", "uma senha"), ("ayuda", "ajuda"), ("privacidad", "privacidade")]),
            L("A rede", "a rede", "place", [("la red", "a rede"), ("la nube", "a nuvem"), ("el sitio web", "o site")]),
            L("Problemas", "os problemas", "problem", [("un problema", "um problema"), ("una pregunta", "uma pergunta"), ("una solución", "uma solução")]),
            L("Revisão", "a revisão", "adjective", [("el teléfono", "o celular"), ("el ordenador", "o computador"), ("nuevo", "novo")]),
        ],
    ),
    (
        "Cultura",
        [
            L("Festas", "as festas", "say", [("feliz cumpleaños", "feliz aniversário"), ("feliz año nuevo", "feliz ano novo"), ("felices fiestas", "boas festas")]),
            L("Aniversários", "os aniversários", "have", [("un pastel", "um bolo"), ("un regalo", "um presente"), ("una tarjeta", "um cartão")]),
            L("Tradições", "as tradições", "like", [("la música", "música"), ("la comida", "comida"), ("la familia", "família")]),
            L("Convites", "os convites", "invitation", [("venir a la fiesta", "vir à festa"), ("traer un regalo", "trazer um presente"), ("celebrar juntos", "celebrar juntos")]),
            L("A música", "a música", "music", [("la música tradicional", "música tradicional"), ("el flamenco", "flamenco"), ("el tango", "tango")]),
            L("A arte", "a arte", "place", [("el museo", "o museu"), ("la galería", "a galeria"), ("el teatro", "o teatro")]),
            L("Comida típica", "a comida típica", "eat", [("una paella", "uma paella"), ("un taco", "um taco"), ("una arepa", "uma arepa")]),
            L("Presentes", "os presentes", "buy", [("flores", "flores"), ("chocolate", "chocolate"), ("un libro", "um livro")]),
            L("O cinema", "o cinema", "movie", [("una película", "um filme"), ("un documental", "um documentário"), ("una comedia", "uma comédia")]),
            L("Revisão", "a revisão", "adjective", [("el festival", "o festival"), ("el concierto", "o show"), ("popular", "popular")]),
        ],
    ),
    (
        "Revisão e projetos",
        [
            L("Revisão de rotina", "a rotina", "routine", [("me levanto", "eu acordo"), ("trabajo", "eu trabalho"), ("me acuesto", "eu me deito")]),
            L("Revisão de comida", "a comida", "eat", [("una manzana", "uma maçã"), ("arroz", "arroz"), ("pollo", "frango")]),
            L("Revisão de lugares", "os lugares", "place", [("el parque", "o parque"), ("la escuela", "a escola"), ("el supermercado", "o supermercado")]),
            L("Revisão de compras", "as compras", "buy", [("una camisa", "uma camisa"), ("pan", "pão"), ("un regalo", "um presente")]),
            L("Revisão de viagens", "as viagens", "transport", [("en tren", "de trem"), ("en avión", "de avião"), ("en coche", "de carro")]),
            L("Planos", "os planos", "want_verb", [("viajar", "viajar"), ("estudiar", "estudar"), ("trabajar", "trabalhar")]),
            L("Convites", "os convites", "invitation", [("salir", "sair"), ("cenar", "jantar"), ("ver una película", "ver um filme")]),
            L("Descrições", "as descrições", "adjective", [("el proyecto", "o projeto"), ("el plan", "o plano"), ("interesante", "interessante")]),
            L("Problemas e soluções", "os problemas", "problem", [("un problema", "um problema"), ("una pregunta", "uma pergunta"), ("una solución", "uma solução")]),
            L("Avaliação", "a revisão", "have", [("un libro", "um livro"), ("una mochila", "uma mochila"), ("un bolígrafo", "uma caneta")]),
        ],
    ),
    (
        "Consolidação A1",
        [
            L("Apresentações", "as apresentações", "phrase", [("Me llamo Ana", "Meu nome é Ana"), ("Soy de España", "Sou da Espanha"), ("Tengo veinte años", "Tenho vinte anos")]),
            L("Minha família", "a família", "phrase", [("Tengo un hermano", "Tenho um irmão"), ("Mi madre es profesora", "Minha mãe é professora"), ("Mi padre trabaja", "Meu pai trabalha")]),
            L("Minha rotina", "a rotina", "phrase", [("Me levanto a las siete", "Eu acordo às sete"), ("Desayuno a las ocho", "Eu tomo café da manhã às oito"), ("Me acuesto a las diez", "Eu me deito às dez")]),
            L("Minha casa", "a casa", "phrase", [("Vivo en un piso", "Eu moro em um apartamento"), ("Hay tres habitaciones", "Há três quartos"), ("Mi cocina es pequeña", "Minha cozinha é pequena")]),
            L("Minha cidade", "a cidade", "phrase", [("Vivo en Madrid", "Eu moro em Madri"), ("Hay un parque", "Há um parque"), ("El transporte es bueno", "O transporte é bom")]),
            L("Comida", "a comida", "phrase", [("Me gusta la pizza", "Eu gosto de pizza"), ("Como fruta", "Eu como fruta"), ("Bebo agua", "Eu bebo água")]),
            L("Trabalho", "o trabalho", "phrase", [("Trabajo en una oficina", "Eu trabalho em um escritório"), ("Mi jefe es simpático", "Meu chefe é simpático"), ("Tengo una reunión", "Eu tenho uma reunião")]),
            L("Viagens", "as viagens", "phrase", [("Viajo en tren", "Eu viajo de trem"), ("El hotel es bonito", "O hotel é bonito"), ("Tengo una maleta", "Eu tenho uma mala")]),
            L("Tempo livre", "o tempo livre", "phrase", [("Me gusta leer", "Eu gosto de ler"), ("Veo una película", "Eu vejo um filme"), ("Salgo con amigos", "Eu saio com amigos")]),
            L("Avaliação final", "a revisão", "phrase", [("Hablo inglés", "Eu falo inglês"), ("Aprendo portugués", "Eu aprendo português"), ("Quiero viajar", "Eu quero viajar")]),
        ],
    ),
]


def tokens(text: str) -> list[str]:
    return re.findall(r"[A-Za-zÀ-ÖØ-öø-ÿ0-9']+", text)


def cap(text: str) -> str:
    return text[:1].upper() + text[1:] if text else text


def em_contract(phrase: str) -> str:
    for article, contraction in (("o ", "no "), ("a ", "na "), ("os ", "nos "), ("as ", "nas ")):
        if phrase.startswith(article):
            return contraction + phrase[len(article):]
    return "em " + phrase


def de_contract(phrase: str) -> str:
    for article, contraction in (("o ", "do "), ("a ", "da "), ("os ", "dos "), ("as ", "das ")):
        if phrase.startswith(article):
            return contraction + phrase[len(article):]
    return "de " + phrase


def article(word: str) -> str:
    return "an" if word[:1].lower() in "aeiou" else "a"


def pt_sentences(pattern: str, items: list[tuple[str, str]]) -> list[str]:
    pt = [item[1] for item in items]
    if pattern == "say":
        return [f"Eu digo {pt[0]}", f"Eu digo {pt[1]}", f"Eu não digo {pt[2]}"]
    if pattern == "number":
        def number(value: str, negative: bool = False) -> str:
            if value == "um":
                return "Eu não tenho um livro" if negative else "Eu tenho um livro"
            return f"{'Eu não tenho' if negative else 'Eu tenho'} {value} livros"
        return [number(pt[0]), number(pt[1]), number(pt[2], True)]
    if pattern == "age":
        return [f"Eu tenho {pt[0]} anos", f"Eu tenho {pt[1]} anos", f"Eu tenho {pt[2]} anos"]
    if pattern == "color":
        return [f"O carro é {pt[0]}", f"O carro é {pt[1]}", f"O carro não é {pt[2]}"]
    if pattern == "classroom":
        return [f"Isto é {pt[0]}", f"Isto é {pt[1]}", f"Isto não é {pt[2]}"]
    if pattern == "family":
        return [f"Eu tenho {pt[0]}", f"Eu tenho {pt[1]}", f"Eu não tenho {pt[2]}"]
    if pattern == "job":
        return [f"Eu sou {pt[0]}", f"Eu sou {pt[1]}", f"Eu não sou {pt[2]}"]
    if pattern == "have":
        return [f"Eu tenho {pt[0]}", f"Eu quero {pt[1]}", f"Eu preciso {de_contract(pt[2])}"]
    if pattern == "like":
        return [f"Eu gosto {de_contract(pt[0])}", f"Eu gosto {de_contract(pt[1])}", f"Eu não gosto {de_contract(pt[2])}"]
    if pattern == "place":
        return [f"Eu estou {em_contract(pt[0])}", f"Eu estou {em_contract(pt[1])}", f"Eu não estou {em_contract(pt[2])}"]
    if pattern == "adjective":
        return [f"{pt[0]} é {pt[2]}", f"{pt[1]} é {pt[2]}", f"Eu vejo {pt[0]}"]
    if pattern == "eat":
        return [f"Eu como {pt[0]}", f"Eu como {pt[1]}", f"Eu não como {pt[2]}"]
    if pattern == "drink":
        return [f"Eu bebo {pt[0]}", f"Eu bebo {pt[1]}", f"Eu não bebo {pt[2]}"]
    if pattern == "want":
        return [f"Eu quero {pt[0]}", f"Eu quero {pt[1]}", f"Eu não quero {pt[2]}"]
    if pattern == "need":
        return [f"Eu preciso {de_contract(pt[0])}", f"Eu preciso {de_contract(pt[1])}", f"Eu não preciso {de_contract(pt[2])}"]
    if pattern == "buy":
        return [f"Eu compro {pt[0]}", f"Eu compro {pt[1]}", f"Eu não compro {pt[2]}"]
    if pattern == "see":
        return [f"Eu vejo {pt[0]}", f"Eu vejo {pt[1]}", f"Eu não vejo {pt[2]}"]
    if pattern == "verb":
        return [f"Eu posso {pt[0]}", f"Eu quero {pt[1]}", f"Eu gosto {de_contract(pt[2])}"]
    if pattern == "hobby":
        return [f"Eu gosto {de_contract(pt[0])}", f"Eu gosto {de_contract(pt[1])}", f"Eu não gosto {de_contract(pt[2])}"]
    if pattern == "routine":
        return [f"{cap(pt[0])} às sete", f"{cap(pt[1])} às oito", f"{cap(pt[2])} às dez"]
    if pattern == "health":
        return [f"Eu tenho {pt[0]}", f"Eu tenho {pt[1]}", f"Eu não tenho {pt[2]}"]
    if pattern == "body":
        return [f"Toque {pt[0]}", f"Toque {pt[1]}", f"Toque {pt[2]}"]
    if pattern == "transport":
        return [f"Eu vou {pt[0]}", f"Eu vou {pt[1]}", f"Eu não vou {pt[2]}"]
    if pattern == "direction":
        return [f"Vá {pt[0]}", f"Vá {pt[1]}", f"Vá {pt[2]}"]
    if pattern == "sport":
        return [f"Eu jogo {pt[0]}", f"Eu jogo {pt[1]}", f"Eu não jogo {pt[2]}"]
    if pattern == "music":
        return [f"Eu escuto {pt[0]}", f"Eu escuto {pt[1]}", f"Eu não escuto {pt[2]}"]
    if pattern == "movie":
        return [f"Eu vejo {pt[0]}", f"Eu vejo {pt[1]}", f"Eu não vejo {pt[2]}"]
    if pattern == "invitation":
        return [f"Você quer {pt[0]}?", f"Você quer {pt[1]}?", f"Você quer {pt[2]}?"]
    if pattern == "time":
        def time(value: str, negative: bool = False) -> str:
            plural = not value.startswith("uma ")
            if negative:
                return f"{'Não são' if plural else 'Não é'} {value}"
            return f"{'São' if plural else 'É'} {value}"
        return [time(pt[0]), time(pt[1]), time(pt[2], True)]
    if pattern == "price":
        return [f"Custa {pt[0]}", f"Custa {pt[1]}", f"Não custa {pt[2]}"]
    if pattern == "weather":
        return [f"Hoje está {pt[0]}", f"Hoje está {pt[1]}", f"Hoje não está {pt[2]}"]
    if pattern == "weather_pt":
        return [f"Hoje tem {pt[0]}", f"Hoje tem {pt[1]}", f"Hoje não tem {pt[2]}"]
    if pattern == "season":
        return [
            f"{cap(em_contract(pt[0]))} o tempo é bom",
            f"{cap(em_contract(pt[1]))} o tempo é bom",
            f"{cap(em_contract(pt[2]))} o tempo é bom",
        ]
    if pattern == "message":
        return [f"Eu envio {pt[0]}", f"Eu envio {pt[1]}", f"Eu não envio {pt[2]}"]
    if pattern == "problem":
        return [f"Eu tenho {pt[0]}", f"Eu tenho {pt[1]}", f"Eu não tenho {pt[2]}"]
    if pattern == "want_verb":
        return [f"Eu quero {pt[0]}", f"Eu quero {pt[1]}", f"Eu não quero {pt[2]}"]
    if pattern == "weekend":
        return [f"{cap(pt[0])} no sábado", f"{cap(pt[1])} no sábado", f"{cap(pt[2])} no sábado"]
    if pattern == "phrase":
        return [cap(pt[0]), cap(pt[1]), cap(pt[2])]
    raise ValueError(f"Patrón desconocido: {pattern}")


def safe_distractors(correct: list[str], candidates: list[list[str]]) -> list[str]:
    pool: list[str] = []
    for candidate in candidates:
        for word in candidate:
            if word not in correct and word not in pool:
                pool.append(word)
    for fallback in ["muito", "hoje", "aqui", "agora", "também", "sempre"]:
        if len(pool) >= 3:
            break
        if fallback not in correct and fallback not in pool:
            pool.append(fallback)
    return pool[:3]


def build_lesson(block: int, index: int, spec: dict) -> dict:
    lesson_id = f"{COURSE_ID}-a1-u{block:02d}-l{index:02d}"
    items = spec["items"]
    if spec["pattern"] == "weather_pt":
        es_sentences = ["Hoy hace sol", "Hoy llueve", "Hoy hace viento"]
    else:
        es_pairs = F1_1.sentences_for(spec["pattern"], [(es, es) for es, _ in items])
        es_sentences = [pair[0] for pair in es_pairs]
    pt_sentences_text = pt_sentences(spec["pattern"], items)
    pt_tokens = [tokens(text) for text in pt_sentences_text]
    first_es, first_pt = items[0]
    return {
        "id": lesson_id,
        "title": spec["title"],
        "cefrLevel": "A1",
        "vocabFrequencyBand": "1000-2000",
        "canDo": f"Puedo usar frases sencillas sobre {spec['topic']} en portugués.",
        "microLesson": {
            "title": spec["title"],
            "goals": [
                f"Reconocer vocabulario de {spec['topic']}.",
                f"Usar frases sencillas sobre {spec['topic']} en portugués.",
            ],
            "sections": [
                {
                    "heading": "Palavras e frases",
                    "body": f"Vocabulario básico de {spec['topic']}. Fíjate en el orden de la frase y en las palabras nuevas.",
                    "examples": [
                        {"source": es_sentences[0], "target": pt_sentences_text[0]},
                        {"source": es_sentences[1], "target": pt_sentences_text[1]},
                    ],
                }
            ],
        },
        "glossary": [{"term": pt, "translations": [es]} for es, pt in items],
        "exercises": [
            {
                "type": "MultipleChoice",
                "id": f"{lesson_id}-e01",
                "question": f"¿Cómo se dice «{first_es}» en portugués?",
                "options": [pt for _, pt in items],
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": f"«{first_es}» se dice «{first_pt}».",
                },
                "feedbackOnSuccess": {"explanation": "Has elegido la traducción correcta."},
            },
            {
                "type": "Translate",
                "id": f"{lesson_id}-e02",
                "sourcePhrase": es_sentences[0],
                "correctTokens": pt_tokens[0],
                "distractorTokens": safe_distractors(pt_tokens[0], [pt_tokens[1], pt_tokens[2]]),
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"El orden correcto es «{' '.join(pt_tokens[0])}».",
                },
                "feedbackOnSuccess": {"explanation": "La frase conserva el orden y el significado."},
            },
            {
                "type": "OrderWords",
                "id": f"{lesson_id}-e03",
                "tokens": (pt_tokens[1][1:] + pt_tokens[1][:1]) if len(pt_tokens[1]) > 1 else pt_tokens[1],
                "answerTokens": pt_tokens[1],
                "sourcePhrase": es_sentences[1],
                "feedbackOnError": {
                    "type": "word_order",
                    "explanation": f"La frase correcta es «{' '.join(pt_tokens[1])}».",
                },
                "feedbackOnSuccess": {"explanation": "El orden produce una frase natural."},
            },
            {
                "type": "MatchPairs",
                "id": f"{lesson_id}-e04",
                "pairs": [{"left": es, "right": pt} for es, pt in items],
                "feedbackOnError": {
                    "type": "vocabulary",
                    "explanation": "Cada palabra tiene una traducción distinta.",
                },
                "feedbackOnSuccess": {"explanation": "Has asociado cada palabra con su traducción."},
            },
            {
                "type": "ListenChoose",
                "id": f"{lesson_id}-e05",
                "audioText": pt_sentences_text[2],
                "options": pt_sentences_text,
                "correctIndex": 0,
                "feedbackOnError": {
                    "type": "comprehension",
                    "explanation": f"El audio dice «{pt_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "Has reconocido la frase del audio."},
            },
            {
                "type": "ListenType",
                "id": f"{lesson_id}-e06",
                "audioText": pt_sentences_text[2],
                "transcription": pt_sentences_text[2],
                "feedbackOnError": {
                    "type": "spelling",
                    "explanation": f"Escribe exactamente «{pt_sentences_text[2]}».",
                },
                "feedbackOnSuccess": {"explanation": "La transcripción coincide con el audio."},
            },
            {
                "type": "SpeakRepeat",
                "id": f"{lesson_id}-e07",
                "targetPhrase": pt_sentences_text[2],
                "audioText": pt_sentences_text[2],
                "feedbackOnError": {
                    "type": "pronunciation",
                    "explanation": f"Repite «{pt_sentences_text[2]}» con claridad.",
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
        print("F1.2 ya existe; no se modifica.")
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
