package com.example.NiceRun;

import android.app.Application;
import android.os.StrictMode;

import com.example.NiceRun.DTO.Sns;
import com.example.NiceRun.DTO.Track;
import com.example.NiceRun.DTO.Trackinfo;

import java.util.List;


/**
 * 앱 전역에서 사용할 수 있는 클래스
 */
public class MyApp extends Application {
    //private MemberInfoItem memberInfoItem;
    private Trackinfo trackinfo;
    private String address;
    private List<Track> track;
    private Sns sns;

    @Override
    public void onCreate() {
        super.onCreate();

        // FileUriExposedException 문제를 해결하기 위한 코드
        // 관련 설명은 책의 [참고] 페이지 참고
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
    /*
    public MemberInfoItem getMemberInfoItem() {
        if (memberInfoItem == null) memberInfoItem = new MemberInfoItem();

        return memberInfoItem;
    }

    public void setMemberInfoItem(MemberInfoItem item) {
        this.memberInfoItem = item;
    }

    public int getMemberSeq() {
        return memberInfoItem.seq;
    }
    */

    public Sns getSns() {
        return sns;
    }

    public void setSns(Sns sns) {
        this.sns = sns;
    }

    public List<Track> getTrack() {
        return track;
    }

    public void setTrack(List<Track> track) {
        this.track = track;
    }

    public void setTrackinfo(Trackinfo trackinfo) {
        this.trackinfo = trackinfo;
    }

    public Trackinfo getTrackinfo() {
        return trackinfo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
