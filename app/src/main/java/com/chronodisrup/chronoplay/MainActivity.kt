package com.chronodisrup.chronoplay

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chronodisrup.chronoplay.core.designsystem.ChronoOrange
import com.chronodisrup.chronoplay.core.designsystem.ChronoTheme
import com.chronodisrup.chronoplay.data.local.LocalMediaEngine
import com.chronodisrup.chronoplay.data.remote.TrackDto
import com.chronodisrup.chronoplay.player.PlaybackController
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.util.Locale

private enum class Destination(val label: String) { HOME("Home"), DISCOVER("Discover"), SEARCH("Search"), LIBRARY("Library") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlaybackController.connect(this)
        setContent { ChronoTheme { ChronoRoot(this) } }
    }
    override fun onDestroy() {
        if (isFinishing) PlaybackController.release()
        super.onDestroy()
    }
}

@Composable
private fun ChronoRoot(context: Context) {
    val scope = rememberCoroutineScope()
    val localEngine = remember { LocalMediaEngine(context) }
    var selected by remember { mutableStateOf(Destination.HOME) }
    var platform by remember { mutableStateOf("youtube") }
    var query by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var trending by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var discover by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var library by remember { mutableStateOf<List<TrackDto>>(emptyList()) }
    var currentTrack by remember { mutableStateOf<TrackDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showLyrics by remember { mutableStateOf(false) }
    var lyrics by remember { mutableStateOf("") }

    fun play(track: TrackDto) {
        currentTrack = track
        if (track.platform == "device") {
            val localUri = track.url ?: return
            PlaybackController.connect(context) { ready ->
                if (ready) PlaybackController.play(localUri, track.title, track.artist)
                else error = "The playback service could not start."
            }
            return
        }
        scope.launch {
            loading = true
            error = null
            try {
                val streamUrl = localEngine.resolve(track)
                PlaybackController.connect(context) { ready ->
                    if (ready) PlaybackController.play(streamUrl, track.title, track.artist)
                    else error = "The playback service could not start."
                }
            } catch (e: Exception) { error = e.message ?: "Unable to play this track." }
            finally { loading = false }
        }
    }

    fun loadLyrics(track: TrackDto) {
        error = "Lyrics are not enabled in standalone mode yet."
    }

    fun download(track: TrackDto) {
        if (track.platform == "device") { error = "This track is already downloaded."; return }
        scope.launch {
            loading = true
            error = null
            try {
                localEngine.download(track)
                error = "Saved to Music/CHRONO PLAY: " + track.title
                library = localEngine.downloadedTracks()
            } catch (e: Exception) { error = e.message ?: "Download failed." }
            finally { loading = false }
        }
    }

    Scaffold(
        containerColor = Color(0xFF090909),
        bottomBar = {
            Column {
                currentTrack?.let { MiniPlayer(it, onToggle = { PlaybackController.toggle() }, onLyrics = { loadLyrics(it) }) }
                NavigationBar(containerColor = Color(0xFF101010), tonalElevation = 0.dp) {
                    Destination.entries.forEach { dest ->
                        val icon = when (dest) {
                            Destination.HOME -> Icons.Default.Home
                            Destination.DISCOVER -> Icons.Default.Explore
                            Destination.SEARCH -> Icons.Default.Search
                            Destination.LIBRARY -> Icons.Default.LibraryMusic
                        }
                        NavigationBarItem(selected = selected == dest, onClick = {
                            selected = dest
                            if (dest == Destination.LIBRARY) scope.launch {
                                library = localEngine.downloadedTracks()
                            }
                            if (dest == Destination.DISCOVER) scope.launch {
                                loading = true
                                try { discover = localEngine.search("new music 2026", "youtube") } catch (e: Exception) { error = e.message }
                                finally { loading = false }
                            }
                        }, icon = { Icon(icon, dest.label) }, label = { Text(dest.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = ChronoOrange, selectedTextColor = ChronoOrange,
                                indicatorColor = Color(0xFF2B1712), unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray))
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("CHRONO", color = ChronoOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("PLAY", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                Text("NO SERVER NEEDED", color = Color.Gray, fontSize = 9.sp, letterSpacing = 1.sp)
            }
            when (selected) {
                Destination.HOME -> HomeContent(trending, loading, ::play, ::download, onExplore = { selected = Destination.SEARCH })
                Destination.SEARCH -> SearchContent(query, { query = it }, platform, { platform = it }, tracks, loading,
                    onSearch = {
                        val repo = repository
                        if (repo == null) error = "Connect your Muriel server in Settings."
                        else scope.launch {
                            loading = true; error = null
                            try { tracks = repo.search(query.trim(), platform) }
                            catch (e: Exception) { error = e.message ?: "Search failed." }
                            finally { loading = false }
                        }
                    }, onPlay = ::play, onDownload = ::download)
                Destination.DISCOVER -> TrackList("Discover", "Fresh music from YouTube search", discover, loading, ::play, ::download)
                Destination.LIBRARY -> TrackList("Your library", "Downloaded to Music/CHRONO PLAY", library, loading, ::play, ::download)
            }
            error?.let { message ->
                Text(message, color = ChronoOrange, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp))
            }
        }
    }

    if (showLyrics) {
        AlertDialog(onDismissRequest = { showLyrics = false }, containerColor = Color(0xFF171717),
            title = { Text("Lyrics", color = Color.White) },
            text = { Text(lyrics.ifBlank { "No lyrics were found for this track." }, color = Color.LightGray) },
            confirmButton = { TextButton(onClick = { showLyrics = false }) { Text("Close", color = ChronoOrange) } })
    }
}

@Composable
private fun HomeContent(tracks: List<TrackDto>, loading: Boolean, onPlay: (TrackDto) -> Unit, onDownload: (TrackDto) -> Unit, onExplore: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item {
            Surface(color = Color(0xFF1C1715), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("YOUR SOUND. YOUR TIME.", color = ChronoOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Text("Find your next favourite.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Search music and save tracks straight to your phone. No account or server setup.", color = Color(0xFFB4A7A2), fontSize = 13.sp)
                    Button(onClick = onExplore, colors = ButtonDefaults.buttonColors(containerColor = ChronoOrange, contentColor = Color.Black)) { Text("Explore music", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item { SectionTitle("Ready when you are", "Search, play and download directly on this device") }
        if (loading && tracks.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = ChronoOrange) }
        if (tracks.isEmpty() && !loading) item { EmptyState("Open Search to find music. Downloads are saved to Music/CHRONO PLAY.") }
        items(tracks) { TrackRow(it, onPlay, onDownload) }
    }
}

@Composable
private fun SearchContent(query: String, onQuery: (String) -> Unit, platform: String, onPlatform: (String) -> Unit,
    tracks: List<TrackDto>, loading: Boolean, onSearch: () -> Unit, onPlay: (TrackDto) -> Unit, onDownload: (TrackDto) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.weight(1f), singleLine = true,
                placeholder = { Text("Songs, artists, albums…") }, leadingIcon = { Icon(Icons.Default.Search, null) })
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onSearch, enabled = query.isNotBlank()) { Icon(Icons.Default.ArrowForward, "Search", tint = ChronoOrange) }
        }
        Row(Modifier.padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("youtube" to "YouTube", "ytmusic" to "YT Music", "soundcloud" to "SoundCloud").forEach { (key, label) ->
                FilterChip(selected = platform == key, onClick = { onPlatform(key) }, label = { Text(label, fontSize = 11.sp) })
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = ChronoOrange)
        if (tracks.isEmpty() && !loading) EmptyState("Search to discover tracks from your connected sources.")
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tracks) { TrackRow(it, onPlay, onDownload) }
        }
    }
}

@Composable
private fun TrackList(title: String, subtitle: String, tracks: List<TrackDto>, loading: Boolean, onPlay: (TrackDto) -> Unit, onDownload: (TrackDto) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) { SectionTitle(title, subtitle) }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = ChronoOrange)
        if (tracks.isEmpty() && !loading) EmptyState("Nothing here yet. Search for music and download your first track.")
        LazyColumn(contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(tracks) { TrackRow(it, onPlay, onDownload) }
        }
    }
}

