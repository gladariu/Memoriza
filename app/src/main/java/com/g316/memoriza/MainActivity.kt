package com.g316.memoriza

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val MIC_PERMISSION_CODE = 101

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRec = false
    private var currentVerseRef = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.addJavascriptInterface(AudioBridge(), "AndroidAudio")
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION_CODE
            )
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun safeFileName(ref: String): String =
        ref.replace(Regex("[^a-zA-Z0-9\\-_]"), "_")

    private fun fileForRef(ref: String): File =
        File(filesDir, "grabacion_${safeFileName(ref)}.mp4")

    inner class AudioBridge {

        @JavascriptInterface
        fun startRecording(verseRef: String): String {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) return "PERMISSION_DENIED"

            return try {
                // Stop any previous recording
                if (isRec) {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null
                    isRec = false
                }
                currentVerseRef = verseRef
                val file = fileForRef(verseRef)

                @Suppress("DEPRECATION")
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                isRec = true
                "OK"
            } catch (e: Exception) {
                "ERROR:${e.message}"
            }
        }

        @JavascriptInterface
        fun stopRecording(): String {
            return try {
                if (isRec) {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null
                    isRec = false
                }
                "OK"
            } catch (e: Exception) {
                "ERROR:${e.message}"
            }
        }

        @JavascriptInterface
        fun isRecording(): Boolean = isRec

        @JavascriptInterface
        fun hasRecordingFor(verseRef: String): Boolean {
            val f = fileForRef(verseRef)
            return f.exists() && f.length() > 0
        }

        @JavascriptInterface
        fun startPlayback(verseRef: String): String {
            return try {
                val file = fileForRef(verseRef)
                if (!file.exists() || file.length() == 0L) return "NO_FILE"
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(file.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        runOnUiThread {
                            webView.evaluateJavascript("onNativePlaybackComplete()", null)
                        }
                    }
                }
                "OK"
            } catch (e: Exception) {
                "ERROR:${e.message}"
            }
        }

        @JavascriptInterface
        fun stopPlayback(): String {
            return try {
                mediaPlayer?.apply { if (isPlaying) stop(); release() }
                mediaPlayer = null
                "OK"
            } catch (e: Exception) {
                "ERROR:${e.message}"
            }
        }

        @JavascriptInterface
        fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

        @JavascriptInterface
        fun listRecordings(): String {
            val files = filesDir.listFiles() ?: return ""
            return files
                .filter { it.name.startsWith("grabacion_") && it.name.endsWith(".mp4") && it.length() > 0 }
                .map { it.name.removePrefix("grabacion_").removeSuffix(".mp4").replace("_", " ") }
                .joinToString("|")
        }

        @JavascriptInterface
        fun deleteRecording(verseRef: String): String {
            return try {
                fileForRef(verseRef).delete()
                "OK"
            } catch (e: Exception) {
                "ERROR:${e.message}"
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MIC_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread {
                    webView.evaluateJavascript("onMicPermissionGranted()", null)
                }
            } else {
                Toast.makeText(
                    this,
                    "Permiso de microfono necesario para grabar",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { mediaRecorder?.release() } catch (e: Exception) {}
        try { mediaPlayer?.release() } catch (e: Exception) {}
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
