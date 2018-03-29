package com.jo.ffmpeg_demo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;

/**
 * Created by rongzhu on 2018/3/26.
 */

public class MainActivity extends Activity implements View.OnClickListener {

    private VideoView videoView;
    private SurfaceView surfaceView;
    private MediaHelper mMediaHelper;
    private FileUtils fileUtils;
    private String modelPath;
    private ProgressBar progressBar;
    private TextView tvProcess;
    private FFmpegExecute fFmpegExecute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = findViewById(R.id.video_model);
        surfaceView = findViewById(R.id.video_record);
        progressBar = findViewById(R.id.progress_bar);
        tvProcess = findViewById(R.id.ffmpeg_process);

        fileUtils = new FileUtils(this);
        modelPath = fileUtils.copyAssets(this);
        videoView.setVideoPath(modelPath);

        mMediaHelper = new MediaHelper(this);
        mMediaHelper.setTargetDir(new File(fileUtils.getStorageDirectory()));
        mMediaHelper.setTargetName(System.currentTimeMillis() + ".mp4");

        fFmpegExecute = new FFmpegExecute(this);
        findViewById(R.id.start_record).setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //启动相机
        mMediaHelper.setSurfaceView(surfaceView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record:
                tvProcess.setText("");
                mMediaHelper.startPreView();
                surfaceView.setVisibility(View.VISIBLE);
                videoView.setVideoPath(modelPath);

                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mMediaHelper.stopRecordSave();
                        mMediaHelper.releaseCamera();
                        String path = mMediaHelper.getTargetFilePath();
                        final String outPath = fileUtils.getStorageDirectory() + "/mixed_video.mp4";
                        try {
                            fFmpegExecute.execute(fFmpegExecute.composeVideoMergeCommands(modelPath, path, outPath), new ExecuteBinaryResponseHandler() {
                                @Override
                                public void onFailure(String s) {
                                    Toast.makeText(MainActivity.this, "FAILED with output : " + s, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onSuccess(String s) {
                                    tvProcess.setText("SUCCESS with output");
                                    videoView.setVideoPath(outPath);
                                    videoView.start();
                                    videoView.setOnCompletionListener(null);
                                    surfaceView.setVisibility(View.GONE);
                                }

                                @Override
                                public void onProgress(String s) {
                                    tvProcess.setText(s);
                                }

                                @Override
                                public void onStart() {
                                    progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onFinish() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        } catch (FFmpegCommandAlreadyRunningException e) {
                            e.printStackTrace();
                        }
                    }
                });
                videoView.start();
                mMediaHelper.record();
                break;
        }
    }
}
