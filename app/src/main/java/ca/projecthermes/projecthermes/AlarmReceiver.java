package ca.projecthermes.projecthermes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import ca.projecthermes.projecthermes.services.WiFiPeerDiscoverService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("hermes", " at AlarmReceiver");
        Toast.makeText(context, "at Alarm Receiver", Toast.LENGTH_LONG).show();
        Intent intent1 = new Intent(context, WiFiPeerDiscoverService.class);
        context.startService(intent1);
    }
}
