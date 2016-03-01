package es.fraggel.otatf;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionThread extends AsyncTask<String, Void, String> {
    public AsyncResponse delegate = null;
    String ultimaActualizacion = "";

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String updateContenido="";
        InputStreamReader isr = null;
        BufferedReader in = null;
        try {
            URL jsonUrl = new URL("http://www.tfandroid.es/desarrollo/ota_check.php?device="+params[0]+"&vendor="+params[1]+"&rom="+params[2]+"&version="+params[3]+"&idioma="+params[4]);
            in = new BufferedReader(new InputStreamReader(jsonUrl.openStream(),"ISO885915"));
            result = in.readLine();

            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            result = "TIMEOUT";
            ultimaActualizacion = "";
            try {
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
            } catch (Exception e) {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
