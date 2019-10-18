package fyp.com.packetsniffer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.Fragments.PacketCapture.PacketAnalysisFragment;

import fyp.com.packetsniffer.Fragments.PacketCapture.TestFragment;
import fyp.com.packetsniffer.Fragments.PacketCapture.UpdateVersionFragment;
import fyp.com.packetsniffer.Fragments.Tab1Fragment;
import fyp.com.packetsniffer.Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForMultiplePermissions();
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        NavigationView navView = findViewById(R.id.nav_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);


        navView.setNavigationItemSelectedListener(this);
        navView.setItemIconTintList(null);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PacketAnalysisFragment()).commit();
            navView.setCheckedItem(R.id.test1);
        }

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
        Tab1Fragment fragment1 = new Tab1Fragment();
        Bundle args = new Bundle();
        args.putInt("num", 1);
        fragment1.setArguments(args);
        switch(menuItem.getItemId()){
            case R.id.test1: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment1)
                    .commit();
                toolbar.setTitle("Scan Network");
                break;
            case R.id.test2: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TestFragment())
                    .commit();
                toolbar.setTitle("Network Details");
                break;
            case R.id.packet_capture: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new UpdateVersionFragment())
                    .commit();
                toolbar.setTitle("Device Records Store");
                break;
            case R.id.packet_analysis: getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PacketAnalysisFragment())
                    .commit();
                toolbar.setTitle("Device Records Store");
                break;

        }

        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    public void askForMultiplePermissions(){
        final int REQUEST_CODE = 13;
        String accessFineLocation = Manifest.permission.ACCESS_FINE_LOCATION;
        String accessCoarseLocation = Manifest.permission.ACCESS_FINE_LOCATION;
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