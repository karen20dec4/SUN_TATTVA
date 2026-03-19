package com.android.sun.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sun.R
import com.android.sun.ui.components.getTattvaIconRes

/**
 * Ecran full-screen pentru selectarea unui sunet personalizat per tattva.
 * Afișat ca overlay în SettingsScreen când utilizatorul apasă pe simbolul tattva.
 *
 * @param tattvaName  Numele tattva (ex: "akasha", "tejas" …)
 * @param tattvaColor Culoarea specifică tattva (pentru gradientul de sus)
 * @param currentUri  URI-ul sunetului personalizat curent, sau null = default
 * @param onUriSelected   Apelat cu noul URI (String) când utilizatorul alege un fișier
 * @param onResetToDefault Apelat când utilizatorul revine la sunetul implicit
 * @param onDismiss   Apelat la închiderea ecranului (buton back)
 */
@Composable
fun TattvaSoundPickerScreen(
    tattvaName: String,
    tattvaColor: Color,
    currentUri: String?,
    onUriSelected: (String) -> Unit,
    onResetToDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Numele afișat al sunetului curent (derivat din URI sau "Implicit")
    var currentFileName by remember(currentUri) {
        mutableStateOf(
            if (currentUri != null) getAudioDisplayName(context, currentUri) else null
        )
    }

    // Lansator pentru selectorul de fișiere audio al sistemului
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Solicită permisiune persistentă pentru ca accesul să reziste repornirilor
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                // Permission may already be granted or unavailable on some Android versions;
                // playback still works for the current session via the picker-granted URI.
                com.android.sun.util.AppLog.e("TattvaSoundPicker", "⚠️ takePersistableUriPermission failed: ${e.message}")
            }

            val uriString = uri.toString()
            currentFileName = getAudioDisplayName(context, uriString)
            onUriSelected(uriString)
        }
    }

    val tattvaDisplayName = tattvaName.replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── GRADIENT HEADER ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Fundal gradient de la culoarea tattva (sus) la transparent (jos)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    tattvaColor.copy(alpha = 0.90f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Conținut centrat în header: icon + titlu
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = getTattvaIconRes(tattvaName)),
                        contentDescription = tattvaDisplayName,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.tattva_sound_picker_title, tattvaDisplayName),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Buton back (stânga-sus)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }
            }

            // ── CONȚINUT PRINCIPAL ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Card: sunetul curent selectat
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = tattvaColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.tattva_sound_current),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currentFileName
                                    ?: stringResource(R.string.tattva_sound_default_label),
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Card: sfat navigare + formate acceptate
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tattva_sound_nav_hint),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tattva_sound_accepted_formats),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Buton principal: selectează fișier audio
                Button(
                    onClick = { fileLauncher.launch(arrayOf("audio/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tattvaColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AudioFile,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tattva_sound_browse),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── BUTON RESET (jos, lipit de margine) ───────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        currentFileName = null
                        onResetToDefault()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.tattva_sound_reset_default),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Returnează numele afișabil al unui fișier audio din URI-ul content://.
 * Dacă interogarea eșuează, returnează ultimul segment al URI-ului.
 */
private fun getAudioDisplayName(context: Context, uriString: String): String? {
    return try {
        val uri = android.net.Uri.parse(uriString)
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: uriString.substringAfterLast('/')
    } catch (_: Exception) {
        uriString.substringAfterLast('/')
    }
}
