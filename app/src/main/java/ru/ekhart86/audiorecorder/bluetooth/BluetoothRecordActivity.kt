package ru.ekhart86.audiorecorder.bluetooth

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import ru.ekhart86.audiorecorder.R
import ru.ekhart86.audiorecorder.sql.DBHelper
import java.io.*
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200


class BluetoothRecordActivity : AppCompatActivity() {

    private val APP_PREFERENCES = "settings"
    private val SELECTED_AUDIO_INPUT = "selectedAudioInput"
    private val SELECTED__FRECUENCY = "selectedFrecuencySampling"
    private lateinit var currentAudioInput: String
    private var currentFrecuencySampling: Int = 22050
    private lateinit var preferences: SharedPreferences

    private val TAG = BluetoothRecordActivity::class.java.canonicalName
    private val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE_FACTOR = 2


    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val recordingInProgress: AtomicBoolean = AtomicBoolean(false)
    private var recorder: AudioRecord? = null
    private var audioManager: AudioManager? = null
    private var recordingThread: Thread? = null
    private var startButton: MaterialButton? = null
    private var stopButton: MaterialButton? = null
    private var bluetoothButton: MaterialButton? = null
    private lateinit var audioInputText: TextView
    private lateinit var frecuencySamplingText: TextView


    private val bluetoothStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        private var bluetoothState = BluetoothState.UNAVAILABLE

