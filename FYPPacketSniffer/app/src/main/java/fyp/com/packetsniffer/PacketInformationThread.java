package fyp.com.packetsniffer;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fyp.com.packetsniffer.Fragments.CmdExecInterface;
import fyp.com.packetsniffer.Fragments.CmdExecNormal;

public class PacketInformationThread extends CmdExecNormal {
    public PacketInformationThread(CmdExecInterface fragment, ArrayList<String> cmds){
        super(fragment, cmds);
        fileReader = new ReadDateOutput();
    }

    private class ReadDateOutput extends ReadOutput{
        @Override
        public void run() {
            BufferedReader br = null;
            String line = null;
            DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            ArrayList<String> result = new ArrayList<>();
            Calendar nextDay = Calendar.getInstance();
            Calendar currentStart = Calendar.getInstance();
            Calendar currentEnd = Calendar.getInstance();
            long count = 0;
            long numOfPackets = 0;
            boolean first = true;
            try{
                br = new BufferedReader(new InputStreamReader(this.input));
                while((line = br.readLine()) != null){
                    Log.d("Line", line.substring(0, 19));
                    if(line != null){
                        if(line.matches("\\d*-\\d*-\\d* \\d*:\\d*:\\d*.*")){
                            Log.d("Line", line.substring(0, 19));
                            if(first){
                                try{
                                    nextDay.setTime(dateFormat.parse(line.substring(0,10)));
                                    nextDay = PacketDate.nextDay(nextDay);
                                    currentStart.setTime(timeFormat.parse(line.substring(0, 19)));
                                    currentStart = PacketDate.getMinRange(3, currentStart);
                                    currentEnd = PacketDate.getMaxRange(3, currentStart);
                                    first = false;
                                    count++;
                                }catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }else{
                                Calendar nextPacket = Calendar.getInstance();
                                try {
                                    nextPacket.setTime(timeFormat.parse(line.substring(0, 19)));
                                }catch (ParseException e){ }
                                if(nextPacket.before(currentEnd)){
                                    count++;
                                }else if(nextPacket.after(currentEnd)){
                                    count = 1;
                                    for(String str: result){
                                        Log.d("OUTPUT1", str);
                                    }
                                    if(nextPacket.after(nextDay)){

                                    }else{
                                        currentStart = PacketDate.getMinRange(3, nextPacket);
                                        currentEnd = PacketDate.getMaxRange(3, nextPacket);
                                        mFragment.printResult(PacketDate.getString(currentStart) + " " + count, 0);
                                    }
                                }
                            }
                            numOfPackets++;
                        }
                        line = null;
                    }
                }
                for(String str: result){
                    Log.d("OUTPUT", str);
                }
                mFragment.printResult(PacketDate.getString(currentStart) + " " + count, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
