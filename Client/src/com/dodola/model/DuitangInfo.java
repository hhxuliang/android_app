package com.dodola.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class DuitangInfo implements Parcelable {

	private int height;
	private String albid = "";
	private String msg = "";
	private String isrc = "";
	
	private ArrayList<Comment> commentList;
	
	public ArrayList<Comment> getCommentList(){return commentList;}
	
	public void setCommentList(ArrayList<Comment> l){ commentList=l;}

	public int getWidth(){
		return 200;
	}
	public String getAlbid() {
		return albid;
	}

	public void setAlbid(String albid) {
		this.albid = albid;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getIsrc() {
		return isrc;
	}

	public void setIsrc(String isrc) {
		this.isrc = isrc;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}


    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        
    	parcel.writeInt(height);
        parcel.writeString(albid);
        parcel.writeString(msg);
        parcel.writeString(isrc);
        parcel.writeTypedList(commentList);
    }
    public static final Parcelable.Creator<DuitangInfo> CREATOR  = new Creator<DuitangInfo>(){
        @Override
        public DuitangInfo createFromParcel(Parcel source) {
            DuitangInfo app=  new DuitangInfo();
            app.height = source.readInt();
            app.albid = source.readString();
            app.msg = source.readString();
            app.isrc = source.readString();
            app.commentList=new ArrayList<Comment>();
            source.readTypedList(app.commentList,Comment.CREATOR);  
            return app;
        }
        @Override
        public DuitangInfo[] newArray(int size) {
            return new DuitangInfo[size];
        }

    };
}
