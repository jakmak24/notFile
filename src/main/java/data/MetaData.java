package data;

import java.io.Serializable;

public class MetaData implements Serializable {

    private String name;
    private String ownerID;
    private int x;
    private int y;
    private long fileLength;

    public MetaData(String name, String ownerID, int x, int y, long fileLength){
        this.name=name;
        this.ownerID=ownerID;
        this.x=x;
        this.y=y;
        this.fileLength=fileLength;
    }

    public MetaData(){

    };


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
