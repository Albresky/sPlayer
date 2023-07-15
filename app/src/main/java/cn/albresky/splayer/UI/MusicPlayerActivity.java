package cn.albresky.splayer.UI;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.Interface.IMusicController;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Service.MusicService;
import cn.albresky.splayer.Utils.AnimationUtils;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.DatetimeUtils;
import cn.albresky.splayer.databinding.ActivityMusicPlayerBinding;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MusicPlayerActivity extends AppCompatActivity {

    private final String TAG = "MusicPlayerActivity";
    private final int UPDATE_PROGRESS = 1;
    private final int seekbarMax = 200;
    private final int handleDelay = 500; // millisecond
    private ActivityMusicPlayerBinding binding;

    private int progressBuf = 3;
    private MusicService.AudioBinder mContorller;
    private MusicConnection mConnection;

    private int playType = 1; // 0: loop one, 1: loop all, 2: shuffle

    private List<Song> mList = new ArrayList<>();
    private Song song;

    private int songIndex;
    private ObjectAnimator rotateAnimator;

    private IMusicController mIMusicController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            int botm = 0, top = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                botm = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                top = insets.getSystemWindowInsetTop();
                botm = insets.getSystemWindowInsetBottom();
            }
            Log.d(TAG, "onApplyWindowInsets: top = " + top + ", botm = " + botm);

            binding.lvRoot.setPadding(0, 0, 0, 0);
            return insets;
        });
    }

    private void initView() {
//        binding.ivMusicCover.setImageBitmap(Converter.createBitmapWithScale(Converter.createBitmapWithNoScale(this, R.drawable.record), 512, 512, false));

        String jList = (String) getIntent().getSerializableExtra("songList");
        Gson gson = new Gson();
        mList = gson.fromJson(jList, new TypeToken<List<Song>>() {
        }.getType());
        songIndex = getIntent().getIntExtra("songIndex", 0);
        song = mList.get(songIndex);

        startMusicService();

        // Music Controller
        Thread thread = new Thread(() -> handler.sendEmptyMessage(UPDATE_PROGRESS));
        thread.start();


        // Cover Animation
        rotateAnimator = AnimationUtils.getRotateAnimation(binding.ivMusicCover);
        startAnimation();

        resetUI();

        // set button Click Listeners
        binding.playOrPause.setOnClickListener(v -> {
            if (mContorller == null) return;
            if (mContorller.isPlaying()) {
                mContorller.pause();
                rotateAnimator.pause();
                binding.playOrPause.setBackgroundResource(R.drawable.detail_play_circle);
            } else {
                if (mContorller.play()) {
                    rotateAnimator.resume();
                    binding.playOrPause.setBackgroundResource(R.drawable.detail_pause_circle);
                }
            }
        });

        binding.playMenu.setOnClickListener(v -> {
            finish();
        });

        binding.btnBack.setOnClickListener(v -> {
            finish();
        });

        binding.playType.setOnClickListener(v -> {
            playType = (playType + 1) % 3;
            switch (playType) {
                case 0:
                    binding.playType.setBackgroundResource(R.drawable.baseline_repeat_one);
                    mContorller.setLooping(true);
                    break;
                case 1:
                    binding.playType.setBackgroundResource(R.drawable.baseline_repeat_all);
                    mContorller.setLooping(false);
                    break;
                case 2:
                    binding.playType.setBackgroundResource(R.drawable.baseline_shuffle);
                    mContorller.setLooping(false);
                    break;
            }
        });

        binding.playNext.setOnClickListener(v -> {
            Log.d(TAG, "onClick: next");
            playNext();
//            int nextIndex = songIndex >= mList.size() - 1 ? 0 : songIndex + 1;
//            songIndex = nextIndex;
//            song = mList.get(songIndex);
//            mContorller.prepare(songIndex);
//            mContorller.play();
//            resetUI();
//            startAnimation();
        });

        binding.playPrev.setOnClickListener(v -> {
            Log.d(TAG, "onClick: previous");
            int previousIndex = songIndex <= 0 ? mList.size() - 1 : songIndex - 1;
            songIndex = previousIndex;
            song = mList.get(songIndex);
            mContorller.prepare(songIndex);
            mContorller.play();
            resetUI();
            startAnimation();
        });


        // seekbar
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "seekbar progress: " + progress);
                if (fromUser) {
                    mContorller.seekTo((int) (1.0 * progress / seekbarMax * song.getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: ");
            }
        });
    }

    private void playNext() {
        int nextIndex = songIndex >= mList.size() - 1 ? 0 : songIndex + 1;
        songIndex = nextIndex;
        song = mList.get(songIndex);
        mContorller.prepare(songIndex);
        mContorller.play();
        resetUI();
        startAnimation();
    }

    private void playRandom() {
        int randomIndex = songIndex;
        while (randomIndex == songIndex) {
            randomIndex = new Random().nextInt(mList.size());
        }
        songIndex = randomIndex;
        song = mList.get(songIndex);
        mContorller.prepare(songIndex);
        mContorller.play();
        resetUI();
        startAnimation();
    }

    public void resetUI() {
        binding.seekBar.setMax(seekbarMax);
        binding.songName.setText(song.getSong());
        binding.songName.setSelected(true);
        binding.singerName.setText(song.getSinger());
        binding.tvTotal.setText(DatetimeUtils.formatTime(song.getDuration()));
        rotateAnimator.end();

        if (song.hasCover()) {
            Uri uri;
            if (song.albumId != 0) {
                uri = Converter.getAudioAlbumImageContentUri(song.getAlbumId());
            } else {
                uri = Converter.getAudioAlbumImageContentUri(this, song.getPath());
            }
            // load background
            Glide.with(this).load(uri).bitmapTransform(new BlurTransformation(this, 25)).into(binding.playBackground);
            // load cover
            Glide.with(this).load(uri).into(binding.ivMusicCover);
        } else {
            // load background
            Glide.with(this).load(R.drawable.detail_background).bitmapTransform(new BlurTransformation(this, 15)).into(binding.playBackground);
            // load cover
            Glide.with(this).load(R.drawable.record).into(binding.ivMusicCover);
        }
    }

    private void startAnimation() {
        if (mContorller != null && mContorller.isPlaying()) {
            rotateAnimator.start();
            binding.playOrPause.setBackgroundResource(R.drawable.detail_pause_circle);
        }
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        mConnection = new MusicConnection();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void updateProgress() {
        int currenPostion = mContorller.getCurrentPosition();
        int newProgress = (int) ((1.0 * currenPostion / song.getDuration()) * seekbarMax);
        binding.seekBar.setProgress(newProgress);
        binding.tvProgress.setText(DatetimeUtils.formatTime(currenPostion));
        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, handleDelay);

        if (seekbarMax - newProgress <= 1) {
            progressBuf -= 1;
        }

        if (progressBuf == 0 || newProgress == seekbarMax) {
            Log.d(TAG, "updateProgress: reachMax" + ",playType:" + playType);
            progressBuf = 3;

            switch (playType) {
                case 0:
                    binding.seekBar.setProgress(0);
                    binding.tvProgress.setText(DatetimeUtils.formatTime(0));
                    break;
                case 1:
                    playNext();
                    break;
                case 2:
                    playRandom();
                    break;
            }
        }
        if (mContorller.isPlaying()) {
            playerViewStart();
        }
    }

    public void playerViewStart() {
        if (rotateAnimator.isPaused()) {
            rotateAnimator.resume();
        } else if (!rotateAnimator.isRunning()) {
            rotateAnimator.start();
            binding.playOrPause.setBackgroundResource(R.drawable.detail_pause_circle);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay, R.anim.bottom_down);
    }

    private class MusicConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            mContorller = (MusicService.AudioBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    updateProgress();
                    break;
            }
        }
    };
}