package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
    private TextView numPacketsText;
    private Button captureBtn;
    private boolean inProgress;
    private Spinner interfaceSpinner;

    private CmdExec cmdRunnable;
    private String fileOut;
    private int selected = 0;

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
        this.captureBtn = (Button) view.findViewById(R.id.capture_btn);
        this.numPacketsText = (TextView) view.findViewById(R.id.capture_num_page);
        this.interfaceSpinner = (Spinner) view.findViewById(R.id.capture_interface);

        this.numPacketsText.setText("Waiting to start");
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
                fileName = fileName.replaceAll(" ", "_");
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

                String cmd = "tcpdump -w - -U | tee " + filePath + " | tcpdump -r -";
                if(selected > 0){
                    cmd += " -i " + selected;
                }
                printToast(cmd);
                cmds.add(cmd);
                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        numPacketsText.setText("Waiting to start");
                        inProgress = false;
                    }else{
                        if(getActivity() != null){
                            numPacketsText.setText("Capturing...");
                            runCmd(cmds);
                            inProgress = true;
                        }
                    }
                }else{
                    runCmd(cmds);
                    numPacketsText.setText("Capturing...");
                    inProgress = true;
                }
            }
        });

        interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                printToast(position + " " + selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected = -1;
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
            interfaces.add("Default (Any)");
            while ((line = in.readLine()) != null) {
                Log.d(TAG, line);
                if(line.matches("(.*)Up(.*)")){
                    String[] availInterface = line.split("\\[");
                    interfaces.add(availInterface[0]);
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
            interfaces.add("Default (Any)");
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
        Thread packetCapture = new Thread(cmdRunnable);
        packetCapture.start();
    }

    @Override
    public void printResult(final ArrayList<String> result,final long numOfPackets) {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numPacketsText.setText(numOfPackets + " packets");
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
    public void onDestroy() {
        if(cmdRunnable != null){
            cmdRunnable.stopRun();
        }
        super.onDestroy();
    }
}
