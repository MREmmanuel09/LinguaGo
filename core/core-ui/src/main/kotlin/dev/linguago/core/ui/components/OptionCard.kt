package dev.linguago.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Tarjeta de opción (RFC §7.2): borde 2dp `--border`, radio 12dp; al
 * seleccionarse cambia a primario. Semántica de RadioButton para TalkBack
 * (invariante I8).
 */
@Composable
fun OptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val borderColor = if (selected) colors.primary else colors.outline
    val contentColor = if (selected) colors.primary else colors.onSurface

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(if (selected) colors.primary.copy(alpha = 0.06f) else Color.Transparent)
                .border(2.dp, borderColor, RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .semantics { role = Role.RadioButton }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
        }
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
