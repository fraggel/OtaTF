package es.fraggel.otatf;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by root on 15/02/15.
 */
public class DownloadReceiver extends BroadcastReceiver {
    Details det=null;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (getClass().getPackage().getName().equals(intent.getPackage())) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    boolean b = TFOta.listaDescargas.containsKey(String.valueOf(referenceId));
                    if (b) {
                        String nombre = TFOta.listaDescargas.get(String.valueOf(referenceId));
                        Toast.makeText(context, nombre + " " + context.getResources().getString(R.string.msgDownFinished), Toast.LENGTH_SHORT).show();
                        det.downloadCompleted();
                    }
                } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                    Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(dm);
                }
            }
        } catch (Exception e) {
            //Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }
    void setMainActivityHandler(Details main){
        det = main;
    }
}
