package cn.albresky.splayer.UI;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import cn.albresky.splayer.Bean.Video;
import cn.albresky.splayer.Utils.Converter;
import cn.albresky.splayer.Utils.DatetimeUtils;
import cn.albresky.splayer.databinding.ActivityVideoPlayerBinding;

public class VideoPlayerActivity extends AppCompatActivity {

    private final String TAG = "VideoPlayerActivity";

    private ActivityVideoPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

            binding.lvRoot.setPadding(0, 0, 0, top);
            return insets;
        });
        initPlayer();
    }

    private void initPlayer() {
        Intent intent = getIntent();
        Video video = (Video) intent.getSerializableExtra("videoInfo");

        if (video == null) {
            return;
        }

        // set video info
        binding.videoPlayer.setVideoPath(video.getPath());
        binding.videoName.setText(video.getName());
        binding.videoPath.setText(video.getPath());
        binding.videoSize.setText(Converter.sizeConvert(video.getSize()));
        binding.videoDuration.setText(DatetimeUtils.formatTime(video.getDuration()));
        binding.videoResolution.setText(Converter.resolutionConvert(video.getWidth(), video.getHeight()));
        binding.videoType.setText(video.getType());
        binding.videoMDate.setText(DatetimeUtils.formatTime(Long.parseLong(video.getDate())));

//         add MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(binding.videoPlayer);
        binding.videoPlayer.setMediaController(mediaController);

        // autoplay
        binding.videoPlayer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed: ");
        finish();
    }
}