package org.blenoAndroid;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity implements  BleConnection.OnMessage{

    @Bind(R.id.response)
    TextView reponse;

    BleConnection bleConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        bleConnection = new BleConnection(getApplicationContext(),"5C:F3:70:60:88:73",this);

    }

    @OnClick(R.id.button)
    public void submit(View view) {
        bleConnection.request();
    }

    @OnClick(R.id.buttonConnect)
    public void connect(View view) {
        bleConnection.connect();
    }

    @Override
    public void onMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reponse.setText(message);
            }
        });
    }
}
