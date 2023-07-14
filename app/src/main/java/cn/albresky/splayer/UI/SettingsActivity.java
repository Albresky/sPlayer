package cn.albresky.splayer.UI;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import cn.albresky.splayer.databinding.ActivitySettingsBinding;


public class SettingsActivity extends AppCompatActivity {


    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}