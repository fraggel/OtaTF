package es.fraggel.otatf;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class ImageThread extends AsyncTask<String, Void, String> {
    public AsyncResponse delegate = null;
    String ultimaActualizacion = "";

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String updateContenido="";
        InputStream bais =null;
        FileOutputStream fos=null;
        try {
            URL jsonUrl = new URL(params[0].trim());
            fos=new FileOutputStream(Environment.getExternalStorageDirectory()+"/tfota/downloads/"+params[1].trim());


            InputStream is = jsonUrl.openStream();
            long total = 0;

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                total +=len1;

                //publishProgress((int)(total*100/lenghtOfFile));

                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            result="image";

        } catch (Exception ex) {
            result = "TIMEOUT";
            ultimaActualizacion = "";
            try {
                if (bais != null) {
                    bais.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {}
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }
}
