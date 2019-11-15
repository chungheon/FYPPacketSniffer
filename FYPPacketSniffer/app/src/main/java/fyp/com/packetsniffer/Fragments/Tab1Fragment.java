package fyp.com.packetsniffer.Fragments;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Random;

import fyp.com.packetsniffer.PacketInformationThread;
import fyp.com.packetsniffer.R;

public class Tab1Fragment extends Fragment implements CmdExecInterface {
    int num = 0;
    GraphView linegraph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);
        TextView tx = view.findViewById(R.id.fragText);
        linegraph = (GraphView) view.findViewById(R.id.graph);
        testCase();
        return view;
    }

    private void testCase(){
        String[] labels = new String[24];
        DataPoint[] output = new DataPoint[24];
        for(int i = 0; i < 24; i++){
            output[i] = new DataPoint(i, 0);
            labels[i] = i + "";
        }
        linegraph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
        /*linegraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if(isValueX){
                    if(value >= 10){
                        return super.formatLabel(value,isValueX) + "\nhr";
                    }else{
                        return "0" + super.formatLabel(value,isValueX) + "\nhr";
                    }
                }
                return super.formatLabel(value, isValueX);
            }
        });*/
        StaticLabelsFormatter labelsFormatter = new StaticLabelsFormatter(linegraph);
        labelsFormatter.setHorizontalLabels(labels);
        linegraph.getViewport().setMinX(0);
        linegraph.getViewport().setMaxX(24);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(output);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(7);
        linegraph.addSeries(series);

        LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(output);
        linegraph.addSeries(lineSeries);
        String sedPath = install("sed");
        cmdExec(sedPath);
        /*ArrayList<String> cmds = new ArrayList<>();
        cmds.add("tcpdump -r /storage/emulated/0/Download/PCAP_Files/0 -c 100 | grep 'length' | " + sedPath + " 's/.*length//'");
        cmds.add("exit");
        PacketInformationThread test = new PacketInformationThread(this, cmds);
        test.start();*/
    }

    public void cmdExec(String libPath){
        try{
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            DataOutputStream outputStream = new DataOutputStream(shell.getOutputStream());
            BufferedReader shell_out = new BufferedReader(new InputStreamReader(shell.getInputStream()));
            outputStream.writeBytes("tcpdump -r /storage/emulated/0/Download/PCAP_Files/0 -c 100 | grep 'length' | " + libPath + " 's/.*length//'");
            outputStream.flush();
            String line = null;
            int count = 0;
            try {
                while(true){
                    if(shell_out.ready()){
                        line = shell_out.readLine();
                    }

                    if(line != null){
                        Log.d("test", line);
                        count = 0;
                        line = null;
                    }

                    synchronized (this){
                        try{
                            wait(10);
                            count++;
                        }catch (InterruptedException e){
                            break;
                        }
                    }
                    if(count >= 300){
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            shell.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printResult(String result, long numOfPackets) {
        /*DataPoint[] output = new DataPoint[24];
        for(int i = 0; i < 24; i++){
            output[i] = new DataPoint(i, 0);
        }
        for(int i = 0; i < result.size(); i++){
            String[] outInfo = result.get(i).split(" ");
            if(outInfo.length >= 3){
                try {
                    long packets = Long.parseLong(outInfo[2]);
                    int hour = Integer.parseInt(outInfo[1]);
                    output[hour] = new DataPoint(hour, packets);
                }catch (NumberFormatException e){ }
            }

        }



        final DataPoint[] graphPoints = output;
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PointsGraphSeries<DataPoint> seriesPoints = new PointsGraphSeries<>(graphPoints);
                    seriesPoints.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            paint.setColor(Color.WHITE);
                            paint.setTextSize(38);
                            int dataVal = (int) dataPoint.getY();
                            if(dataVal != 0){
                                canvas.drawText(String.valueOf(dataVal), x, y, paint);
                            }
                        }
                    });
                    LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(graphPoints);
                    lineSeries.setDrawBackground(true);
                    linegraph.addSeries(lineSeries);
                    linegraph.addSeries(seriesPoints);
                }
            });
        }*/
    }

    @Override
    public void printToast(String message) {
        final String msg = message;
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void cmdDone() {

    }
    public String install(String library){
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open(library);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(getActivity().getFilesDir() + "/" + library);
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
            return getActivity().getFilesDir() + "/" + library;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Error";
    }

    /*public void testInstall() {
        InputStream is = null;
        FileOutputStream fileOutputStream = null;
        try {
            is = getActivity().getAssets().open("traceroute_arm");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            File targetFile = new File(getActivity().getFilesDir() + "/traceroute");
            fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(buffer);

            is.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void testBinary() {
        try {
            String libPath = getActivity().getFilesDir() + "/traceroute";

            //ProcessBuilder pb = new ProcessBuilder("chmod 777 " + libPath);
            //pb.command(libPath + " --version\n");
            // pb.command("traceroute --version\n");
            //pb.command("ls -l /data/user/0/fyp.com.packetsniffer/files");
            Process shell = Runtime.getRuntime().exec("chmod 777 " + libPath + "\n");
            DataOutputStream outputStream = new DataOutputStream(shell.getOutputStream());
            BufferedReader shell_out = new BufferedReader(new InputStreamReader(shell.getErrorStream()));
            outputStream.writeBytes(libPath + " --version | grep \'version\'\n");
            outputStream.flush();
            String line = null;
            int count = 0;
            try {
                while(true){
                    if(shell_out.ready()){
                        line = shell_out.readLine();
                    }

                    if(line != null){
                        Log.d("test", line);
                        count = 0;
                        line = null;
                    }

                    synchronized (this){
                        try{
                            wait(10);
                            count++;
                        }catch (InterruptedException e){
                            break;
                        }
                    }
                    if(count >= 300){
                        break;
                    }
                }
            } catch (IOException e) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void installAsset(String assetName, String fileName, String dirPath, ArrayList<String> cmds) {
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
    }*/
}