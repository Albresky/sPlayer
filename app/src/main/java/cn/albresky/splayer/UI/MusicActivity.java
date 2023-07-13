package cn.albresky.splayer.UI;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Adapter.MusicListAdapter;
import cn.albresky.splayer.Bean.Song;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.MusicScanner;
import cn.albresky.splayer.databinding.ActivityMusiclistBinding;

public class MusicActivity extends AppCompatActivity implements MusicListAdapter.OnItemClickListener {

    private final String TAG = "MusicActivity";

    private RecyclerView rvMusic;

    private LinearLayout layScanMusic;

    private MusicListAdapter mAdapter;

    private List<Song> mList = new ArrayList<>();

    private ObjectAnimator rotateAnimator;

    private ActivityMusiclistBinding binding;

    private static MediaPlayer mPlayer;

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
            getMusicList();
        });
        binding.btnPlay.setOnClickListener(v -> {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    playerPause();
                } else {
                    mPlayer.start();
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

        BitmapFactory.Options mOptions = new BitmapFactory.Options();
        mOptions.inScaled = false;
        Bitmap defaultCover = BitmapFactory.decodeResource(this.getResources(), R.mipmap.record, mOptions);
        binding.playerSongCover.setImageBitmap(Converter.createBitmapWithScale(defaultCover, false));

        // initialize cover animation
        rotateAnimator = ObjectAnimator.ofFloat(binding.playerSongCover, "rotation", 0f, 360f);//添加旋转动画，旋转中心默认为控件中点
        rotateAnimator.setDuration(12000);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    private void getMusicList() {
        mList.clear();
        mList = MusicScanner.getMusicData(this);

        if (mList.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new MusicListAdapter(mList, this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                rvMusic.setLayoutManager(layoutManager);
                rvMusic.setAdapter(mAdapter);
                layScanMusic.setVisibility(View.GONE);
                binding.layRefresh.setVisibility(View.VISIBLE);
                binding.layRefresh.setRefreshing(false);
            } else {
                mAdapter.updateData(mList);
                binding.layRefresh.setRefreshing(false);
            }

        } else {
            Toast.makeText(this, "no music found", Toast.LENGTH_SHORT).show();
        }
    }

    private void playerStart(int position) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mList.get(position).getPath());
            mPlayer.prepare();
            mPlayer.start();

            binding.playerSongName.setText(String.format("%s - %s", mList.get(position).getSong(), mList.get(position).getSinger()));
            binding.playerSongName.setSelected(true);
            binding.playerSongCover.setImageBitmap(MusicScanner.getAlbumPicture(this, mList.get(position).getPath(), 1));
            binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_pause));

            rotateAnimator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playerPause() {
        if (mPlayer != null) {
            mPlayer.pause();
            binding.btnPlay.setIcon(AppCompatResources.getDrawable(this, R.drawable.baseline_play));
            binding.playerSongName.setSelected(false);
            rotateAnimator.pause();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: position = " + position);
        playerStart(position);
    }
}