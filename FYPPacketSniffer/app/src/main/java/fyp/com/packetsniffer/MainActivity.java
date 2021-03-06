package fyp.com.packetsniffer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.Fragments.Aircrack.AircrackFragment;
import fyp.com.packetsniffer.Fragments.DevicesConnected.DeviceConnectFragment;
import fyp.com.packetsniffer.Fragments.NetworkUtils.NetworkUtilsFragment;
import fyp.com.packetsniffer.Fragments.PacketCapture.PacketAnalysisFragment;
import fyp.com.packetsniffer.Fragments.PacketCapture.PacketCaptureFragment;
import fyp.com.packetsniffer.Fragments.UpdateVersion.UpdateVersionFragment;
import fyp.com.packetsniffer.Fragments.WifiInfo.WifiInfoFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MainActivity";
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;

    private ArrayList<byte[]> result = new ArrayList<>();
    private Long numPackets = new Long(0);
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForMultiplePermissions();
        initView(savedInstanceState);
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
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutUsFragment()).commit();
            getSupportActionBar().setTitle("About App");
        }
    }

    public void enableViews(final boolean enable, final int mode, final String title, final String backTitle) {
        if(enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getSupportActionBar().setTitle(title);
                        if(mode == 1){
                            enableViews(false, 1, "", "");
                        }else{
                            enableViews(false, 2, backTitle, "");
                        }

                        onBackPressed();
                    }
                });
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

        } else if(mode == 1){
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
                    getSupportActionBar().setTitle(title);
                    enableViews(false, 1, "", "");
                    onBackPressed();
                }
            });
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }

    public void printToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
    }

    public void storeResult(ArrayList<byte[]> result, Long numPackets){
        this.result = result;
        this.numPackets = numPackets;
    }

    public void clearResult(){
        if(this.result != null){
            this.result.clear();
        }
        if(numPackets != null){
            numPackets = new Long(0);
        }
    }

    public ArrayList<byte[]> getResult(){
        return this.result;
    }

    public Long getNumPackets(){
        return this.numPackets;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            if(getSupportFragmentManager().getBackStackEntryCount() != 0){
                super.onBackPressed();
            }else{
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
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
            case R.id.update_libraries: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UpdateVersionFragment())
                    .commit();
                toolbar.setTitle("Update Libraries");
                    break;
            case R.id.network_utils:
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NetworkUtilsFragment())
                    .commit();
                toolbar.setTitle("Network Utils");
                break;
            case R.id.aircrack_suite:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AircrackFragment())
                        .commit();
                toolbar.setTitle("Aircrack-ng");
                break;
            case R.id.about_us:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AboutUsFragment())
                        .commit();
                toolbar.setTitle("About App");
                break;
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