        override fun onReceive(context: Context?, intent: Intent) {

            when (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)) {
                AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                    Log.i(
                        TAG,
                        "Bluetooth HFP Headset is connected"
                    )
                    handleBluetoothStateChange(BluetoothState.AVAILABLE)
                }
                AudioManager.SCO_AUDIO_STATE_CONNECTING -> {
                    Log.i(
                        TAG,
                        "Bluetooth HFP Headset is connecting"
                    )
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE)
                    Log.i(
                        TAG,
                        "Bluetooth HFP Headset is disconnected"
                    )
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE)
                }
                AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                    Log.i(
                        TAG,
                        "Bluetooth HFP Headset is disconnected"
                    )
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE)
                }
                AudioManager.SCO_AUDIO_STATE_ERROR -> {
                    Log.i(
                        TAG,
                        "Bluetooth HFP Headset is in error state"
                    )
                    handleBluetoothStateChange(BluetoothState.UNAVAILABLE)
                }
            }
        }

        private fun handleBluetoothStateChange(state: BluetoothState) {
            if (bluetoothState == state) {
                return
            }
            bluetoothState = state
            bluetoothStateChanged(state)
        }
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)
        //Добавляем заголовок
        val actionBar = supportActionBar
        actionBar!!.title = getString(R.string.new_record)
        preferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        startButton = findViewById<View>(R.id.btnStart) as MaterialButton
        stopButton = findViewById<View>(R.id.btnStop) as MaterialButton
        bluetoothButton = findViewById<View>(R.id.btnBluetooth) as MaterialButton
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioInputText = findViewById(R.id.inputSoundBluetooth)
        frecuencySamplingText = findViewById(R.id.samplingFrequencyBluetooth)
        //Получаем записанные в SharedPreferences радиобатоны, если ничего нет, то будут выбраны микрофон и 22050
        currentAudioInput =
            preferences.getString(SELECTED_AUDIO_INPUT, getString(R.string.microphone)).toString()
        //Получаем частоту дискретизации, если не записана никакая то используем 22050
        currentFrecuencySampling =
            preferences.getInt(SELECTED__FRECUENCY, 22050)
        audioInputText.text = currentAudioInput
        frecuencySamplingText.text = currentFrecuencySampling.toString()

        ActivityCompat.requestPermissions(
            this, permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )

    }

    //Проверить есть ли разрешение от пользователя на запись аудио
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }


    fun clickStartBtn(view: View) {
        startRecording()

    }

    fun clickActivateBluetoothBtn(view: View) {
        activateBluetoothSco()

    }

    fun clickStopRecordingBtn(view: View) {
        stopRecording()

    }

    override fun onResume() {
        super.onResume()
        bluetoothButton!!.isEnabled = calculateBluetoothButtonState()
        startButton!!.isEnabled = calculateStartRecordButtonState()
        stopButton!!.isEnabled = calculateStopRecordButtonState()
        registerReceiver(
            bluetoothStateReceiver, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        )
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
        unregisterReceiver(bluetoothStateReceiver)
    }

    private  fun getBufferSize() : Int {
        return AudioRecord.getMinBufferSize(
            currentFrecuencySampling,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR
    }


    private fun startRecording() {
        // В зависимости от устройства может потребоваться изменить AudioSource, DEFAULT либо VOICE_COMMUNICATION

        recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            currentFrecuencySampling,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            getBufferSize()
        )
        recorder!!.startRecording()
        recordingInProgress.set(true)
        recordingThread =
            Thread(RecordingRunnable(), "Recording Thread")
        recordingThread!!.start()
        bluetoothButton!!.isEnabled = calculateBluetoothButtonState()
        startButton!!.isEnabled = calculateStartRecordButtonState()
        stopButton!!.isEnabled = calculateStopRecordButtonState()
        Toast.makeText(applicationContext, "Запись началась", Toast.LENGTH_LONG).show()
    }

    private fun stopRecording() {
        if (null == recorder) {
            return
        }
        recordingInProgress.set(false)
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        recordingThread = null
        bluetoothButton!!.isEnabled = calculateBluetoothButtonState()
        startButton!!.isEnabled = calculateStartRecordButtonState()
        stopButton!!.isEnabled = calculateStopRecordButtonState()
        //Конвертируем полученный pcm файл в mp4 формат
        decodeToMp4(
            "${externalCacheDir!!.absolutePath}/audioRecordBluetooth.pcm",
            "${externalCacheDir!!.absolutePath}/audioRecordBluetooth.mp4"
        )
        //Записываем в базу
        DBHelper.addRecordToDB(this, "${externalCacheDir!!.absolutePath}/audioRecordBluetooth.mp4")

    }


    private fun activateBluetoothSco() {
        if (!audioManager!!.isBluetoothScoAvailableOffCall) {
            Log.e(
                TAG, "SCO не доступен, запись невозможна"
            )
            Toast.makeText(
                applicationContext,
                "SCO не доступен, запись невозможна",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!audioManager!!.isBluetoothScoOn) {
            audioManager!!.startBluetoothSco()
        }
    }

    private fun bluetoothStateChanged(state: BluetoothState) {
        Log.i(TAG, "Bluetooth state changed to:$state")
        if (BluetoothState.UNAVAILABLE == state && recordingInProgress.get()) {
            stopRecording()
        }
        bluetoothButton!!.isEnabled = calculateBluetoothButtonState()
        startButton!!.isEnabled = calculateStartRecordButtonState()
        stopButton!!.isEnabled = calculateStopRecordButtonState()
    }

    private fun calculateBluetoothButtonState(): Boolean {
        return !audioManager!!.isBluetoothScoOn
    }

    private fun calculateStartRecordButtonState(): Boolean {
        return audioManager!!.isBluetoothScoOn && !recordingInProgress.get()
    }

    private fun calculateStopRecordButtonState(): Boolean {
        return audioManager!!.isBluetoothScoOn && recordingInProgress.get()
    }

    private inner class RecordingRunnable : Runnable {
        override fun run() {
            var path = "${externalCacheDir!!.absolutePath}/audioRecordBluetooth.pcm"
            println(path)
            val file = File(path)
            val buffer: ByteBuffer = ByteBuffer.allocateDirect(getBufferSize())
            try {
                FileOutputStream(file).use { outStream ->
                    while (recordingInProgress.get()) {
                        val result = recorder!!.read(buffer, getBufferSize())
                        if (result < 0) {
                            throw RuntimeException(
                                "Reading of audio buffer failed: " + getBufferReadFailureReason(
                                    result
                                )
                            )
                        }
                        outStream.write(
                            buffer.array(),
                            0,
                            getBufferSize()
                        )
                        buffer.clear()
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException("Writing of recorded audio failed", e)
            }
        }

        private fun getBufferReadFailureReason(errorCode: Int): String {
            return when (errorCode) {
                AudioRecord.ERROR_INVALID_OPERATION -> "ERROR_INVALID_OPERATION"
                AudioRecord.ERROR_BAD_VALUE -> "ERROR_BAD_VALUE"
                AudioRecord.ERROR_DEAD_OBJECT -> "ERROR_DEAD_OBJECT"
                AudioRecord.ERROR -> "ERROR"
                else -> "Unknown ($errorCode)"
            }
        }
    }

    internal enum class BluetoothState {
        AVAILABLE, UNAVAILABLE
    }

    //Метод декодирует pcm файл в mp4
    private fun decodeToMp4(inputPath: String?, outputPath: String?) {
        val pcmEncoder = PCMEncoder(384000, currentFrecuencySampling, 1)
        pcmEncoder.setOutputPath(outputPath)
        pcmEncoder.prepare()
        val initialFile = File(inputPath!!)
        try {
            val targetStream: InputStream = FileInputStream(initialFile)
            pcmEncoder.encode(targetStream, currentFrecuencySampling)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pcmEncoder.stop()
    }
}