package fyp.com.packetsniffer.Fragments.NetworkUtils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.ECField;
import java.util.ArrayList;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class PingActivity extends Fragment {
    private String libPath;
    private View view;
    private TextView hostNameText;
    private TextView hostIPText;
    private TextView pingDetailText;
    private TextView pingResultText;
    private GraphView packetGraph;
    private ProgressBar progressBarPing;
    private PingShell ping;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_network_ping, container, false);
        installAsset("ping", "ping", getActivity().getFilesDir().toString());
        initView();
        initListener();
        return view;
    }

    private void initView(){
        this.hostNameText = (TextView) view.findViewById(R.id.ping_host_name_text);
        this.hostIPText = (TextView) view.findViewById(R.id.ping_host_ip_text);
        this.pingDetailText = (TextView) view.findViewById(R.id.ping_detail_text);
        this.pingResultText = (TextView) view.findViewById(R.id.ping_result_text);
        this.packetGraph = (GraphView) view.findViewById(R.id.ping_graphview);
        this.progressBarPing = (ProgressBar) view.findViewById(R.id.ping_progress_bar);

        Bundle args = getArguments();
        try{
            String ip = args.getString("host");
            this.hostNameText.setText(ip);
            this.ping = new PingShell(this, libPath, ip);
            this.ping.start();
            startProgressBar();

        }catch (Exception e){
            Toast.makeText(getContext().getApplicationContext(),
                    "Unable to ping",
                    Toast.LENGTH_SHORT).show();
        }

        packetGraph.getViewport().setScrollable(true);
        packetGraph.getViewport().setScalable(false);
        hideAllUI();
    }

    private void initListener(){
        this.view.setFocusableInTouchMode(true);
        this.view.requestFocus();
        this.view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle("Network Utils");
                }
                return false;
            }
        });
    }

    private void hideAllUI(){
        pingDetailText.setVisibility(View.GONE);
        pingResultText.setVisibility(View.GONE);
        packetGraph.setVisibility(View.GONE);
        hostIPText.setVisibility(View.GONE);
    }

    public void startProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarPing.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void stopProgressBar() {
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarPing.setVisibility(View.GONE);
                }
            });
        }
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

    public void printHostIP(final String ip){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hostIPText.setVisibility(View.VISIBLE);
                    hostIPText.setText(ip);
                }
            });
        }
    }

    public void printToast(final String message){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void updateDetails(final ArrayList<String> result){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    packetGraph.setVisibility(View.VISIBLE);
                    if(pingDetailText.getVisibility() == View.GONE){
                        pingDetailText.setText(result.get(0));
                    }
                    pingDetailText.setVisibility(View.VISIBLE);
                    packetGraph.removeAllSeries();
                    int count = 0;
                    DataPoint[] dataPoints = new DataPoint[result.size()];
                    for(String res: result){
                        String[] time = res.replace("=", "").split("time")[1].split(" ");
                        double resTime;
                        try{
                            resTime = Double.parseDouble(time[0]);
                        }catch (NumberFormatException e){
                            resTime = Double.valueOf(0);
                        }
                        dataPoints[count] = new DataPoint(count, resTime);
                        count++;
                    }

                    LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(dataPoints);
                    lineSeries.setDrawDataPoints(true);
                    lineSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                        @Override
                        public void onTap(Series series, final DataPointInterface dataPoint) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pingDetailText.setText(result.get((int)dataPoint.getX()));
                                }
                            });
                        }
                    });

                    lineSeries.setDataPointsRadius(20);
                    lineSeries.setDrawBackground(true);
                    packetGraph.getViewport().setMinY(0);
                    packetGraph.getViewport().setXAxisBoundsManual(true);
                    packetGraph.getViewport().setMaxX(result.size()-1);
                    packetGraph.addSeries(lineSeries);


                }
            });
        }
    }

    public void printResult(final String result){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pingResultText.setVisibility(View.VISIBLE);;
                    pingResultText.setText(result);

                    stopProgressBar();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        if(this.ping != null){
            ping.stopRun();
        }
        super.onDestroy();
    }
}
