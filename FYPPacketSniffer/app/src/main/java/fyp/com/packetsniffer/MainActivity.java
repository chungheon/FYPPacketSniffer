package fyp.com.packetsniffer;

import android.drm.DrmStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import fyp.com.packetsniffer.Fragments.Tab1Fragment;
import fyp.com.packetsniffer.Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);

        NavigationView nv = findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(this);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            Tab1Fragment fragment1 = new Tab1Fragment();
            Bundle args = new Bundle();
            args.putInt("num", 1);
            fragment1.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment1).commit();
            nv.setCheckedItem(R.id.test1);
        }


        /*viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Tab 1");
        adapter.addFragment(new Tab1Fragment(), "");
        adapter.addFragment(new Tab1Fragment(), "Tab 3");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
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
        Tab1Fragment fragment2 = new Tab1Fragment();
        args = new Bundle();
        args.putInt("num", 2);
        fragment2.setArguments(args);
        Tab1Fragment fragment3 = new Tab1Fragment();
        args = new Bundle();
        args.putInt("num", 3);
        fragment3.setArguments(args);
        switch(menuItem.getItemId()){
            case R.id.test1: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment1).commit();
                            break;
            case R.id.test2: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2).commit();
                            break;
            case R.id.test3: getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment3).commit();
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

    }
}