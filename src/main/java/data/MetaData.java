package data;

import java.io.Serializable;

public class MetaData implements Serializable {

    private String name;
    private String ownerID;
    private int x;
    private int y;
    private long fileLength;
    private boolean accessPublic;

    public MetaData(String name, String ownerID, int x, int y, long fileLength, boolean accessPublic) {
        this.name = name;
        this.ownerID = ownerID;
        this.x = x;
        this.y = y;
        this.fileLength = fileLength;
        this.accessPublic = accessPublic;
    }

    public MetaData(String name, String ownerID, int x, int y, long fileLength) {
        this(name, ownerID, x, y, fileLength, true);
    }

    public MetaData() {}

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

    public boolean isAccessPublic() {
        return accessPublic;
    }

    public void setAccessPublic(boolean accessPublic) {
        this.accessPublic = accessPublic;
    }

    public static final class Builder {
        private String name;
        private String ownerID;
        private int x;
        private int y;
        private long fileLength;
        private boolean accessPublic = true;

        public Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ownerID(String ownerID) {
            this.ownerID = ownerID;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder fileLength(long fileLength) {
            this.fileLength = fileLength;
            return this;
        }

        public Builder accessPublic(boolean accessPublic) {
            this.accessPublic = accessPublic;
            return this;
        }

        public MetaData build() {
            return new MetaData(name, ownerID, x, y, fileLength, accessPublic);
        }
    }
}
