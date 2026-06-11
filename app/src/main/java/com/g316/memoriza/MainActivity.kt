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
    private var audioFile: File? = null
    private var isRec = false

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
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION_CODE
            )
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class AudioBridge {

        @JavascriptInterface
        fun startRecording(): String {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED) {
                return "PERMISSION_DENIED"
            }
            return try {
                stopRecording()
                audioFile = File(cacheDir, "recording.mp4")
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(audioFile!!.absolutePath)
                    prepare()
                    start()
                }
                isRec = true
                "OK"
            } catch (e: Exception) {
                "ERROR: ${e.message}"
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
                "ERROR: ${e.message}"
            }
        }

        @JavascriptInterface
        fun isRecording(): Boolean = isRec

        @JavascriptInterface
        fun hasRecording(): Boolean =
            audioFile?.exists() == true && (audioFile?.length() ?: 0) > 0

        @JavascriptInterface
        fun startPlayback(): String {
            return try {
                if (audioFile == null || !audioFile!!.exists()) return "NO_FILE"
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile!!.absolutePath)
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
                "ERROR: ${e.message}"
            }
        }

        @JavascriptInterface
        fun stopPlayback(): String {
            return try {
                mediaPlayer?.apply { if (isPlaying) stop(); release() }
                mediaPlayer = null
                "OK"
            } catch (e: Exception) {
                "ERROR: ${e.message}"
            }
        }

        @JavascriptInterface
        fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
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
                Toast.makeText(this, "Permiso de microfono necesario para grabar", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaPlayer?.release()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}