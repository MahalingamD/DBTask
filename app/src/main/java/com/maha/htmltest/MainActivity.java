package com.maha.htmltest;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.maha.htmltest.data.AppDatabase;
import com.maha.htmltest.data.entity.TestTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    AppDatabase mAppDatabase;

    ArrayList<String> mURLList = new ArrayList<>();

    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppDatabase = AppDatabase.getDatabase(this);
        mButton = findViewById(R.id.mButton);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        File filepath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "test.html");
        FileOutputStream fos = null;

        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;


        //int i = 0;
        for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {

            mURLList.add("http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_" + alphabet + ".html");
         //   System.out.println(mURLList.get(i));
           // i++;
        }





        // new Download().execute(mURLList);
        new Download().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mURLList);

        File direct = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "a.html");


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new writeToDB().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mURLList);
            }
        });


        // file_download("http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_a.html");

       /* try {
            fos = new FileOutputStream(filepath);

            url = new URL("http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_a.html");
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                byte[] buffer = line.getBytes();
                fos.write(buffer, 0, buffer.length);

            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                is.close();
                fos.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
*/

    }


    public void file_download(String uRl) {
       /* File direct = new File(Environment.getExternalStorageDirectory()
                + "/dhaval_files");*/
        File direct = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "a");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/a", "a.html");

        mgr.enqueue(request);

    }


    public class Download extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Calculating...");
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(true);
            dialog.show();
        }

        protected ArrayList<String> doInBackground(ArrayList<String>... passing) {
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> passed = passing[0]; //get passed arraylist

            //Some calculations...

            URL url;
            InputStream is = null;
            BufferedReader br;
            String line;


            for (int i = 0; i < passed.size(); i++) {

                Log.e("Error", passed.get(i));
                String[] aFilename = passed.get(i).split("_");

                File filepath = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), aFilename[1]);
                FileOutputStream fos = null;

                File file = new File(filepath.getAbsolutePath());
                if (file.exists()) {

                } else {

                    try {
                        fos = new FileOutputStream(filepath);

                        url = new URL(passed.get(i));
                        is = url.openStream();  // throws an IOException
                        br = new BufferedReader(new InputStreamReader(is));

                        while ((line = br.readLine()) != null) {
                            System.out.println(line);
                            byte[] buffer = line.getBytes();
                            fos.write(buffer, 0, buffer.length);

                        }
                    } catch (MalformedURLException mue) {
                        mue.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {
                        try {
                            is.close();
                            fos.close();
                        } catch (IOException ioe) {
                            // nothing to see here
                        }
                    }
                }
            }

            return result; //return result
        }

        protected void onPostExecute(ArrayList<String> result) {
            dialog.dismiss();

            List<TestTable> testTables= mAppDatabase.testTable().aList();
            for(int i=0;i<testTables.size();i++){
                TestTable aTestTable=testTables.get(i);

                Log.e("Print",aTestTable.mValue);
            }
        }
    }


    public class writeToDB extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Calculating...");
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(true);
            dialog.show();
        }

        protected ArrayList<String> doInBackground(ArrayList<String>... passing) {
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> passed = passing[0]; //get passed arraylist

            try {
                for (int i = 0; i < passed.size(); i++) {
                    String[] aFilename = passed.get(i).split("_");

                    String aBuffer = "";

                    File myFile = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES) +"/"+ aFilename[1]);
                    FileInputStream fIn = new FileInputStream(myFile);
                    BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                    String aDataRow = "";
                    while ((aDataRow = myReader.readLine()) != null) {
                        aBuffer += aDataRow;
                    }
                    myReader.close();

                    TestTable aTable = new TestTable();
                    aTable.mValue = aBuffer;
                    mAppDatabase.testTable().insert(aTable);
                    Log.e("RowCount", "" + mAppDatabase.testTable().aRowCount());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

         /*   Log.e("Read", aBuffer);
            TestTable aTable = new TestTable();
            aTable.mValue = aBuffer;
            mAppDatabase.testTable().insert(aTable);
            Log.e("RowCount", "" + mAppDatabase.testTable().aRowCount()); */

            return result; //return result
        }

        protected void onPostExecute(ArrayList<String> result) {
            dialog.dismiss();

        }
    }


}
