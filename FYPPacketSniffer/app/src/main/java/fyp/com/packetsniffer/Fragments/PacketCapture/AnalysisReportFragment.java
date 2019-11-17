package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class AnalysisReportFragment extends Fragment{

    private View view;

    private Button analyseBtn;
    private TextView selectedText;
    private TextView fileInfoText;
    private TextView pageText;
    private RecyclerView output;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;
    private ARPReportThread arpReportThread;
    private GatherFileInfo fileInfoThread;
    private NumPacketThread numPacketThread;
    private String filePath;
    private GraphView packetBarChart;
    private ArrayList<String> result;
    private long movement;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_analysis_report, container, false);

        if(getActivity() != null){
            this.result = ((MainActivity) getActivity()).getResult();
            initView();
            initListener();
        }

        return view;
    }

    public void initView(){
        this.selectedText = (TextView) view.findViewById(R.id.report_select_file_text);
        this.fileInfoText = (TextView) view.findViewById(R.id.report_file_information);
        this.output = (RecyclerView) view.findViewById(R.id.report_analysis_information);
        this.pageText = (TextView) view.findViewById(R.id.report_page_information);
        this.packetBarChart = (GraphView) view.findViewById(R.id.report_num_packet_graph);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());

        Bundle args = getArguments();
        try{
            filePath = args.getString("capturefile");
        }catch (Exception e){
            filePath = "-";
        }
        long numOfPackets = 0;
        try{
            String packetStr = args.getString("numpackets");
            Log.d("packetStr", packetStr);
            numOfPackets = Long.parseLong(packetStr);
        }catch (Exception e){
            numOfPackets = 0;
        }
        selectedText.setText(filePath);
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
        pageText.setText("1/1");
        fileInfoThread = new GatherFileInfo(this, result, numOfPackets);
        fileInfoThread.execute("");
        arpReportThread = new ARPReportThread(this, result);
        numPacketThread = new NumPacketThread(this, result);
        packetBarChart.setTitle("Packets From IP Address");
        packetBarChart.setVisibility(View.GONE);
    }

    public void initListener(){
        output.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if(pageInfo.length >= 2){
                        long totalpages = Integer.parseInt(pageInfo[1]) * 1050;
                        movement += dx;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText((((movement + 300)/1050)) + "/" + pageInfo[1]);
                            }
                        });
                        if(movement > totalpages){
                            movement = totalpages;
                        }
                    }
                } else {
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    if((movement - 1050) <= (dx * -1)){
                        movement = 1050;
                    }else{
                        movement += dx;
                        if(pageInfo.length >= 2){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pageText.setText((((movement + 600)/1050)) + "/" + pageInfo[1]);
                                }
                            });
                        }
                    }

                    if(movement == 1050 && pageInfo.length >= 2){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pageText.setText("1/" + pageInfo[1]);
                            }
                        });
                    }
                }
            }
        });

        this.view.setFocusableInTouchMode(true);
        this.view.requestFocus();
        this.view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    ((MainActivity)getActivity()).getSupportActionBar().setTitle("Packet Analysis");
                    ((MainActivity)getActivity()).enableViews(false, 1, "", "");
                }
                return false;
            }
        });
    }

    public void printFileInformation(final String info){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileInfoText.setText(info);
                }
            });
        }
    }

    public void printAnalysis(final String output){
        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int prevSize = mRVAdapter.getItemCount();
                    mRVAdapter.addPage(output);
                    mRVAdapter.notifyItemRangeInserted(prevSize, 1);
                    final String[] pageInfo = pageText.getText().toString().split("/");
                    String text = pageInfo[0] + "/" + mRVAdapter.getItemCount();
                    pageText.setText(text);
                }
            });
        }
    }

    public void printIPNumPacket(final List<Pair<String, Double>> data){
        if(getActivity() != null && data.size() != 0){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    packetBarChart.setVisibility(View.VISIBLE);
                    packetBarChart.removeAllSeries();
                    String[] keys = new String[data.size() + 1];
                    DataPoint[] dataPoints = new DataPoint[data.size() + 1];
                    DataPoint[] graphPoints = new DataPoint[data.size() + 1];
                    keys[0] = "";
                    dataPoints[0] = new DataPoint(0, 0);
                    graphPoints[0] = new DataPoint(0, 0);
                    int count = 0;
                    for(Pair<String, Double> val: data){
                        keys[count + 1] = val.first;
                        Double numPackets = val.second;
                        dataPoints[count + 1] = new DataPoint(count + 1, numPackets);
                        graphPoints[count + 1] = new DataPoint(count + 1 - 0.15, numPackets);
                        count++;
                    }

                    BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
                    final String[] finalKeys = keys;
                    packetBarChart.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
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
                            return  Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
                        }
                    });
                    packetBarChart.setLayoutParams(new LinearLayout.LayoutParams(keys.length * 300, 600));

                    PointsGraphSeries<DataPoint> seriesPoints = new PointsGraphSeries<>(graphPoints);
                    seriesPoints.setCustomShape(new PointsGraphSeries.CustomShape() {
                        @Override
                        public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                            paint.setColor(Color.WHITE);
                            paint.setTextSize(40);
                            int dataVal = (int) dataPoint.getY();
                            if(dataVal != 0){
                                canvas.drawText(String.valueOf(dataVal), x, y, paint);
                            }
                        }
                    });
                    series.setSpacing(70);
                    StaticLabelsFormatter labelsFormatter = new StaticLabelsFormatter(packetBarChart);
                    labelsFormatter.setHorizontalLabels(finalKeys);
                    packetBarChart.getGridLabelRenderer().setTextSize(30f);
                    packetBarChart.getGridLabelRenderer().reloadStyles();
                    packetBarChart.getViewport().setMaxX(keys.length);
                    packetBarChart.getViewport().setScrollable(true);
                    packetBarChart.addSeries(series);
                    packetBarChart.addSeries(seriesPoints);
                }

            });
        }

    }

}
