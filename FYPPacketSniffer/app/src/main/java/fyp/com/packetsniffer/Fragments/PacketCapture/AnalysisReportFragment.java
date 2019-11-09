package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.R;

public class AnalysisReportFragment extends Fragment implements CmdExecInterface {

    private View view;

    private Button selectFileBtn;
    private Button analyseBtn;
    private TextView selectedText;
    private TextView fileInfoText;
    private TextView pageText;
    private RecyclerView output;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerViewPageAdapter mRVAdapter;
    private long movement;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_analysis_report, container, false);

        initView();

        return view;
    }

    public void initView(){
        this.analyseBtn = (Button) view.findViewById(R.id.report_start_btn);
        this.selectFileBtn = (Button) view.findViewById(R.id.report_select_file_btn);
        this.selectedText = (TextView) view.findViewById(R.id.report_select_file_text);
        this.fileInfoText = (TextView) view.findViewById(R.id.report_file_information);
        this.output = (RecyclerView) view.findViewById(R.id.report_analysis_information);
        this.pageText = (TextView) view.findViewById(R.id.report_page_information);

        mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext(),
                LinearLayout.HORIZONTAL,
                false);
        mRVAdapter = new RecyclerViewPageAdapter(getContext().getApplicationContext(), new ArrayList<String>());
        output.setAdapter(mRVAdapter);
        output.setLayoutManager(mLayoutManager);
    }

    public void initListener(){
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
                    if((movement - 1050) <= (dx * -1)){
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
    }

    @Override
    public void printResult(ArrayList<String> result, long numOfPackets) {

    }

    @Override
    public void printToast(String message) {

    }

    @Override
    public void cmdDone() {

    }
}
