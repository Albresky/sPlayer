package cn.albresky.splayer.Utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Bean.Video;

public class VideoScanner {
    /**
     * 利用 MediaStore 接口，扫描存储内的音频文件
     */

    public static final String TAG = "VideoScanner";

    public static List<Video> getVideoData(Context context) {
        List<Video> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Video.Media.TITLE);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Video video = new Video();
                video.name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                video.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                video.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                video.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                video.type = Converter.typeConvert(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));

                File mFile = new File(video.path);
                if (!mFile.exists()) {
                    Log.d(TAG, "getVideoData: " + video.path + " not exists");
                    continue;
                }

                if (video.size > 1000 * 800) {
                    list.add(video);
                }
            }
            cursor.close();
        }
        return list;
    }

}