@Composable
private fun TrackRow(track: TrackDto, onPlay: (TrackDto) -> Unit, onDownload: (TrackDto) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onPlay(track) }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        if (!track.thumbnail.isNullOrBlank()) {
            AsyncImage(model = track.thumbnail, contentDescription = null, modifier = Modifier.size(54.dp).background(Color(0xFF202020), RoundedCornerShape(9.dp)))
        } else Box(Modifier.size(54.dp).background(Color(0xFF241A17), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, null, tint = ChronoOrange)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(track.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            Text(formatDuration(track.duration), color = Color(0xFF777777), fontSize = 10.sp)
        }
        IconButton(onClick = { onDownload(track) }, enabled = track.platform != "device") { Icon(Icons.Default.Download, "Download", tint = if (track.platform == "device") Color.DarkGray else Color.LightGray) }
        IconButton(onClick = { onPlay(track) }) { Icon(Icons.Default.PlayCircle, "Play", tint = ChronoOrange, modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun MiniPlayer(track: TrackDto, onToggle: () -> Unit, onLyrics: () -> Unit) {
    Surface(color = Color(0xFF1B1B1B), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.GraphicEq, null, tint = ChronoOrange, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(track.artist, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
            }
            IconButton(onClick = onLyrics) { Icon(Icons.Default.Lyrics, "Lyrics", tint = Color.LightGray) }
            IconButton(onClick = onToggle) { Icon(Icons.Default.PlayArrow, "Play or pause", tint = Color.White) }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color(0xFF858585), fontSize = 12.sp)
    }
}
@Composable private fun EmptyState(message: String) {
    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.MusicOff, null, tint = ChronoOrange, modifier = Modifier.size(34.dp))
        Spacer(Modifier.height(10.dp))
        Text(message, color = Color.Gray, fontSize = 13.sp)
    }
}
private fun formatDuration(value: Double?): String {
    val seconds = (value ?: 0.0).toInt().coerceAtLeast(0)
    return String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)
}
private fun safeFileName(value: String): String = value.replace(Regex("[^A-Za-z0-9 ._-]"), "_").take(90).trim()


private fun queryDeviceDownloads(context: Context): List<TrackDto> {
    val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val cursor = manager.query(DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL))
    val items = mutableListOf<TrackDto>()
    cursor.use { rows ->
        val idColumn = rows.getColumnIndex(DownloadManager.COLUMN_ID)
        val titleColumn = rows.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val descriptionColumn = rows.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)
        val localUriColumn = rows.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
        while (rows.moveToNext()) {
            val localUri = if (localUriColumn >= 0) rows.getString(localUriColumn) else null
            if (localUri.isNullOrBlank()) continue
            val description = if (descriptionColumn >= 0) rows.getString(descriptionColumn).orEmpty() else ""
            val artist = description.substringAfter("•", "Downloaded").trim()
            items += TrackDto(
                id = "device_${if (idColumn >= 0) rows.getLong(idColumn) else items.size}",
                title = if (titleColumn >= 0) rows.getString(titleColumn).orEmpty() else "Downloaded track",
                artist = artist,
                url = localUri,
                platform = "device"
            )
        }
    }
    return items
}
