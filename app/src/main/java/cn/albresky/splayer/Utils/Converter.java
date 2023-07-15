package cn.albresky.splayer.Utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Converter {

    private static final String TAG = "Converter";

    public static String resolutionConvert(int w, int h) {
        return w + "x" + h;
    }

    public static String sizeConvert(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return size / 1024 + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / (1024 * 1024) + "MB";
        } else {
            return size / (1024 * 1024 * 1024) + "GB";
        }
    }

    public static String typeConvert(String mimeType) {
        String type;
        String category;
        try {
            if (mimeType.contains("/")) {
                String[] str = mimeType.split("/");
                category = str[0];
                type = str[1];
            } else {
                throw new Exception("mimeType is not valid");
            }
        } catch (Exception e) {
            Log.e(TAG, "typeConvert: ", e);
            type = category = "Unknown";

        }
        if (category.equals("audio")) {
            // Audio MIME
            switch (type) {
                case "aac":
                case "aac-adts":
                    return "aac";
                case "ac3":
                    return "ac3";
                case "mpeg":
                    return "mp3";
                case "mp4":
                    return "m4a";
                case "flac":
                    return "flac";
                case "ape":
                    return "ape";
                case "ogg":
                    return "oga";
                case "wav":
                case "x-wav":
                    return "wav";
                case "webm":
                    return "weba";
                case "3gpp":
                    return "3gp";
                case "3gpp2":
                    return "3g2";
                default:
                    return "unknown";
            }
        } else if (category.equals("video")) {
            // Video MIME
            switch (type) {
                case "x-ms-wmv":
                    return "wav";
                case "webm":
                    return "webm";
                case "wma":
                    return "wma";
                case "mpeg":
                    return "mpeg";
                case "mpeg2":
                case "mp2ts":
                    return "m2ts";
                case "mp4":
                    return "mp4";
                case "mpeg4":
                    return "mpeg4";
                case "mkv":
                    return "mkv";
                case "ogg":
                    return "ogg";
                case "mp2t":
                    return "ts";
                case "x-msvideo":
                case "avi":
                    return "avi";
                case "x-flv":
                    return "flv";
                case "3gpp":
                    return "3gp";
                case "3gpp2":
                    return "3g2";
                default:
                    return "unknown";
            }
        } else {
            return "Unknown";
        }
    }

    public static Bitmap createBitmapWithScale(Bitmap bitmap, int targetX, int targetY, boolean filter) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float sx = ((float) targetX / width);
        float sy = ((float) targetY / height);
        matrix.postScale(sx, sy);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, filter);
    }

    public static Bitmap createBitmapWithNoScale(Context context, int resId) {
        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inScaled = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, mOptions);
    }

    public static Uri getAudioAlbumImageContentUri(long albumId) {
        Uri imgUri = null;
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        imgUri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Log.d(TAG, "AudioCoverImgUri = " + imgUri.toString());
        return imgUri;
    }

    public static Uri getAudioAlbumImageContentUri(Context context, String filePath) {
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA + "=? ";
        String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID};

        Cursor cursor = context.getContentResolver().query(
                audioUri,
                projection,
                selection,
                new String[]{filePath}, null);

        Uri imgUri = null;
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            if (index >= 0) {
                Log.e(TAG, "getAudioAlbumImageContentUri: more than one result");
                long albumId = cursor.getLong(index);
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                imgUri = ContentUris.withAppendedId(sArtworkUri,
                        albumId);
                Log.d(TAG, "AudioCoverImgUri = " + imgUri.toString());
                cursor.close();
            }
        }
        return imgUri;

    }

}


