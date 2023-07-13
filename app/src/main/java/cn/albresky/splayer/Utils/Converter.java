package cn.albresky.splayer.Utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class Converter {

    private static final String TAG = "Converter";

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
        if (!category.equals("audio") && !category.equals("video")) {
            return "Unknown";
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
            case "mp4":
                return "MP4";
            case "mkv":
                return "MKV";
            case "avi":
                return "AVI";
            default:
                return "Unknown";
        }
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


