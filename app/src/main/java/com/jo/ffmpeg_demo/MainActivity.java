package com.jo.ffmpeg_demo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.tangyx.video.ffmpeg.FFmpegCommands;
import com.tangyx.video.ffmpeg.FFmpegRun;

import java.io.File;

/**
 * Created by rongzhu on 2018/3/26.
 */

public class MainActivity extends Activity {

    private VideoView videoView;
    private SurfaceView surfaceView;
    private MediaHelper mMediaHelper;
    private FileUtils fileUtils;
    private String modelPath;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.video_model);
        surfaceView = findViewById(R.id.video_record);
        progressBar = findViewById(R.id.progress_bar);

        fileUtils = new FileUtils(this);
        modelPath = fileUtils.copyAssets(this);
        videoView.setVideoPath(modelPath);

        mMediaHelper = new MediaHelper(this);
        mMediaHelper.setTargetDir(new File(fileUtils.getStorageDirectory()));
        mMediaHelper.setTargetName(System.currentTimeMillis() + ".mp4");

        findViewById(R.id.start_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.start();
                mMediaHelper.record();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaHelper.stopRecordSave();
                String path = mMediaHelper.getTargetFilePath();
                final String outPath = fileUtils.getStorageDirectory() + "/mixed_video.mp4";
                String[] commands = FFmpegCommands.splitVideo(modelPath, path, outPath);
                FFmpegRun.execute(commands, new FFmpegRun.FFmpegRunListener() {
                    @Override
                    public void onStart() {
                        progressBar.setVisibility(View.VISIBLE);
                        mMediaHelper.releaseCamera();
                    }

                    @Override
                    public void onEnd(int result) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent v = new Intent(Intent.ACTION_VIEW);
                        v.setDataAndType(Uri.parse(outPath), "video/mp4");
                        startActivity(v);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //启动相机
        mMediaHelper.setSurfaceView(surfaceView);
    }

}
