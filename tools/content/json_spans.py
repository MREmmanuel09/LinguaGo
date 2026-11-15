"""Edición quirúrgica de JSON preservando el formato original.

`json` de la biblioteca estándar no expone posiciones, así que este módulo
implementa un parser mínimo que devuelve nodos con `start`/`end`. Se usa para
corregir valores concretos de los `course.json` sin reformatear 13 MB por
archivo (los diffs deben ser revisables).
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class Node:
    """Valor JSON con su rango de caracteres en el documento original."""

    kind: str
    start: int
    end: int
    value: Any = None
    members: dict[str, "Node"] = field(default_factory=dict)
    items: list["Node"] = field(default_factory=list)

    def child(self, key: str) -> "Node | None":
        return self.members.get(key)


class JsonParser:
    def __init__(self, text: str) -> None:
        self.text = text
        self.length = len(text)

    def parse(self) -> Node:
        node = self.parse_value(0)
        return node

    def skip_ws(self, index: int) -> int:
        while index < self.length and self.text[index] in " \t\r\n":
            index += 1
        return index

    def parse_value(self, index: int) -> Node:
        index = self.skip_ws(index)
        if index >= self.length:
            raise ValueError("JSON inesperado: fin de documento")
        char = self.text[index]
        if char == "{":
            return self.parse_object(index)
        if char == "[":
            return self.parse_array(index)
        if char == '"':
            return self.parse_string(index)
        if self.text.startswith("true", index):
            return Node("bool", index, index + 4, True)
        if self.text.startswith("false", index):
            return Node("bool", index, index + 5, False)
        if self.text.startswith("null", index):
            return Node("null", index, index + 4, None)
        return self.parse_number(index)

    def parse_object(self, index: int) -> Node:
        start = index
        index += 1
        members: dict[str, Node] = {}
        while True:
            index = self.skip_ws(index)
            if index >= self.length:
                raise ValueError("Objeto sin cerrar")
            if self.text[index] == "}":
                return Node("object", start, index + 1, members=members)
            key_node = self.parse_string(index)
            key = key_node.value
            index = self.skip_ws(key_node.end)
            if self.text[index] != ":":
                raise ValueError(f"Falta ':' tras la clave {key!r}")
            value_node = self.parse_value(index + 1)
            members[key] = value_node
            index = self.skip_ws(value_node.end)
            if self.text[index] == ",":
                index += 1
                continue
            if self.text[index] == "}":
                return Node("object", start, index + 1, members=members)
            raise ValueError(f"Objeto mal formado cerca de {self.text[index:index + 20]!r}")

    def parse_array(self, index: int) -> Node:
        start = index
        index += 1
        items: list[Node] = []
        while True:
            index = self.skip_ws(index)
            if index >= self.length:
                raise ValueError("Array sin cerrar")
            if self.text[index] == "]":
                return Node("array", start, index + 1, items=items)
            item = self.parse_value(index)
            items.append(item)
            index = self.skip_ws(item.end)
            if self.text[index] == ",":
                index += 1
                continue
            if self.text[index] == "]":
                return Node("array", start, index + 1, items=items)
            raise ValueError(f"Array mal formado cerca de {self.text[index:index + 20]!r}")

    def parse_string(self, index: int) -> Node:
        start = index
        index += 1
        escaped = False
        while index < self.length:
            char = self.text[index]
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                raw = self.text[start:index + 1]
                return Node("string", start, index + 1, json.loads(raw))
            index += 1
        raise ValueError("String sin cerrar")

    def parse_number(self, index: int) -> Node:
        start = index
        if self.text[index] == "-":
            index += 1
        while index < self.length and self.text[index] in "0123456789.eE+-":
            index += 1
        raw = self.text[start:index]
        value = float(raw) if any(char in raw for char in ".eE") else int(raw)
        return Node("number", start, index, value)


def walk(node: Node):
    """Recorre todos los nodos compuestos del documento."""
    yield node
    if node.kind == "object":
        for child in node.members.values():
            yield from walk(child)
    elif node.kind == "array":
        for child in node.items:
            yield from walk(child)


def apply_edits(text: str, edits: list[tuple[int, int, str]]) -> str:
    """Aplica reemplazos [start, end) sin solapamientos."""
    ordered = sorted(edits, key=lambda edit: edit[0], reverse=True)
    previous_start = len(text) + 1
    for start, end, replacement in ordered:
        if end > previous_start:
            raise ValueError("Ediciones solapadas")
        text = text[:start] + replacement + text[end:]
        previous_start = start
    return text


def load(path: Path) -> tuple[str, Node]:
    text = path.read_bytes().decode("utf-8")
    return text, JsonParser(text).parse()


def save(path: Path, text: str) -> None:
    path.write_bytes(text.encode("utf-8"))
