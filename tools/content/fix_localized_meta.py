"""Traduce el metalenguaje inglés de la gramática en los cursos PT/FR/JA.

Las lecciones se generaron desde especificaciones inglesas y conservaron
etiquetas como «Passive reporting» o «Will have + participio» en cursos cuyo
idioma meta no es el inglés. Se sustituyen por explicaciones en español
(source language) sin formas verbales de otro idioma.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from json_spans import apply_edits, load, save  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
COURSES = {
    "pt": ROOT / "content/content-es-pt/src/main/assets/courses/es-pt/course.json",
    "fr": ROOT / "content/content-es-fr/src/main/assets/courses/es-fr/course.json",
    "ja": ROOT / "content/content-es-ja/src/main/assets/courses/es-ja/course.json",
}
FIELDS = {
    "label",
    "description",
    "goals",
    "body",
    "canDo",
    "scenario",
    "note",
    "translation",
    "explanation",
    "question",
    "prompt",
    "selfAssessmentCriteria",
    "instructions",
    "translations",
    "sourcePhrase",
    "source",
}

REPLACEMENTS = [
    # Frases largas primero para no romper composites.
    ("Observa cómo se usa ", "Fíjate en "),
    ("It is said / believed + that presenta una fuente de forma impersonal.", "La atribución impersonal presenta una fuente sin identificarla."),
    ("Will have + participio sitúa un resultado antes de una fecha.", "El futuro perfecto sitúa un resultado antes de una fecha."),
    ("Who, which, that y where añaden información sobre personas, cosas y lugares.", "Las relativas explicativas añaden información sobre personas, cosas y lugares."),
    ("Who, which y where añaden información sobre una persona, cosa o lugar.", "Las relativas añaden información sobre una persona, cosa o lugar."),
    ("The more..., the more... y comparaciones complejas.", "Las comparaciones correlativas expresan proporción."),
    ("Be + participio desplaza el foco a la acción o al resultado.", "La voz pasiva desplaza el foco a la acción o al resultado."),
    ("May, might y could expresan distintos grados de posibilidad.", "Los modales de posibilidad expresan distintos grados de certeza."),
    ("Will, going to y el presente continuo expresan planes y predicciones.", "El futuro simple, el futuro próximo y el presente continuo expresan planes y predicciones."),
    ("Must, have to y should expresan obligación y consejo.", "Los modales expresan obligación, necesidad y consejo."),
    ("If + past perfect, would have + participio expresa hipótesis pasadas.", "La condicional irreal expresa hipótesis pasadas."),
    ("If + presente, will + infinitivo expresa una condición real y su consecuencia.", "La condicional real expresa una condición y su consecuencia."),
    ("If + presente, will + infinitivo expresa condiciones reales.", "La condicional real expresa condiciones posibles."),
    ("Reported speech ajusta tiempos, pronombres y expresiones de tiempo.", "El estilo indirecto ajusta tiempos, pronombres y expresiones de tiempo."),
    ("Reported speech: El estilo indirecto ajusta tiempos, pronombres y referencias.", "El estilo indirecto ajusta tiempos, pronombres y referencias."),
    ("Going to expresa planes e intenciones ya decididos.", "El futuro próximo expresa planes e intenciones ya decididos."),
    ("Could you...? y Would you...? suavizan una petición.", "Las fórmulas de cortesía suavizan una petición."),
    ("Used to describe hábitos pasados que ya no ocurren.", "El imperfecto describe hábitos pasados que ya no ocurren."),
    ("Should y shouldn't dan consejos y recomendaciones.", "Los modales de consejo dan recomendaciones."),
    ("Should, ought to y could ayudan a recomendar sin imponer.", "Los modales de consejo ayudan a recomendar sin imponer."),
    ("Will expresa predicciones, promesas y decisiones espontáneas.", "El futuro simple expresa predicciones, promesas y decisiones espontáneas."),
    ("Have to expresa obligación externa; don't have to, ausencia de obligación.", "La obligación externa se distingue de la ausencia de obligación."),
    ("Must expresa obligación fuerte; mustn't, prohibición.", "La obligación fuerte se distingue de la prohibición."),
    ("In the morning, on Monday y at seven sitúan la acción en el tiempo.", "Las expresiones de tiempo sitúan la acción."),
    ("To, in order to y so that expresan finalidad.", "Las conjunciones finales expresan propósito."),
    ("Suavizar peticiones con could, would y I was wondering.", "Las fórmulas de cortesía suavizan las peticiones."),
    ("Must have, might have y should have especulan sobre el pasado.", "Los modales perfectos especulan sobre el pasado."),
    ("Mixed conditionals: Combina pasado y presente en hipótesis complejas.", "Las condicionales mixtas combinan pasado y presente en hipótesis complejas."),
    ("Cleft sentences: It is... / What... destaca una parte de la información.", "Las oraciones escindidas destacan una parte de la información."),
    ("Participle clauses: Las cláusulas de participio reducen y conectan ideas.", "Las cláusulas de participio reducen y conectan ideas."),
    ("Inversion: La inversión enfática coloca el auxiliar antes del sujeto.", "La inversión enfática coloca el auxiliar antes del sujeto."),
    ("Hedging: Matizar una opinión con I'm afraid, it seems o might.", "Atenuación: matizar una opinión con fórmulas de duda."),
    ("Passive reporting: It is said / believed + that presenta una fuente de forma impersonal.", "Atribución impersonal: presenta una fuente sin identificarla."),
    ("Diplomatic hedging: Matizar una postura con I wonder, it seems o might.", "Atenuación diplomática: matizar una postura con fórmulas de duda."),
    ("Softening requests: Suavizar peticiones con could, would y I was wondering.", "Suavizar peticiones: usa fórmulas de cortesía."),
    ("Fronting: El fronting desplaza un elemento al inicio para destacarlo.", "Anteposición: desplaza un elemento al inicio para destacarlo."),
    ("Nominalisation: Convertir verbos en nombres hace el texto más formal.", "Nominalización: convertir verbos en nombres hace el texto más formal."),
    ("Speculation: May, might y could expresan distintos grados de posibilidad.", "Especulación: los modales de posibilidad expresan distintos grados de certeza."),
    ("Understatement: La atenuación presenta algo grave como si fuera menor.", "Atenuación: presenta algo grave como si fuera menor."),
    ("Advanced comparatives: The more..., the more... y comparaciones complejas.", "Comparaciones avanzadas: las comparaciones correlativas expresan proporción."),
    ("Non-defining relative clauses: Las relativas explicativas añaden información con comas.", "Relativas explicativas: añaden información con comas."),
    ("Future perfect: Will have + participio sitúa un resultado antes de una fecha.", "Futuro perfecto: sitúa un resultado antes de una fecha."),
    ("Passive voice: Be + participio desplaza el foco a la acción o al resultado.", "Voz pasiva: desplaza el foco a la acción o al resultado."),
    ("Third conditional: If + past perfect, would have + participio expresa hipótesis pasadas.", "Condicional irreal: expresa hipótesis pasadas."),
    ("Reported speech: El estilo indirecto ajusta tiempos, pronombres y referencias.", "Estilo indirecto: ajusta tiempos, pronombres y referencias."),
    ("Perfect gerund: Having + participio expresa una acción anterior.", "Gerundio perfecto: expresa una acción anterior."),
    ("Advanced quantifiers: Little, few, plenty of y most matizan la cantidad.", "Cuantificadores avanzados: matizan la cantidad."),
    ("Purpose: To, in order to y so that expresan finalidad.", "Finalidad: las conjunciones finales expresan propósito."),
    ("Concession: Although, despite y however introducen concesiones.", "Concesión: las conjunciones introducen concesiones."),
    ("Modal perfect: Must have, might have y should have especulan sobre el pasado.", "Modales perfectos: especulan sobre el pasado."),
    ("Subjunctive: El subjuntivo expresa deseos, dudas y valoraciones.", "Subjuntivo: expresa deseos, dudas y valoraciones."),
    ("I agree, I don't agree y I think... expresan una postura.", "Las fórmulas de acuerdo y desacuerdo expresan una postura."),
    ("I think, in my opinion y it seems to me introducen una opinión.", "Las fórmulas de opinión introducen una valoración."),
    ("I'm afraid, it seems o might", "fórmulas de duda"),
    ("Matizar una opinión con I'm afraid, it seems o might.", "Matizar una opinión con fórmulas de duda."),
    ("Little, few, plenty of y most matizan la cantidad.", "Los cuantificadores matizan la cantidad."),
    ("Some, much, many, a lot of y enough expresan cantidad.", "Los cuantificadores expresan cantidad."),
    ("Was/were + -ing describe el fondo de una acción pasada.", "El pasado continuo describe el fondo de una acción pasada."),
    ("Have/has + participio conecta una experiencia pasada con el presente.", "El presente perfecto conecta una experiencia pasada con el presente."),
    ("Compara con -er/more ... than y tan ... como.", "Compara con estructuras comparativas y de igualdad."),
    ("Compara con -er/more ... than o tan ... como.", "Compara con estructuras comparativas y de igualdad."),
    ("Who, which, that y where", "Las relativas explicativas"),
    ("Who, which y where", "Las relativas"),
    ("It is said / believed + that", "La atribución impersonal"),
    ("If + past perfect, would have + participio", "La condicional irreal"),
    ("If + presente, will + infinitivo", "La condicional real"),
    ("Will have + participio", "El futuro perfecto"),
    ("Having + participio", "El gerundio perfecto"),
    ("To, in order to y so that", "Las conjunciones finales"),
    ("Be + participio", "La voz pasiva"),
    ("The more..., the more...", "Las comparaciones correlativas"),
    ("Passive reportative", "Atribución impersonal"),
    ("Passive reporting", "Atribución impersonal"),
    ("Non-defining relative clauses", "Relativas explicativas"),
    ("Advanced comparatives", "Comparaciones avanzadas"),
    ("Advanced quantifiers", "Cuantificadores avanzados"),
    ("Future perfect", "Futuro perfecto"),
    ("Passive voice", "Voz pasiva"),
    ("Third conditional", "Condicional irreal"),
    ("Reported speech", "Estilo indirecto"),
    ("Perfect gerund", "Gerundio perfecto"),
    ("Going to", "Futuro próximo"),
    ("going to", "el futuro próximo"),
    ("Used to", "Hábitos pasados"),
    ("used to", "los hábitos pasados"),
    ("Should para consejos", "Consejos"),
    ("should para consejos", "los consejos"),
    ("Must y mustn't", "Obligación y prohibición"),
    ("must y mustn't", "la obligación y la prohibición"),
    ("Have to", "Obligación externa"),
    ("have to", "la obligación externa"),
    ("Will", "Futuro simple"),
    ("will", "el futuro simple"),
    ("Voix passive", "Voz pasiva"),
    ("Tricolon, anaphore, chiasme, question rhétorique.", "El tricolon, la anáfora, el quiasmo y la pregunta retórica."),
    # Artículos portugueses colados en plantillas españolas.
    (" de as ", " de las "),
    (" de os ", " de los "),
    (" de o ", " del "),
    (" de a ", " de la "),
    (" sobre as ", " sobre las "),
    (" sobre os ", " sobre los "),
    (" sobre o ", " sobre el "),
    (" sobre a ", " sobre la "),
    (" situación de as ", " situación de las "),
    (" situación de os ", " situación de los "),
    (" situación de o ", " situación del "),
    (" situación de a ", " situación de la "),
]


def walk_nodes(node, path=()):
    if node.kind == "object":
        for key, child in node.members.items():
            yield from walk_nodes(child, path + (key,))
    elif node.kind == "array":
        for item in node.items:
            yield from walk_nodes(item, path)
    else:
        yield path, node


def clean_mixed_topic(value: str) -> str:
    """Sustituye plantillas con el tema en otro idioma por frases genéricas."""
    patterns = [
        (
            r"Observa cómo se usa .+? en una situación de [^.]+\.(?=\s|$)",
            "Fíjate en la gramática en este contexto.",
        ),
        (
            r"Puedo usar frases sencillas sobre [^.]+ en (portugués|francés|japonés)\.(?=\s|$)",
            r"Puedo usar frases sencillas en \1 sobre este tema.",
        ),
        (
            r"Usar frases sencillas sobre [^.]+ en (portugués|francés|japonés)\.(?=\s|$)",
            r"Usar frases sencillas en \1 sobre este tema.",
        ),
        (r"Vocabulario básico de [^.]+\.(?=\s|$)", "Vocabulario básico del tema."),
        (r"Reconocer vocabulario de [^.]+\.(?=\s|$)", "Reconocer el vocabulario del tema."),
        (
            r"Resolver una tarea comunicativa sobre [^.]+\.(?=\s|$)",
            "Resolver una tarea comunicativa sobre este tema.",
        ),
    ]
    for pattern, replacement in patterns:
        value = re.sub(pattern, replacement, value)
    return value


def clean_english_terms(value: str) -> str:
    """Último barrido de términos ingleses sueltos que quedan en metas."""
    patterns = [
        (r"\bhedging\b", "atenuación"),
        (r"\bfuture perfect\b", "futuro perfecto"),
        (r"\bpassive reportative\b", "atribución impersonal"),
        (r"\bpassive reporting\b", "atribución impersonal"),
        (r"\bnon-defining relative clauses\b", "relativas explicativas"),
        (r"\badvanced comparatives\b", "comparaciones avanzadas"),
        (r"\badvanced quantifiers\b", "cuantificadores avanzados"),
        (r"\bpassive voice\b", "voz pasiva"),
        (r"\bthird conditional\b", "condicional irreal"),
        (r"\breported speech\b", "estilo indirecto"),
        (r"\bperfect gerund\b", "gerundio perfecto"),
        (r"\bmixed conditionals\b", "condicionales mixtas"),
        (r"\bmodal perfect\b", "modales perfectos"),
        (r"\bcleft sentences\b", "oraciones escindidas"),
        (r"\bparticiple clauses\b", "cláusulas de participio"),
        (r"\bsoftening requests\b", "fórmulas de cortesía"),
        (r"\bdiplomatic hedging\b", "atenuación diplomática"),
        (r"\bquestion tags\b", "preguntas de confirmación"),
        (r"\bgoing to\b", "futuro próximo"),
        (r"\bused to\b", "hábitos pasados"),
        (r"\bhave to\b", "obligación externa"),
        (r"\bshould y shouldn't\b", "los modales de consejo"),
        (r"\bcould you\b", "las fórmulas de cortesía"),
        (r"\bwould you\b", "las fórmulas de cortesía"),
        (r"\bI'm afraid\b", "las fórmulas de duda"),
        (r"\bit seems\b", "parece"),
        (r"\bI was wondering\b", "me preguntaba"),
        (r"\bI wonder\b", "me pregunto"),
        (r"\binversion\b", "inversión"),
        (r"\bfronting\b", "anteposición"),
        (r"\bnominalisation\b", "nominalización"),
        (r"\bparallelism\b", "paralelismo"),
        (r"\bspeculation\b", "especulación"),
        (r"\bunderstatement\b", "atenuación"),
        (r"\bsubjunctive\b", "subjuntivo"),
        (r"\bpurpose\b", "finalidad"),
        (r"\bconcession\b", "concesión"),
        (r"\bcomparatives\b", "comparaciones"),
        (r"\bsuperlatives\b", "superlativos"),
        (r"\bwill\b", "futuro simple"),
        (r"\bwould\b", "condicional"),
        (r"\bcould\b", "podría"),
        (r"\bshould\b", "debería"),
        (r"\bmight\b", "podría"),
        (r"\bmust\b", "deber"),
    ]
    for pattern, replacement in patterns:
        value = re.sub(pattern, replacement, value, flags=re.IGNORECASE)
    return value


def main() -> None:
    dry_run = "--dry-run" in sys.argv
    for course, path in COURSES.items():
        text, root = load(path)
        edits = []
        changed = 0
        for field_path, node in walk_nodes(root):
            if not field_path or field_path[-1] not in FIELDS:
                continue
            if node.kind != "string" or not isinstance(node.value, str):
                continue
            value = node.value
            if not value:
                continue
            updated = clean_mixed_topic(value)
            for old, new in REPLACEMENTS:
                updated = updated.replace(old, new)
            updated = clean_english_terms(updated)
            if updated == value:
                continue
            edits.append((node.start, node.end, json.dumps(updated, ensure_ascii=False)))
            changed += 1
        if edits and not dry_run:
            save(path, apply_edits(text, edits))
        print(f"{course}: {changed} cadenas actualizadas" + (" (dry-run)" if dry_run else ""))


if __name__ == "__main__":
    main()
