package es.fraggel.otatf;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by Fraggel on 9/08/13.
 */

public class UpdateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND,2);
        Intent intent2 = new Intent(context, NotifyService.class);
        PendingIntent pintent = PendingIntent.getService(context, 0, intent2,
                0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SharedPreferences ajustes=context.getSharedPreferences("otatf", Context.MODE_PRIVATE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), Long.parseLong(ajustes.getString("timeInterval", "86400000")), pintent);
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),21600000, pintent);
        context.startService(new Intent(context,NotifyService.class));

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.SECOND,2);
    }
}
