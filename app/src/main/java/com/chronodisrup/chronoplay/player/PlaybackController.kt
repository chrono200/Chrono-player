package com.chronodisrup.chronoplay.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

object PlaybackController {
    @Volatile private var controller: MediaController? = null
    private var pending: ListenableFuture<MediaController>? = null

    fun connect(context: Context, onReady: (Boolean) -> Unit = {}) {
        if (controller != null) { onReady(true); return }
        if (pending != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        pending = future
        future.addListener({
            try {
                controller = future.get()
                onReady(true)
            } catch (_: Exception) {
                onReady(false)
            } finally {
                pending = null
            }
        }, Executor { it.run() })
    }

    fun play(url: String, title: String, artist: String) {
        val player = controller ?: return
        player.setMediaItem(MediaItem.Builder().setUri(url).setMediaMetadata(
            androidx.media3.common.MediaMetadata.Builder().setTitle(title).setArtist(artist).build()
        ).build())
        player.prepare()
        player.play()
    }

    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun isPlaying(): Boolean = controller?.isPlaying == true
    fun release() {
        controller?.release()
        controller = null
        pending?.cancel(false)
        pending = null
    }
}
