package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ReportThread implements Runnable {
    private Thread thread;
    private ArrayList<String> data;
    private AnalysisReportFragment mFragment;
    private ArrayList<String> requests;
    private ArrayList<String> replies;

    public ReportThread(AnalysisReportFragment fragment, ArrayList<String> data){
        this.data = data;
        this.mFragment = fragment;
        thread = new Thread(this);
        thread.start();
    }

    public void run(){
        gatherCaptureInfo();
        analyseData();
    }

    private void gatherCaptureInfo(){
        long numOfPackets = 0;
        boolean first = true;
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(int j = 0; j < data.size(); j++){
            String[] dataInfo = data.get(j).split("\\|");
            int count = 0;
            for(int i = 0; i < dataInfo.length; i++){
                String line = dataInfo[i].replace("\n", "");
                if(line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                    numOfPackets++;
                    if(first){
                        try{
                            startTime.setTime(timeFormat.parse(line.substring(0,19)));
                        } catch (ParseException e) {
                            startTime = null;
                        }
                        first = false;
                    }
                }
            }

            if(j == data.size() - 1){
                for(int i = dataInfo.length - 1; i > 0; i--) {
                    String line = dataInfo[i].replace("\n", "");
                    if(line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                        try{
                            endTime.setTime(timeFormat.parse(line.substring(0,19)));
                        } catch (ParseException e) {
                            endTime = null;
                        }
                        break;
                    }
                }
            }
        }
        if(numOfPackets == 0){
            startTime = null;
            endTime = null;
        }
        String info = "";
        info += "Number Of Packets: " + numOfPackets + "\n";
        if(startTime != null){
            String start = timeFormat.format(startTime.getTime());
            info += "Start Time: " + start + "\n";
        }else{
            info += "Start Time: -\n";
        }

        if(endTime != null){
            String end = timeFormat.format(endTime.getTime());
            info += "End Time: " + end;
        }else{
            info += "End Time: -";
        }

        mFragment.printFileInformation(info);
    }

    private void analyseData(){
        requests = new ArrayList<>();
        replies = new ArrayList<>();
        String request = "";
        String reply = "";
        long reqCount = 0;
        long replyCount = 0;
        for(int j = 0; j < data.size(); j++) {
            String[] dataInfo = data.get(j).split("\\|");
            for(int i = 0; i < dataInfo.length; i++){
                String line = dataInfo[i].replace("\n", "");
                if(line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                    if(line.matches(".*ARP.*who-has.*")){
                        request += "\n" + line;
                        reqCount++;
                        if(reqCount % 100 == 0){
                            requests.add(request);
                            request = "";
                        }
                    }else if(line.matches(".*ARP.*Reply.*")){
                        reply += "\n" + line;
                        replyCount++;
                        if(replyCount % 100 == 0){
                            replies.add(reply);
                            reply = "";
                        }
                    }
                }
            }
        }
        if(replyCount % 100 != 0){
            requests.add(request);
        }

        if(reqCount % 100 != 0){
            replies.add(reply);
        }

        String analysis = "";
        analysis += "ARP REQUEST: " + reqCount + "\n";
        analysis += "ARP REPLY: " + replyCount;
        mFragment.printAnalysis(analysis);
        for(String req : requests){
            mFragment.printAnalysis(req);
        }

        for(String rep: replies){
            mFragment.printAnalysis(rep);
        }

    }
}
