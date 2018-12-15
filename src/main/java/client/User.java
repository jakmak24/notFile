package client;

public class User {
    private final String userName;
    private final String groupName;
    private final String userID;

    public User(String userName,String groupName){
        this.userName=userName;
        this.groupName=groupName;
        this.userID = groupName+"."+userName;
    }

    public String getUserName() {
        return userName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getUserID() {
        return userID;
    }
}
