package fyp.com.packetsniffer.Fragments.UpdateVersion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.PacketCapture.RecyclerViewPageAdapter;
import fyp.com.packetsniffer.R;

public class UpdateVersionFragment extends Fragment implements CmdExecInterface {
    private final String TAG = "UpdateVersionFrag";
    private View view;

    private Button updateBtn;
    private Spinner optionSpinner;
    private RecyclerView output;
    private int selected = 0;
    private UpdateVersionFragment mFragment = this;
    private UpdateVersionThread cmdRunnable;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private long movement = 1050;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_update_version, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.output = (RecyclerView) view.findViewById(R.id.result_text);
        this.updateBtn = (Button) view.findViewById(R.id.update_btn);
        optionSpinner = (Spinner) view.findViewById(R.id.ver_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext().getApplicationContext(),
                R.array.update_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        optionSpinner.setAdapter(adapter);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<byte[]>());
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
    }

    private void initListener(){

        optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected = -1;
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();

                String dirPath = getActivity().getFilesDir().getPath() + "/bin";
                File dir = new File(dirPath);
                if(!dir.exists()){
                    dir.mkdirs();
                }

                //For Future Development to cater to both ARM and x86 Architecures
                /*if(selected < 0){
                    printToast("Please select one of the choices");
                    return;
                }else if(selected == 0){*/
                    cmds.add("mount -o rw,remount /system");
                    installAsset("tcpdump_arm", "tcpdump", dirPath, cmds);
                    installAsset("aircrack-ng", "aircrack-ng", dirPath, cmds);
                    installAsset("traceroute_arm", "traceroute", dirPath, cmds);
                    /*For future when nmap suite to be added
                    installAsset("nmap-os-db_arm", "nmap-os-db",dirPath, cmds);
                    installAsset("nmap-services_arm", "nmap-services",dirPath, cmds);
                    installAsset("nmap-payloads_arm", "nmap-payloads",dirPath, cmds);
                    installAsset("nmap-mac-prefixes_arm", "nmap-mac-prefixes",dirPath, cmds);
                    installAsset("nmap_arm", "nmap",dirPath, cmds);

                    cmds.add("/system/xbin/nmap --help | grep \'Nmap 7.31\'");*/
                    cmds.add("/system/xbin/tcpdump --version");
                    cmds.add("/system/xbin/aircrack-ng | grep \'Aircrack-ng 1.2 rc4\'");
                    cmds.add("/system/xbin/traceroute --version | grep \'version\'");
                    cmds.add("mount -o ro,remount /system");
                    cmds.add("exit");

                /*}else if(selected == 1){
                    cmds.add("mount -o rw,remount /system");
                    installAsset("tcpdump_x86", "tcpdump", dirPath, cmds);
                    installAsset("nmap-os-db_x86", "nmap-os-db",dirPath, cmds);
                    installAsset("nmap-services_x86", "nmap-services",dirPath, cmds);
                    installAsset("nmap-payloads_x86", "nmap-payloads",dirPath, cmds);
                    installAsset("nmap-mac-prefixes_x86", "nmap-mac-prefixes",dirPath, cmds);
                    installAsset("nse_main_x86.lua", "nse_main.lua",dirPath, cmds);
                    installAsset("nmap_x86", "nmap",dirPath, cmds);
                    installAsset("traceroute_x86", "traceroute", dirPath, cmds);
                    cmds.add("tcpdump --version");
                    cmds.add("nmap --help | grep \'Nmap 7.31\'");
                    cmds.add("traceroute --version");
                    cmds.add("mount -o ro,remount /system");
                    cmds.add("exit");
                }*/

                if(cmdRunnable != null){
                    cmdRunnable.stopRun();
                    final ArrayList<String> allCmd = cmds;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<byte[]>());
                            output.setAdapter(mRVAdapter);
                            cmdRunnable = new UpdateVersionThread(mFragment, allCmd);
                            Thread update = new Thread(cmdRunnable);
                            update.start();
                        }
                    });
                }else{
                    cmdRunnable = new UpdateVersionThread(mFragment, cmds);
                    Thread update = new Thread(cmdRunnable);
                    update.start();
                }



            }
        });
    }

    public void installAsset(String assetName, String fileName, String dirPath, ArrayList<String> cmds){
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();

            cmds.add("chmod 755 " + targetFile.getPath());
            cmds.add("cp " + targetFile.getPath() + " /system/xbin/" + fileName);
            cmds.add("rm " + targetFile.getPath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if(is != null){
                try {
                    is.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error closing stream");
                }
            }

            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }

    }

    public void installAssetLUA(String assetName, String fileName, String dirPath, ArrayList<String> cmds){
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();

            cmds.add("chmod 755 " + targetFile.getPath());
            cmds.add("cp " + targetFile.getPath() + " /system/xbin/nselib/" + fileName);
            cmds.add("rm " + targetFile.getPath());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if(is != null){
                try {
                    is.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error closing stream");
                }
            }

            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }

    }

    @Override
    public void printResult(final String result, final long numOfPackets) {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    mRVAdapter.addPage(result);
                    mRVAdapter.notifyItemRangeInserted(prevSize, 1);
                }
            });
        }
    }

    @Override
    public void printToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void cmdDone() {

    }
}