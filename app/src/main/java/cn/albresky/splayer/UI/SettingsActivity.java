package cn.albresky.splayer.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import cn.albresky.splayer.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "loadSettings";
    private ActivitySettingsBinding binding;
    private boolean enableDeepScan = false;
    private int scanDepth = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }


    private void initView() {
        Log.d(TAG, "initView: ");

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadSettings();

        binding.deepScan.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "onCheckedChanged: ");
            binding.depthSlider.setEnabled(isChecked);
            scanDepth = isChecked ? Math.round(binding.depthSlider.getValue()) : 4;
            saveSettings();
        });

        binding.txtCache.setText(getTotalCacheSize());

        binding.btnClearCache.setOnClickListener(v -> {
            Log.d(TAG, "initView: ");
            clearAllCache();
        });

        binding.btnRestartApp.setOnClickListener(v -> {
            Log.d(TAG, "btn exit clicked: ");
            System.exit(0);
        });

        binding.toolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "onNavigationClick: ");
            finish();
        });

        binding.btnAbout.setOnClickListener(v -> {
            Log.d(TAG, "btnAbout clicked: ");
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
    }


    private void loadSettings() {
        Log.d(TAG, "loadSettings: ");
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        enableDeepScan = sp.getBoolean("enableDeepScan", false);
        scanDepth = sp.getInt("scanDepth", 4);
        if (enableDeepScan) {
            binding.depthSlider.setEnabled(true);
            binding.depthSlider.setValue(scanDepth);
        }
        binding.deepScan.setChecked(sp.getBoolean("enableDeepScan", false));
    }


    private void saveSettings() {
        Log.d(TAG, "saveSettings: ");
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("enableDeepScan", binding.deepScan.isChecked());
        editor.putInt("scanDepth", Math.round(binding.depthSlider.getValue()));
        editor.apply();
    }


    public String getTotalCacheSize() {
        long cacheSize;
        try {
            cacheSize = getFolderSize(this.getCacheDir());
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                cacheSize += getFolderSize(this.getExternalCacheDir());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0.0MB";
        }
        return getFormatSize(cacheSize);
    }


    public String getFormatSize(long size) {
        long kb = size / 1024;
        int m = (int) (kb / 1024);
        int kbs = (int) (kb % 1024);
        return m + "." + kbs + "MB";
    }


    public long getFolderSize(File file) throws Exception {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


    public void clearAllCache() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (deleteDir(this.getExternalCacheDir())) {
                Log.d(TAG, "clearAllCache:  ");
                Toast.makeText(this, "缓存清除成功", Toast.LENGTH_SHORT).show();
                binding.txtCache.setText("0.0MB");
            }
        }
    }


    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }


    @Override
    public void finish() {
        Log.d(TAG, "finish: ");
        super.finish();
        saveSettings();
    }
}