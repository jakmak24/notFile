package client;

public class User {

    private final String userID;
    private final String groupID;

    public User(String userID,String groupID){
        this.userID=userID;
        this.groupID=groupID;
    }

    public String getGroupID() {
        return groupID;
    }

    public String getUserID() {
        return userID;
    }
}
