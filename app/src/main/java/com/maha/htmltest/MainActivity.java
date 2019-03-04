package com.maha.htmltest;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.maha.htmltest.data.AppDatabase;
import com.maha.htmltest.data.entity.TestTable;
import com.maha.htmltest.utils.NetworkManager;

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
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

   AppDatabase mAppDatabase;

   ArrayList<String> mURLList = new ArrayList<>();

   Button mButton;
   TextView mExeTime, mExeTime_sec, mExeTime_min;
   long startTime;

   private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
   private static final int REQUEST_PERMISSION_SETTING = 101;
   private boolean sentToSettings = false;
   private SharedPreferences permissionStatus;

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );
      setContentView( R.layout.activity_main );

      mAppDatabase = AppDatabase.getDatabase( this );
      mButton = findViewById( R.id.mButton );
      mExeTime = findViewById( R.id.exection_time );
      mExeTime_sec = findViewById( R.id.exection_time_sec );
      mExeTime_min = findViewById( R.id.exection_time_min );

      permissionStatus = getSharedPreferences( "permissionStatus", MODE_PRIVATE );

      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy( policy );


      for( char alphabet = 'a'; alphabet <= 'z'; alphabet++ ) {
         mURLList.add( "http://www.mso.anu.edu.au/~ralph/OPTED/v003/wb1913_" + alphabet + ".html" );
      }

      mExeTime.setText( new StringBuilder().append( "" ).append( 0 ).append( " nano sec" ).toString() );
      mExeTime_sec.setText( new StringBuilder().append( "" ).append( 0 ).append( " sec" ).toString() );
      mExeTime_min.setText( new StringBuilder().append( "" ).append( 0 ).append( " mins" ).toString() );


      //askPermission();

      mButton.setOnClickListener( new View.OnClickListener() {
         @Override
         public void onClick( View view ) {
            new writeToDB().executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, mURLList );
         }
      } );

   }

   private void askPermission() {

      if( ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
         if( ActivityCompat.shouldShowRequestPermissionRationale( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) ) {
            //Show Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
            builder.setTitle( "Need Storage Permission" );
            builder.setMessage( "This app needs storage permission." );
            builder.setPositiveButton( "Grant", new DialogInterface.OnClickListener() {
               @Override
               public void onClick( DialogInterface dialog, int which ) {
                  dialog.cancel();
                  ActivityCompat.requestPermissions( MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, EXTERNAL_STORAGE_PERMISSION_CONSTANT );
               }
            } );
            builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
               @Override
               public void onClick( DialogInterface dialog, int which ) {
                  dialog.cancel();
               }
            } );
            builder.show();
         } else if( permissionStatus.getBoolean( Manifest.permission.WRITE_EXTERNAL_STORAGE, false ) ) {
            //Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
            builder.setTitle( "Need Storage Permission" );
            builder.setMessage( "This app needs storage permission." );
            builder.setPositiveButton( "Grant", new DialogInterface.OnClickListener() {
               @Override
               public void onClick( DialogInterface dialog, int which ) {
                  dialog.cancel();
                  sentToSettings = true;
                  Intent intent = new Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
                  Uri uri = Uri.fromParts( "package", getPackageName(), null );
                  intent.setData( uri );
                  startActivityForResult( intent, REQUEST_PERMISSION_SETTING );
                  Toast.makeText( getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG ).show();
               }
            } );
            builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
               @Override
               public void onClick( DialogInterface dialog, int which ) {
                  dialog.cancel();
               }
            } );
            builder.show();
         } else {
            //just request the permission
            ActivityCompat.requestPermissions( MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, EXTERNAL_STORAGE_PERMISSION_CONSTANT );
         }

         SharedPreferences.Editor editor = permissionStatus.edit();
         editor.putBoolean( Manifest.permission.WRITE_EXTERNAL_STORAGE, true );
         editor.commit();


      } else {
         //You already have the permission, just go ahead.
         proceedAfterPermission();
      }

   }

   private void proceedAfterPermission() {
      if( NetworkManager.isInternetOnCheck( MainActivity.this ) )
         new Download().executeOnExecutor( AsyncTask.THREAD_POOL_EXECUTOR, mURLList );
   }


   public class Download extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
      ProgressDialog dialog;

      @Override
      protected void onPreExecute() {
         dialog = new ProgressDialog( MainActivity.this );
         dialog.setTitle( "Calculating..." );
         dialog.setMessage( "Please wait..." );
         dialog.setIndeterminate( true );
         dialog.show();
      }

      protected ArrayList<String> doInBackground( ArrayList<String>... passing ) {
         ArrayList<String> result = new ArrayList<String>();
         ArrayList<String> passed = passing[ 0 ]; //get passed arraylist

         URL url;
         InputStream is = null;
         BufferedReader br;
         String line;


         for( int i = 0; i < passed.size(); i++ ) {

            Log.e( "Error", passed.get( i ) );
            String[] aFilename = passed.get( i ).split( "_" );

            File filepath = new File( Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES ), aFilename[ 1 ] );
            FileOutputStream fos = null;

            File file = new File( filepath.getAbsolutePath() );
            if( file.exists() ) {

            } else {

               try {
                  fos = new FileOutputStream( filepath );

                  url = new URL( passed.get( i ) );
                  is = url.openStream();  // throws an IOException
                  br = new BufferedReader( new InputStreamReader( is ) );

                  while( ( line = br.readLine() ) != null ) {
                     // System.out.println( line );
                     byte[] buffer = line.getBytes();
                     fos.write( buffer, 0, buffer.length );

                  }
               } catch( MalformedURLException mue ) {
                  mue.printStackTrace();
               } catch( IOException ioe ) {
                  ioe.printStackTrace();
               } finally {
                  try {
                     is.close();
                     fos.close();
                  } catch( IOException ioe ) {
                     // nothing to see here
                  }
               }
            }
         }

         return result; //return result
      }

      protected void onPostExecute( ArrayList<String> result ) {
         dialog.dismiss();
      }
   }


   public class writeToDB extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {
      ProgressDialog dialog;

      @Override
      protected void onPreExecute() {
         dialog = new ProgressDialog( MainActivity.this );
         dialog.setTitle( "Calculating..." );
         dialog.setMessage( "Please wait..." );
         dialog.setIndeterminate( true );
         dialog.show();
         startTime = System.nanoTime();


      }

      protected ArrayList<String> doInBackground( ArrayList<String>... passing ) {
         ArrayList<String> result = new ArrayList<String>();
         ArrayList<String> passed = passing[ 0 ]; //get passed arraylist

         try {
            for( int i = 0; i < passed.size(); i++ ) {
               String[] aFilename = passed.get( i ).split( "_" );

               String aBuffer = "";

               File myFile = new File( Environment.getExternalStoragePublicDirectory(
                       Environment.DIRECTORY_PICTURES ) + "/" + aFilename[ 1 ] );
               FileInputStream fIn = new FileInputStream( myFile );
               BufferedReader myReader = new BufferedReader( new InputStreamReader( fIn ) );
               String aDataRow = "";
               while( ( aDataRow = myReader.readLine() ) != null ) {
                  aBuffer += aDataRow;
               }
               myReader.close();

               TestTable aTable = new TestTable();
               aTable.mValue = aBuffer;
               mAppDatabase.testTable().insert( aTable );
               Log.e( "RowCount", "" + mAppDatabase.testTable().aRowCount() );
            }
         } catch( FileNotFoundException e ) {
            e.printStackTrace();
         } catch( IOException e ) {
            e.printStackTrace();
         }


         return result; //return result
      }

      protected void onPostExecute( ArrayList<String> result ) {
         dialog.dismiss();

         long endTime = System.nanoTime();
         long totalTime = endTime - startTime;
         Log.e( "Runtime", "" + totalTime );
         mExeTime.setText( "" + totalTime + " nano sec" );

         long aSeconds = TimeUnit.NANOSECONDS.toSeconds( totalTime );

         mExeTime_sec.setText( "" + aSeconds + " sec" );

         long aMins = TimeUnit.NANOSECONDS.toMinutes( totalTime );

         mExeTime_min.setText( "" + aMins + " mins" );

      }
   }

   @Override
   public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults ) {
      super.onRequestPermissionsResult( requestCode, permissions, grantResults );
      if( requestCode == EXTERNAL_STORAGE_PERMISSION_CONSTANT ) {
         if( grantResults.length > 0 && grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ) {
            //The External Storage Write Permission is granted to you... Continue your left job...
            //  proceedAfterPermission();
         } else {
            if( ActivityCompat.shouldShowRequestPermissionRationale( MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) ) {
               //Show Information about why you need the permission
               AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
               builder.setTitle( "Need Storage Permission" );
               builder.setMessage( "This app needs storage permission" );
               builder.setPositiveButton( "Grant", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick( DialogInterface dialog, int which ) {
                     dialog.cancel();


                     ActivityCompat.requestPermissions( MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, EXTERNAL_STORAGE_PERMISSION_CONSTANT );


                  }
               } );
               builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick( DialogInterface dialog, int which ) {
                     dialog.cancel();
                  }
               } );
               builder.show();
            } else {
               Toast.makeText( getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG ).show();
            }
         }
      }
   }


   @Override
   protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
      super.onActivityResult( requestCode, resultCode, data );
      if( requestCode == REQUEST_PERMISSION_SETTING ) {
         if( ActivityCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
            //Got Permission
            //  proceedAfterPermission();
         }
      }
   }


   @Override
   protected void onResume() {
      super.onResume();
      askPermission();
   }

   @Override
   protected void onPostResume() {
      super.onPostResume();
      if( sentToSettings ) {
         if( ActivityCompat.checkSelfPermission( MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
            //Got Permission
            //  proceedAfterPermission();
         }
      }
   }


}
