package com.albresky.splayer.UI;

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
import com.albresky.splayer.Utils.MusicScanner;
import com.albresky.splayer.databinding.ActivityMusiclistBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private final String TAG = "MusicActivity";

    private RecyclerView rvMusic;

    private LinearLayout layScanMusic;

    private MusicListAdapter mAdapter;

    private List<Song> mList = new ArrayList<Song>();

    private MaterialButton btnLocationPlayMusic;

    private ShapeableImageView ivLogo;

    private MaterialTextView tvSongName;

    private MaterialButton btnPlay;

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
        binding.btnScanMusic.setOnClickListener(v -> {
            Log.d(TAG, "initView: btnScanMusic clicked");
            getMusicList();
        });
        binding.layRefresh.setOnRefreshListener(
                () -> {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                    getMusicList();
                }
        );
        rvMusic = binding.rvMusic;
        layScanMusic = binding.layScanMusic;
        btnLocationPlayMusic = binding.btnPlay;
        ivLogo = binding.ivLogo;
        tvSongName = binding.tvSongName;
        btnPlay = binding.btnPlay;
    }

    private void getMusicList() {
        mList.clear();
        Log.d(TAG, "扫描本地文件夹");
        mList = MusicScanner.getMusicData(this);

        if (mList != null && mList.size() > 0) {
            mAdapter = new MusicListAdapter(mList, getApplicationContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvMusic.setLayoutManager(layoutManager);
            rvMusic.setAdapter(mAdapter);
            layScanMusic.setVisibility(View.GONE);
            binding.layRefresh.setVisibility(View.VISIBLE);
            binding.layRefresh.setRefreshing(false);
        } else {
            Toast.makeText(this, "no music found", Toast.LENGTH_SHORT).show();
        }
    }


}