package cn.albresky.splayer.UI;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
//            int botm = 0, top = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
//                botm = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
//
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                top = insets.getSystemWindowInsetTop();
//                botm = insets.getSystemWindowInsetBottom();
//            }
//            Log.d(TAG, "onApplyWindowInsets: top = " + top + ", botm = " + botm);
//
//            binding.layBody.setPadding(0, top, 0, top * 2);
//            binding.layBottom.setPadding(0, botm / 10, 0, botm);
//            return insets;
//        });


        binding.btnScanMusic.setOnClickListener(v -> {
            Log.d(TAG, "initView: btnScanMusic clicked");
            binding.btnScanMusic.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);

            // register service
            startMusicService();

            getMusicList();
        });
        binding.btnPlay.setOnClickListener(v -> {
            if (mContorller != null) {
                if (mContorller.isPlaying()) {
                    playerPause();
                } else {
                    mContorller.play();
                    binding.playerSongName.setSelected(true);
                    binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));
                    rotateAnimator.resume();
                }
            } else {
                playerStart(0);
            }
        });
        binding.layRefresh.setOnRefreshListener(
                () -> {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                    getMusicList();
                }
        );
        binding.layRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        rvMusic = binding.rvMusic;
        layScanMusic = binding.layScanMusic;

        binding.playerSongCover.setImageBitmap(Converter.createBitmapWithScale(Converter.createBitmapWithNoScale(this, R.mipmap.record), 120, 120, false));
        binding.playerSongCover.setOnClickListener(v -> {
            startMusicPlayerActivity(mIndex);
        });
        binding.playerSongName.setOnClickListener(v -> {
            startMusicPlayerActivity(mIndex);
        });

        // initialize cover animation
        rotateAnimator = AnimationUtils.getRotateAnimation(binding.playerSongCover);
    }

    private void getMusicList() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Background work
            mList.clear();
            mList = MusicScanner.getMusicData(this);
            handler.post(() -> {
                // UI Thread work
                if (mList.size() > 0) {
                    binding.playerSongCover.setClickable(true);
                    binding.playerSongName.setClickable(true);
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

    private void playerStart(int position) {
        try {
//            mContorller.release();
            mContorller.setSongPath(mList.get(position).getPath());
            mContorller.play();

            binding.playerSongName.setText(String.format("%s - %s", mList.get(position).getSong(), mList.get(position).getSinger()));
            binding.playerSongName.setSelected(true);
            binding.playerSongCover.setImageBitmap(MusicScanner.getAlbumPicture(this, MusicScanner.isAlbumContainCover(mList.get(position).getPath()), 1));
            binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));

            rotateAnimator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        if (mContorller != null) {
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
        intent.putExtra("songInfo", mList.get(position));
        startActivity(intent);
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