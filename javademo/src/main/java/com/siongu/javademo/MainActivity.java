package com.siongu.javademo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.siongu.sucker.annotation.annotations.SuckClick;
import com.siongu.sucker.annotation.annotations.SuckView;
import com.siongu.sucker.api.Sucker;

public class MainActivity extends Activity {
    @SuckView(R.id.text)
    Button text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Sucker.suck(this);
    }

    @SuckClick(R.id.text)
    public void click(View v) {
        Toast.makeText(this, "suck click", Toast.LENGTH_SHORT).show();
    }
}
