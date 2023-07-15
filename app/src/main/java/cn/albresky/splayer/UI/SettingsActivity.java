package cn.albresky.splayer.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import cn.albresky.splayer.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "loadSettings";
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadSettings();
    }

    private void initView() {
        Log.d(TAG, "initView: ");
        binding.deepScan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "onCheckedChanged: ");
            saveSettings();
        });
    }

    private void loadSettings() {
        Log.d(TAG, "loadSettings: ");
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        binding.deepScan.setChecked(sp.getBoolean("enableDeepScan", false));
    }

    private void saveSettings() {
        Log.d(TAG, "saveSettings: ");
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("enableDeepScan", binding.deepScan.isChecked());
        editor.apply();
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish: ");
        super.finish();
        saveSettings();
    }
}