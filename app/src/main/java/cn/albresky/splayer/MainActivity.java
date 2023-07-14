package cn.albresky.splayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import cn.albresky.splayer.UI.MusicActivity;
import cn.albresky.splayer.UI.VideoActivity;
import cn.albresky.splayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int PERMISSION_READ_EXTERNAL = 0;
    private final int PERMISSION_WRITE_EXTERNAL = 1;
    private ActivityMainBinding binding;
    private boolean[] permissionGranted = {false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Log.d(TAG, "onCreate() called");


        checkPermissons();

        /*
         * test zone
         * */
//        List<Video> mVideos = VideoScanner.getVideoData(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // test zone end
    }

    private void checkPermissons() {
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.equals(RESULT_OK)) {
                        startActivity(new Intent(getApplicationContext(), MusicActivity.class));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (!Environment.isExternalStorageManager()) {
                            Toast.makeText(this, "请求所有文件访问权限失败", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "已获得所有文件访问权限", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MusicActivity.class));
                        }
                    }
                });

        binding.btnMusic.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                if (permissionGranted[PERMISSION_READ_EXTERNAL] && permissionGranted[PERMISSION_WRITE_EXTERNAL]) {
                    startActivity(new Intent(getApplicationContext(), MusicActivity.class));
                } else if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL);
                } else if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL);
                }
            } else {
                if (!Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "请求所有文件访问权限", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    launcher.launch(intent);
                } else {
                    Toast.makeText(this, "已获得所有文件访问权限", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MusicActivity.class));
                }
            }
        });

        binding.btnVideo.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), VideoActivity.class));
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted[PERMISSION_READ_EXTERNAL] = true;
                } else {
                    Log.d(TAG, "Read Permission denied");
                    Toast.makeText(this, "读外置存储权限未授予", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSION_WRITE_EXTERNAL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted[PERMISSION_WRITE_EXTERNAL] = true;
                } else {
                    Log.d(TAG, "Write Permission denied");
                    Toast.makeText(this, "写外置存储权限未授予", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}