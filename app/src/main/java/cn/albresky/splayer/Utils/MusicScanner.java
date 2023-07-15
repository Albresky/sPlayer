package cn.albresky.splayer.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;

public class MusicScanner {
    /**
     * 利用 MediaStore 接口，扫描存储内的音频文件
     */

    public static final String TAG = "MusicScanner";


    public static List<Song> getMusicData(Context context) {
        List<Song> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
                null, MediaStore.Audio.Media.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                song.singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                song.albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                song.album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                song.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                song.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                song.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                song.type = Converter.typeConvert(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)));
                Log.d(TAG, song.song);


                // check song.path file exists or not
                File mFile = new File(song.path);
                if (!mFile.exists()) {
                    Log.d(TAG, "getMusicData: " + song.path + " not exists");
                    continue;
                }

                if (song.duration == 0) {
                    Log.d(TAG, "getMusicData: " + song.path + " not supported");
                    continue;
                }

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

    public static byte[] isAlbumContainCover(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        byte[] data;
        try {
            mmr.setDataSource(path);
            data = mmr.getEmbeddedPicture();
        } catch (Exception e) {
            Log.e(TAG, "getAlbumPicture: ", e);
            data = null;
        }
        return data;
    }

    public static Bitmap getAlbumPicture(Context context, byte[] data, int type) {
//        byte[] data = isAlbumContainCover(path);

        Bitmap albumPicture;
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inScaled = false;

        if (data != null) {
            albumPicture = Converter.createBitmapWithScale(BitmapFactory.decodeByteArray(data, 0, data.length, mOptions), 120, 120, false);
        } else {
            albumPicture = BitmapFactory.decodeResource(context.getResources(), R.drawable.record, mOptions);
            // type == 1, small picture
            if (type == 1) {
                albumPicture = Converter.createBitmapWithScale(albumPicture, 120, 120, false);
            } else {
                // type != 1, large picture
                albumPicture = Converter.createBitmapWithScale(albumPicture, 1024, 1024, false);
            }

        }
        return albumPicture;
    }


}

