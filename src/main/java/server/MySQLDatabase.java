package server;

import data.Attribute;
import data.MetaData;
import data.messages.LoginMessage;
import data.messages.SearchResponseTorrentMessage;
import data.messages.TorrentRecordMessage;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MySQLDatabase extends Database{
    private Connection connect;

    public MySQLDatabase() {
        // This will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://localhost/notFile?"
                    + "user=root&password=admin");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        // Setup the connection with the DB
    }

    @Override
    public String login(LoginMessage loginMessage) {
        try (PreparedStatement preparedStatement = connect.prepareStatement("SELECT password , groupID FROM notFile.users WHERE groupID = ? AND userID = ? ")){
                preparedStatement.setString(1, loginMessage.getGroupName());
                preparedStatement.setString(2, loginMessage.getUserName());

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        if (resultSet.getString("password").equals(loginMessage.getPassword())){
                            return "OK";
                        } else {
                            return "ACCESS_DENIED";
                        }
                    } else {
                        try (PreparedStatement preparedStatement2 = connect.prepareStatement("insert into  notFile.users values (default, ?, ?, ?)")) {
                            // Parameters start with 1
                            preparedStatement2.setString(1, loginMessage.getUserName());
                            preparedStatement2.setString(2, loginMessage.getGroupName());
                            preparedStatement2.setString(3, loginMessage.getPassword());
                            preparedStatement2.executeUpdate();
                            return "NEW";
                        }
                    }
                }

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addTorrent(TorrentRecordMessage trm) {
        try (PreparedStatement preparedStatement =
                 connect.prepareStatement("insert into  notFile.torrents values (default, ?, ?, ?, ? , ?, ?, ?)")) {
            // Parameters start with 1
            preparedStatement.setString(1, trm.getMetaData().getName());
            preparedStatement.setLong(2, trm.getMetaData().getFileLength());
            preparedStatement.setInt(3, trm.getMetaData().getX());
            preparedStatement.setInt(4, trm.getMetaData().getY());
            preparedStatement.setString(5, trm.getMetaData().getOwnerID());
            preparedStatement.setBoolean(6, trm.getMetaData().isAccessPublic());
            preparedStatement.setBlob(7, new SerialBlob(trm.getTorrentFileData()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TorrentRecordMessage getTorrent(int id){
        // Result set get the result of the SQL query
        try (PreparedStatement preparedStatement = connect.prepareStatement("select * from notFile.torrents where id = ?")) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new TorrentRecordMessage(
                        new MetaData(resultSet.getString("name"),
                            resultSet.getString("owner"),
                            resultSet.getInt("x"),
                            resultSet.getInt("y"),
                            resultSet.getLong("filesize"),
                            resultSet.getBoolean("public")),
                        resultSet.getBytes("data"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SearchResponseTorrentMessage searchTorrents(List<Attribute> attributes) {
        StringBuilder queryBuilder = new StringBuilder();
        Iterator <Attribute> iterator = attributes.iterator();

        if(iterator.hasNext()){
            queryBuilder.append(" WHERE " );
        }
        while(iterator.hasNext()){
            Attribute attribute = iterator.next();
            queryBuilder.append(attribute.getName()).append(" ")
                    .append(Attribute.relationToOperator.get(attribute.getRelation())).append(" ");
            if (attribute.getType().equals("String")){
                queryBuilder.append("'"+attribute.getValue()+"'");
            }else{
                queryBuilder.append(attribute.getValue());
            }
            if (iterator.hasNext())
                queryBuilder.append(" AND ");
        }

        String condition = queryBuilder.toString();

        List<MetaData> results = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        try (Statement stmt = connect.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM notFile.torrents "+ condition)) {
            while (resultSet.next()) {
                results.add(new MetaData(
                    resultSet.getString("name"),
                    resultSet.getString("owner"),
                    resultSet.getInt("x"),
                    resultSet.getInt("y"),
                    resultSet.getLong("filesize"),
                    resultSet.getBoolean("public")));
                indexes.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SearchResponseTorrentMessage(results,indexes);
    }

    @Override
    public SearchResponseTorrentMessage getAllTorrents() {
        List<MetaData> results = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        try (Statement stmt = connect.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT * FROM notFile.torrents ")) {
            while (resultSet.next()) {
                results.add(new MetaData(
                        resultSet.getString("name"),
                        resultSet.getString("owner"),
                        resultSet.getInt("x"),
                        resultSet.getInt("y"),
                        resultSet.getLong("filesize"),
                        resultSet.getBoolean("public")));
                indexes.add(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SearchResponseTorrentMessage(results,indexes);
    }
}
