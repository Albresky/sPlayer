package cn.albresky.splayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Bean.Song;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private final String TAG = "MusicService";
    private final IBinder mBinder = new AudioBinder();

    private int songIndex;

    private List<Song> songList = new ArrayList<>();
    private MediaPlayer mPlayer;

    public MusicService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: MusicService started");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MusicService created");

        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            addListener();
        }
    }

    public void addListener() {
        mPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MusicService destroyed");
        mPlayer.release();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: song completed");
    }


    public class AudioBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }

        public void updateSongList(List<Song> list) {
            songList = list;
        }

        public void prepare(int index) {
            songIndex = index;
            try {
                mPlayer.reset();
                mPlayer.setDataSource(songList.get(index).getPath());
                mPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getSongIndex() {
            return songIndex;
        }

        public void setSongPath(String songPath) {
            try {
                mPlayer.reset();
              /*  release();
                mPlayer = new MediaPlayer();*/
                addListener();
                mPlayer.setDataSource(songPath);
                mPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        public void release() {
//            if (mPlayer != null) {
//                mPlayer.release();
//            }
//        }

        public void play() {
            try {
                mPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void pause() {
            if (mPlayer != null) {
                mPlayer.pause();
            }
        }

        public void stop() {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
            }
        }

        public boolean isPlaying() {
            if (mPlayer != null) {
                return mPlayer.isPlaying();
            }
            return false;
        }

        public int getDuration() {
            if (mPlayer != null) {
                return mPlayer.getDuration();
            }
            return 0;
        }

        public int getCurrentPosition() {
            if (mPlayer != null) {
                return mPlayer.getCurrentPosition();
            }
            return 0;
        }

        public void seekTo(int position) {
            if (mPlayer != null) {
                mPlayer.seekTo(position);
            }
        }
    }
}