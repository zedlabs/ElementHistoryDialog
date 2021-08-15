package me.zed.sampleapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import me.zed.elementhistorydialog.ElementHistoryDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_show).setOnClickListener(v -> {

            EditText edt1 = findViewById(R.id.id_edt);
            EditText edt2 = findViewById(R.id.type_edt);

            if(!edt1.getText().toString().isEmpty() && !edt2.getText().toString().isEmpty()) {
                ElementHistoryDialog ehd = ElementHistoryDialog.create(Integer.parseInt(edt1.getText().toString()), edt2.getText().toString());
                ehd.show(getSupportFragmentManager(), "sample-application");
            }

        });

    }
}