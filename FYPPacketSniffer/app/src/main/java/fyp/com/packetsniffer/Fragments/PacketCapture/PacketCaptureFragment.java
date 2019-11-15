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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecNormal;
import fyp.com.packetsniffer.Fragments.DirectoryChooser;
import fyp.com.packetsniffer.Fragments.FileChooser;
import fyp.com.packetsniffer.R;

public class PacketCaptureFragment extends Fragment implements CmdExecInterface {
    private static final String TAG = "PacketCapture";
    private View view;
    private EditText fileOutput;

    private TextView numPacketsText;
    private TextView dirText;
    private Button dirBtn;
    private Button captureBtn;
    private boolean inProgress;
    private Spinner interfaceSpinner;

    private CmdExecNormal cmdRunnable;
    private String fileOut;
    private int selected = 0;
    private String selectedFile = "";
    private DirectoryChooser directoryChooser;
    private String dirPath = "";

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
        this.dirBtn = (Button) view.findViewById(R.id.capture_directory_btn);
        this.dirText = (TextView) view.findViewById(R.id.capture_directory_text);

        this.numPacketsText.setText("Waiting to start");
        inProgress = false;
        interfaces = new ArrayList<String>();
        dirText.setText("External Storage");
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
                    fileName = "PCAP";
                }
                fileName += ".pcap";
                if (dirPath.equals("")) {
                    dirPath = Environment.getExternalStorageDirectory().toString();
                }
                String filePath = dirPath +
                        "/" + fileName;
                File file = new File(filePath);
                fileOut = fileName;
                int count = 0;
                while(file.exists()){
                    count++;
                    filePath = dirPath +
                            "/" + count + "_" + fileName;
                    fileOut = count + "_" + fileName;
                    file = new File(filePath);
                }

                String cmd = "tcpdump";
                if(selected > -1) {
                    cmd += " -i " + (selected + 1);
                }
                cmd += " -w - | tee " +  filePath + " | tcpdump -tttt -r -";

                cmds.add(cmd);
                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        inProgress = false;
                        captureBtn.setText("Start Capture");
                        if(numPacketsText.getText().toString().equals("Capturing...")){
                            numPacketsText.setText("0 Packets");
                            interfaceSpinner.setEnabled(true);
                            fileOutput.setEnabled(true);
                        }
                    }else{
                        numPacketsText.setText("Capturing...");
                        runCmd(cmds, filePath);
                        inProgress = true;
                        captureBtn.setText("Stop Capturing");
                        interfaceSpinner.setEnabled(false);
                        fileOutput.setEnabled(false);
                    }
                }else{
                    runCmd(cmds, filePath);
                    numPacketsText.setText("Capturing...");
                    inProgress = true;
                    captureBtn.setText("Stop Capturing");
                    interfaceSpinner.setEnabled(false);
                    fileOutput.setEnabled(false);
                }
            }
        });

        interfaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selected = -1;
            }
        });

        dirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directoryChooser = new DirectoryChooser(getActivity());
                directoryChooser.setDirectoryListener(new DirectoryChooser.DirectorySelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        dirText.setText(file.getName());
                        dirPath = file.getPath();
                        printToast("Selected Directory: " + file.getPath());
                    }
                });
                directoryChooser.showDialog();
            }
        });
    }

    /*


    */
    private void updateSpinner(){

        BufferedReader in = null;
        DataOutputStream outputStream = null;
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("su");
            p = pb.start();
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            outputStream = new DataOutputStream(p.getOutputStream());
            outputStream.writeBytes("tcpdump -D\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            String line;
            while ((line = in.readLine()) != null) {
                if(line.matches("(.*)Up(.*)")){
                    String[] availInterface = line.split("\\[");
                    interfaces.add(availInterface[0]);
                }
            }
            in.close();
            int exitCode = p.waitFor();

        } catch (IOException e) {
            interfaces.clear();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        } catch (InterruptedException e) {
            interfaces.clear();
            if(in != null){
                try{
                    in.close();
                } catch (IOException ex) { }
            }
        }finally {
            if(p != null){
                p.destroy();
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

    private void runCmd(ArrayList<String> args, String filePath){
        cmdRunnable = new CapturePacketThread(this, args, filePath);
        cmdRunnable.start();
    }

    @Override
    public void printResult(String result,final long numOfPackets) {
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
            if(inProgress){
                cmdRunnable.stopRun();
            }
        }
        super.onDestroy();
    }
}
