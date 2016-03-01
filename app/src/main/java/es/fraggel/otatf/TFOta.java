package es.fraggel.otatf;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TFOta extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,AsyncResponse {
    String dev=null;
    String vendor=null;
    String rom=null;
    String idioma=null;
    String pathRecovery=null;
    float version=-1;
    TextView txtDevice=null;
    TextView txtVendor=null;
    TextView txtRom=null;
    TextView txtVersion=null;
    TextView txtUpdate=null;

    FloatingActionButton btnActions=null;

    String theme=null;
    boolean hayinternet=false;
    static HashMap<String, String> listaDescargas = new HashMap<String, String>();
    static long downloadREF = -1;
    static NotificationManager mNotificationManagerUpdate=null;
    private int SIMPLE_NOTFICATION_UPDATE=7777;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnActions = (FloatingActionButton) findViewById(R.id.btnActions);
        btnActions.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BufferedReader br=null;

        try {
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
                    version=Float.parseFloat(cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.version=", ""));
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.recpath")!=-1){
                    pathRecovery=cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.recpath=", "");
                }
                if(cadenaLeida.trim().indexOf("ro.tfota.theme")!=-1){
                    theme=cadenaLeida.trim().replaceAll(" ", "").replaceAll("ro.tfota.theme=", "");
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
            /*if("black".equals(theme)){
                setTheme(R.style.AppThemeBlack);
            }else if("white".equals(theme)){
                setTheme(R.style.AppThemeWhite);

            }*/
            mNotificationManagerUpdate = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManagerUpdate.cancel(SIMPLE_NOTFICATION_UPDATE);
            File f1 = new File(Environment.getExternalStorageDirectory() + "/tfota/downloads/");
            if (!f1.exists()) {
                f1.mkdirs();
            }

            txtDevice= (TextView)findViewById(R.id.txtDevice);
            txtVendor= (TextView)findViewById(R.id.txtVendor);
            txtRom= (TextView)findViewById(R.id.txtRom);
            txtVersion= (TextView)findViewById(R.id.txtVersion);
            txtUpdate= (TextView)findViewById(R.id.txtUpdate);


            btnActions.setOnClickListener(this);
            if(dev==null && vendor==null && rom==null && version==-1){
                txtDevice.setText(getResources().getString(R.string.errorNotFound));
                txtVendor.setText(getResources().getString(R.string.errorNotFound));
                txtRom.setText(getResources().getString(R.string.errorNotFound));
                txtVersion.setText(getResources().getString(R.string.errorNotFound));
            }else{
                txtUpdate.setText(getResources().getString(R.string.txtActualVersion));
                txtDevice.setText(dev);
                txtVendor.setText(vendor);
                txtRom.setText(rom);
                txtVersion.setText(String.valueOf(version));
                btnActions.setEnabled(true);
            }

        }catch(Exception e){

        }finally {
            try {
                br.close();
            }catch(Exception e){}

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_update) {
            Intent intent = new Intent(this, TFOta.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, ConfigActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btnActions){
            Toast.makeText(getApplicationContext(),getResources().getText(R.string.txtCheckUpd),Toast.LENGTH_SHORT).show();
            comprobarVersion(dev,vendor,rom,String.valueOf(version),idioma);
        }
    }
    private void comprobarVersion(String dev,String vendor,String rom,String version,String idioma) {
        try {
            VersionThread asyncTask = new VersionThread();
            asyncTask.delegate = this;
            asyncTask.execute(dev,vendor,rom,version,idioma);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.msgErrorVersion), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean comprobarConexion() {
        ConnectivityManager cn=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf=cn.getActiveNetworkInfo();
        if(nf != null && nf.isConnected()==true )
        {
            hayinternet=true;

        }
        else
        {
            hayinternet=false;
        }
        return hayinternet;
    }

    @Override
    public void processFinish(String output) {
        try {
            if (output != null && !"TIMEOUT".equals(output)) {
                String[] split = output.split(";");
                if(split.length<2 || "".equals(output.trim()) || version>=Float.parseFloat(split[3])){
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.msgNoNeedUpdate), Toast.LENGTH_SHORT).show();
                }else{
                    Bundle b=new Bundle();
                    b.putStringArray("Update",split);
                    Intent it=new Intent(this,Details.class);
                    it.putExtras(b);
                    startActivity(it);
                }
            }else{
                /*btnActions.setEnabled(true);
                btnActions.setText(getResources().getString(R.string.msgCheckUpdate));*/
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.msgNoPossibleConnect), Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            /*btnActions.setEnabled(true);
            btnActions.setText(getResources().getString(R.string.msgCheckUpdate));*/
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.msgNoPossibleConnect), Toast.LENGTH_SHORT).show();
        }
    }
    private String asignaFecha() {
        String fecha_mod=null;
        Calendar cal=Calendar.getInstance();
        String day=String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        String month=String.valueOf((cal.get(Calendar.MONTH)+1));
        String year=String.valueOf(cal.get(Calendar.YEAR));
        if(day.length()<2){
            day="0"+day;
        }
        if(month.length()<2){
            month="0"+month;
        }
        fecha_mod=(day+"/"+month+"/"+year);
        return fecha_mod;
    }
}
