package com.dodola.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Comment implements Parcelable {
	private String ID;
	private String username;
	private String message;
	
	public void setID(String id){ID=id;}
	public void setUsername(String name){username=name;}
	public void setMessage(String m){message=m;}
	
	public String getID(){return ID;}
	public String getUsername(){return username;}
	public String getMessage(){return message;}
	
	@Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(ID);
        parcel.writeString(username);
        parcel.writeString(message);
    }
    public static final Parcelable.Creator<Comment> CREATOR  = new Creator<Comment>(){
        @Override
        public Comment createFromParcel(Parcel source) {
        	Comment app=  new Comment();
            app.ID = source.readString();
            app.username = source.readString();
            app.message = source.readString();
            return app;
        }
        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }

    };
}