package dev.linguago.core.ui.components

/** Par de piezas que se pueden encajar; la derecha puede llevar matiz/registro. */
public data class PuzzlePairUi(
    public val left: String,
    public val right: String,
    public val rightBadge: String? = null,
    public val rightDetail: String? = null,
)
