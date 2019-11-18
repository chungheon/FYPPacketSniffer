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
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

import fyp.com.packetsniffer.MainActivity;
import fyp.com.packetsniffer.R;

public class AnalysisReportFragment extends Fragment{

    private View view;

    private TextView selectedText;
    private TextView fileInfoText;
    private TextView recvBarTitle;
    private TextView sendBarTitle;
    private TextView pageText;
    private RecyclerView arpInfo;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewNormalPageAdapter mRVAdapter;
    private ARPReportThread arpReportThread;
    private GatherFileInfo fileInfoThread;
    private NumPacketThread numPacketThread;
    private String filePath;
    private GraphView recvBarChart;
    private GraphView sendBarChart;
    private ArrayList<byte[]> result;
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

    private void initView(){
        this.selectedText = (TextView) view.findViewById(R.id.report_select_file_text);
        this.fileInfoText = (TextView) view.findViewById(R.id.report_file_information);
        this.recvBarTitle = (TextView) view.findViewById(R.id.report_recv_title);
        this.sendBarTitle = (TextView) view.findViewById(R.id.report_send_title);
        this.arpInfo = (RecyclerView) view.findViewById(R.id.report_analysis_information);
        this.pageText = (TextView) view.findViewById(R.id.report_page_information);
        this.recvBarChart = (GraphView) view.findViewById(R.id.report_num_recv_graph);
        this.sendBarChart = (GraphView) view.findViewById(R.id.report_num_send_graph);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewNormalPageAdapter(getContext().getApplicationContext(), new ArrayList<byte[]>());

        Bundle args = getArguments();
        try{
            this.filePath = args.getString("capturefile");
        }catch (Exception e){
            this.filePath = "-";
        }
        long numOfPackets = 0;
        try{
            String packetStr = args.getString("numpackets");
            numOfPackets = Long.parseLong(packetStr);
        }catch (Exception e){
            numOfPackets = 0;
        }
        this.selectedText.setText(filePath);
        this.arpInfo.setAdapter(mRVAdapter);
        this.arpInfo.setLayoutManager(mLayoutManager);
        this.pageText.setText("1/1");
        this.fileInfoThread = new GatherFileInfo(this, result, numOfPackets);
        this.fileInfoThread.execute("");
        this.arpReportThread = new ARPReportThread(this, result);
        this.numPacketThread = new NumPacketThread(this, result);

        hideAllUI();
    }

    private void hideAllUI(){
        this.sendBarChart.setVisibility(View.GONE);
        this.recvBarChart.setVisibility(View.GONE);
        this.arpInfo.setVisibility(View.GONE);
        this.pageText.setVisibility(View.GONE);
        this.recvBarTitle.setVisibility(View.GONE);
        this.sendBarTitle.setVisibility(View.GONE);
    }

    private void initListener(){
        arpInfo.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    pageText.setVisibility(View.VISIBLE);
                    arpInfo.setVisibility(View.VISIBLE);
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

    public void printNumRecv(final List<Pair<String, Double>> data){
        if(getActivity() != null && data.size() != 0){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recvBarTitle.setVisibility(View.VISIBLE);
                    recvBarChart.setVisibility(View.VISIBLE);
                    recvBarChart.removeAllSeries();
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
                    recvBarChart.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
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
                    recvBarChart.setLayoutParams(new LinearLayout.LayoutParams(keys.length * 300, 600));

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
                    StaticLabelsFormatter labelsFormatter = new StaticLabelsFormatter(recvBarChart);
                    labelsFormatter.setHorizontalLabels(finalKeys);
                    recvBarChart.getGridLabelRenderer().setTextSize(30f);
                    recvBarChart.getGridLabelRenderer().reloadStyles();
                    recvBarChart.getViewport().setMaxX(keys.length);
                    recvBarChart.getViewport().setScrollable(true);
                    recvBarChart.addSeries(series);
                    recvBarChart.addSeries(seriesPoints);
                }

            });
        }
    }

    public void printNumSend(final List<Pair<String, Double>> data){
        if(getActivity() != null && data.size() != 0){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendBarTitle.setVisibility(View.VISIBLE);
                    sendBarChart.setVisibility(View.VISIBLE);
                    sendBarChart.removeAllSeries();
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
                    sendBarChart.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
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
                    sendBarChart.setLayoutParams(new LinearLayout.LayoutParams(keys.length * 300, 600));

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
                    StaticLabelsFormatter labelsFormatter = new StaticLabelsFormatter(sendBarChart);
                    labelsFormatter.setHorizontalLabels(finalKeys);
                    sendBarChart.getGridLabelRenderer().setTextSize(30f);
                    sendBarChart.getGridLabelRenderer().reloadStyles();
                    sendBarChart.getViewport().setMaxX(keys.length);
                    sendBarChart.getViewport().setScrollable(true);
                    sendBarChart.addSeries(series);
                    sendBarChart.addSeries(seriesPoints);
                }

            });
        }
    }

    @Override
    public void onDestroy() {
        if(arpReportThread != null){
            arpReportThread.stopRun();
        }
        if(numPacketThread != null){
            numPacketThread.stopRun();
        }

        try {
            if(arpReportThread != null){
                arpReportThread.thread.join();

            }
            if(numPacketThread != null){
                numPacketThread.thread.join();
            }
        } catch (InterruptedException e) { }
        super.onDestroy();
    }
}
