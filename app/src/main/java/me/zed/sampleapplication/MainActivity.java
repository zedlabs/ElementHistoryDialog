package me.zed.sampleapplication;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import me.zed.elementhistorydialog.ElementHistoryDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_show).setOnClickListener(v -> {

            ElementHistoryDialog ehd = ElementHistoryDialog.create(1, "node");
            ehd.show(getSupportFragmentManager(), "sample-application");
        });

    }
}