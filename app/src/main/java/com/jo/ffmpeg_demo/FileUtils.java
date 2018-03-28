package com.jo.ffmpeg_demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    /**
     * sd卡的根目录
     */
    private static String mSdRootPath = Environment.getExternalStorageDirectory().getPath();
    /**
     * 手机的缓存根目录
     */
    private static String mDataRootPath = null;
    /**
     * 保存Image的目录名
     */
    private final static String FOLDER_NAME = "/ffmpeg_demo";

    public final static String IMAGE_NAME = "/cache";

    public FileUtils(Context context) {
        mDataRootPath = context.getCacheDir().getPath();
        makeAppDir();
    }

    public String makeAppDir() {
        String path = getStorageDirectory();
        File folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        path = path + IMAGE_NAME;
        folderFile = new File(path);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        return path;
    }

    /**
     * 获取储存Image的目录
     *
     * @return
     */
    public String getStorageDirectory() {
        String localPath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                mSdRootPath + FOLDER_NAME : mDataRootPath + FOLDER_NAME;
        File folderFile = new File(localPath);
        if (!folderFile.exists()) {
            folderFile.mkdir();
        }
        return localPath;
    }

    public String getMediaVideoPath() {
        String directory = getStorageDirectory();
        directory += "/video";
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdir();
        }
        return directory;
    }

    /**
     * 删除文件
     */
    public void deleteFile(String deletePath, String videoPath) {
        File file = new File(deletePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.listFiles().length == 0) {
                        f.delete();
                    } else {
                        deleteFile(f.getAbsolutePath(), videoPath);
                    }
                } else if (!f.getAbsolutePath().equals(videoPath)) {
                    f.delete();
                }
            }
        }
    }

    public String copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            if (!filename.contains("mp4")) {
                continue;
            }
            InputStream in = null;
            OutputStream out = null;
            File outFile = null;
            try {
                in = assetManager.open(filename);
                outFile = new File(getStorageDirectory(), filename);
                outFile.createNewFile();
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
            if (outFile != null) {
                return outFile.getAbsolutePath();
            } else {
                return null;
            }
        }
        return null;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
