package com.uclacsm117.mysimlpepoll;

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

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.*;

public class CreatePollActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private class MapNode{
        public MapNode(LinearLayout l, EditText t){
            this.container = l;
            this.text = t;
        }
        LinearLayout container;
        EditText text;
    }

    private GoogleApiClient client;

    private HashMap<Button,MapNode> button_container_map = new HashMap<Button,MapNode>();

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

    }
}
