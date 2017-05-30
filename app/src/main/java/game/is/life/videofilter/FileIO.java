package game.is.life.videofilter;

import android.os.Environment;

import java.io.File;

/**
 * Created by yzhao on 5/29/17.
 */

public class FileIO {

    private static final String APP_FOLDER = "Video_filter";
    private static final String VIDEO_FILE_NAME = "render_video.mp4";
    private static Integer videoCount = 0;
    private static FileIO fileIO = null;

    private FileIO(){
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + APP_FOLDER);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (!success) {
            throw new RuntimeException("Failed to create app folder");
        }



    }

    public static FileIO getInstance(){
        if (fileIO == null){
            fileIO = new FileIO();
            return fileIO;
        }else
            return fileIO;
    }

    public File getAppFolder(){
        StringBuilder stringBuilder= new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory());
        stringBuilder.append(File.separator);
        stringBuilder.append(APP_FOLDER);
        return new File(stringBuilder.toString());
    }

    public File getFile(){
        videoCount += 1;
        StringBuilder stringBuilder= new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory());
        stringBuilder.append(File.separator);
        stringBuilder.append(APP_FOLDER);
        stringBuilder.append(File.separator);
        stringBuilder.append(videoCount.toString());
        stringBuilder.append("_");
        stringBuilder.append(VIDEO_FILE_NAME);
        return new File(stringBuilder.toString());
    }
}
