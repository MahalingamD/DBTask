//---------------------------------------------------------------------------
//
// file. TPINetworkManager.java
//
// copyright.
// Sundharam Motors.
//
// authors.
// Jayaprakash S
//
// note.
// The file contains the implementation of network checking process.
//
// description.
//  * The purpose of this class is to check whether the network available r not. 
//  
//---------------------------------------------------------------------------

package com.maha.htmltest.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

public class NetworkManager {

   /**
    * Check the internet connection and return true or false
    *
    * @param aContext Context
    * @return boolean
    */

   public final boolean isInternetOn( Context aContext ) {
      try {

         try {

            if( aContext == null )
               return false;

            ConnectivityManager connectivityManager = ( ConnectivityManager ) aContext
                    .getSystemService( Context.CONNECTIVITY_SERVICE );
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null;
         } catch( Exception e ) {
            e.printStackTrace();
            return false;
         }
      } catch( Exception e ) {
         e.printStackTrace();
         return false;
      }
   }

   public static boolean isInternetOnCheck( AppCompatActivity aContext ) {

      boolean aResult = false;

      ConnectivityManager aConnecMan = ( ConnectivityManager ) aContext.getSystemService( Context.CONNECTIVITY_SERVICE );
      //
      if( ( aConnecMan.getNetworkInfo( 0 ).getState() == NetworkInfo.State.CONNECTED )
              || ( aConnecMan.getNetworkInfo( 0 ).getState() == NetworkInfo.State.CONNECTING )
              || ( aConnecMan.getNetworkInfo( 1 ).getState() == NetworkInfo.State.CONNECTING )
              || ( aConnecMan.getNetworkInfo( 1 ).getState() == NetworkInfo.State.CONNECTED ) ) {

         aResult = true;

      } else if( ( aConnecMan.getNetworkInfo( 0 ).getState() == NetworkInfo.State.DISCONNECTED )
              || ( aConnecMan.getNetworkInfo( 1 ).getState() == NetworkInfo.State.DISCONNECTED ) ) {

         aResult = false;
      }

      return aResult;
   }
}