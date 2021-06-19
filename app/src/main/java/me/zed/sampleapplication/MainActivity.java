package me.zed.sampleapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import me.zed.elementhistorydialog.ElementHistoryDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ElementHistoryDialog ehd = ElementHistoryDialog.create(129, "node");
        ehd.show(getSupportFragmentManager(), "sample-application");
    }
}