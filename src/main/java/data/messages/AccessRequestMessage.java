package data.messages;

import java.io.Serializable;

public class AccessRequestMessage implements Serializable {

    private int id;
    private String userId;

    public AccessRequestMessage(){}

    public AccessRequestMessage(int id, String userId){
        this.id=id;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
