package ro.pub.cs.systems.eim.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

public class DefinitionReceiver extends BroadcastReceiver {

    private TextView definitionTextView;

    public DefinitionReceiver(TextView definitionTextView) {
        this.definitionTextView = definitionTextView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String definition = intent.getStringExtra("definition");
        if (definition != null) {
            definitionTextView.setText(definition);  // Actualizează UI-ul cu definiția
            Log.d("DEBUG", "Definiția a fost afișată: " + definition);
        } else {
            Log.e("ERROR", "Definiția este null!");
        }
    }
}
 // comm
