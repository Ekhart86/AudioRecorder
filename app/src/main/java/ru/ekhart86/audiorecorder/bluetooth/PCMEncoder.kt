package ru.ekhart86.audiorecorder.bluetooth

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


class PCMEncoder(
    private val bitrate: Int,
    private val sampleRate: Int,
    private val channelCount: Int
) {

    private var mediaFormat: MediaFormat? = null
    private var mediaCodec: MediaCodec? = null
    private var mediaMuxer: MediaMuxer? = null
    private lateinit var codecInputBuffers: Array<ByteBuffer>
    private lateinit var codecOutputBuffers: Array<ByteBuffer>
    private var bufferInfo: MediaCodec.BufferInfo? = null
    private var outputPath: String? = null
    private var audioTrackId = 0
    private var totalBytesRead = 0
    private var presentationTimeUs = 0.0

    companion object {
        private const val TAG = "PCMEncoder"
        private const val COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm"
        private const val CODEC_TIMEOUT = 5000
    }

    fun setOutputPath(outputPath: String?) {
        this.outputPath = outputPath
    }


    fun prepare() {
        checkNotNull(outputPath) { "outputPath должен быть установлен первым!" }
        try {
            mediaFormat = MediaFormat.createAudioFormat(
                COMPRESSED_AUDIO_FILE_MIME_TYPE,
                sampleRate,
                channelCount
            )
            mediaFormat.run {
                this!!.setInteger(
                    MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC
                )
                setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            }
            mediaCodec =
                MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE)
            mediaCodec!!.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec!!.start()
            codecInputBuffers = mediaCodec!!.inputBuffers
            codecOutputBuffers = mediaCodec!!.outputBuffers
            bufferInfo = MediaCodec.BufferInfo()
            mediaMuxer = MediaMuxer(outputPath!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            totalBytesRead = 0
            presentationTimeUs = 0.0
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Ошибка инициализации PCMEncoder",
                e
            )
        }
    }

    fun stop() {
        Log.d(TAG, "Остановка PCMEncoder")
        handleEndOfStream()
        mediaCodec!!.stop()
        mediaCodec!!.release()
        mediaMuxer!!.stop()
        mediaMuxer!!.release()
    }

    private fun handleEndOfStream() {
        val inputBufferIndex =
            mediaCodec!!.dequeueInputBuffer(CODEC_TIMEOUT.toLong())
        mediaCodec!!.queueInputBuffer(
            inputBufferIndex,
            0,
            0,
            presentationTimeUs.toLong(),
            MediaCodec.BUFFER_FLAG_END_OF_STREAM
        )
        writeOutputs()
    }


    fun encode(inputStream: InputStream, sampleRate: Int) {
        Log.d(TAG, "Старт кодирования InputStream")
        val tempBuffer = ByteArray(2 * sampleRate)
        var hasMoreData = true
        var stop = false
        while (!stop) {
            var inputBufferIndex = 0
            var currentBatchRead = 0
            while (inputBufferIndex != -1 && hasMoreData && currentBatchRead <= 50 * sampleRate) {
                inputBufferIndex =
                    mediaCodec!!.dequeueInputBuffer(CODEC_TIMEOUT.toLong())
                if (inputBufferIndex >= 0) {
                    val buffer = codecInputBuffers[inputBufferIndex]
                    buffer.clear()
                    val bytesRead = inputStream.read(tempBuffer, 0, buffer.limit())
                    if (bytesRead == -1) {
                        mediaCodec!!.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            presentationTimeUs.toLong(),
                            0
                        )
                        hasMoreData = false
                        stop = true
                    } else {
                        totalBytesRead += bytesRead
                        currentBatchRead += bytesRead
                        buffer.put(tempBuffer, 0, bytesRead)
                        mediaCodec!!.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            bytesRead,
                            presentationTimeUs.toLong(),
                            0
                        )
                        presentationTimeUs = 1000000L * (totalBytesRead / 2) / sampleRate.toDouble()
                    }
                }
            }
            writeOutputs()
        }
        inputStream.close()
        Log.d(TAG, "Конец кодирования InputStream")
    }

    private fun writeOutputs() {
        var outputBufferIndex = 0
        while (outputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
            outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(
                bufferInfo!!,
                CODEC_TIMEOUT.toLong()
            )
            if (outputBufferIndex >= 0) {
                val encodedData = codecOutputBuffers[outputBufferIndex]
                encodedData.position(bufferInfo!!.offset)
                encodedData.limit(bufferInfo!!.offset + bufferInfo!!.size)
                if (bufferInfo!!.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0 && bufferInfo!!.size != 0) {
                    mediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
                } else {
                    mediaMuxer!!.writeSampleData(
                        audioTrackId,
                        codecOutputBuffers[outputBufferIndex],
                        bufferInfo!!
                    )
                    mediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mediaFormat = mediaCodec!!.outputFormat
                audioTrackId = mediaMuxer!!.addTrack(mediaFormat!!)
                mediaMuxer!!.start()
            }
        }
    }
}
