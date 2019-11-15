package fyp.com.packetsniffer.Fragments.DevicesConnected;

/*Device History Fragment class
This class is in-charge of the passing live data retrieved to be displayed
on the UI for purpose of viewing pass scan results

 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class DevicesHistoryFragment extends Fragment {

    private final String TAG = "DeviceHistoryFragment"; //Tag used for debugging purposes
    private ArrayList<DeviceInformation> devices; //List of Devices found global variable
    private RecyclerView listOfDevices; //RecyclerView to display Devices information

    private View view; //View object reference to pass data to UI

    //Setup the view and listeners for when fragment is created
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_device_history, container, false);
        final Bundle args = getArguments();


        String path = "";
        try
        {
            path = args.getString("Filename");
            initView(path);
            initListener();
        }
        catch(final Exception e) {
            Toast.makeText(getContext().getApplicationContext(),
                    "An Error has occurred unable to continue", Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
        }

        return this.view;
    }

    //Initialization of all objects and UI components
    private void initView(String path){
        devices = new ArrayList<>();

        listOfDevices = (RecyclerView) this.view.findViewById(R.id.recycler_view_devices);

        Log.d(TAG, "File path: " + path);
        readFromFile(path);

        RecyclerViewDeviceAdapter deviceAdapter = new RecyclerViewDeviceAdapter(devices);
        listOfDevices.setAdapter(deviceAdapter);
        listOfDevices.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    //Set back press button
    private void initListener(){
        this.view.setFocusableInTouchMode(true);
        this.view.requestFocus();
        this.view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle("Scan History");
                    ((MainActivity)getActivity()).enableViews(true, 1, "Scan History");
                }
                return false;
            }
        });
    }

    //Read stored cache of past scans
    private void readFromFile(String path){
        File file = new File(path);
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File Not Found");
            return;
        }

        String line;
        try {
            br.readLine();
            br.readLine();
            while(((line = br.readLine()) != null)){
                Log.d(TAG, "LINE: " + line);
                String[] info = line.split(",");
                DeviceInformation dev = new DeviceInformation();
                dev.setHostName(info[0]);
                dev.setIpAddrs(info[1]);
                dev.setMacAddrs(info[2]);
                dev.setMacVendor(info[3]);
                devices.add(dev);
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading from file");
            return;
        }

    }
}
