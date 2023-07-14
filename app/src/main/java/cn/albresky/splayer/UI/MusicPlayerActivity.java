package cn.albresky.splayer.UI;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Service.MusicService;
import cn.albresky.splayer.Utils.AnimationUtils;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.DatetimeUtils;
import cn.albresky.splayer.Utils.MusicScanner;
import cn.albresky.splayer.databinding.ActivityMusicPlayerBinding;

public class MusicPlayerActivity extends AppCompatActivity {


    private final String TAG = "MusicPlayerActivity";
    private final int UPDATE_PROGRESS = 1;
    ActivityMusicPlayerBinding binding;
    private MusicService.AudioBinder mContorller;
    private MusicConnection mConnection;
    private Song song;

    private long duration;

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
        binding.ivMusicCover.setImageBitmap(Converter.createBitmapWithScale(Converter.createBitmapWithNoScale(this, R.mipmap.record), 512, 512, false));

        song = (Song) getIntent().getSerializableExtra("songInfo");

        startMusicService();


        // ToDo
        binding.ivMusicCover.setImageBitmap(MusicScanner.getAlbumPicture(this, song.getPath(), 2));
        binding.songName.setText(song.getSong());
        binding.singerName.setText(song.getSinger());
        binding.tvTotal.setText(DatetimeUtils.formatTime(song.getDuration()));


        // Music Controller
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
        });
        thread.start();

        // Cover Animation
        ObjectAnimator rotateAnimator = AnimationUtils.getRotateAnimation(binding.ivMusicCover);
        rotateAnimator.start();

        // seekbar
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度条改变
                if (fromUser) {
                    mContorller.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始触摸进度条
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止触摸进度条
            }
        });
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        mConnection = new MusicConnection();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void updateProgress() {
        int currenPostion = mContorller.getCurrentPosition();
        binding.seekBar.setProgress((int) (currenPostion / song.getDuration()) * 1000);
        binding.tvProgress.setText(DatetimeUtils.formatTime(currenPostion));

        Log.d(TAG, "updateProgress: " + currenPostion + " / " + song.getDuration());
        //使用Handler每500毫秒更新一次进度条
        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 500);
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