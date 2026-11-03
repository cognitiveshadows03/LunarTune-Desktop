/*
 * LunarTune (2026)
 * © cognitiveshadows03 — github.com/cognitiveshadows03
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package dev.citali.lunartune.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.citali.lunartune.desktop.app.DesktopAppViewModel

@Composable
fun SettingsScreen(viewModel: DesktopAppViewModel) {
    val integrations by viewModel.integrations.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.size(4.dp))
            Text(
                "Tune integrations and the desktop client.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionTitle("Integrations")

        IntegrationCard(
            icon = { Icon(DiscordIcon, contentDescription = null, modifier = Modifier.size(22.dp)) },
            title = "Discord Rich Presence",
            description = "Show the current track as your Discord activity.",
            checked = integrations.discordEnabled,
            onCheckedChange = { value ->
                viewModel.updateIntegrations { it.copy(discordEnabled = value) }
            },
        ) {
            OutlinedTextField(
                value = integrations.discordApplicationId,
                onValueChange = { value ->
                    viewModel.updateIntegrations { it.copy(discordApplicationId = value) }
                },
                label = { Text("Discord application ID") },
                placeholder = { Text("Create an app at discord.com/developers") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        IntegrationCard(
            icon = { Icon(LastFmIcon, contentDescription = null, modifier = Modifier.size(22.dp)) },
            title = "Last.fm",
            description = "Scrobble the tracks you listen to.",
            checked = integrations.lastfmEnabled,
            onCheckedChange = { value ->
                viewModel.updateIntegrations { it.copy(lastfmEnabled = value) }
            },
        ) {
            OutlinedTextField(
                value = integrations.lastfmSessionKey,
                onValueChange = { value ->
                    viewModel.updateIntegrations { it.copy(lastfmSessionKey = value) }
                },
                label = { Text("Session key") },
                placeholder = { Text("Paste your Last.fm session key") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.size(12.dp))
            SwitchRow(
                label = "Send Now Playing",
                description = "Report the currently playing track to Last.fm.",
                checked = integrations.lastfmSendNowPlaying,
                onCheckedChange = { value ->
                    viewModel.updateIntegrations { it.copy(lastfmSendNowPlaying = value) }
                },
            )
        }

        Spacer(Modifier.size(24.dp))
        SectionTitle("About")
        AboutRow(label = "Application", value = "LunarTune Desktop")
        AboutRow(label = "Version", value = "5.2.0-alpha")
        AboutRow(label = "Engine", value = "Compose Multiplatform · LavaPlayer")
        AboutRow(label = "Lyrics", value = "BetterLyrics · LrcLib · KuGou · and more")
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, bottom = 10.dp),
    )
}

@Composable
private fun IntegrationCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 6.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Switch(checked = checked, onCheckedChange = onCheckedChange)
            }
            if (checked) {
                Spacer(Modifier.size(14.dp))
                HorizontalDivider()
                Spacer(Modifier.size(14.dp))
                content()
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
