package es.fraggel.otatf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

/**
 * Created by Fraggel on 10/08/13.
 */
public class NotifyService extends Service implements AsyncResponse {

    static NotificationManager mNotificationManagerUpdate=null;
    private int SIMPLE_NOTFICATION_UPDATE=7777;
    SharedPreferences ajustes=null;
    String dev=null;
    String vendor=null;
    String rom=null;
    String pathRecovery=null;
    float version=-1;
    String idioma=null;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            BufferedReader br=null;
            br=new BufferedReader(new FileReader(new File("/system/build.prop")));
            String cadenaLeida=br.readLine();
            while(cadenaLeida!=null){
                if(cadenaLeida.trim().indexOf("ro.tfota.device")!=-1){
                    dev = cadenaLeida.trim().replaceAll(" ","").replaceAll("ro.tfota.device=", "");
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.vendor")!=-1){
                    vendor = cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.vendor=", "");
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.rom")!=-1){
                    rom = cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.rom=", "");
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.version")!=-1){
                    version= Float.parseFloat(cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.version=", ""));
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.recpath")!=-1){
                    pathRecovery=cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.recpath=", "");
                }
                cadenaLeida=br.readLine();
            }
            Locale current = getResources().getConfiguration().locale;
            if(current.toString().indexOf("es")!=-1){
                idioma="es";
            }else if(current.toString().indexOf("en")!=-1){
                idioma="en";
            }else if(current.toString().indexOf("it")!=-1){
                idioma="it";
            }
            ajustes=getSharedPreferences("otatf", Context.MODE_PRIVATE);
            try {
                VersionThread asyncTask = new VersionThread();
                asyncTask.delegate = this;
                asyncTask.execute(dev,vendor,rom, String.valueOf(version),idioma);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgErrorVersion), Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();


    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void processFinish(String output) {
        try {
            if (output != null && !"TIMEOUT".equals(output)) {
                String[] split = output.split(";");

                if(ajustes.getBoolean("notificacionesNews",true)){
                    Resources res = this.getResources();
                    mNotificationManagerUpdate = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    final Notification notifyDetails = new Notification(R.mipmap.ic_launcher, getApplicationContext().getResources().getString(R.string.ntfTitleNVTxt), System.currentTimeMillis());
                    CharSequence contentTitle = getApplicationContext().getResources().getString(R.string.ntfTitleNVTxt);
                    CharSequence contentText = split[2]+" "+split[3];
                    Intent launch_intent = new Intent();
                    launch_intent.setComponent(new ComponentName("es.fraggel.otatf", "es.fraggel.otatf.TFOta"));
                    PendingIntent intent2;

                    intent2 = PendingIntent.getActivity(getApplicationContext(), 0,
                            launch_intent, Intent.FLAG_ACTIVITY_NEW_TASK);
                    //notifyDetails..setLatestEventInfo(getApplicationContext(), contentTitle, contentText, intent2);
                    mNotificationManagerUpdate.notify(SIMPLE_NOTFICATION_UPDATE, notifyDetails);
                }
            }
        } catch (Exception e) {

        }
    }
}
