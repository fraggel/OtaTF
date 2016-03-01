package es.fraggel.otatf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by Fraggel on 10/08/13.
 */
public class ConfigActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {
    SharedPreferences ajustes=null;
    SharedPreferences.Editor editorAjustes=null;
    boolean onlyWifi=false;
    boolean notificacionesNews=true;
    Switch notificacionesChkNews = null;
    Switch chkOnlyWifi = null;
    Spinner spnTimeInterval=null;
    String[] listaTimes=null;
    String theme=null;
    protected void onResume() {
        super.onResume();

    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.content_config);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarConfig);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutConfig);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_viewConfig);
        navigationView.setNavigationItemSelectedListener(this);
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
        }catch(Exception e){}
        try {

            Resources res = this.getResources();
            ajustes=getSharedPreferences("otatf", Context.MODE_PRIVATE);
            notificacionesNews=ajustes.getBoolean("notificacionesNews",true);
            notificacionesChkNews = (Switch) findViewById(R.id.notificacionChkNews);
            notificacionesChkNews.setOnCheckedChangeListener(this);
            if(notificacionesNews){
                notificacionesChkNews.setChecked(true);
            }else{
                notificacionesChkNews.setChecked(false);
            }
            onlyWifi=ajustes.getBoolean("onlyWifi",true);
            chkOnlyWifi = (Switch) findViewById(R.id.chkOnlyWifi);
            chkOnlyWifi.setOnCheckedChangeListener(this);
            if(onlyWifi){
                chkOnlyWifi.setChecked(true);
            }else{
                chkOnlyWifi.setChecked(false);
            }
            spnTimeInterval =(Spinner) findViewById(R.id.spnTimeInterval);
            listaTimes=getResources().getStringArray(R.array.time_values);
            String timeInterval = ajustes.getString("timeInterval", listaTimes[0].trim());
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.time, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnTimeInterval.setAdapter(adapter);
            spnTimeInterval.setOnItemSelectedListener(this);

            int cont=0;
            for(int x=0;x<listaTimes.length-1;x++){
                cont=x;
                if(timeInterval.equals(listaTimes[x])){
                    break;
                }
            }
            spnTimeInterval.setSelection(cont);
        }catch(Exception e){}

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editorAjustes=ajustes.edit();
        if(buttonView.getId()== R.id.notificacionChkNews){
            if(buttonView.isChecked()){
                editorAjustes.putBoolean("notificacionesNews",true);
            }else{
                editorAjustes.putBoolean("notificacionesNews",false);
            }
        }else if(buttonView.getId()== R.id.chkOnlyWifi){
            if(buttonView.isChecked()){
                editorAjustes.putBoolean("onlyWifi",true);
            }else{
                editorAjustes.putBoolean("onlyWifi",false);
            }
        }
        editorAjustes.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Spinner spinner = (Spinner) adapterView;
        editorAjustes=ajustes.edit();
        if(!"".equals(listaTimes[i].trim())) {
            editorAjustes.putString("timeInterval", listaTimes[i].trim());
            editorAjustes.commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

        }else if(id == R.id.nav_share){
            /*ShareActionProvider mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(getIntent());
            }*/
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutConfig);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}