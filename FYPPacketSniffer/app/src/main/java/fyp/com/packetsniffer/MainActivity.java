package fyp.com.packetsniffer;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fyp.com.packetsniffer.Fragments.WifiInfo.WifiInfoFragment;
import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.Fragments.DevicesConnected.DeviceConnectFragment;
import fyp.com.packetsniffer.Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForMultiplePermissions();
        initView();
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new WifiInfoFragment(), "Wifi Information");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        View view1 = getLayoutInflater().inflate(R.layout.tab_one, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.packet_capture);
        tabLayout.getTabAt(0).setCustomView(view1);
    }

    public void askForMultiplePermissions(){
        final int REQUEST_CODE = 13;
        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessCoarseLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessWifi = Manifest.permission.ACCESS_WIFI_STATE;
        String changeWifi = Manifest.permission.CHANGE_WIFI_STATE;
        String internet = Manifest.permission.INTERNET;
        String accessNetwork = Manifest.permission.ACCESS_NETWORK_STATE;

        List<String> permissionList = new ArrayList<>();

        if(!hasPermission(accessNetwork)){
            permissionList.add(accessNetwork);
        }

        if(!hasPermission(changeWifi)){
            permissionList.add(changeWifi);
        }
        if (!hasPermission(accessFineLocation)){
            permissionList.add(accessFineLocation);
        }

        if (!hasPermission(accessWifi)){
            permissionList.add(accessWifi);
        }

        if (!hasPermission(internet)){
            permissionList.add(internet);
        }

        if (!hasPermission(accessCoarseLocation)){
            permissionList.add(accessCoarseLocation);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE);
        }
    }

    public boolean hasPermission(String permission) {
        return  ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 13: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted. Now you can call your method to open camera, fetch contact or whatever
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission was denied.......
                    // You can again ask for permission from here
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }

    public boolean hasPermission(String permission) {
        return  ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void askForMultiplePermissions(){
        final int REQUEST_CODE = 13;
        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessCoarseLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessWifi = Manifest.permission.ACCESS_WIFI_STATE;
        String changeWifi = Manifest.permission.CHANGE_WIFI_STATE;
        String internet = Manifest.permission.INTERNET;
        String accessNetwork = Manifest.permission.ACCESS_NETWORK_STATE;

        List<String> permissionList = new ArrayList<>();

        if(!hasPermission(accessNetwork)){
            permissionList.add(accessNetwork);
        }

        if(!hasPermission(changeWifi)){
            permissionList.add(changeWifi);
        }
        if (!hasPermission(accessFineLocation)){
            permissionList.add(accessFineLocation);
        }

        if (!hasPermission(accessWifi)){
            permissionList.add(accessWifi);
        }

        if (!hasPermission(internet)){
            permissionList.add(internet);
        }

        if (!hasPermission(accessCoarseLocation)){
            permissionList.add(accessCoarseLocation);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 13: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission was granted. Now you can call your method to open camera, fetch contact or whatever
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission was denied.......
                    // You can again ask for permission from here
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }
}