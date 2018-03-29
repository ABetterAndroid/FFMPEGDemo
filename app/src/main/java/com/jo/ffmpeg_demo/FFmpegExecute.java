package com.jo.ffmpeg_demo;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

/**
 * Created by tangyx
 * Date 2017/8/4
 * email tangyx@live.com
 */

public class FFmpegExecute {

    private FFmpeg ffmpeg;

    public FFmpegExecute(Context context) {
        ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
        }
    }

    public void execute(String[] commands, ExecuteBinaryResponseHandler executeBinaryResponseHandler) throws FFmpegCommandAlreadyRunningException {
        ffmpeg.execute(commands, executeBinaryResponseHandler);
    }

    /**
     * 视频左右分屏 https://stackoverflow.com/a/42257415/6080136
     * https://stackoverflow.com/a/39220071/6080136
     * @param rightVideoUrl
     * @param outPutUrl
     * @return
     */
    public String[] composeVideoMergeCommands(String leftVideoUrl, String rightVideoUrl, String outPutUrl) {
        File outPutFile = new File(outPutUrl);
        if (outPutFile.exists()) {
            outPutFile.delete();
        }
        String[] commands = new String[11];
        commands[0] = "-i";
        commands[1] = leftVideoUrl;
        commands[2] = "-i";
        commands[3] = rightVideoUrl;
        commands[4] = "-filter_complex";
        commands[5] = "[0:v]scale=480:640,setsar=1[l];[1:v]scale=480:640,setsar=1[r];[l][r]hstack;[0][1]amix";
        commands[6] = "-c:v";
        commands[7] = "libx264";
        commands[8] = "-preset";
        commands[9] = "ultrafast";
        commands[10] = outPutUrl;
        return commands;
    }

    private String[] commandString2Array(String commandString) {
        if (commandString.startsWith("ffmpeg")) {
            commandString = commandString.split("ffmpeg")[1];
        }
        return commandString.trim().split(" ");
    }
}
