package cn.albresky.splayer.UI;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.albresky.splayer.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    private final String TAG = "AboutActivity";
    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        binding.toolbarAbout.setNavigationOnClickListener(v -> {
            Log.d(TAG, "onNavigationClick: ");
            finish();
        });
    }
}