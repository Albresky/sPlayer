package com.albresky.splayer.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.albresky.splayer.R;
import com.albresky.splayer.databinding.ActivityMainBinding;
import com.albresky.splayer.databinding.ActivityMusicBinding;


public class MainActivity extends AppCompatActivity {

    private String LOG_TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Log.d(LOG_TAG, "onCreate() called");

        binding.btnMusic.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MusicActivity.class));
        });
    }
}