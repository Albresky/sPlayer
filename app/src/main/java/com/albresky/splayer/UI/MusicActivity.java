package com.albresky.splayer.UI;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.albresky.splayer.Adapter.MusicListAdapter;
import com.albresky.splayer.Bean.Song;
import com.albresky.splayer.R;
import com.albresky.splayer.Utils.MusicScanner;
import com.albresky.splayer.databinding.ActivityMusiclistBinding;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity implements MusicListAdapter.OnItemClickListener {

    private final String TAG = "MusicActivity";

    private RecyclerView rvMusic;

    private LinearLayout layScanMusic;

    private MusicListAdapter mAdapter;

    private List<Song> mList = new ArrayList<Song>();


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
                    binding.btnPlay.setIcon(getDrawable(R.drawable.baseline_pause));
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
    }

    private void getMusicList() {
        mList.clear();
        mList = MusicScanner.getMusicData(this);

        if (mList != null && mList.size() > 0) {
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

            binding.playerSongName.setText(mList.get(position).getSong() + " - " + mList.get(position).getSinger());
            binding.playerSongName.setSelected(true);
            binding.playerSongCover.setImageBitmap(MusicScanner.getAlbumPicture(this, mList.get(position).getPath(), 1));
            binding.btnPlay.setIcon(getDrawable(R.drawable.baseline_pause));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playerPause() {
        if (mPlayer != null) {
            mPlayer.pause();
            binding.btnPlay.setIcon(getDrawable(R.drawable.baseline_play));
            binding.playerSongName.setSelected(false);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick: position = " + position);
        playerStart(position);
    }
}