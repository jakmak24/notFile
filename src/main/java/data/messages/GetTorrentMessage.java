package data.messages;

import java.io.Serializable;

public class GetTorrentMessage implements Serializable{

    private int id;

    public GetTorrentMessage(){}

    public GetTorrentMessage(int id){
        this.id=id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
