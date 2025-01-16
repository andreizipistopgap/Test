package ro.pub.cs.systems.eim.test;
import ro.pub.cs.systems.eim.test.DefinitionReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText wordInput;
    private Button searchButton;
    private TextView definitionTextView;
    private DefinitionReceiver definitionReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Legăm componentele UI de cod
        wordInput = findViewById(R.id.wordInput);
        searchButton = findViewById(R.id.searchButton);
        definitionTextView = findViewById(R.id.definitionTextView);

        // Înregistrăm receiver-ul pentru a asculta broadcast-ul
        definitionReceiver = new DefinitionReceiver(definitionTextView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(definitionReceiver, new IntentFilter("ro.pub.cs.systems.eim.test.DEFINITION_BROADCAST"), Context.RECEIVER_NOT_EXPORTED);
        }

        // Setăm listener pentru butonul de căutare
        searchButton.setOnClickListener(v -> {
            String word = wordInput.getText().toString().trim();

            if (!word.isEmpty()) {
                fetchDefinition(word);
            } else {
                Toast.makeText(MainActivity.this, "Introduceți un cuvânt!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDefinition(String word) {
        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject firstEntry = response.getJSONObject(0);
                            JSONArray meaningsArray = firstEntry.getJSONArray("meanings");
                            JSONObject firstMeaning = meaningsArray.getJSONObject(0);
                            JSONArray definitionsArray = firstMeaning.getJSONArray("definitions");
                            JSONObject firstDefinitionObject = definitionsArray.getJSONObject(0);
                            String definition = firstDefinitionObject.getString("definition");

                            // Trimitere broadcast cu definiția
                            Intent intent = new Intent("ro.pub.cs.systems.eim.test.DEFINITION_BROADCAST");
                            intent.putExtra("definition", definition);
                            sendBroadcast(intent);

                            Log.d("DEBUG", "Broadcast trimis cu definiția: " + definition);

                        } catch (Exception e) {
                            Log.e("ERROR", "Eroare de parsare JSON: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Nu s-a găsit definiția!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Eroare la accesarea serviciului!", Toast.LENGTH_SHORT).show();
                        Log.e("ERROR", "Eroare la accesarea API: " + error.getMessage());
                    }
                });

        requestQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(definitionReceiver);  // Deregistrează receiver-ul pentru a evita scurgeri de memorie
    }
}
