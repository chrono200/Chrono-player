package com.chronodisrup.chronoplay

import android.app.Application
import com.yausername.youtubedl_android.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL

class ChronoPlayApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread {
            runCatching {
                YoutubeDL.getInstance().init(this)
                FFmpeg.getInstance().init(this)
            }
        }.start()
    }
}