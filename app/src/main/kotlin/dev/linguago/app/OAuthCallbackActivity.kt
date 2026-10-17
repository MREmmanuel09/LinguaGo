package dev.linguago.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.linguago.core.auth.AuthRedirect
import dev.linguago.core.auth.OAuthSession
import javax.inject.Inject

/**
 * Receptor del redirect OAuth (ADR-011): el navegador regresa a
 * `dev.linguago.oauth://callback?code=...` (intent-filter en el manifest)
 * y esta actividad valida el URI antes de devolver el code a
 * [OAuthSession], que estaba esperando en la pantalla original.
 */
@AndroidEntryPoint
public class OAuthCallbackActivity : ComponentActivity() {
    @Inject
    internal lateinit var pending: OAuthSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolveRedirect(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        resolveRedirect(intent)
    }

    /** Valida el redirect y entrega el code (o el error) a la sesión. */
    private fun resolveRedirect(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data == null || data.scheme != SCHEME || data.host != HOST) {
            pending.resolve(AuthRedirect(error = "redirect_invalido"))
            return
        }
        pending.resolve(
            AuthRedirect(
                code = data.getQueryParameter("code"),
                error = data.getQueryParameter("error"),
            ),
        )
    }

    private companion object {
        const val SCHEME = "dev.linguago.oauth"
        const val HOST = "callback"
    }
}
