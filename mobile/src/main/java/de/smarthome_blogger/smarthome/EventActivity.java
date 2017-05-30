package de.smarthome_blogger.smarthome;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.system.Dialogs;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.SaveData;

public class EventActivity extends AppCompatActivity {

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Intent abfangen
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(MainActivity.EXTRA_TYPE)){
            type = intent.getStringExtra(MainActivity.EXTRA_TYPE);
        }

        setTitle(type);

        //EventFragment einsetzen
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.EXTRA_TYPE, type);

        Fragment eventFragment = new EventFragment();
        eventFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.frame, eventFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onSupportNavigateUp(){
        super.onSupportNavigateUp();
        onBackPressed();
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_event_type:
                loadEventTypes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Lädt alle Ereignis-Typen vom Server
     */
    public void loadEventTypes(){
        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "geteventtypes");
        requestData.put("username", SaveData.getUsername(getApplicationContext()));
        requestData.put("password", SaveData.getPassword(getApplicationContext()));

        HTTPRequest.sendRequest(getApplicationContext(), requestData, SaveData.getServerIp(getApplicationContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                switch (result){
                    default:

                        try{
                            JSONObject types = new JSONObject(result);

                            JSONArray eventTypes = types.getJSONArray("types");

                            final String[] items = new String[eventTypes.length()];

                            for(int i = 0; i < eventTypes.length(); i++){
                                items[i] = eventTypes.getString(i);
                            }

                            Dialogs.singleChoiceDialog("Ereignistypen wählen", "Abbrechen", EventActivity.this, items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(EventActivity.this, EventActivity.class);
                                    intent.putExtra(MainActivity.EXTRA_TYPE, items[which]);
                                    startActivity(intent);
                                    dialog.dismiss();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Dialogs.fehlermeldung("Ereignistypen konnten nicht geladen werden", findViewById(R.id.frame));
                        }

                        break;
                }
            }

            @Override
            public void onError(String msg) {

            }
        });
    }
}
