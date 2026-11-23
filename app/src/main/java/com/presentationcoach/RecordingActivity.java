package com.presentationcoach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordingActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 101;

    // Whisper-compatible recording settings
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG =
            AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT =
            AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private Thread recordingThread;

    private TextView recordButton;
    private TextView timerText;
    private TextView recordStatus;

    private boolean isRecording = false;

    private long recordingStartTime;
    private long recordingDurationSeconds = 0;

    private String audioFilePath;
    private String practiceMode = "Talk";

    private final Handler timerHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (!isRecording) {
                        return;
                    }

                    long elapsedMillis =
                            System.currentTimeMillis()
                                    - recordingStartTime;

                    long seconds =
                            elapsedMillis / 1000;

                    long minutes =
                            seconds / 60;

                    long remainingSeconds =
                            seconds % 60;

                    String time =
                            String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d",
                                    minutes,
                                    remainingSeconds
                            );

                    timerText.setText(time);

                    timerHandler.postDelayed(
                            this,
                            500
                    );
                }
            };


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_recording
        );


        // =========================================
        // FIND VIEWS
        // =========================================

        recordButton =
                findViewById(R.id.recordButton);

        timerText =
                findViewById(R.id.timerText);

        recordStatus =
                findViewById(R.id.recordStatus);

        TextView backButton =
                findViewById(R.id.backButton);


        // =========================================
        // GET MODE
        // =========================================

        String incomingMode =
                getIntent().getStringExtra(
                        "practice_mode"
                );

        // This app now supports Talk practice only.
        practiceMode = "Talk";


        // =========================================
        // BACK BUTTON
        // =========================================

        backButton.setOnClickListener(v -> {

            if (isRecording) {
                stopRecording();
            } else {
                finish();
            }
        });


        // =========================================
        // MODE BUTTONS
        // =========================================

        setupModeButtons();


        // =========================================
        // RECORD BUTTON
        // =========================================

        recordButton.setOnClickListener(v -> {

            if (isRecording) {

                stopRecording();

            } else {

                checkMicrophonePermission();

            }
        });


        // =========================================
        // INITIAL MODE
        // =========================================

        updateModeAppearance();
    }


    // =========================================================
    // MODE BUTTON SETUP
    // =========================================================

    private void setupModeButtons() {
        // Talk is the only available practice mode.
        practiceMode = "Talk";
    }


    // =========================================================
    // FIND VIEW BY TEXT
    // =========================================================

    private View findViewContainingText(
            View view,
            String text
    ) {

        if (view instanceof TextView) {

            String currentText =
                    ((TextView) view)
                            .getText()
                            .toString();

            if (
                    currentText
                            .toLowerCase(Locale.getDefault())
                            .contains(
                                    text.toLowerCase(
                                            Locale.getDefault()
                                    )
                            )
            ) {

                return view;
            }
        }


        if (view instanceof ViewGroup) {

            ViewGroup group =
                    (ViewGroup) view;

            for (
                    int i = 0;
                    i < group.getChildCount();
                    i++
            ) {

                View result =
                        findViewContainingText(
                                group.getChildAt(i),
                                text
                        );

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }


    // =========================================================
    // UPDATE SELECTED MODE
    // =========================================================

    private void updateModeAppearance() {
        // No mode selector: Talk is always active.
        practiceMode = "Talk";
    }


    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private void checkMicrophonePermission() {

        if (
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                )
                        == PackageManager.PERMISSION_GRANTED
        ) {

            startRecording();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    RECORD_AUDIO_PERMISSION_CODE
            );
        }
    }


    // =========================================================
    // START RECORDING
    // =========================================================

    private void startRecording() {

        // Only Talk uses this recording screen.
        if (!practiceMode.equals("Talk")) {

            Toast.makeText(
                    this,
                    practiceMode +
                            " uses the AI conversation screen.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // =========================================
        // FIND AUDIO BUFFER SIZE
        // =========================================

        int minBufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE,
                        CHANNEL_CONFIG,
                        AUDIO_FORMAT
                );

        if (
                minBufferSize ==
                        AudioRecord.ERROR
                        ||
                        minBufferSize ==
                                AudioRecord.ERROR_BAD_VALUE
        ) {

            Toast.makeText(
                    this,
                    "This device does not support 16 kHz recording.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        int bufferSize =
                Math.max(
                        minBufferSize,
                        4096
                );


        try {

            // =========================================
            // AUDIO DIRECTORY
            // =========================================

            File musicDirectory =
                    getExternalFilesDir(
                            Environment.DIRECTORY_MUSIC
                    );

            if (musicDirectory == null) {

                Toast.makeText(
                        this,
                        "Unable to access audio storage",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            if (!musicDirectory.exists()) {
                musicDirectory.mkdirs();
            }


            // =========================================
            // CREATE WAV FILE
            // =========================================

            String timestamp =
                    new SimpleDateFormat(
                            "yyyyMMdd_HHmmss",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    );


            File audioFile =
                    new File(
                            musicDirectory,
                            "session_"
                                    + timestamp
                                    + ".wav"
                    );


            audioFilePath =
                    audioFile.getAbsolutePath();


            // =========================================
            // CREATE AUDIO RECORDER
            // =========================================

            audioRecord =
                    new AudioRecord(
                            MediaRecorder.AudioSource.MIC,
                            SAMPLE_RATE,
                            CHANNEL_CONFIG,
                            AUDIO_FORMAT,
                            bufferSize
                    );


            if (
                    audioRecord.getState()
                            != AudioRecord.STATE_INITIALIZED
            ) {

                audioRecord.release();
                audioRecord = null;

                Toast.makeText(
                        this,
                        "Could not initialize microphone.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }


            // =========================================
            // CREATE WAV HEADER
            // =========================================

            createEmptyWavFile(
                    audioFilePath
            );


            // =========================================
            // START RECORDING
            // =========================================

            audioRecord.startRecording();

            isRecording = true;

            recordingStartTime =
                    System.currentTimeMillis();

            recordingDurationSeconds = 0;


            timerText.setText(
                    "00:00"
            );

            timerHandler.post(
                    timerRunnable
            );


            recordStatus.setText(
                    "Recording your voice..."
            );

            recordButton.setText(
                    "■"
            );


            Toast.makeText(
                    this,
                    "Recording started 🎙️",
                    Toast.LENGTH_SHORT
            ).show();


            // =========================================
            // START BACKGROUND AUDIO THREAD
            // =========================================

            recordingThread =
                    new Thread(
                            () -> recordAudioToWav(
                                    audioFilePath,
                                    bufferSize
                            ),
                            "AudioRecordingThread"
                    );

            recordingThread.start();


        } catch (Exception e) {

            e.printStackTrace();

            releaseAudioRecorder();

            isRecording = false;

            timerHandler.removeCallbacks(
                    timerRunnable
            );

            Toast.makeText(
                    this,
                    "Could not start recording",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // =========================================================
    // RECORD PCM AUDIO
    // =========================================================

    private void recordAudioToWav(
            String filePath,
            int bufferSize
    ) {

        byte[] audioBuffer =
                new byte[bufferSize];

        FileOutputStream outputStream = null;

        long totalAudioBytes = 0;


        try {

            outputStream =
                    new FileOutputStream(
                            filePath,
                            true
                    );


            while (isRecording) {

                int bytesRead =
                        audioRecord.read(
                                audioBuffer,
                                0,
                                audioBuffer.length
                        );


                if (bytesRead > 0) {

                    outputStream.write(
                            audioBuffer,
                            0,
                            bytesRead
                    );

                    totalAudioBytes +=
                            bytesRead;
                }
            }


        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (outputStream != null) {

                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // Update WAV header
            try {

                writeWavHeader(
                        filePath,
                        totalAudioBytes
                );

            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }


    // =========================================================
    // CREATE EMPTY WAV HEADER
    // =========================================================

    private void createEmptyWavFile(
            String filePath
    ) throws IOException {

        FileOutputStream outputStream =
                new FileOutputStream(
                        filePath
                );

        byte[] header =
                new byte[44];

        outputStream.write(header);

        outputStream.close();
    }


    // =========================================================
    // WRITE PROPER WAV HEADER
    // =========================================================

    private void writeWavHeader(
            String filePath,
            long audioDataLength
    ) throws IOException {

        RandomAccessFile wavFile =
                new RandomAccessFile(
                        filePath,
                        "rw"
                );

        wavFile.seek(0);


        long fileSize =
                audioDataLength + 36;


        // RIFF
        wavFile.writeBytes("RIFF");

        writeIntLittleEndian(
                wavFile,
                (int) fileSize
        );


        // WAVE
        wavFile.writeBytes("WAVE");


        // fmt
        wavFile.writeBytes("fmt ");

        writeIntLittleEndian(
                wavFile,
                16
        );


        // PCM format
        writeShortLittleEndian(
                wavFile,
                (short) 1
        );


        // Mono
        writeShortLittleEndian(
                wavFile,
                (short) 1
        );


        // Sample rate
        writeIntLittleEndian(
                wavFile,
                SAMPLE_RATE
        );


        // Byte rate
        int byteRate =
                SAMPLE_RATE * 1 * 16 / 8;

        writeIntLittleEndian(
                wavFile,
                byteRate
        );


        // Block align
        writeShortLittleEndian(
                wavFile,
                (short) (1 * 16 / 8)
        );


        // Bits per sample
        writeShortLittleEndian(
                wavFile,
                (short) 16
        );


        // data
        wavFile.writeBytes("data");


        writeIntLittleEndian(
                wavFile,
                (int) audioDataLength
        );


        wavFile.close();
    }


    // =========================================================
    // LITTLE ENDIAN HELPERS
    // =========================================================

    private void writeIntLittleEndian(
            RandomAccessFile file,
            int value
    ) throws IOException {

        file.write(
                value & 0xff
        );

        file.write(
                (value >> 8) & 0xff
        );

        file.write(
                (value >> 16) & 0xff
        );

        file.write(
                (value >> 24) & 0xff
        );
    }


    private void writeShortLittleEndian(
            RandomAccessFile file,
            short value
    ) throws IOException {

        file.write(
                value & 0xff
        );

        file.write(
                (value >> 8) & 0xff
        );
    }


    // =========================================================
    // STOP RECORDING
    // =========================================================

    private void stopRecording() {

        if (!isRecording) {
            return;
        }


        // Stop new reads
        isRecording = false;


        // Calculate duration
        recordingDurationSeconds =
                (
                        System.currentTimeMillis()
                                - recordingStartTime
                ) / 1000;


        // Stop AudioRecord
        if (audioRecord != null) {

            try {

                audioRecord.stop();

            } catch (Exception e) {

                e.printStackTrace();
            }
        }


        // Wait for recording thread
        if (recordingThread != null) {

            try {

                recordingThread.join(
                        1500
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

            recordingThread = null;
        }


        // Release recorder
        releaseAudioRecorder();


        timerHandler.removeCallbacks(
                timerRunnable
        );


        recordStatus.setText(
                "Recording saved ✨"
        );

        recordButton.setText(
                "●"
        );


        Toast.makeText(
                this,
                "WAV recording saved!",
                Toast.LENGTH_SHORT
        ).show();


        System.out.println(
                "WAV AUDIO FILE: "
                        + audioFilePath
        );

        System.out.println(
                "DURATION: "
                        + recordingDurationSeconds
                        + " seconds"
        );


        // =========================================
        // OPEN REPORT
        // =========================================

        openReport();
    }


    // =========================================================
    // RELEASE AUDIO RECORDER
    // =========================================================

    private void releaseAudioRecorder() {

        if (audioRecord != null) {

            try {

                if (
                        audioRecord.getRecordingState()
                                == AudioRecord.RECORDSTATE_RECORDING
                ) {

                    audioRecord.stop();
                }

            } catch (Exception e) {

                e.printStackTrace();
            }


            try {

                audioRecord.release();

            } catch (Exception e) {

                e.printStackTrace();
            }


            audioRecord = null;
        }
    }


    // =========================================================
    // OPEN REPORT
    // =========================================================

    private void openReport() {

        if (
                audioFilePath == null
                        ||
                        audioFilePath.isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Audio file was not created.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        File audioFile =
                new File(
                        audioFilePath
                );


        if (
                !audioFile.exists()
                        ||
                        audioFile.length() <= 44
        ) {

            Toast.makeText(
                    this,
                    "Audio recording is empty.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        RecordingActivity.this,
                        SessionReportActivity.class
                );


        intent.putExtra(
                "audio_file_path",
                audioFilePath
        );


        intent.putExtra(
                "practice_mode",
                practiceMode
        );


        intent.putExtra(
                "duration_seconds",
                recordingDurationSeconds
        );


        startActivity(intent);


        finish();
    }


    // =========================================================
    // PERMISSION RESULT
    // =========================================================

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (
                requestCode
                        == RECORD_AUDIO_PERMISSION_CODE
        ) {

            if (
                    grantResults.length > 0
                            &&
                            grantResults[0]
                                    == PackageManager.PERMISSION_GRANTED
            ) {

                startRecording();

            } else {

                Toast.makeText(
                        this,
                        "Microphone permission is required.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }


    // =========================================================
    // BACK PRESS
    // =========================================================

    @Override
    public void onBackPressed() {

        if (isRecording) {

            stopRecording();

        } else {

            super.onBackPressed();
        }
    }


    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        timerHandler.removeCallbacks(
                timerRunnable
        );


        if (isRecording) {

            isRecording = false;


            try {

                if (audioRecord != null) {

                    if (
                            audioRecord.getRecordingState()
                                    == AudioRecord.RECORDSTATE_RECORDING
                    ) {

                        audioRecord.stop();
                    }

                    audioRecord.release();

                    audioRecord = null;
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }


        super.onDestroy();
    }
}