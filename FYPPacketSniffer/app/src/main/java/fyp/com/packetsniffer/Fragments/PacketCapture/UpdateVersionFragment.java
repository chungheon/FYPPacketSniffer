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

    private TextView pageText;
    private Button updateBtn;
    private Spinner optionSpinner;
    private RecyclerView output;
    private int selected = 0;
    private UpdateVersionFragment mFragment = this;
    private CmdExecError cmdRunnable;

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
        pageText = (TextView) view.findViewById(R.id.page_text);
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
        this.pageText.setText("0/0");
    }

    private void initListener(){
        output.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    long totalpages = Integer.parseInt(pageInfo[1]) * 1050;
                    if(movement <= totalpages && pageInfo.length == 3){
                        movement += dx;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText((((movement + 200)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }
                    if(movement > totalpages){
                        movement = totalpages;
                    }
                } else {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if((movement-1050)<= (dx * -1)){
                        movement = 1050;
                    }else{
                        movement += dx;
                        if(pageInfo.length == 3){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 600)/1050)) + "/" + pageInfo[1] + "/" + pageInfo[2]);
                                }
                            });
                        }
                    }

                    if(movement == 1050 && pageInfo.length == 3){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText("1/" + pageInfo[1] + "/" + pageInfo[2]);
                            }
                        });
                    }

                }
            }
        });

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

                String dirPath = "/data/data/" + getActivity().getPackageName() + "/MyFiles";
                File dir = new File(dirPath);
                if(!dir.exists()){
                    dir.mkdirs();
                }

                if(selected < 0){
                    printToast("Please select one of the choices");
                    return;
                }else if(selected == 0){

                    try {
                        InputStream is = getActivity().getAssets().open("tcpdump_arm");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);

                        File targetFile = new File(dirPath + "/tcpdump");
                        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                        fileOutputStream.write(buffer);

                        fileOutputStream.close();
                    } catch (IOException e) { }
                }else if(selected == 1){
                    try {
                        InputStream is = getActivity().getAssets().open("tcpdump_x86");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);

                        File targetFile = new File(dirPath + "/tcpdump");
                        FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                        fileOutputStream.write(buffer);

                        fileOutputStream.close();
                    } catch (IOException e) { }
                }

                cmds.add("mount -o rw,remount /system");
                cmds.add("chmod 777 " +  dirPath + "/tcpdump");
                cmds.add("cp " + dirPath + "/tcpdump /system/xbin/tcpdump");
                cmds.add("rm " + dirPath + "/tcpdump");
                cmds.add("tcpdump --version");
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
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    mRVAdapter.notifyItemRangeInserted(prevSize, 5);
                    String text = pageInfo[0] + "/" + result.size();
                    pageText.setText(text);
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