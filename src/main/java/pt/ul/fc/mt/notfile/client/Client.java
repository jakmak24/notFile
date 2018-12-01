package pt.ul.fc.mt.notfile.client;

public class Client {
    private final String userID;
    private final String groupID;

    public Client(String userID, String groupID) {
        this.userID = userID;
        this.groupID = groupID;
    }

    public String getUserID() {
        return userID;
    }

    public String getGroupID() {
        return groupID;
    }
}
