package fyp.com.packetsniffer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.Fragments.DevicesConnected.DeviceConnectFragment;
import fyp.com.packetsniffer.Fragments.NetworkUtils.TraceActivity;
import fyp.com.packetsniffer.Fragments.PacketCapture.PacketAnalysisFragment;
import fyp.com.packetsniffer.Fragments.PacketCapture.PacketCaptureFragment;
import fyp.com.packetsniffer.Fragments.Tab1Fragment;
import fyp.com.packetsniffer.Fragments.UpdateVersion.UpdateVersionFragment;
import fyp.com.packetsniffer.Fragments.WifiInfo.WifiInfoFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MainActivity";
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;
    private boolean mNavListenerReg = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForMultiplePermissions();
        initView(savedInstanceState);

        // Default Fragment when Startup
        if (savedInstanceState == null) {
            navView.getMenu().performIdentifierAction(R.id.scan_devices,0);
        }


    }

    private void initView(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navView = (NavigationView) findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);


        navView.setNavigationItemSelectedListener(this);
        navView.setItemIconTintList(null);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DeviceConnectFragment()).commit();
            //getSupportActionBar().setTitle("Scan Network");
            //navView.setCheckedItem(R.id.scan_devices);
        }
    }

    public void enableViews(final boolean enable, final int mode) {
        if(enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSupportActionBar().setTitle("Scan Network");
                        if(mode == 1){
                            enableViews(false, 1);
                        }else{
                            enableViews(false, 2);
                        }

                        onBackPressed();
                    }
                });
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

        } else if(mode == 1){
            Log.d(TAG, "Do false");
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                    R.string.nav_drawer_open, R.string.nav_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }else if(mode == 2){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableViews(false, 1);
                    onBackPressed();
                }
            });
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
        Log.d(TAG, enable + " " + " Mode" + mode);
    }

    public void printToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch(menuItem.getItemId()){
            case R.id.scan_devices: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DeviceConnectFragment())
                    .commit();
                toolbar.setTitle("Scan Network");
                break;
            case R.id.wifi_details: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new WifiInfoFragment())
                    .commit();
                toolbar.setTitle("Network Details");
                break;
            case R.id.packet_capture: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PacketCaptureFragment())
                    .commit();
                toolbar.setTitle("Packet Capture");
                break;
            case R.id.packet_analysis: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PacketAnalysisFragment())
                    .commit();
                toolbar.setTitle("Packet Analysis");
                break;
            case R.id.test5: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UpdateVersionFragment())
                    .commit();
                toolbar.setTitle("Packet Capture");
                    break;
            case R.id.test6: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TraceActivity())
                    .commit();
                toolbar.setTitle("Trace Route");
        }

        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    public void askForMultiplePermissions(){
        final int REQUEST_CODE = 13;
        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
        String accessWifi = Manifest.permission.ACCESS_WIFI_STATE;
        String changeWifi = Manifest.permission.CHANGE_WIFI_STATE;
        String internet = Manifest.permission.INTERNET;
        String accessNetwork = Manifest.permission.ACCESS_NETWORK_STATE;
        String readExternal = Manifest.permission.READ_EXTERNAL_STORAGE;
        String writeExternal = Manifest.permission.WRITE_EXTERNAL_STORAGE;


        List<String> permissionList = new ArrayList<>();

        if(!hasPermission(readExternal)){
            permissionList.add(readExternal);
        }

        if(!hasPermission(accessNetwork)){
            permissionList.add(accessNetwork);
        }

        if(!hasPermission(writeExternal)){
            permissionList.add(writeExternal);
        }

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
}