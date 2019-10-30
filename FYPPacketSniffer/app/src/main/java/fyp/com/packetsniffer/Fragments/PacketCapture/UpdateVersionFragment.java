package fyp.com.packetsniffer.Fragments.PacketCapture;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import fyp.com.packetsniffer.R;

public class UpdateVersionFragment extends Fragment implements PacketCaptureInterface {
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
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext().getApplicationContext(),
                R.array.update_choices, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        optionSpinner.setAdapter(adapter);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
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

                if(selected < 0){
                    printToast("Please select one of the choices");
                    return;
                }else if(selected == 0){
                    cmds.add("mount -o rw,remount /system");

                    try {
                        InputStream is = getActivity().getAssets().open("tcpdump_arm");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);

                        File targetFile = new File(dirPath + "/tcpdump");
                        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                        fileOutputStream.write(buffer);

                        is.close();
                        fileOutputStream.close();

                        is = getActivity().getAssets().open("aircrack-ng");
                        buffer = new byte[is.available()];
                        is.read(buffer);
                        targetFile = new File(dirPath + "/aircrack-ng");
                        fileOutputStream = new FileOutputStream(targetFile);
                        fileOutputStream.write(buffer);

                        is.close();
                        fileOutputStream.close();

                        cmds.add("cp " + dirPath + "/aircrack-ng /system/xbin/aircrack-ng");
                        cmds.add("chmod 777 " + "system/xbin/aircrack-ng");
                        cmds.add("rm " + dirPath + "/aircrack-ng");
                        cmds.add("which aircrack-ng");
                        cmds.add("cp " + dirPath + "/tcpdump /system/xbin/tcpdump");
                        cmds.add("chmod 777 " + "system/xbin/tcpdump");
                        cmds.add("rm " + dirPath + "/tcpdump");
                        cmds.add("tcpdump --version");
                    } catch (IOException e) { }
                }else if(selected == 1){
                    try {
                        InputStream is = getActivity().getAssets().open("tcpdump_x86");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);

                        File targetFile = new File(dirPath + "/tcpdump");
                        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                        fileOutputStream.write(buffer);

                        is.close();
                        fileOutputStream.close();
                        cmds.add("cp " + dirPath + "/tcpdump /system/xbin/tcpdump");
                        cmds.add("chmod 777 " + "system/xbin/tcpdump");
                        cmds.add("rm " + dirPath + "/tcpdump");
                        cmds.add("tcpdump --version");
                    } catch (IOException e) { }
                }

                cmds.add("mount -o ro,remount /system");

                if(cmdRunnable != null){
                    cmdRunnable.stopRun();
                    final ArrayList<String> allCmd = cmds;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
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

    @Override
    public void printResult(final ArrayList<String> result, final long numOfPackets) {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    mRVAdapter.bindList(result);
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