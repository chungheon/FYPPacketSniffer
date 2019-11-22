package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private TextView guideText;
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
    private GraphView statGraph;
    private List<String> interfaces;
    private HashMap<String, Double> numSend;
    private String libPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_packet_capture, container, false);
        installAsset("killall_arm", "killall", getActivity().getFilesDir().toString());
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
        this.guideText = (TextView) view.findViewById(R.id.capture_guide);
        this.statGraph = (GraphView) view.findViewById(R.id.capture_statical_graph);

        this.numPacketsText.setText("Waiting to start");
        inProgress = false;
        interfaces = new ArrayList<String>();
        dirText.setText("External Storage");
        updateSpinner();
        numSend = new HashMap<>();
        statGraph.setVisibility(View.GONE);
    }

    private void initListener(){

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> cmds = new ArrayList<>();
                String fileName = fileOutput.getText().toString();
                fileName = fileName.replaceAll(" ", "_");
                Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(fileName);
                boolean specialChar = m.find();
                if(specialChar){
                    printToast("The file name should contain only letters and numbers.");
                    return;
                }

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
                cmd += " -w - | tee " +  filePath + " | tcpdump -ttttvvvv -r -";

                cmds.add(cmd);
                if(cmdRunnable != null){
                    if(inProgress){
                        cmdRunnable.stopRun();
                        captureBtn.setText("Start Capture");
                        inProgress = false;
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
                        statGraph.setVisibility(View.VISIBLE);
                        guideText.setVisibility(View.GONE);
                    }
                }else{
                    runCmd(cmds, filePath);
                    numPacketsText.setText("Capturing...");
                    inProgress = true;
                    captureBtn.setText("Stop Capturing");
                    interfaceSpinner.setEnabled(false);
                    fileOutput.setEnabled(false);
                    guideText.setVisibility(View.GONE);
                    statGraph.setVisibility(View.VISIBLE);
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

    private void setPermissions() {
        try {
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            shell.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) { }
    }

    private void installAsset(String assetName, String fileName, String dirPath) {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(dirPath + "/" + fileName);
            libPath = targetFile.getPath();
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
            setPermissions();
        } catch (IOException e) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }

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

    @Override
    public void onResume() {
        super.onResume();
    }

    private void runCmd(ArrayList<String> args, String filePath){
        cmdRunnable = new CapturePacketThread(this, args, filePath, libPath);
        cmdRunnable.start();
    }

    @Override
    public void printResult(final String result,final long numOfPackets) {
        if(numSend.containsKey(result)){
            numSend.put(result, numSend.get(result) + Double.valueOf(1));
        }else{
            numSend.put(result, Double.valueOf(1));
        }
        final List<Pair<String, Double>> update = updateInfoDouble(numSend);

        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numPacketsText.setText(numOfPackets + " packets");

                    statGraph.removeAllSeries();

                    String[] keys = new String[update.size() + 1];
                    DataPoint[] dataPoints = new DataPoint[update.size() + 1];
                    DataPoint[] graphPoints = new DataPoint[update.size() + 1];
                    int count = 0;
                    keys[0] = "";
                    dataPoints[0] = new DataPoint(0, 0);
                    graphPoints[0] = new DataPoint(0, 0);

                    for (Pair<String, Double> val: update) {
                        keys[count + 1] = val.first;
                        Double numPackets = val.second;
                        dataPoints[count + 1] = new DataPoint(count + 1, numPackets);
                        graphPoints[count + 1] = new DataPoint(count + 1 - 0.15, numPackets);
                        count++;
                    }

                    BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
                    final String[] finalKeys = keys;
                    statGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (isValueX) {
                                int val = (int) value;
                                return finalKeys[val];
                            } else {
                                return super.formatLabel(value, isValueX);
                            }
                        }
                    });


                    series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                        @Override
                        public int get(DataPoint data) {
                            return Color.rgb((int) data.getX() * 255 / 4, (int) Math.abs(data.getY() * 255 / 6), 100);
                        }
                    });
                    statGraph.setLayoutParams(new LinearLayout.LayoutParams(keys.length * 300, 600));

                    PointsGraphSeries<DataPoint> seriesPoints = new PointsGraphSeries<>(graphPoints);
                    seriesPoints.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            paint.setColor(Color.WHITE);
                            paint.setTextSize(40);
                            int dataVal = (int) dataPoint.getY();
                            if (dataVal != 0) {
                                canvas.drawText(String.valueOf(dataVal), x, y, paint);
                            }
                        }
                    });
                    series.setSpacing(70);
                    StaticLabelsFormatter labelsFormatter = new StaticLabelsFormatter(statGraph);
                    labelsFormatter.setHorizontalLabels(finalKeys);
                    statGraph.getGridLabelRenderer().setTextSize(30f);
                    statGraph.getGridLabelRenderer().reloadStyles();
                    statGraph.getViewport().setMaxX(keys.length);
                    statGraph.getViewport().setScrollable(true);
                    statGraph.addSeries(series);
                    statGraph.addSeries(seriesPoints);
                }
            });
        }
    }

    private List<Pair<String, Double>> updateInfoDouble(HashMap<String, Double> numPackets){
        List<Pair<String, Double>> dataArr = new ArrayList<>();
        for(String key: numPackets.keySet()){
            Pair<String, Double> pair = new Pair<>(key,numPackets.get(key));
            dataArr.add(pair);
        }
        Collections.sort(dataArr, new Comparator<Pair<String, Double>>() {
            @Override
            public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                return (int) (o2.second - o1.second);
            }
        });
        return dataArr;
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
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });

        }

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
