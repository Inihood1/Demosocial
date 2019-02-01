package com.inihood.funspace.android.me.helper;

import android.media.MediaRecorder;

public class Storage {
    public static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    public static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    public static final String AUDIO_FOLDER = "Funspace/" + "audio";
    public static final String IMAGE_FOLDER = "Funspace/" + "image";
    public static final String IMAGE_FORMAT = ".jpg";

    public static int currentFormat = 0;
    public int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.OutputFormat.THREE_GPP };
    public static String file_exts[] = { IMAGE_FOLDER, IMAGE_FORMAT };
}
