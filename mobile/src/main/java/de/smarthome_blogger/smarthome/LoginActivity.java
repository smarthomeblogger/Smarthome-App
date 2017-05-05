package de.smarthome_blogger.smarthome;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.SaveData;

import static de.smarthome_blogger.smarthome.system.Dialogs.fehlermeldung;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField, passwordField, serverIpField;
    private Button loginButton;
    private CheckBox saveLogin;
    private View loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Actionbar verstecken
        getSupportActionBar().hide();

        //Views deklarieren
        usernameField = (EditText) findViewById(R.id.username);
        passwordField = (EditText) findViewById(R.id.password);
        serverIpField = (EditText) findViewById(R.id.server_ip);

        loadingAnimation = findViewById(R.id.loading_animation);

        loginButton = (Button) findViewById(R.id.login_button);

        saveLogin = (CheckBox) findViewById(R.id.save_login);

        //Dem Button eine Aktion zuweisen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();
                String serverIp = serverIpField.getText().toString();

                //Eingaben auf Vollständigkeit prüfen
                if(username.equals("")){
                    usernameField.setError("Bitte gib deinen Nutzernamen ein");
                }
                else if(password.equals("")){
                    passwordField.setError("Bitte gib dein Passwort ein");
                }
                else if(serverIp.equals("")){
                    serverIpField.setError("Bitte gib die Adresse des Servers ein");
                }
                else{
                    login(username, password, serverIp, saveLogin.isChecked());
                }
            }
        });

        //Haken je nach gespeicherten Daten setzen oder nicht
        saveLogin.setChecked(SaveData.getSaveLoginData(getApplicationContext()));

        //Wenn Nutzerdaten vorhanden: automatisch einloggen
        if(SaveData.getSaveLoginData(getApplicationContext())){
            String username = SaveData.getUsername(getApplicationContext());
            String password = SaveData.getPassword(getApplicationContext());
            String serverIp = SaveData.getServerIp(getApplicationContext());

            if(username != null && password != null && serverIp != null){
                //Textfelder mit gespeicherten Daten füllen
                usernameField.setText(username);
                passwordField.setText(password);
                serverIpField.setText(serverIp);

                saveLogin.setChecked(true);

                login(username, password, serverIp, saveLogin.isChecked());
            }
        }
    }

    /**
     * Versucht den Nutzer einzuloggen
     * @param username
     * @param password
     * @param serverIp
     */
    public void login(final String username, final String password, final String serverIp, final boolean saveData){
        loadingAnimation.setVisibility(View.VISIBLE);

        Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getrooms");
        requestData.put("username", username);
        requestData.put("password", password);

        HTTPRequest.sendRequest(getApplicationContext(), requestData, serverIp, new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                loadingAnimation.setVisibility(View.GONE);

                if(result.equals("wrongdata")){
                    fehlermeldung("Anmeldung nicht möglich! Bitte überprüfe deine Eingaben", findViewById(R.id.frame));
                }
                else if(result.equals("unknownuser")){
                    usernameField.setError("Dieser Nutzer existiert nicht");
                }
                else{
                    SaveData.setLoginData(getApplicationContext(), username, password, saveData);
                    SaveData.setServerIp(getApplicationContext(), serverIp);

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ROOMS, result);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onError(String msg) {
                loadingAnimation.setVisibility(View.GONE);
                fehlermeldung(msg, findViewById(R.id.frame));
            }
        });
    }
}
