package naxess.practiceandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ExtraDetails extends AppCompatActivity
{
    TextView sampleText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "", Snackbar.LENGTH_LONG)
                        .setAction("", null).show();
            }
        });
        Button back = (Button)findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
        Intent intent = getIntent();
        String doorbell = intent.getStringExtra("doorbell");
        if(doorbell == "nobodyhome")
        {
            sampleText = (TextView)findViewById(R.id.details);
            sampleText.setText("Please enter a valid zip code.");
        }
        else
        {
            String speed = intent.getStringExtra("data");
            sampleText = (TextView)findViewById(R.id.details);
            sampleText.setText(speed);
        }
    }
}
