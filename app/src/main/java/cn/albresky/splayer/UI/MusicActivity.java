package cn.albresky.splayer.UI;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.albresky.splayer.Adapter.MusicListAdapter;
import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Service.MusicService;
import cn.albresky.splayer.Utils.AnimationUtils;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.MusicScanner;
import cn.albresky.splayer.Utils.SuperScanner;
import cn.albresky.splayer.databinding.ActivityMusiclistBinding;

public class MusicActivity extends AppCompatActivity implements MusicListAdapter.OnItemClickListener {

    private final String TAG = "MusicActivity";
    private MusicService.AudioBinder mContorller;
    private MusicConnection mConnection;
    private RecyclerView rvMusic;
    private LinearLayout layScanMusic;
    private MusicListAdapter mAdapter;
    private List<Song> mList = new ArrayList<>();
    private int mIndex;
    private ObjectAnimator rotateAnimator;
    private ActivityMusiclistBinding binding;

    private boolean enableDeepScan;

    private int scanDepth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusiclistBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        initView();
    }

    private void initView() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadSettings();

        binding.btnScanMusic.setOnClickListener(v -> {
            Log.d(TAG, "initView: btnScanMusic clicked");
            binding.btnScanMusic.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);

            // register service
            startMusicService();
            String tText = enableDeepScan ? "深度扫描音乐中[level=" + scanDepth + "]" : "扫描音乐中";
            Toast.makeText(this, tText, Toast.LENGTH_SHORT).show();
            getMusicList();
        });

        binding.btnPlay.setEnabled(false);
        binding.btnPlay.setOnClickListener(v -> {
            if (mContorller != null) {
//                if (!mContorller.isPrepared()) {
//                    Log.d(TAG, "initView:[btn_play clicked] [isPrepared=false] => playerStart(0)");
//                    playerStart(0);
//                } else
                if (mContorller.isPlaying()) {
                    Log.d(TAG, "initView:[btn_play clicked] [isPlaying=true] => playerPause()");
                    playerPause();
                } else {
                    Log.d(TAG, "initView:[btn_play clicked] [isPlaying=false] => playerResume()");
                    mContorller.play();
                    binding.playerSongName.setSelected(true);
                    binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));
                    rotateAnimator.resume();
                }
            } else {
                Log.d(TAG, "initView:[btn_play clicked] [mContorller=null] => playerStart(0)");
                playerStart(0);
            }
        });

        binding.layRefresh.setOnRefreshListener(
                () -> {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                    getMusicList();
                }
        );

        binding.toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "onNavigationClick: ");
            finish();
        });

        binding.layRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        rvMusic = binding.rvMusic;
        layScanMusic = binding.layScanMusic;

        // initialize cover animation
        rotateAnimator = AnimationUtils.getRotateAnimation(binding.playerSongCover);

        binding.playerSongCover.setImageBitmap(Converter.createBitmapWithScale(Converter.createBitmapWithNoScale(this, R.drawable.record), 120, 120, false));

        binding.playerSongCover.setEnabled(false);
        binding.playerSongCover.setOnClickListener(v -> {
            Log.d(TAG, "initView: playerSongCover clicked");
            startMusicPlayerActivity(mIndex);
        });
        binding.playerSongName.setEnabled(false);
        binding.playerSongName.setOnClickListener(v -> {
            Log.d(TAG, "initView: playerSongName clicked");
            startMusicPlayerActivity(mIndex);
        });


    }

    private void getMusicList() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "getMusicList: enableDeepScan = " + enableDeepScan);

        executor.execute(() -> {
            // Background work
            mList.clear();
            if (enableDeepScan) {
                Log.d(TAG, "getAudioData: enableDeepScan|ScanType:{flac,mp3,aac,wav,ogg,m4a,oga,ac3}|ScanDepth:" + scanDepth);
                SuperScanner sScanner = new SuperScanner();
                sScanner.setScanType(new String[]{"flac", "mp3", "aac", "wav", "ogg", "m4a", "oga", "ac3"});
                sScanner.setScanDepth(scanDepth);
                sScanner.startScan();
                mList = sScanner.getAudioData();
                if (mList != null && mList.size() > 0) {
                    Log.d(TAG, "onCreate: mList.size() = " + mList.size());
                } else {
                    Log.d(TAG, "onCreate: mList is null");
                }
            } else {
                Log.d(TAG, "getAudioData: disableDeepScan");
                mList = MusicScanner.getMusicData(this);

            }

            handler.post(() -> {
                // UI Thread work
                if (mList.size() > 0) {
                    if (mAdapter == null) {
                        mAdapter = new MusicListAdapter(mList, this);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rvMusic.setLayoutManager(layoutManager);
                        rvMusic.setAdapter(mAdapter);
                        rvMusic.setItemViewCacheSize(6);
                        layScanMusic.setVisibility(View.GONE);
                        binding.layRefresh.setVisibility(View.VISIBLE);
                    } else {
                        mAdapter.updateData(mList);
                    }
                } else {
                    Toast.makeText(this, "未找到音频文件", Toast.LENGTH_SHORT).show();
                }
                binding.layRefresh.setRefreshing(false);
                binding.progressBar.setVisibility(View.GONE);
                binding.btnScanMusic.setText(getResources().getText(R.string.txt_scan_music));
            });
        });

    }

    private void updatePlayerUI(int position) {
        binding.playerSongName.setText(String.format("%s - %s", mList.get(position).getSong(), mList.get(position).getSinger()));
        binding.playerSongName.setSelected(true);
        binding.playerSongCover.setImageBitmap(MusicScanner.getAlbumPicture(this, MusicScanner.isAlbumContainCover(mList.get(position).getPath()), 1));
        binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));
    }

    void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        enableDeepScan = sharedPreferences.getBoolean("enableDeepScan", false);
        scanDepth = sharedPreferences.getInt("scanDepth", 4);
    }

    private boolean playerStart(int position) {
        try {
            mContorller.updateSongList(mList);
            mContorller.prepare(position);
            mContorller.play();
            updatePlayerUI(position);
            rotateAnimator.start();
            if (mContorller.isPlaying()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void playerPause() {
        if (mContorller != null) {
            mContorller.pause();
            binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_play));
            binding.playerSongName.setSelected(false);
            rotateAnimator.pause();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: position = " + position);
        playerStart(position);
        mIndex = position;
        binding.playerSongCover.setEnabled(true);
        binding.playerSongName.setEnabled(true);
        binding.btnPlay.setEnabled(true);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        if (mContorller != null) {
            int nowSongIndex = mContorller.getSongIndex();
            if (nowSongIndex != mIndex) {
                mIndex = nowSongIndex;
                updatePlayerUI(mIndex);
            }

            if (mContorller.isPlaying()) {
                binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));
                rotateAnimator.resume();
            } else {
                binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_play));
                rotateAnimator.pause();
            }
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mContorller != null)
            mContorller.stop();
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    private void startMusicPlayerActivity(int position) {
        Intent intent = new Intent(this, MusicPlayerActivity.class);
        Gson gson = new Gson();
        String jList = gson.toJson(mList);
        intent.putExtra("songList", jList);
        intent.putExtra("songIndex", position);
        startActivity(intent);
        overridePendingTransition(R.anim.bottom_up, R.anim.stay);
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        mConnection = new MusicConnection();
        bindService(intent, mConnection, BIND_AUTO_CREATE);
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


}