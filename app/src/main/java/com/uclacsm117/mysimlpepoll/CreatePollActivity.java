package com.uclacsm117.mysimlpepoll;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.*;

public class CreatePollActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private static final int REQUEST_CONNECT_DEVICE = 1;




    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 3;

    private class MapNode{
        public MapNode(LinearLayout l, EditText t){
            this.container = l;
            this.text = t;
        }
        LinearLayout container;
        EditText text;
    }

    private ArrayList<BluetoothDevice> m_device_list = new ArrayList<BluetoothDevice>();

    private GoogleApiClient client;

    private HashMap<Button,MapNode> button_container_map = new HashMap<Button,MapNode>();

    private ArrayList<String> selected_device_mac_addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void AddPollOption(View view) {
        Button delete_btn = new Button(this);
        EditText new_option = new EditText(this);
        new_option.setHint("new option");

        LinearLayout new_option_container = new LinearLayout(this);
        new_option_container.setOrientation(LinearLayout.HORIZONTAL);
        new_option_container.setHorizontalGravity(Gravity.CENTER);
        new_option_container.addView(new_option);
        new_option_container.addView(delete_btn);

        delete_btn.setText("Delete");

        delete_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button b = (Button) v;
                        DeleteOption(b);
                    }
                }

        );

        LinearLayout create_option_layout = (LinearLayout) findViewById(R.id.options_container_layout);
        create_option_layout.addView(new_option_container);

        MapNode new_node = new MapNode(new_option_container,new_option);

        this.button_container_map.put(delete_btn, new_node);
    }

    private void DeleteOption(Button b){

        LinearLayout create_option_layout = (LinearLayout) findViewById(R.id.options_container_layout);
        MapNode to_delete= this.button_container_map.get(b);
        LinearLayout option_to_delete = to_delete.container;
        create_option_layout.removeView(option_to_delete);
        this.button_container_map.remove(b);
    }


    public void SubmitNewPoll(View view) {
        EditText question_text = (EditText) findViewById(R.id.question_text);
        String q_text = question_text.getText().toString();
        String body = "\007";
        for (Map.Entry<Button, MapNode> entry : this.button_container_map.entrySet()) {

            body = body + entry.getValue().text.getText().toString() + "\n" + "\007";

        }

        LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);
        LinearLayout create_poll_layout = (LinearLayout) findViewById(R.id.create_poll_layout);

        main_create_poll.removeView(create_poll_layout);

        EditText display_msg = new EditText(this);
        display_msg.setText(question_text.getText().toString() + "\n" + body);
        display_msg.setKeyListener(null);

        main_create_poll.addView(display_msg);

        setupBluetooth();



    }



    public void setupBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {

            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        else{
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else{
                Intent serverIntent = new Intent(getApplicationContext(), GetDevice.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:

                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    this.selected_device_mac_addresses = data.getExtras().getStringArrayList(GetDevice.SELECTED_MAC_ADDRESSES);
                    EditText display_msg = new EditText(this);
                    String addresses_string = "\n";
                    for(String a: this.selected_device_mac_addresses){
                        addresses_string = addresses_string + a + "\n";
                    }
                    display_msg.setText(addresses_string);
                    display_msg.setKeyListener(null);
                    LinearLayout main_create_poll = (LinearLayout) findViewById(R.id.main_create_poll);

                    main_create_poll.addView(display_msg);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Intent serverIntent = new Intent(getApplicationContext(), GetDevice.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

                }
                else {
                    // User did not enable Bluetooth or an error occurred

                    Toast.makeText(getApplicationContext(), "error enabling bluetooth",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }

    }



}
