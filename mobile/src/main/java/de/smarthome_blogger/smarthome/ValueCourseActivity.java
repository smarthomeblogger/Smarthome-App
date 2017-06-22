package de.smarthome_blogger.smarthome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.system.Dialogs;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;

import static android.view.View.GONE;

public class ValueCourseActivity extends AppCompatActivity {

    private String location, devicetype, id, datum, einheit;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_course);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Intent abfangen
        Intent intent = getIntent();
        if(intent != null){
            location = intent.getStringExtra(MainActivity.EXTRA_LOCATION);
            devicetype = intent.getStringExtra(MainActivity.EXTRA_DEVICETYPE);
            id = intent.getStringExtra(MainActivity.EXTRA_ID);
            datum = intent.getStringExtra(MainActivity.EXTRA_START_DATE);
            einheit = intent.getStringExtra(MainActivity.EXTRA_UNIT);
        }

        chart = (LineChart) findViewById(R.id.chart);

        setTitle(datum);

        loadDayData(datum);
    }

    @Override
    public boolean onSupportNavigateUp(){
        super.onSupportNavigateUp();
        onBackPressed();
        getSupportFragmentManager().popBackStack();
        return true;
    }

    /**
     * Zeigt den Verlauf des Sensors am angegebenen Datum an
     * @param datum das Datum
     */
    private void loadDayData(String datum){
        findViewById(R.id.loading_animation).setVisibility(View.VISIBLE);
        chart.setVisibility(GONE);

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getgraphdata");

        requestData.put("room", location);
        requestData.put("type", devicetype);
        requestData.put("id", id);
        requestData.put("von", datum);
        requestData.put("bis", datum);
        requestData.put("username", SaveData.getUsername(getApplicationContext()));
        requestData.put("password", SaveData.getPassword(getApplicationContext()));

        HTTPRequest.sendRequest(getApplicationContext(), requestData, SaveData.getServerIp(getApplicationContext()),
                new HTTPRequest.HTTPRequestCallback() {
                    @Override
                    public void onRequestResult(String result) {
                        findViewById(R.id.loading_animation).setVisibility(GONE);
                        findViewById(R.id.empty_item).setVisibility(GONE);

                        switch(result){
                            case "wrongdata":
                                Dialogs.fehlermeldung("Anmeldung nicht möglich!\nBitte logge dich erneut ein.",
                                        findViewById(R.id.frame));
                                break;
                            case "unknownuser":
                                Dialogs.fehlermeldung("Dieser Benutzer existiert nicht!\nBitte logge dich erneut ein.",
                                        findViewById(R.id.frame));
                                break;
                            case "nopermission":
                                Dialogs.fehlermeldung("Du hast dazu keine Berechtigung.",
                                        findViewById(R.id.frame));
                                break;
                            default:
                                try{
                                    JSONObject object = new JSONObject(result);

                                    JSONArray values = object.getJSONArray("values");

                                    einheit = object.getString("einheit");

                                    double[] valueList = new double[values.length()];
                                    String[] timeList = new String[values.length()];

                                    for(int i = 0; i < values.length(); i++){
                                        JSONObject c = values.getJSONObject(i);

                                        valueList[i] = c.getDouble("value");
                                        timeList[i] = c.getString("time");
                                    }

                                    showValueCourse(valueList, timeList);

                                }
                                catch(Exception e){
                                    e.printStackTrace();

                                    onError("");
                                }
                                break;
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        findViewById(R.id.loading_animation).setVisibility(GONE);

                        chart.setVisibility(GONE);

                        findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                        ((ImageView)findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon("overview"));
                        ((TextView)findViewById(R.id.empty_title)).setText("Fehler beim Laden");
                        ((TextView)findViewById(R.id.empty_info)).setText("Die Verlaufsdaten konnten nicht geladen werden");
                    }
                });
    }

    private void showValueCourse(double[] values, String[] time){
        ArrayList<Entry> vals = new ArrayList<>();

        final ArrayList<String> times = new ArrayList<>();

        for(int i = 0; i < values.length; i++){
            vals.add(new Entry(i, (float) values[i]));

            times.add(time[i]);
        }

        //Einstellungen des Graphen
        String title = "Werteverlauf";

        if(einheit != null){
            title = "Werteverlauf in "+einheit;
        }

        LineDataSet valueLine = new LineDataSet(vals, title);

        valueLine.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        valueLine.setCubicIntensity(0.2f);

        valueLine.setAxisDependency(YAxis.AxisDependency.LEFT);
        valueLine.setColor(getResources().getColor(R.color.colorPrimary));

        valueLine.setDrawCircles(false);
        valueLine.setLineWidth(1.5f);
        valueLine.setDrawValues(false);

        Legend legend = chart.getLegend();
        legend.setTextSize(20);

        LineData chartLineData = new LineData(valueLine);

        //Graph anzeigen
        chart.animateY(500);
        chart.setData(chartLineData);

        chart.getDescription().setEnabled(false);

        chart.setScaleYEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                try{
                    return times.get((int) value);
                }catch(Exception e){
                    e.printStackTrace();
                    return String.valueOf(value);
                }
            }
        });

        chart.invalidate();
        chart.setVisibility(View.VISIBLE);
    }
}
