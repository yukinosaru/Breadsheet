package com.dkcy.breadsheet;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.TextView.*;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.webkit.WebSettings;

import java.sql.SQLException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    // Objects for ease of reference
    private Recipe recipe = new Recipe();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the database helper for accessing data
        DataBaseHelper myHelper = new DataBaseHelper(this);
        try {
            myHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        myHelper.openDataBase();


        // Set up the bread selector
        Spinner spinner = (Spinner) findViewById(R.id.breadSelector);
        String[] doughNames = myHelper.getDoughNames();    // Gets list of doughs from database
        myHelper.close();

        // Create an ArrayAdapter using the doughnames string array and a default spinner layout (simple_spinner_item)
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, doughNames);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Sets up listener for spinner
        // On select, recalculate ingredient volumes
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String bread = adapterView.getSelectedItem().toString();
                EditText doughWeight = (EditText) findViewById(R.id.doughWeight);
                String weight = doughWeight.getText().toString();
                String[][] ingredients = getIngredients(bread,weight);
                displayIngredients(ingredients);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Sets up listener for Dough weight
        EditText doughWeight = (EditText) findViewById(R.id.doughWeight);
        doughWeight.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String doughWeight = v.getText().toString();
                    Spinner doughType = (Spinner) findViewById(R.id.breadSelector);
                    String bread = doughType.getSelectedItem().toString();
                    String[][] ingredients = getIngredients(bread,doughWeight);
                    displayIngredients(ingredients);
                }
                return false;
            }
        });
    }


    String[][] getIngredients(String bread, String doughWeight) {
        // Testing inputs are fed properly
        /* final TextView textIngredients = (TextView) findViewById(R.id.textIngredients);
        textIngredients.setText("Dough weight is " + doughWeight);

        final TextView textRecipe = (TextView) findViewById(R.id.textRecipe);
        textRecipe.setText("Bread type is " + bread); */
        int parsedWeight = 0;
        float proportion = 0;
        float weight = 0;
        float totalproportion = 0;

        DecimalFormat twoDP = new DecimalFormat("#.#");

        if(!doughWeight.isEmpty()) {
            parsedWeight = Integer.parseInt(doughWeight);
        }

        DataBaseHelper myHelper = new DataBaseHelper(this);
        try {
            myHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        myHelper.openDataBase();

        String[][] ingredients = myHelper.getRecipe(bread);    // Returns string array of ingredients and proportions
        String ingredientText = "Flour (100%): ";    // Initialise text


        // Build ingredient string from array
        // Of format:  Ingredient (Bakers percentage%): Weight
        for (int i=0; i < ingredients.length ; i++){
            totalproportion += Float.valueOf(ingredients[i][1]);
        }

        Float flourWeight = parsedWeight * 100 / totalproportion;
        flourWeight = Float.valueOf(twoDP.format(flourWeight));
        ingredientText += flourWeight + "\n";
        ingredients[0][1] = String.valueOf(flourWeight);

        for (int i=1; i < ingredients.length ;i++) {
            proportion = Float.valueOf(ingredients[i][1]);
            weight = parsedWeight * proportion / totalproportion;
            weight = Float.valueOf(twoDP.format(weight));
            ingredientText += ingredients[i][0] + " (" + ingredients[i][1] + "%): " + weight + "\n";
            ingredients[i][1] = String.valueOf(weight);
        }

        // Find textview and display ingredient string
        final TextView textRecipe = (TextView) findViewById(R.id.textRecipe);
        textRecipe.setText(ingredientText);

        recipe.setIngredients(ingredients);
        return ingredients;
    }

    void displayIngredients(String[][] ingredients){
        // Display absolute volumes as a circle using webview
        WebView webview = (WebView) findViewById(R.id.doughnut);
        String webViewHeader = "<html>"
                +  "<head>"
                +    "<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>"
                +    "<script type=\"text/javascript\">"
                +    "google.charts.load('current', {'packages':['corechart']});"
                +    "google.charts.setOnLoadCallback(drawChart);"
                +    "function drawChart() {"
                +       "var data = google.visualization.arrayToDataTable(["
                +           "['Ingredient', 'Weight, g'],";

        String webViewFooter = "]);"
                +        "var options = {"
                +         "chartArea:{width:'80%',height:'80%'},"
                +          "pieHole: 0.8,"
                +          "pieSliceTextStyle: {"
                +            "color: 'black',"
                +            "fontName: 'Raleway',"
                +            "fontSize: 15,"
                +          "},"
                +          "pieSliceText: 'value',"
                +         "'legend':'none',"
                +        "};"
                +        "var chart = new google.visualization.PieChart(document.getElementById('piechart'));"
                +        "chart.draw(data, options);"
                +      "}"
                +    "</script>"
                +  "</head>"
                +  "<body>"
                +    "<div id=\"piechart\" style=\"width: 330px; height: 330px;\"></div>"
                +  "</body>"
                + "</html>";

        String webViewValues = "";

        String[][] weights = recipe.getIngredients();
        for (int i=0; i < weights.length ;i++) {
            webViewValues += "['" + weights[i][0] + "', " + weights[i][1] + "],\n";
        }

        String content = webViewHeader + webViewValues + webViewFooter;

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.requestFocusFromTouch();
        webview.loadDataWithBaseURL( "file:///android_asset/", content, "text/html", "utf-8", null );
    }

}
