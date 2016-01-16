package com.example.samdhani.wifilisttrial;

import android.content.Intent;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    Button wifiButton;
    Button mapButton;
    TextView wifilist;
    static List<String> ssids;     //To store the SSIDs of the LPS networks filtered form the rest
    Trilateration triObj = new Trilateration();
    List<ScanResult> initialresults;
    List<ScanResult> results;
    List<ScanResult> lpsResults = null;
    String[] xc = new String[5];
    String[] yc = new String[5];
    double[] freq = new double[5];
    double[] ss    =   new double[5];
    static double[] dist  =   new double[5];
    static double[] x  =   new double[5];
    static double[] y = new double[5];
    //for storing the parameters like coordinates, signal strength and distances of the TOP5 LPS sources having highest signal strength

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifilist = (TextView)findViewById(R.id.textView);
        wifiButton = (Button)findViewById(R.id.button);
        mapButton = (Button)findViewById(R.id.button2);
        ssids = new ArrayList<String>();
        final Intent openMaps = new Intent("android.intent.action.MAPSACTIVITY");

        mapButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ssids.size()>0)  //check if LPS based hotspots are found
                    startActivity(openMaps);

                else
                    wifilist.setText("No networks found. Click the 'Get WifiList' button");

            }
        });


        wifiButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                wifilist.setText("");
                ssids.clear();  //clears ssids list for every click so that list doesn't accumulate
                getWifiList();  //calls the getWifiLIst method which scans for all available networks and filters LPS networks
            }
        });
    }

    public void getWifiList(){
        String connectivity_context = Context.WIFI_SERVICE;
        final WifiManager wifi = (WifiManager)getSystemService(connectivity_context);
        initialresults = wifi.getScanResults();    //Imports all the available wifi networks into the list.
        results = sort(initialresults);    //sorts all available networks based on the signal strength.

        wifi.startScan();



        if (results!=null) {
            int i = 0;  //To keep track of the index of the SSIDs associated parameters
            for(ScanResult result:results){
                if (result.SSID.startsWith("lps")&& i < 5 && result.SSID.contains("x") && result.SSID.contains("y")) {
                    //filters the lps networks from the rest
                    ssids.add(result.SSID);
                    xc[i] = result.SSID.substring(result.SSID.indexOf("x")+1,result.SSID.indexOf("y"));
                    yc[i] = result.SSID.substring(result.SSID.indexOf("y")+1);
                    x[i] = Double.parseDouble(xc[i]);
                    y[i] = Double.parseDouble(yc[i]);
                    freq[i]  = result.frequency;
                    ss[i] = result.level;
                    double exp = (27.55 - (20 * Math.log10(result.frequency)) + Math.abs(result.level)) / 20.0;
                    dist[i] = Math.pow(10.0,exp);
                    wifilist.append(result.SSID + " ("+x[i]+","+y[i]+")\n");

                    Log.v("LPS", result.SSID + " " + x[i] + " " + y[i]);
                    i++;
                }
            }
            if(ssids.size()>=3){
                //code for accurate trilateration
                Vector3D yourLocation = triObj.myTrilateration(x[0],y[0],x[1],y[1],x[2],y[2],dist[0],dist[1],dist[2]);
                if(yourLocation!=null){
                    wifilist.append("\n Your Location is : ("+yourLocation.x+","+yourLocation.y+")");
                }
                else{
                    //code if accurate trilateration didnt work
                }
            }
            else{
                wifilist.append("At least 3 LPS networks required");
            }

        }
        else{
            wifilist.setText("No WIFI networks found");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static List<ScanResult> sort(List<ScanResult> a) {
        int N = a.size();
        for (int i = 1; i < N; i++)
            for (int j = i; j > 0; j--)
                if (a.get(j-1).level <a.get(j).level)
                {
                    ScanResult s = a.get(j);
                    a.set(j,a.get(j-1));
                    a.set(j-1,s);
                }

                else break;
        return a;
    }
}
