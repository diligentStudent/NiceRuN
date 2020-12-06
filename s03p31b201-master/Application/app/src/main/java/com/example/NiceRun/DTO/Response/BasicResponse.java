package com.example.NiceRun.DTO.Response;

import com.example.NiceRun.DTO.Calendar;
import com.example.NiceRun.DTO.Mytrack;
import com.example.NiceRun.DTO.DownloadTrack;
import com.example.NiceRun.DTO.Sns;
import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.CalendarSuccess;
import com.example.NiceRun.DTO.Trackinfo;
import com.example.NiceRun.DTO.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class BasicResponse {
    @SerializedName("status") boolean status;
    @SerializedName("data") String data;
    @SerializedName("object") object object;


    public class object {
        @SerializedName("downloadList") List<Boolean> downloadList;
        @SerializedName("uploadList") List<Boolean> uploadList;
        @SerializedName("savedList") List<Boolean> savedList;
        @SerializedName("total") String total;
        @SerializedName("count") String count;

        @SerializedName("user") User user;
        @SerializedName("calendar") Calendar calendar;
        @SerializedName("calendarList") List<Calendar> calendarList;
        @SerializedName("trackinfo") Trackinfo trackinfo;
        @SerializedName("trackinfoList") List<Trackinfo> trackinfoList;
        @SerializedName("mytrack") Mytrack mytrack;
        @SerializedName("mytrackList") List<Mytrack> mytrackList;
        @SerializedName("sns") Sns sns;
        @SerializedName("snsList") List<Sns> snsList;
        @SerializedName("track") Track track;
        @SerializedName("trackList") List<Track> trackList;

        @SerializedName("daycalendar") List<CalendarSuccess> daycalendar; // String date, int flag

        public List<Boolean> getDownloadList() { return downloadList;}
        public List<Boolean> getUploadList() { return uploadList;}
        public List<Boolean> getSavedList() {return savedList;}

        public User getUser() {return user;}
        public Calendar getCalendar() {return calendar;}
        public List<Calendar> getCalendarList() {return calendarList;}
        public Trackinfo getTrackinfo() {return trackinfo;}
        public List<Trackinfo> getTrackinfoList() {return trackinfoList;}
        public Mytrack getMytrack() {return mytrack;}
        public List<Mytrack> getMytrackList() {return mytrackList;}
        public Sns getSns() {return sns;}
        public List<Sns> getSnsList() {return snsList;}
        public Track getTrack() {return track;}
        public List<Track> getTrackList() {return trackList;}

        public List<CalendarSuccess> getDaycalendar() {return daycalendar;}

        public String getTotal() {return total;}
        public String getCount() {return count;}

        @Override
        public String toString() {
            return "object{" +
                    "downloadList=" + downloadList +
                    ", uploadList=" + uploadList +
                    ", savedList=" + savedList +
                    ", user=" + user +
                    ", calendar=" + calendar +
                    ", calendarList=" + calendarList +
                    ", trackinfo=" + trackinfo +
                    ", trackinfoList=" + trackinfoList +
                    ", mytrack=" + mytrack +
                    ", mytrackList=" + mytrackList +
                    ", sns=" + sns +
                    ", snsList=" + snsList +
                    ", track=" + track +
                    ", trackList=" + trackList +
                    ", daycalendar=" + daycalendar +
                    '}';
        }
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }

    public object getObject() {
        return object;
    }
    public void setObject(object object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "BasicResponse{" +
                "object=" + object +
                ", data='" + data + '\'' +
                ", status=" + status +
                '}';
    }
}
