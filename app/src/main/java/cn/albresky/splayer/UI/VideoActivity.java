package cn.albresky.splayer.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.albresky.splayer.Adapter.VideoAdapter;
import cn.albresky.splayer.Bean.Video;
import cn.albresky.splayer.R;
import cn.albresky.splayer.Utils.SuperScanner;
import cn.albresky.splayer.Utils.VideoScanner;
import cn.albresky.splayer.databinding.ActivityVideoBinding;

public class VideoActivity extends AppCompatActivity implements VideoAdapter.OnItemClickListener {

    private static List<Video> mList = new ArrayList<>();
    private final String TAG = "VideoActivity";
    private ActivityVideoBinding binding;
    private boolean enableDeepScan = false;
    private VideoAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        enableDeepScan = sharedPreferences.getBoolean("enableDeepScan", false);
    }

    private void initView() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadSettings();
        binding.btnScanVideo.setOnClickListener(v -> {
            Toast.makeText(this, "开始扫描视频文件", Toast.LENGTH_SHORT).show();
            binding.btnScanVideo.setText("");
            binding.progressBar.setVisibility(View.VISIBLE);
            getVideoList(true);
        });

        binding.layRefresh.setOnRefreshListener(
                () -> {
                    Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // your code to update the UI thread here
                            getVideoList(false);

                        }
                    });
                }
        );
        binding.layRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        binding.toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "onNavigationClick: ");
            finish();
        });
    }

    private void getVideoList(boolean enableCache) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "getVideoList: enableDeepScan:" + enableDeepScan);
        executor.execute(() -> {
            //Background work
            mList.clear();
            // try to get video list from cache
            if (!enableCache || !loadCache()) {
//            if (true) {
                if (enableDeepScan) {
                    Log.d(TAG, "getVideoList: enableDeepScan");
                    SuperScanner sScanner = new SuperScanner();
                    sScanner.setScanType(new String[]{"mp4", "mkv", "webm"});
                    sScanner.startScan();
                    mList = sScanner.getVideoData();
                    if (mList != null && mList.size() > 0) {
                        Log.d(TAG, "onCreate: mList.size() = " + mList.size());
                    } else {
                        Log.d(TAG, "onCreate: mList is null");
                    }
                } else {
                    Log.d(TAG, "getVideoList: disableDeepScan");
                    mList = VideoScanner.getVideoData(this);
                }
            }

            handler.post(() -> {
                //UI Thread work
                if (mList.size() > 0) {
                    if (mAdapter == null) {
                        Log.d(TAG, "getVideoList: mAdapter == null");
                        mAdapter = new VideoAdapter(mList, this);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        binding.rvVideo.setLayoutManager(layoutManager);
                        binding.rvVideo.setAdapter(mAdapter);
                        binding.rvVideo.setItemViewCacheSize(20);
                        binding.layScanVideo.setVisibility(View.GONE);
                        binding.layRefresh.setVisibility(View.VISIBLE);
                    } else {
                        mAdapter.updateData(mList);
                    }
                    if (!writeCache()) {
                        Log.d(TAG, "getVideoList: write cache failed");
                    }
                } else {
                    Toast.makeText(this, "未找到视频文件", Toast.LENGTH_SHORT).show();
                }
                binding.btnScanVideo.setText(getResources().getText(R.string.txt_scan_video));
                binding.progressBar.setVisibility(View.GONE);
                binding.layRefresh.setRefreshing(false);
            });
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        startVideoPlayerActivity(position);
    }

    private void startVideoPlayerActivity(int videoIndex) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra("videoInfo", mList.get(videoIndex));
        startActivity(intent);
    }

    private boolean loadCache() {
        Log.d(TAG, "loadCache: load cache from shared preference");
        SharedPreferences sp = getSharedPreferences("videoListCache", MODE_PRIVATE);
        String json = sp.getString("videoList", "");
        if (json.equals("")) {
            return false;
        } else {
            Gson gson = new Gson();
            mList = gson.fromJson(json, new TypeToken<List<Video>>() {
            }.getType());
            return true;
        }
    }

    private boolean writeCache() {
        Log.d(TAG, "writeCache: write cache to shared preference");
        SharedPreferences sp = getSharedPreferences("videoListCache", MODE_PRIVATE);
        if (sp.contains("videoList")) {
            sp.edit().remove("videoList").apply();
        }
        Gson gson = new Gson();
        String json = gson.toJson(mList);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("videoList", json);
        return editor.commit();
    }

}
