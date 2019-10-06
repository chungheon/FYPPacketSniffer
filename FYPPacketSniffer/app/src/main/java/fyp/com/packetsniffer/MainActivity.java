package fyp.com.packetsniffer;

<<<<<<< HEAD
import android.drm.DrmStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
=======
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
>>>>>>> DevicesConnected
import android.support.v4.view.ViewPager;
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

import fyp.com.packetsniffer.Fragments.DevicesConnected.DeviceConnectFragment;

import fyp.com.packetsniffer.Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
<<<<<<< HEAD

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
            Tab1Fragment fragment1 = new Tab1Fragment();
            Bundle args = new Bundle();
            args.putInt("num", 1);
            fragment1.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment1).commit();
            navView.setCheckedItem(R.id.test1);
        }


        /*viewPager = (ViewPager) findViewById(R.id.viewPager);
=======
        askForMultiplePermissions();
        initView();
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);
>>>>>>> DevicesConnected
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new DeviceConnectFragment(), "Devices Connected");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
<<<<<<< HEAD
        tabLayout.getTabAt(1).setIcon(R.drawable.packet_capture);
        View view1 = getLayoutInflater().inflate(R.layout.tab_one, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.packet_capture);
        tabLayout.getTabAt(0).setCustomView(view1);*/
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Tab1Fragment fragment1 = new Tab1Fragment();
        Bundle args = new Bundle();
        args.putInt("num", 1);
        fragment1.setArguments(args);
        switch(menuItem.getItemId()){
            case R.id.test1: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment1).commit();
                            toolbar.setTitle("Scan Network");
                            break;
            case R.id.test2: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Tab1Fragment()).commit();
                             toolbar.setTitle("Network Details");
                            break;
            case R.id.test3: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Tab1Fragment()).commit();
                             toolbar.setTitle("Device Records Store");
                            break;

        }

        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

=======
        View view1 = getLayoutInflater().inflate(R.layout.layout_tab, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.deviceconn2);
        tabLayout.getTabAt(0).setCustomView(view1);
>>>>>>> DevicesConnected
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
}