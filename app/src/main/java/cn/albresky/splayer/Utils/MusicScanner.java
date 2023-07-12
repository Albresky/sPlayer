package cn.albresky.splayer.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;

import java.util.ArrayList;
import java.util.List;

public class MusicScanner {
    /**
     * 利用 MediaStore 接口，扫描存储内的音频文件
     */

    public static final String TAG = "MusicScanner";

    public static String typeConvert(String mimeType) {
        String type;
        try {
            if (mimeType.contains("/")) {
                String[] str = mimeType.split("/");
                type = str[1];
            } else {
                throw new Exception("mimeType is not valid");
            }
        } catch (Exception e) {
            Log.e(TAG, "typeConvert: ", e);
            type = "Unknown";
        }
        switch (type) {
            case "mpeg":
                return "MP3";
            case "wav":
                return "WAV";
            case "flac":
                return "FLAC";
            case "ape":
                return "APE";
            case "aac":
                return "AAC";
            case "ogg":
                return "OGG";
            case "wma":
                return "WMA";
            default:
                return "Unknown";
        }
    }

    public static List<Song> getMusicData(Context context) {
        List<Song> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.Media.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                song.albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                song.type = typeConvert(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)));
                Log.d(TAG, song.song);

                if (song.size > 1000 * 800) {
                    if (song.song.contains("-")) {
                        String[] str = song.song.split("-");
                        song.singer = str[0];
                        song.song = str[1];
                    }
                    list.add(song);
                }
            }
            cursor.close();
        }
        return list;
    }


    public static Bitmap getAlbumPicture(Context context, String path, int type) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture;
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inScaled = false;

        if (data != null) {
            albumPicture = createBitmapWithScale(BitmapFactory.decodeByteArray(data, 0, data.length, mOptions), false);
        } else {
            if (type == 1) {
                albumPicture = BitmapFactory.decodeResource(context.getResources(), R.mipmap.record, mOptions);
            } else {
                albumPicture = BitmapFactory.decodeResource(context.getResources(), R.mipmap.notify_music, mOptions);
            }
            albumPicture = createBitmapWithScale(albumPicture, false);
        }
        return albumPicture;
    }

    public static Bitmap createBitmapWithScale(Bitmap bitmap, boolean filter) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float sx = ((float) 120 / width);
        float sy = ((float) 120 / height);
        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, filter);
    }
}

