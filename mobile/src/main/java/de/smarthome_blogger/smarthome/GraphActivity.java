package de.smarthome_blogger.smarthome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.items.GraphDayItem;
import de.smarthome_blogger.smarthome.system.Dialogs;
import de.smarthome_blogger.smarthome.system.HTTPRequest;
import de.smarthome_blogger.smarthome.system.Icons;
import de.smarthome_blogger.smarthome.system.SaveData;

public class GraphActivity extends AppCompatActivity {

    private String location, devicetype, id, einheit;
    private LineChart lineChart;
    private ArrayList<GraphDayItem> dayList;

    //Für benutzerdefinierten Zeitraum
    private final long dayMilliseconds = 60 * 60 * 24 * 1000;
    private DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        setTitle(bundle.getString(MainActivity.EXTRA_TITLE));

        location = bundle.getString(MainActivity.EXTRA_LOCATION);
        devicetype = bundle.getString(MainActivity.EXTRA_DEVICETYPE);
        id = bundle.getString(MainActivity.EXTRA_DEVICE);

        //Graph definieren
        lineChart = (LineChart) findViewById(R.id.linechart);
        lineChart.setDescription(null);
        lineChart.setHighlightPerDragEnabled(false);
        lineChart.setScaleYEnabled(false);

        dayList = new ArrayList<>();

        Long now = System.currentTimeMillis();
        Long then = now - ((long) (30 * dayMilliseconds)); //30 Tage zuvor

        loadGraphData(formatter.format(then), formatter.format(now));
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        onBackPressed();
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.activity_graph, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id  = item.getItemId();

        if(id == R.id.change_period){
            Dialogs.chooseTimePeriodDialog(this, findViewById(R.id.frame), new Dialogs.OnPeriodChosenListener(){
                @Override
                public void onPeriodChosen(String start, String end){
                    loadGraphData(start, end);
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Lädt die Archivdaten des Sensors im angegebenen Bereich vom Server
     * @param vonDatum
     * @param bisDatum
     */
    public void loadGraphData(String vonDatum, String bisDatum){
        lineChart.setVisibility(View.GONE);

        findViewById(R.id.empty_item).setVisibility(View.GONE);

        findViewById(R.id.loading_animation).setVisibility(View.VISIBLE);

        final Map<String, String> requestData = new HashMap<>();
        requestData.put("action", "getgraphdata");
        requestData.put("room", "location");
        requestData.put("type", devicetype);
        requestData.put("id", id);
        requestData.put("von", vonDatum);
        requestData.put("bis", bisDatum);

        requestData.put("username", SaveData.getUsername(getApplicationContext()));
        requestData.put("password", SaveData.getPassword(getApplicationContext()));

        HTTPRequest.sendRequest(getApplicationContext(), requestData, SaveData.getServerIp(getApplicationContext()), new HTTPRequest.HTTPRequestCallback() {
            @Override
            public void onRequestResult(String result) {
                findViewById(R.id.loading_animation).setVisibility(View.GONE);

                switch(result){
                    default:
                        try{
                            JSONObject jsonObject = new JSONObject(result);

                            JSONArray values = jsonObject.getJSONArray("values");

                            einheit = jsonObject.getString("einheit");

                            dayList.clear();

                            for(int i = 0; i < values.length(); i++){
                                JSONObject o = values.getJSONObject(i);

                                dayList.add(new GraphDayItem(o.getString("date"), BigDecimal.valueOf(o.getDouble("min")).floatValue(),
                                        BigDecimal.valueOf(o.getDouble("max")).floatValue()));
                            }

                            fillLineChart();

                            if(dayList.isEmpty()){
                                lineChart.setVisibility(View.GONE);

                                findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                                ((ImageView) findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon("overview"));
                                ((TextView) findViewById(R.id.empty_title)).setText("Keine Daten");
                                ((TextView) findViewById(R.id.empty_info)).setText("Für den ausgewählten Zeitraum sind keine Daten vorhanden.");
                            }

                        }
                        catch(JSONException e){
                            e.printStackTrace();

                            onError("");
                        }
                        break;
                }
            }

            @Override
            public void onError(String msg) {
                lineChart.setVisibility(View.GONE);

                findViewById(R.id.empty_item).setVisibility(View.VISIBLE);
                ((ImageView) findViewById(R.id.empty_icon)).setImageResource(Icons.getDrawerIcon("overview"));
                ((TextView) findViewById(R.id.empty_title)).setText("Keine Daten");
                ((TextView) findViewById(R.id.empty_info)).setText("Die Daten konnten nicht geladen werden");
            }
        });
    }

    /**
     * Füllt den LineChart mit Werten
     */
    public void fillLineChart(){
        if(dayList.isEmpty()){
            return;
        }

        ArrayList<Entry> minVals = new ArrayList<>();
        ArrayList<Entry> maxVals = new ArrayList<>();

        final ArrayList<String> dates = new ArrayList<>();

        for(int i = 0; i < dayList.size(); i++){
            GraphDayItem item = dayList.get(i);

            minVals.add(new Entry(i, (float) item.getMinVal()));
            maxVals.add(new Entry(i, (float) item.getMaxVal()));

            dates.add(item.getDate());
        }

        //Einstellungen des Graphen
        final LineDataSet minLine = new LineDataSet(minVals, "Minumum");

        minLine.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        minLine.setCubicIntensity(0.2f);

        minLine.setAxisDependency(YAxis.AxisDependency.LEFT);
        minLine.setColor(getResources().getColor(R.color.colorPrimary));
        minLine.setCircleSize(4.5f);
        minLine.setCircleColor(getResources().getColor(R.color.colorPrimary));
        minLine.setDrawCircles(true);
        minLine.setDrawValues(false);

        final LineDataSet maxLine = new LineDataSet(maxVals, "Maximum");

        maxLine.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        maxLine.setCubicIntensity(0.2f);

        maxLine.setAxisDependency(YAxis.AxisDependency.LEFT);
        maxLine.setColor(getResources().getColor(R.color.red));
        maxLine.setCircleSize(4.5f);
        maxLine.setCircleColor(getResources().getColor(R.color.red));
        maxLine.setDrawCircles(true);
        maxLine.setDrawValues(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(minLine);
        dataSets.add(maxLine);

        Legend legend = lineChart.getLegend();
        legend.setTextSize(20);

        LineData lineData = new LineData(dataSets);

        lineChart.animateY(500);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                try{
                    return dates.get((int) value);
                }
                catch(Exception e){
                    e.printStackTrace();
                    return String.valueOf(value);
                }
            }
        });

        lineChart.invalidate();
        lineChart.setVisibility(View.VISIBLE);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = minLine.getEntryIndex(e);

                if(index == -1){
                    index = maxLine.getEntryIndex(e);
                }

                if(index == -1){
                    return;
                }

                //ValueCourseActivity starten
                Intent intent = new Intent(GraphActivity.this, ValueCourseActivity.class);
                intent.putExtra(MainActivity.EXTRA_START_DATE, dayList.get(index).getDate());
                intent.putExtra(MainActivity.EXTRA_DEVICETYPE, devicetype);
                intent.putExtra(MainActivity.EXTRA_ID, id);
                intent.putExtra(MainActivity.EXTRA_LOCATION, location);
                intent.putExtra(MainActivity.EXTRA_UNIT, einheit);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }
}
