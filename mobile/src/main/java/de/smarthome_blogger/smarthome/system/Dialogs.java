package de.smarthome_blogger.smarthome.system;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import de.smarthome_blogger.smarthome.R;

/**
 * Created by Sascha on 18.04.2017.
 */

public class Dialogs {

    /**
     * Stellt die angegebene Nachricht in einer Snackbar dar
     * @param msg die Nachricht
     * @param v die View, in der die Nachricht angezeigt werden soll
     */
    public static void fehlermeldung(String msg, View v){
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Erstellt einen Dialog, um ein Item auszuwählen
     * @param title Titel des Dialogs
     * @param cancelButtonText Titel des "Abbrechen"-Buttons
     * @param context Kontext der App
     * @param items Zur Wahl stehende Items
     * @param onOk Listener für Item-Klicks
     * @param onCancel Listener für Cancel-Klicks
     */
    public static void singleChoiceDialog(String title, String cancelButtonText, Context context,
        String[] items, final DialogInterface.OnClickListener onOk, final DialogInterface.OnClickListener onCancel){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);

        builder.setItems(items, onOk);

        builder.setNegativeButton(cancelButtonText, onCancel);

        builder.show();

    }

    /**
     * Erstellt einen Dialog, um einen Zeitraum zu bestimmen
     * @param activity
     * @param view
     * @param periodChosenListener
     */
    public static void chooseTimePeriodDialog(final Activity activity, final View view, final OnPeriodChosenListener periodChosenListener){
        final long dayMilliseconds = 60 * 60 * 24 * 1000;
        final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        String[] items = {"Letzte 30 Tage", "Letzte 60 Tage", "Letzte 90 Tage",
                "Dieser Monat", "Letzter Monat", "Dieses Jahr", "Letztes Jahr", "Benutzerdefiniert"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Zeitraum wählen");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Long now = System.currentTimeMillis();
                Long then;
                String vonDatum, bisDatum;
                int tag, monat, jahr;

                Calendar cal = Calendar.getInstance();

                switch(which){
                    case 0:
                        then = now - ((long) 30 * dayMilliseconds);
                        periodChosenListener.onPeriodChosen(formatter.format((then)), formatter.format(now));
                        break;
                    case 1:
                        then = now - ((long) 60 * dayMilliseconds);
                        periodChosenListener.onPeriodChosen(formatter.format((then)), formatter.format(now));
                        break;
                    case 2:
                        then = now - ((long) 90 * dayMilliseconds);
                        periodChosenListener.onPeriodChosen(formatter.format((then)), formatter.format(now));
                        break;
                    case 3:
                        tag = 31;
                        monat = cal.get(Calendar.MONTH);
                        jahr = cal.get(Calendar.YEAR);

                        switch (monat){
                            case Calendar.FEBRUARY:
                                if((jahr%4)==0){
                                    tag = 29;
                                }
                                else tag = 28;
                                break;
                            case Calendar.APRIL:
                            case Calendar.JUNE:
                            case Calendar.SEPTEMBER:
                            case Calendar.NOVEMBER:
                                tag = 30;
                                break;
                        }

                        if(monat == Calendar.DECEMBER){
                            monat = Calendar.JANUARY;
                        }
                        else{
                            monat++;
                        }

                        vonDatum = "01." + monat + "." + jahr;
                        bisDatum = tag + "." + monat + "." + jahr;
                        periodChosenListener.onPeriodChosen(vonDatum, bisDatum);
                        break;
                    case 4:
                        tag = 31;
                        monat = cal.get(Calendar.MONTH);
                        jahr = cal.get(Calendar.YEAR);

                        if(monat == Calendar.JANUARY){
                            monat = Calendar.DECEMBER;
                        }
                        else{
                            monat--;
                        }

                        switch (monat){
                            case Calendar.FEBRUARY:
                                if((jahr%4)==0){
                                    tag = 29;
                                }
                                else tag = 28;
                                break;
                            case Calendar.APRIL:
                            case Calendar.JUNE:
                            case Calendar.SEPTEMBER:
                            case Calendar.NOVEMBER:
                                tag = 30;
                                break;
                        }

                        if(monat == Calendar.DECEMBER){
                            monat = Calendar.JANUARY;
                        }
                        else{
                            monat++;
                        }

                        vonDatum = "01." + monat + "." + jahr;
                        bisDatum = tag + "." + monat + "." + jahr;
                        periodChosenListener.onPeriodChosen(vonDatum, bisDatum);
                        break;
                    case 5:
                        vonDatum = "01.01." + cal.get(Calendar.YEAR);
                        bisDatum = "31.12." + cal.get(Calendar.YEAR);
                        periodChosenListener.onPeriodChosen(vonDatum, bisDatum);
                        break;
                    case 6:
                        jahr = cal.get(Calendar.YEAR)-1;

                        vonDatum = "01.01." + jahr;
                        bisDatum = "31.12." + jahr;
                        periodChosenListener.onPeriodChosen(vonDatum, bisDatum);
                        break;
                    case 7:
                        Dialogs.chooseCustomTimePeriodDialog(activity, view, periodChosenListener);
                        break;
                }
            }
        });

        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     * Erstellt einen Dialog, um einen eigenen Zeitraum zu wählen
     * @param activity
     * @param v
     * @param periodChosenListener
     */
    public static void chooseCustomTimePeriodDialog(Activity activity, final View v,
                                                    final OnPeriodChosenListener periodChosenListener){
        final Map<String, String> periodData = new HashMap<>();
        periodData.put("start", null);
        periodData.put("end", null);

        final Map<String, Long> periodTimestamps = new HashMap<>();
        periodTimestamps.put("start", null);
        periodTimestamps.put("end", null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Zeitraum wählen");

        final View periodChooserView = activity.getLayoutInflater().inflate(R.layout.period_chooser, null);

        final DatePicker datePicker = (DatePicker) periodChooserView.findViewById(R.id.datepicker);

        final Button setStartDate = (Button) periodChooserView.findViewById(R.id.startDate);
        final Button setEndDate = (Button) periodChooserView.findViewById(R.id.endDate);

        builder.setView(periodChooserView);

        setStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth()+1;
                int year = datePicker.getYear();

                periodData.put("start", ((day<10)?("0"+day):day)+"."+((month<10)?("0"+month):month)+"."+year);

                Calendar calendar = new GregorianCalendar(year, month, day);
                periodTimestamps.put("start", calendar.getTimeInMillis());

                setStartDate.setText(periodData.get("start"));
            }
        });

        setEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth()+1;
                int year = datePicker.getYear();

                periodData.put("end", ((day<10)?("0"+day):day)+"."+((month<10)?("0"+month):month)+"."+year);

                Calendar calendar = new GregorianCalendar(year, month, day);
                periodTimestamps.put("end", calendar.getTimeInMillis());

                setStartDate.setText(periodData.get("end"));
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(periodData.get("start") == null || periodData.get("end") == null){
                    Dialogs.fehlermeldung("Bitte lege ein Start- und ein Enddatum fest.", v);
                }
                else{
                    if(periodTimestamps.get("start") < periodTimestamps.get("end")){
                        periodChosenListener.onPeriodChosen(periodData.get("start"), periodData.get("end"));
                    }
                    else{
                        Dialogs.fehlermeldung("Das Startdatum muss vor dem Enddatum liegen.", v);
                    }
                }
            }
        });

        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }

    public interface OnPeriodChosenListener{
        /**
         * Wird aufgerufen, wenn ein Zeitraum ausgewählt wurde
         * @param start
         * @param end
         */
        void onPeriodChosen(String start, String end);
    }

}
