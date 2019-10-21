package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*Device Connect Fragment class
This class is in-charge of the passing live data retrieved to be displayed
on the UI for selecting available records of past scans

 */


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class ScanHistoryFragment extends Fragment {
    private static final String TAG = "ScanHistory"; //Tag used for debugging purposes
    private ArrayList<Pair<String, String>> wifiSSIDs; //List of past scans based on names and mac address
    private RecyclerView wifiList; //RecyclerView to display list of past scans to select
    private View view; //View object reference to pass data to UI

    //Setup the view and listeners for when fragment is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_scan_history, container, false);
        initView();
        displayHistory();
        return this.view;
    }

    //Initialization of all objects and UI components
    private void initView(){
        this.view.setFocusableInTouchMode(true);
        this.view.requestFocus();
        this.view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle("Scan Network");
                    ((MainActivity)getActivity()).enableViews(false, 1);
                }
                Log.d(TAG, keyCode + " pressed");
                return false;
            }
        });

        this.wifiList = (RecyclerView) this.view.findViewById(R.id.recycler_view_history);
        this.wifiSSIDs = new ArrayList<>();
    }

    //Retrieve history of past scans saved in caches
    private void displayHistory(){
        getHistory();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RecyclerViewHistoryAdapter adapter = new RecyclerViewHistoryAdapter(wifiSSIDs,
                        getContext().getApplicationContext(),
                        new RecyclerViewHistoryAdapter.OnItemClickListener(){
                            @Override
                            public void onItemClick(Pair<String, String> item) {
                                String path = getActivity().getFilesDir() + "/ScanHistory/"
                                        + item.first + "_" + item.second;
                                DevicesHistoryFragment devHistory = new DevicesHistoryFragment();
                                Bundle args = new Bundle();
                                args.putString("Filename", path);
                                devHistory.setArguments(args);
                                ((MainActivity)getActivity()).getSupportActionBar().setTitle(item.first);
                                ((MainActivity)getActivity()).enableViews(true, 2);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .addToBackStack(null)
                                        .replace(R.id.fragment_container, devHistory, "DevicesHistory")
                                        .commit();
                            }
                        });
                wifiList.setAdapter(adapter);
                wifiList.setLayoutManager(new LinearLayoutManager(getActivity()));
            }
        });
    }

    //Read cache containing past scans
    private void getHistory(){
        String path = getActivity().getFilesDir().toString() + "/ScanHistory";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++)
            {
                String[] wifiSSID = files[i].getName().split("_");
                wifiSSIDs.add(new Pair<String,String>(wifiSSID[0], wifiSSID[1]));
            }
        }
    }


}
