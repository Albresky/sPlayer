package cn.albresky.splayer.Utils;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import com.dhl.filescanner.AbstractScanCallback;
import com.dhl.filescanner.FileScanner;

import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.Bean.Video;

public class SuperScanner {

    private final String TAG = "SuperScanner";
    private FileScanner mFileScanner;
    private boolean noMedia = true;
    private long mStartTime;
    private boolean scanHiddenEnable = true;
    private boolean scanFileDetailsEnable = true;
    private int threadCount = 8;
    private String[] scanTypes;
    private int scanDepth = 4;
    private List<FileScanner.FindItem> scannedFiles;
    private volatile boolean scanFinished = false;

    public List<FileScanner.FindItem> getScannedFiles() {
        return scannedFiles;
    }

    public void setScanType(String[] types) {
        scanTypes = types;
    }

    public void setThreadCount(int count) {
        threadCount = count;
    }


    public List<Song> getAudioData() {
        while (!scanFinished) ;
        if (scannedFiles == null || scannedFiles.size() == 0) {
            return null;
        }

        List<Song> songs = new ArrayList<>();


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (FileScanner.FindItem item : scannedFiles) {
            try {
                mmr.setDataSource(item.path);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Song song = new Song();
            song.song = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            song.path = item.path;
            song.duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            song.size = item.size;
            song.type = Converter.typeConvert(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
            song.singer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            song.album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            if (song.duration == 0) {
                Log.d(TAG, "getMusicData: " + song.path + " not supported");
                continue;
            }
            if (song.size > 1000 * 800) {
                if (song.song != null) {
                    if (song.song.contains("-")) {
                        String[] str = song.song.split("-");
                        song.singer = str[0];
                        song.song = str[1];
                    }
                } else {
                    song.song = "未知歌曲";
                    song.singer = "未知艺术家";
                    song.album = "未知专辑";
                }
                songs.add(song);
            }
        }
        try {
            mmr.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<Video> getVideoData() {

        while (!scanFinished) ;
        if (scannedFiles == null || scannedFiles.size() == 0) {
            return null;
        }

        List<Video> videos = new ArrayList<>();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for (FileScanner.FindItem item : scannedFiles) {
            try {
                mmr.setDataSource(item.path);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Video video = new Video();
            video.name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            video.path = item.path;
            video.duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            video.size = item.size;
            video.type = Converter.typeConvert(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE));
            video.width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            video.height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            video.date = String.valueOf(item.modifyTime);


            if (video.size > 1000 * 800) {
                video.isCheck = false;

                if (video.name == null) {
                    video.name = item.path.substring(item.path.lastIndexOf("/") + 1);
                }
                videos.add(video);
            }
        }
        return videos;
    }


    public void setScanDepth(int depth) {
        scanDepth = depth;
    }


    public void startScan() {
        Log.d(TAG, "startScan: ");
        scanFinished = false;
        if (mFileScanner != null) mFileScanner.stopScan();
        if (scanTypes == null) {
            Log.d(TAG, "startScan: scanTypes is null");
            return;
        }
        mFileScanner = new FileScanner();

        String[] filteredsuffixes = null;
        if (noMedia) {
            filteredsuffixes = new String[]{"jpg", "jpeg", "png", "bmp", "gif"};
        }


        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        String[] scanPath = new String[]{sdPath};

        mFileScanner.setScanParams(scanTypes, filteredsuffixes, threadCount, scanDepth, scanFileDetailsEnable);
        mFileScanner.setScanHiddenEnable(scanHiddenEnable);
        mFileScanner.setScanPath(scanPath);


        mFileScanner.startScan(new AbstractScanCallback() {
            @Override
            public void onScanStart() {
                mStartTime = System.currentTimeMillis();
            }

            @Override
            public void onScanFinish(final List<FileScanner.FindItem> files, final boolean isCancel) {
                final long time = System.currentTimeMillis() - mStartTime;
                final int count = files == null ? 0 : files.size();

                // print scanned files
                if (files != null) {
                    scannedFiles = files;

                    for (FileScanner.FindItem file : files) {
                        Log.d(TAG, "onScanFinish: " + file.path);
                    }
                }

                scanFinished = true;
                Log.d("SuperScanner", "onScanFinish: {fileCount:" + count + ",timeSpent:" + time + "}");
            }
        });
    }
}
