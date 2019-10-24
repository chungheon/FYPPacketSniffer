package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment implements PacketCaptureInterface {
    private static final String TAG = "PacketCapture";
    private View view;
    private EditText fileOutput;
    private TextView pageText;
    private Button captureBtn;
    private Button refreshBtn;
    private RecyclerView captureOutput;
    private boolean inProgress;
    private Spinner interfaceSpinner;

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;

    private CmdExec cmdRunnable;
    private long movement = 1050;
    private String fileOut;
    private boolean background;

    private List<String> interfaces;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.fileOutput = (EditText) view.findViewById(R.id.capture_file_name);
        this.captureOutput = (RecyclerView) view.findViewById(R.id.capture_output);
        this.captureBtn = (Button) view.findViewById(R.id.capture_btn);
        this.pageText = (TextView) view.findViewById(R.id.capture_num_page);
        this.refreshBtn = (Button) view.findViewById(R.id.capture_refresh_pages);
        this.interfaceSpinner = (Spinner) view.findViewById(R.id.capture_interface);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        captureOutput.setAdapter(mRVAdapter);
        captureOutput.setLayoutManager(mLayoutManager);
        ViewCompat.setNestedScrollingEnabled(captureOutput, true);
        this.pageText.setText("0/0");
        inProgress = false;
        interfaces = new ArrayList<String>();
        updateSpinner();
    }

    private void initListener(){

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                String fileName = fileOutput.getText().toString();
                if(fileName.equals("")){
                    fileName = "PCAP.pcap";
                }
                String filePath = Environment.getExternalStorageDirectory().toString() +
                        "/" + fileName;
                File file = new File(filePath);
                fileOut = fileName;
                int count = 1;
                while(file.exists()){
                    filePath = Environment.getExternalStorageDirectory().toString() +
                            "/" + count + "_" + fileName;
                    fileOut = count + "_" + fileName;
                    count++;
                    file = new File(filePath);

                }

                cmds.add("tcpdump -w - -U | tee " + filePath + " | tcpdump -r - -tttt");
                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        inProgress = false;
                    }else{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
                                captureOutput.setAdapter(mRVAdapter);
                                pageText.setText("0/0");
                            }
                        });
                        runCmd(cmds);
                        inProgress = true;
                    }
                }else{
                    runCmd(cmds);
                    inProgress = true;
                }
            }
        });

        captureOutput.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPages();
            }
        });
    }

    private void updateSpinner(){

        BufferedReader in = null;
        try {
            String[] cmds = {"su", "-c", "tcpdump -D"};
            Process process = Runtime.getRuntime().exec(cmds);
            process.waitFor();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                Log.d(TAG, line);
                if(line.matches("(.*)Up(.*)")){
                    String[] availInterface = line.split("\\[");
                    interfaces.add(availInterface[0].substring(0,availInterface[0].length()-2));
                }
            }



            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            interfaces.clear();
            if(in != null){
                try{
                    in.close();
                } catch (IOException ex) { }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            interfaces.clear();
            if(in != null){
                try{
                    in.close();
                } catch (IOException ex) { }
            }
        }

        if(interfaces.isEmpty()){
            interfaces.add("-");
            ArrayAdapter<String> adp = new ArrayAdapter<String>(getContext().getApplicationContext()
                    ,android.R.layout.simple_spinner_dropdown_item,interfaces);
            interfaceSpinner.setAdapter(adp);
        }else{
            ArrayAdapter<String> adp = new ArrayAdapter<String>(getContext().getApplicationContext()
                    ,android.R.layout.simple_spinner_dropdown_item,interfaces);
            interfaceSpinner.setAdapter(adp);
        }
    }

    private void runCmd(ArrayList<String> args){
        cmdRunnable = new CapturePacketThread(this, args);
        Thread test = new Thread(cmdRunnable);
        test.start();
    }

    @Override
    public void printResult(final ArrayList<String> result,final long numOfPackets) {
        if(getActivity() != null && !background){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    int diff = result.size() - prevSize;
                    mRVAdapter.bindList(result);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    mRVAdapter.notifyItemRangeInserted(prevSize, 1);
                    pageText.setText(pageInfo[0] + "/" + result.size() + "/" + numOfPackets + " packets");

                }
            });
        }
    }


    @Override
    public void printToast(final String message) {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void cmdDone() {
        printToast("Packets saved to " + fileOut);
    }

    @Override
    public void onPause() {
        background = true;
        super.onPause();
    }

    @Override
    public void onResume() {
        background = false;
        super.onResume();
    }

    private void resetPages(){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    captureOutput.setAdapter(mRVAdapter);
                    captureOutput.setLayoutManager(mLayoutManager);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if(pageInfo.length > 3){
                        pageText.setText("1/" + mRVAdapter.getItemCount() + "/" + pageInfo[2]);
                    }
                    movement = 1050;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if(cmdRunnable != null){
            cmdRunnable.stopRun();
        }
        super.onDestroy();
    }
}
