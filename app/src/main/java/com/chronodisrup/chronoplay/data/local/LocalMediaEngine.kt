package com.chronodisrup.chronoplay.data.local

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.chronodisrup.chronoplay.data.remote.TrackDto
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalMediaEngine(private val context: Context) {
    private val workDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir, "chrono-work")

    suspend fun search(query: String, platform: String): List<TrackDto> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Enter a song or artist to search." }
        check(workDir.mkdirs() || workDir.isDirectory) { "Storage is unavailable." }
        val prefix = if (platform == "soundcloud") "scsearch20:" else "ytsearch20:"
        val playlist = YoutubeDL.getInstance().getPlaylistInfo(prefix + query)
        val entries = call(playlist, "getEntries") as? Iterable<*> ?: emptyList<Any>()
        entries.mapNotNull { entry ->
            if (entry == null) return@mapNotNull null
            val id = value(entry, "getId")?.toString() ?: return@mapNotNull null
            val title = value(entry, "getTitle")?.toString()?.takeIf { it.isNotBlank() } ?: "Unknown track"
            val artist = (value(entry, "getUploader") ?: value(entry, "getChannel") ?: value(entry, "getArtist"))?.toString() ?: "Unknown artist"
            val duration = (value(entry, "getDuration") as? Number)?.toDouble()
            val thumbnail = value(entry, "getThumbnail")?.toString()
            val webpage = (value(entry, "getWebpageUrl") ?: value(entry, "getUrl"))?.toString()
                ?: "https://www.youtube.com/watch?v=$id"
            TrackDto(id = id, title = title, artist = artist, duration = duration, thumbnail = thumbnail,
                url = webpage, platform = if (platform == "soundcloud") "soundcloud" else "youtube")
        }.take(20)
    }

    suspend fun resolve(track: TrackDto): String = withContext(Dispatchers.IO) {
        val source = track.url ?: error("This result has no source URL.")
        YoutubeDL.getInstance().getInfo(source).url?.takeIf { it.isNotBlank() }
            ?: error("The source did not provide a playable audio stream.")
    }

    suspend fun download(track: TrackDto): Uri = withContext(Dispatchers.IO) {
        check(workDir.mkdirs() || workDir.isDirectory) { "Cannot create the download workspace." }
        val safeId = (track.id ?: track.title).replace(Regex("[^A-Za-z0-9_-]"), "_").take(60)
        val request = YoutubeDLRequest(track.url ?: error("This result has no source URL."))
        request.addOption("--no-playlist")
        request.addOption("--no-mtime")
        request.addOption("-x")
        request.addOption("--audio-format", "mp3")
        request.addOption("--audio-quality", "0")
        request.addOption("-o", File(workDir, "$safeId.%(ext)s").absolutePath)
        YoutubeDL.getInstance().execute(request, "chrono-download-$safeId") { _, _, _ -> }
        val file = File(workDir, "$safeId.mp3")
        check(file.exists() && file.length() > 0L) { "No MP3 file was produced." }
        publishToMusic(file, track)
    }

    suspend fun downloadedTracks(): List<TrackDto> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION)
        val selection = if (Build.VERSION.SDK_INT >= 29) "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?" else null
        val args = if (selection != null) arrayOf("Music/CHRONO PLAY/%") else null
        val out = mutableListOf<TrackDto>()
        context.contentResolver.query(collection, projection, selection, args, "${MediaStore.Audio.Media.DATE_ADDED} DESC")?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                out += TrackDto(id = "device_$id", title = c.getString(titleCol) ?: "Downloaded track",
                    artist = c.getString(artistCol) ?: "Unknown artist", duration = c.getLong(durationCol) / 1000.0,
                    url = Uri.withAppendedPath(collection, id.toString()).toString(), platform = "device")
            }
        }
        out
    }

    private fun publishToMusic(source: File, track: TrackDto): Uri {
        val name = safeName(track.artist + " - " + track.title) + ".mp3"
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, name)
            put(MediaStore.Audio.Media.TITLE, track.title)
            put(MediaStore.Audio.Media.ARTIST, track.artist)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/CHRONO PLAY")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Android could not create the music file.")
        try {
            context.contentResolver.openOutputStream(uri)?.use { output -> source.inputStream().use { it.copyTo(output) } }
                ?: error("Could not write the downloaded track.")
            if (Build.VERSION.SDK_INT >= 29) {
                val ready = ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }
                context.contentResolver.update(uri, ready, null, null)
            }
            source.delete()
            return uri
        } catch (e: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw e
        }
    }

    private fun safeName(value: String) = value.replace(Regex("[^A-Za-z0-9 ._-]"), "_").trim().take(120).ifBlank { "CHRONO PLAY track" }
    private fun call(target: Any, method: String): Any? = runCatching {
        target.javaClass.methods.first { it.name == method && it.parameterCount == 0 }.invoke(target)
    }.getOrNull()
    private fun value(target: Any, method: String): Any? = call(target, method)
}