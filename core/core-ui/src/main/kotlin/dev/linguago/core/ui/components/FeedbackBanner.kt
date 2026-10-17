package dev.linguago.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.linguago.core.ui.theme.LinguaTokens

/**
 * Banner de feedback (RFC §7.2): verde (`--accent`) para aciertos y coral
 * (`--destructive`) para errores.
 */
@Composable
fun FeedbackBanner(
    isCorrect: Boolean,
    message: String,
    modifier: Modifier = Modifier,
) {
    val statusColor = if (isCorrect) LinguaTokens.Accent else LinguaTokens.Destructive
    val iconRes = if (isCorrect) LinguaIcons.CheckRes else LinguaIcons.CloseRes

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LinguaTokens.RADIUS_CARD_DP.dp))
                .background(statusColor.copy(alpha = 0.18f))
                .semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite
                }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.padding(2.dp),
            colorFilter = ColorFilter.tint(statusColor),
        )
        Text(
            text = message,
            color = statusColor,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
