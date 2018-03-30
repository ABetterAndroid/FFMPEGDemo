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

    /**
     * 提取单独的视频，没有声音
     *
     * @param videoUrl
     * @param outUrl
     * @return
     */
    public String[] extractVideo(String videoUrl, String outUrl) {
        File outPutFile = new File(outUrl);
        if (outPutFile.exists()) {
            outPutFile.delete();
        }
        String[] commands = new String[7];
        commands[0] = "-i";
        commands[1] = videoUrl;
        commands[2] = "-vcodec";
        commands[3] = "copy";
        commands[4] = "-an";
        commands[5] = "-y";
        commands[6] = outUrl;
        return commands;
    }

    /**
     * 音频，视频合成
     * @param videoUrl
     * @param musicOrAudio
     * @param outputUrl
     * @return
     */
    public String[] composeVideo(String videoUrl, String musicOrAudio, String outputUrl) {
        File outPutFile = new File(outputUrl);
        if (outPutFile.exists()) {
            outPutFile.delete();
        }
        String[] commands = new String[9];
        //输入
        commands[0] = "-i";
        commands[1] = videoUrl;
        //音乐
        commands[2] = "-i";
        commands[3] = musicOrAudio;
        //覆盖输出
        commands[4] = "-vcodec";
        commands[5] = "copy";
        commands[6] = "-acodec";
        commands[7] = "copy";
        //输出文件
        commands[8] = outputUrl;
        return commands;
    }

    private String[] commandString2Array(String commandString) {
        if (commandString.startsWith("ffmpeg")) {
            commandString = commandString.split("ffmpeg")[1];
        }
        return commandString.trim().split(" ");
    }

    /**
     * 添加背景音乐
     *(因执行时间过长：选择了extractVideo + composeVideo)
     * @param videoin          视频文件
     * @param audioin          音频文件
     * @param output           输出路径
     * @param videoVolume      视频原声音音量(例:0.7为70%)
     * @param audioVolume      背景音乐音量(例:1.5为150%)
     */
    public String[] music(String videoin, String audioin, String output, float videoVolume, float audioVolume) {
        File outPutFile = new File(output);
        if (outPutFile.exists()) {
            outPutFile.delete();
        }
        String[] commands = new String[16];
        commands[0] = "-y";
        commands[1] = "-i";
        commands[2] = videoin;
        commands[3] = "-i";
        commands[4] = audioin;
        commands[5] = "-filter_complex";
        commands[6] = "[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + videoVolume + "[a0];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=" + audioVolume + "[a1];[a0][a1]amix=inputs=2:duration=first[aout]";
        commands[7] = "-map";
        commands[8] = "[aout]";
        commands[9] = "-ac";
        commands[10] = "2";
        commands[11] = "-c:v";
        commands[12] = "copy";
        commands[13] = "-map";
        commands[14] = "0:v:0";
        commands[15] = output;
        return commands;
    }
}
