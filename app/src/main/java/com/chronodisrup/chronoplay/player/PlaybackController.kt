package com.chronodisrup.chronoplay.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

object PlaybackController {
    @Volatile private var controller: MediaController? = null
    private var pending: ListenableFuture<MediaController>? = null
    private val callbacks = mutableListOf<(Boolean) -> Unit>()

    @Synchronized fun connect(context: Context, onReady: (Boolean) -> Unit = {}) {
        controller?.let { onReady(true); return }
        callbacks.add(onReady)
        if (pending != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        pending = future
        future.addListener({
            val ready = try {
                controller = future.get()
                true
            } catch (_: Exception) { false }
            val waiting = synchronized(this) {
                pending = null
                callbacks.toList().also { callbacks.clear() }
            }
            waiting.forEach { callback -> runCatching { callback(ready) } }
        }, Executor { it.run() })
    }

    fun play(url: String, title: String, artist: String) {
        val player = controller ?: return
        val metadata = MediaMetadata.Builder().setTitle(title).setArtist(artist).build()
        player.setMediaItem(MediaItem.Builder().setUri(url).setMediaMetadata(metadata).build())
        player.prepare()
        player.play()
    }

    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }

    fun release() {
        controller?.release()
        controller = null
        pending?.cancel(false)
        pending = null
        callbacks.clear()
    }
}