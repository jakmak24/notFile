package data.messages;

import java.io.Serializable;

public class LoginMessage implements Serializable{


    public LoginMessage(){}
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginMessage(String userName, String groupName, String password) {
        this.userName = userName;
        this.groupName = groupName;
        this.password = password;
    }

    private String userName;
    private String groupName;
    private String password;

}
