package de.smarthome_blogger.smarthome;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Intent-Keys
    final static String EXTRA_ROOMS = "EXTA_ROOMS";
    final static String EXTRA_TITLE = "EXTRA_TITLE";
    final static String EXTRA_LOCATION = "EXTRA_LOCATION";

    String roomData;

    //NavigationDrawer
    Menu drawerMenu;
    ArrayList<DrawerItem> drawerItemList = new ArrayList<>();
    DrawerLayout drawerLayout;
    FragmentTransaction fragmentTransaction;

    //Header
    ImageView headerImage;
    TextView headerName;
    View headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Smarthome");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        drawerMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);

        //Intent abfangen
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.containsKey(MainActivity.EXTRA_ROOMS)){
            roomData = extras.getString(MainActivity.EXTRA_ROOMS);
            createRooms(roomData);
        }
        else{
            loadRooms();
        }

        //Header laden
        headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        headerName = (TextView) headerLayout.findViewById(R.id.username);
        headerName.setText(SaveData.getUsername(getApplicationContext()));

        //Das erste DrawerItem auswählen
        try{
            onNavigationItemSelected(drawerMenu.getItem(0));
        }
        catch(NullPointerException npe){
            npe.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_logout){
            SaveData.deleteAllUserData(getApplicationContext());

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ArrayList menuItemArray = new ArrayList();

        for(int i = 0; i < drawerMenu.size(); i++){
            menuItemArray.add(drawerMenu.getItem(i));
            drawerMenu.getItem(i).setChecked(false);
        }

        item.setChecked(true);

        drawerLayout.closeDrawers();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();

        int position = menuItemArray.indexOf(item);

        Bundle bundle;

        try{
            bundle = new Bundle();
            bundle.putString(MainActivity.EXTRA_TITLE, drawerItemList.get(position).getName());
            bundle.putString(MainActivity.EXTRA_LOCATION, drawerItemList.get(position).getLocation());
            bundle.putString(MainActivity.EXTRA_ROOMS, roomData);
            setTitle(drawerItemList.get(position).getName());
            drawerItemList.get(position).getFragment().setArguments(bundle);
            fragmentTransaction.replace(R.id.frame, drawerItemList.get(position).getFragment());
            fragmentTransaction.commit();
        }
        catch(IllegalStateException ise){
            ise.printStackTrace();
        }

        return true;
    }

    /**
     * Lädt auf dem Server angelegte Räume und führt dann createRooms() aus
     */
    public void loadRooms(){
        Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getrooms");
        requestData.put("username", SaveData.getUsername(getApplicationContext()));
        requestData.put("password", SaveData.getPassword(getApplicationContext()));

        HTTPRequest.sendRequest(getApplicationContext(), requestData, SaveData.getServerIp(getApplicationContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                if(!result.equals("")){
                    roomData = result;
                    createRooms(result);
                }
                else fehlermeldung("Serverfehler");
            }

            @Override
            public void onError(String msg) {
                fehlermeldung(msg);
            }
        });
    }

    /**
     * Erstellt anhand des übergebenen Strings eine Reihe von Räumen und fügt sie dem NavigationDrawer hinzu
     * @param roomData
     */
    public void createRooms(String roomData){
        try{
            JSONObject jsonObj = null;
            try{
                jsonObj = new JSONObject(roomData);
            }
            catch(JSONException e){
                e.printStackTrace();
            }

            JSONArray rooms = jsonObj.getJSONArray("rooms");

            //Statichen Menüpunkt hinzufügen
            drawerItemList.add(new DrawerItem("Übersicht", Icons.getDrawerIcon("overview"), "overview", new OverviewFragment()));

            for(int i = 0; i < rooms.length(); i++){
                JSONObject o = rooms.getJSONObject(i);

                drawerItemList.add(new DrawerItem(o.getString("name"), Icons.getRoomIcon(o.getString("icon")), o.getString("location"), new RoomFragment()));
            }

            //Statischen Menüpunkt hinzufügen
            drawerItemList.add(new DrawerItem("Einstellungen", Icons.getDrawerIcon("settings"), "settings", new SettingsFragment()));
        }
        catch(Exception e){
            fehlermeldung("Fehler beim Laden der Räume");
        }

        for(int i = 0; i < drawerItemList.size(); i++){
            drawerMenu.add(drawerItemList.get(i).getName());
            drawerMenu.getItem(i).setIcon(drawerItemList.get(i).getIcon());
        }
    }

    public void fehlermeldung(String msg){
        Snackbar.make(findViewById(R.id.frame), msg, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Enthält die Informationen eines DrawerItems für den NavigationDrawer
     */
    class DrawerItem{
        String name, location;
        int icon_id;
        Fragment fragment;

        /**
         * Konstruktor für ein DrawerItem
         * @param menuName
         * @param menuIcon
         * @param location_name
         * @param frag
         */
        public DrawerItem(String menuName, int menuIcon, String location_name, Fragment frag){
            this.name = menuName;
            this.icon_id = menuIcon;
            this.location = location_name;
            this.fragment = frag;
        }

        /**
         * Gibt den Namen des Items zurück
         * @return
         */
        public String getName(){
            return name;
        }

        /**
         * Gibt die Location eines Items zurück
         * @return
         */
        public String getLocation(){
            return location;
        }

        /**
         * Gibt die Icon-ID des Items zurück
         * @return
         */
        public int getIcon(){
            return icon_id;
        }

        /**
         * Gibt das Fragment des Items zurück
         * @return
         */
        public Fragment getFragment(){
            return fragment;
        }

        /**
         * Gibt zurück, ob Fragment eine Instanz von RoomFragment ist
         * @return
         */
        public boolean isRoom(){
            return fragment instanceof RoomFragment;
        }
    }
}
