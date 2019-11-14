package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
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
    private ReportThread reportThread;
    private String filePath;
    ArrayList<String> result;
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

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());

        Bundle args = getArguments();
        for(String arg: args.keySet()){
            Object obj = args.get(arg);
            if(obj instanceof String){
                Log.d("key" , "KEY: " + arg + " VALUE:" + obj);
            }
        }
        try{
            filePath = args.getString("capturefile");
        }catch (Exception e){
            filePath = "-";
        }
        selectedText.setText(filePath);
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
        pageText.setText("1/0");
        reportThread = new ReportThread(this, result);

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
                    ((MainActivity)getActivity()).enableViews(false, 1, "");
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

}
