package fyp.com.packetsniffer;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import fyp.com.packetsniffer.Fragments.Tab1Fragment;
import fyp.com.packetsniffer.Fragments.TabAdapter;

public class MainActivity extends AppCompatActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), "Tab 1");
        adapter.addFragment(new Tab1Fragment(), "");
        adapter.addFragment(new Tab1Fragment(), "Tab 3");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        View view1 = getLayoutInflater().inflate(R.layout.tab_one, null);
        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.packet1);
        tabLayout.getTabAt(0).setCustomView(view1);
        View view2 = getLayoutInflater().inflate(R.layout.tab_one, null);
        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.packet2);
        tabLayout.getTabAt(1).setCustomView(view2);
    }
}