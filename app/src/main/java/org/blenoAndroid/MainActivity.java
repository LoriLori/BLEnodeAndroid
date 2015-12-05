package org.blenoAndroid;


import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.joanzapata.android.BaseAdapterHelper;
import com.joanzapata.android.QuickAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements  BleConnection.OnMessage{

    @Bind(R.id.response)
    TextView reponse;

    @Bind(R.id.listView)
    ListView listView;

    BleConnection bleConnection;
    private QuickAdapter<JSONObject> adapter;

    @Bind(R.id.buttonConnect)
    public Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        bleConnection = new BleConnection(getApplicationContext(),"5C:F3:70:60:88:73",this);

        final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:SS");
        final SimpleDateFormat onlyTime = new SimpleDateFormat("kk:mm");
        if (adapter == null)
            adapter = new QuickAdapter<JSONObject>(this, R.layout.list_item) {
                @Override
                protected void convert(BaseAdapterHelper helper, JSONObject status) {
                    try {
                        String start_at = onlyTime.format(date.parse(status.getString("start_at")));

                        helper.setText(R.id.list_item, start_at+" "+status.getString("description"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
        };
        listView.setAdapter(adapter);

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
                adapter.clear();
                try {
                    JSONArray jsonObj = new JSONArray(message.toString());
                    for (int i = 0; i < jsonObj.length(); i++) {
                        adapter.add((JSONObject) jsonObj.get(i));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                reponse.setText(message);
            }
        });
    }

    @Override
    public void onConnectionChange(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonConnect.setText(message);
            }
        });
    }
}
