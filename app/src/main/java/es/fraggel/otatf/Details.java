package es.fraggel.otatf;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;


public class Details extends ActionBarActivity implements View.OnClickListener, AsyncResponse {

    TextView txtUpdate=null;
    TextView txtChangelog=null;
    TextView lblSize=null;
    FloatingActionButton btnActions=null;
    ImageButton imgROM=null;
    String urlDestino=null;
    DownloadReceiver yourBR = null;
    String nombreFichero=null;
    String md5=null;
    String urlWeb=null;
    String pathRecovery=null;
    SharedPreferences ajustes=null;
    String theme=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ajustes=getSharedPreferences("otatf", Context.MODE_PRIVATE);
        BufferedReader brh=null;

        try {
            brh = new BufferedReader(new FileReader(new File("/system/build.prop")));
            String cadenaLeida = brh.readLine();
            while (cadenaLeida != null) {
                if (cadenaLeida.trim().indexOf("ro.tfota.theme") != -1) {
                    theme = cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.theme=", "");
                }
                cadenaLeida = brh.readLine();
            }
            /*if ("black".equals(theme)) {
                setTheme(R.style.AppThemeBlack);
            } else if ("white".equals(theme)) {
                setTheme(R.style.AppThemeWhite);

            }*/
        }catch(Exception e){

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        try {
            Bundle b = this.getIntent().getExtras();
            String[] split = b.getStringArray("Update");
            if (split.length > 0) {
                txtUpdate = (TextView) findViewById(R.id.txtUpdate);
                txtChangelog = (TextView) findViewById(R.id.txtChangelog);
                lblSize=(TextView)findViewById(R.id.lblSize);
                txtChangelog.setMovementMethod(new ScrollingMovementMethod());
                btnActions = (FloatingActionButton) findViewById(R.id.btnActions);
                btnActions.setOnClickListener(this);
                txtUpdate.setText(getResources().getString(R.string.txtNewVersion) + " " + split[2] + " " + split[3]);
                if("black".equals(theme)){
                    txtUpdate.setTextColor(Color.parseColor("#c11227"));
                }else if("white".equals(theme)) {
                    txtUpdate.setTextColor(Color.parseColor("#c11227"));
                }
                String txtChange = "";
                String[] split1 = split[4].split("#");
                for (int x = 0; x < split1.length; x++) {
                    txtChange = txtChange + new String(split1[x].getBytes("ISO-8859-15"),"ISO-8859-15") + "\n";
                }

                txtChangelog.setText(txtChange);
                imgROM = (ImageButton) findViewById(R.id.imgROM);
                imgROM.setOnClickListener(this);
                lblSize.setText(getResources().getString(R.string.msgUpdateFound) + " " + split[6] + "MB");
                btnActions.setEnabled(true);
                urlDestino=split[5];
                md5=split[8];
                BufferedReader br=null;

                    br=new BufferedReader(new FileReader(new File("/system/build.prop")));
                    String cadenaLeida=br.readLine();
                    while(cadenaLeida!=null){

                        if(cadenaLeida.trim().indexOf("ro.tfota.recpath")!=-1){
                            pathRecovery=cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.recpath=", "");
                        }

                        cadenaLeida=br.readLine();
                    }
                nombreFichero = urlDestino.split("/")[urlDestino.split("/").length - 1];
                try {
                    ImageThread asyncTask = new ImageThread();
                    asyncTask.delegate = this;
                    asyncTask.execute(split[7],md5);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgErrorVersion), Toast.LENGTH_SHORT).show();
                }
                urlWeb=split[9];
                if(new File(Environment.getExternalStorageDirectory()+"/tfota/downloads/"+nombreFichero).exists()) {
                    if(Utilidades.checkFileMD5(new File(Environment.getExternalStorageDirectory() + "/tfota/downloads/" + nombreFichero), md5)) {
                        downloadCompleted();
                    }else{
                        descargaIncorrecta();
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.btnActions) {
                if(getResources().getString(R.string.txtInstallUpd).equals(lblSize.getText())){
                    String cad="";
                    cad= Environment.getExternalStorageDirectory() + "/tfota/downloads/"+nombreFichero;
                    cad=cad.replaceAll(Environment.getExternalStorageDirectory().getAbsolutePath(),pathRecovery);
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.msgTitleInstall))
                            .setMessage(getResources().getString(R.string.msgTextInstall)+cad)
                            .setPositiveButton(getResources().getString(R.string.msgInstallManual), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ((PowerManager) getSystemService(POWER_SERVICE)).reboot("recovery");
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.msgInstallAuto), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String cad="";
                                        cad= Environment.getExternalStorageDirectory() + "/tfota/downloads/"+nombreFichero;
                                        cad=cad.replaceAll(Environment.getExternalStorageDirectory().getAbsolutePath(),pathRecovery);

                                        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(new File("/cache/recovery/extendedcommand")));

                                        bos.write(("run_program(\"/sbin/umount\",\""+pathRecovery+"\");\n").getBytes());
                                        bos.write(("run_program(\"/sbin/mount,\""+pathRecovery+"\");\n").getBytes());
                                        bos.write(("install_zip(\"" + cad + "\");\n").getBytes());
                                        bos.flush();
                                        bos.close();

                                        BufferedOutputStream bos2=new BufferedOutputStream(new FileOutputStream(new File("/cache/recovery/openrecoveryscript")));

                                        bos2.write(("unmount "+pathRecovery+"\n").getBytes());
                                        bos2.write(("mount "+pathRecovery+"\n").getBytes());
                                        bos2.write(("install " + cad + "\n").getBytes());
                                        bos2.flush();
                                        bos2.close();
                                        ((PowerManager) getSystemService(POWER_SERVICE)).reboot("recovery");
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }else {

                    boolean onlyWifi=ajustes.getBoolean("onlyWifi",true);
                    if(onlyWifi){
                        if (Utilidades.wifiConnected(this)) {
                            yourBR = new DownloadReceiver();
                            yourBR.setMainActivityHandler(this);
                            IntentFilter callInterceptorIntentFilter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
                            registerReceiver(yourBR, callInterceptorIntentFilter);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDestino));
                            String nombreFichero = "";
                            nombreFichero = urlDestino.split("/")[urlDestino.split("/").length - 1];

                            request.setDescription(nombreFichero);
                            request.setTitle(nombreFichero);
                            if (Build.VERSION.SDK_INT >= 11) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                            }
                            request.setDestinationInExternalPublicDir("/tfota/downloads/", nombreFichero);

                            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            TFOta.listaDescargas.put(String.valueOf(manager.enqueue(request)), nombreFichero);
                            lblSize.setText(getResources().getString(R.string.txtDownloading));
                            btnActions.setEnabled(false);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgDownInit) + " " + nombreFichero, Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgNoWifi), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        if (Utilidades.dataAvailable(this)) {
                            yourBR = new DownloadReceiver();
                            yourBR.setMainActivityHandler(this);
                            IntentFilter callInterceptorIntentFilter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
                            registerReceiver(yourBR, callInterceptorIntentFilter);
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDestino));
                            String nombreFichero = "";
                            nombreFichero = urlDestino.split("/")[urlDestino.split("/").length - 1];

                            request.setDescription(nombreFichero);
                            request.setTitle(nombreFichero);
                            if (Build.VERSION.SDK_INT >= 11) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                            }
                            request.setDestinationInExternalPublicDir("/tfota/downloads/", nombreFichero);

                            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                            TFOta.listaDescargas.put(String.valueOf(manager.enqueue(request)), nombreFichero);
                            lblSize.setText(getResources().getString(R.string.txtDownloading));
                            btnActions.setEnabled(false);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgDownInit) + " " + nombreFichero, Toast.LENGTH_SHORT).show();
                        }else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgNoInet), Toast.LENGTH_SHORT).show();
                            }
                    }

                }
            }
            if(view.getId() == R.id.imgROM){
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(urlWeb));
                startActivity(i);

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void downloadCompleted(){
        if(Utilidades.checkFileMD5(new File(Environment.getExternalStorageDirectory() + "/tfota/downloads/" + nombreFichero), md5)) {
            lblSize.setText(getResources().getString(R.string.txtInstallUpd));
            btnActions.setEnabled(true);
        }else{
            descargaIncorrecta();
        }
    }
    private void descargaIncorrecta() {
        File f =new File(Environment.getExternalStorageDirectory() + "/tfota/downloads/");
                    File[] files = f.listFiles();
                    for (int x=0;x<files.length;x++){
                        File ff=files[x];
                        ff.delete();
                    }
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.msgDownFailed), Toast.LENGTH_SHORT).show();
        lblSize.setText(getResources().getString(R.string.msgUpdateFound));
        btnActions.setEnabled(true);
    }

    @Override
    public void processFinish(String output) {
        if (output != null && !"TIMEOUT".equals(output)) {
            imgROM.setBackground(Drawable.createFromPath(Environment.getExternalStorageDirectory() + "/tfota/downloads/" + md5.trim()));
        }
    }
}
