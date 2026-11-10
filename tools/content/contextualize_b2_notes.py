"""Contextualiza las notas culturales de las réplicas B2 de F0."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "es-pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "es-fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "es-ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}

NOTES = {
    "es-pt": {
        "Negociar um prazo": "En una negociación de plazos, «dar um jeito» es encontrar una salida práctica cuando el plan inicial no funciona.",
        "Quem decide o quê": "Cuando varias personas deciden, conviene «ficar de olho» en quién asume cada responsabilidad.",
        "Suavizar uma crítica": "Al suavizar una crítica, «quebrar o galho» es ofrecer una solución provisional en lugar de señalar solo el fallo.",
        "Rebater com evidências": "Para rebatir con datos, «pôr a mão na massa» significa pasar de la teoría a la comprobación práctica.",
        "Uma decisão difícil": "Una decisión difícil puede «dar com os burros n'água»; la expresión recuerda que equivocarse forma parte del proceso.",
        "Ler nas entrelinhas": "Si alguien «está com a corda no pescoço», a menudo lo insinúa sin decirlo; leer nas entrelinhas ayuda a detectarlo.",
        "O elefante na sala": "En una conversación sobre «o elefante na sala», «fazer vista grossa» es precisamente lo que impide resolver el problema.",
        "Propostas para o futuro": "Al evaluar propuestas, «custar os olhos da cara» obliga a justificar si la inversión merece la pena.",
        "Negociar com a prefeitura": "En una negociación con la prefeitura, «acertar na mosca» es dar con la propuesta que responde a la necesidad real.",
        "O futuro em debate": "En un debate sobre el futuro, «a cereja do bolo» es el detalle final que hace convincente una propuesta.",
    },
    "es-fr": {
        "Négocier un délai": "Dans une négociation de délai, «tirer les ficelles» consiste à identifier qui décide vraiment.",
        "Qui décide quoi": "Quand les rôles sont flous, «mettre la main à la pâte» aide à clarifier qui fait quoi.",
        "Nuancer une critique": "Nuancer une critique, c'est aussi reconnaître ce qui «coûte les yeux de la tête» et ce qui en vaut la peine.",
        "Réfuter avec des preuves": "Réfuter avec des preuves, c'est ne pas «poser un lapin» au débat : il faut être présent et argumenter.",
        "Une décision difficile": "Dans une décision difficile, «tirer son épingle du jeu» consiste à préserver l'essentiel.",
        "Lire entre les lignes": "Lire entre les lignes permet de deviner qui «a le bras long» dans une négociation.",
        "L'éléphant dans la pièce": "«Faire la sourde oreille», c'est ignorer l'éléphant dans la pièce au lieu de nommer le problème.",
        "Propositions pour l'avenir": "Une proposition pour l'avenir ne doit pas «casser les pieds» : elle doit être claire et utile.",
        "Négocier avec la mairie": "Négocier avec la mairie exige de «mettre les points sur les i» pour éviter les malentendus.",
        "L'avenir en débat": "Dans un débat sur l'avenir, «jeter l'éponge» trop tôt, c'est renoncer avant d'avoir exploré les alternatives.",
    },
    "es-ja": {
        "締め切りを交渉する": "Al negociar un plazo, si estás tan ocupado que «猫の手も借りたい», cuesta ordenar las condiciones.",
        "誰が決めるのか": "Para descubrir quién decide, preguntar a alguien con «顔が広い» contactos es el atajo.",
        "批判を和らげる": "Aunque suavices una crítica, las observaciones «耳が痛い» no se pueden evitar; por eso importa cómo las transmites.",
        "証拠で反論する": "Para rebatir con pruebas, la clave es saber cuánta información puede soltar alguien «口が堅い».",
        "難しい決断": "Antes de una decisión difícil, «目を通す» los documentos relevantes ordena la base del juicio.",
        "行間を読む": "Al leer entre líneas, hay que distinguir si la otra persona quiere que «手を貸す» o solo que la escuches.",
        "部屋の中の象": "Para hablar del elefante en la habitación, primero hay que «足を運ぶ» al lugar.",
        "未来への提案": "Una propuesta de futuro debe tener fundamentos para poder defenderla «胸を張る».",
        "市役所と交渉する": "Al negociar con el ayuntamiento, «水に流す» los malentendidos pasados acelera el acuerdo.",
        "未来を語る": "En una reunión para hablar del futuro no se puede «油を売る»; hacen falta acciones concretas.",
    },
}


def json_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def main() -> None:
    for course_id, path in COURSES.items():
        text = path.read_text(encoding="utf-8")
        course = json.loads(text)
        updated = 0
        for level in course["levels"]:
            for unit in level["units"]:
                if unit["id"] != f"{course_id}-b2-p0-un-1":
                    continue
                for lesson in unit["lessons"]:
                    new_note = NOTES[course_id].get(lesson["title"])
                    if new_note is None:
                        continue
                    old_note = lesson["culturalNotes"][0]["note"]
                    old_fragment = f'"note": {json_string(old_note)}'
                    new_fragment = f'"note": {json_string(new_note)}'
                    if old_fragment not in text:
                        raise SystemExit(f"No se encontró la nota de {lesson['id']}")
                    text = text.replace(old_fragment, new_fragment, 1)
                    updated += 1
        if updated != 10:
            raise SystemExit(f"{course_id}: se esperaban 10 notas, hay {updated}")
        path.write_text(text, encoding="utf-8", newline="\n")
        json.loads(path.read_text(encoding="utf-8"))
        print(f"{course_id}: {updated} notas B2 contextualizadas.")


if __name__ == "__main__":
    main()
