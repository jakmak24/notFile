package data.messages;

import java.io.Serializable;

public class LoginMessage implements Serializable{


    public LoginMessage(){}
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginMessage(String userID, String groupID, String password) {
        this.userID = userID;
        this.groupID = groupID;
        this.password = password;
    }

    private String userID;
    private String groupID;
    private String password;

}
