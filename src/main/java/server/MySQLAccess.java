package server;

import data.Attribute;
import data.MetaData;
import data.TorrentRecordMessage;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLAccess {
    private Connection connect;

    public MySQLAccess() throws SQLException, ClassNotFoundException {
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        connect = DriverManager.getConnection("jdbc:mysql://localhost/notFile?"
                + "user=root&password=admin");
    }

    public void addTorrent(TorrentRecordMessage trm) throws SQLException {
        try (PreparedStatement preparedStatement =
                 connect.prepareStatement("insert into  notFile.torrents values (default, ?, ?, ?, ? , ?, ?)")) {
            // Parameters start with 1
            preparedStatement.setString(1, trm.getMetaData().getName());
            preparedStatement.setLong(2, trm.getMetaData().getFileLength());
            preparedStatement.setInt(3, trm.getMetaData().getX());
            preparedStatement.setInt(4, trm.getMetaData().getY());
            preparedStatement.setString(5, trm.getMetaData().getOwnerID());
            preparedStatement.setBlob(6, new SerialBlob(trm.getTorrentFileData()));
            preparedStatement.executeUpdate();
        }
    }

    public TorrentRecordMessage getTorrent(int id) throws SQLException {
        // Result set get the result of the SQL query
        try (PreparedStatement preparedStatement = connect.prepareStatement("select * from notFile.torrents where id = ?")) {
            // Parameters start with 1
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new TorrentRecordMessage(
                        new MetaData(resultSet.getString("name"),
                            resultSet.getString("owner"),
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getLong("filesize")),
                        resultSet.getBytes("data"));
                }
            }
        }
        return null;
    }

    public void readDataBase() throws Exception {
        try (// Statements allow to issue SQL queries to the database
             Statement statement = connect.createStatement();
             // Result set get the result of the SQL query
             ResultSet resultSet = statement.executeQuery("select * from notFile.torrents")) {
            writeMetaData(resultSet);
            writeResultSet(resultSet);
        }
    }

    public List<MetaData> searchInDatabase(List<Attribute> attributes) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        for (Attribute attribute : attributes) {
            queryBuilder.append(attribute.getName()).append(" ")
                .append(Attribute.relationToOperator.get(attribute.getRelation())).append(" ")
                .append(attribute.getValue())
                .append(" AND ");
        }
        String condition = queryBuilder.toString();
        if (condition.endsWith("AND ")) {
            condition = condition.substring(0, condition.length() - 4);
        }

        List<MetaData> results = new ArrayList<>();
        try (Statement stmt = connect.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM notFile.torrents WHERE " + condition)) {
            while (resultSet.next()) {
                results.add(new MetaData(
                    resultSet.getString("name"),
                    resultSet.getString("owner"),
                    resultSet.getInt("x"),
                    resultSet.getInt("y"),
                    resultSet.getLong("filesize")));
            }
        }
        return results;
    }

    private void writeMetaData(ResultSet resultSet) throws SQLException {
        // Now get some metadata from the database
        // Result set get the result of the SQL query
        System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
        for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            System.out.println("Column " + i + " " + resultSet.getMetaData().getColumnName(i));
        }
    }

    private void writeResultSet(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            int x = resultSet.getInt("x");
            int y = resultSet.getInt("y");
            String owner = resultSet.getString("owner");
            Blob data = resultSet.getBlob("data");
            System.out.println("name: " + name);
            System.out.println("x: " + x);
            System.out.println("y: " + y);
            System.out.println("Owner: " + owner);
            System.out.println("Data: " + data);
        }
    }

    public static void main(String[] args) {
        try {
            MySQLAccess mySQLAccess = new MySQLAccess();

            MetaData metaData = new MetaData("elo.torrent", "Kuba", 0, 0, 123124);
            TorrentRecordMessage torrentRecordMessage = new TorrentRecordMessage(metaData, "Swoighwoegh".getBytes());
            mySQLAccess.addTorrent(torrentRecordMessage);
            mySQLAccess.getTorrent(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
