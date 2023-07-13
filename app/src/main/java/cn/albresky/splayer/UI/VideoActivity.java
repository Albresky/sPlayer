package cn.albresky.splayer.UI;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cn.albresky.splayer.Adapter.VideoAdapter;
import cn.albresky.splayer.Bean.Video;
import cn.albresky.splayer.Utils.VideoScanner;
import cn.albresky.splayer.databinding.ActivityVideoBinding;

public class VideoActivity extends AppCompatActivity implements VideoAdapter.OnItemClickListener {

    private ActivityVideoBinding binding;

    private List<Video> mList = new ArrayList<>();

    private VideoAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        binding.btnScanVideo.setOnClickListener(v -> {
            getVideoList();
        });
    }

    private void getVideoList() {
        mList.clear();
        mList = VideoScanner.getVideoData(this);
        if (mList.size() > 0) {
            if (mAdapter == null) {
                mAdapter = new VideoAdapter(mList, this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                binding.rvVideo.setLayoutManager(layoutManager);
                binding.rvVideo.setAdapter(mAdapter);
                binding.rvVideo.setItemViewCacheSize(20);
                binding.layScanVideo.setVisibility(View.GONE);
                binding.layRefresh.setVisibility(View.VISIBLE);
                binding.layRefresh.setRefreshing(false);
            } else {
                mAdapter.updateData(mList);
                binding.layRefresh.setRefreshing(false);
            }
        } else {
            Toast.makeText(this, "未找到视频文件", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
//        Toast.makeText(this, "点击了" + position, Toast.LENGTH_SHORT).show();
    }
}
