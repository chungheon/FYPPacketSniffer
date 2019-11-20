package fyp.com.packetsniffer.Fragments.PacketCapture;

import android.os.AsyncTask;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class GatherFileInfo extends AsyncTask<String, Integer, String> {
    private ArrayList<byte[]> data;
    private long numOfPackets;
    private AnalysisReportFragment mFragment;

    public GatherFileInfo(AnalysisReportFragment fragment, ArrayList<byte[]> packets, long numOfPackets){
        this.data = packets;
        this.numOfPackets = numOfPackets;
        this.mFragment = fragment;
    }
    @Override
    protected String doInBackground(String... strings) {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean found = false;
        for(int j = 0; j < data.size(); j++) {
            String[] dataInfo = new String(data.get(j)).split("\\|");
            int count = 0;
            for (int i = 0; i < dataInfo.length; i++) {
                String line = dataInfo[i].replace("\n", "");
                if (line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")) {
                    try {
                        startTime.setTime(timeFormat.parse(line.substring(0, 19)));
                    } catch (ParseException e) {
                        startTime = null;
                    }
                    found = true;
                    break;
                }
            }
            if(found){
                break;
            }
        }
        if(!found){
            startTime = null;
        }

        found  = false;

        for(int j = data.size() -1; j >= 0; j--){
            String[] dataInfo = new String(data.get(j)).split("\\|");
            for(int i = dataInfo.length - 1; i > 0; i--) {
                String line = dataInfo[i].replace("\n", "");
                if(line.matches(".*\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                    try{
                        endTime.setTime(timeFormat.parse(line.substring(0,19)));
                    } catch (ParseException e) {
                        endTime = null;
                    }
                    found = true;
                    break;
                }
            }
            if(found){
                break;
            }
        }

        if(!found){
            endTime = null;
        }
        String info = "";
        info += "Number Of Packets: " + numOfPackets +  "\n";
        if(startTime != null){
            String start = timeFormat.format(startTime.getTime());
            info += "Start Time: " + start + "(GMT+8)\n";
        }else{
            info += "Start Time: -\n";
        }

        if(endTime != null){
            String end = timeFormat.format(endTime.getTime());
            info += "End Time: " + end + "(GMT+8)";
        }else{
            info += "End Time: -";
        }
        return info;
    }

    @Override
    protected void onPostExecute(String s) {
        mFragment.printFileInformation(s);
    }
}
