package cn.albresky.splayer.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
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
                video.width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                video.height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                video.videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));

                File mFile = new File(video.path);
                if (!mFile.exists()) {
                    Log.d(TAG, "getVideoData: " + video.path + " not exists");
                    continue;
                }

                if (video.size > 1000 * 800) {
                    video.isCheck = false;
                    list.add(video);
                }
            }
            cursor.close();
        }
        return list;
    }

    public static Bitmap getVideoThumbnail(String path, int width, int height) {
        File mFile = new File(path);
        if (!mFile.exists()) {
            Log.d(TAG, "createVideoThumbnail: " + path + " not exists");
            return null;
        }
        Bitmap bitmap;
        try {
//            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                bitmap = mmr.getPrimaryImage();
                bitmap = mmr.getScaledFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height);
            } else {
                bitmap = mmr.getFrameAtTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }
        if (bitmap != null && bitmap.getWidth() > 300) {
            bitmap = Converter.createBitmapWithScale(bitmap, false);
        }
        return bitmap;
    }
}
