package Team;

import java.sql.*;

/**
 * Created by Admin on 9/7/2014.
 */
public class Team {
    private Connection DB;

    public Team(String db) throws ClassNotFoundException {

        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        try {
            // create database connection
            DB = DriverManager.getConnection("jdbc:sqlite:"+db+".db");
            Statement statement = DB.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS team(id INTEGER NOT NULL, fName TEXT NOT NULL, lName TEXT NOT NULL)");
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS id_idx ON team(id)");
        }
        catch(SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        }
    }

    public Connection getDB() {
        return DB;
    }

    public Member getMember(String id) {
        String fName = null;
        String lName = null;
        try {
            Statement statement = DB.createStatement();
            statement.setQueryTimeout(30);
            ResultSet rs = statement.executeQuery("SELECT * FROM team WHERE id = "+id);
            if (rs.next()) {
                fName = rs.getString("fName");
                lName = rs.getString("lName");
                rs.close();
            }else
            {
                rs.close();
                return null;
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
        System.out.println(id+" "+fName+" "+lName);
        Member output = new Member(id, fName, lName);
        output.register(DB);
        return output;
    }

    public boolean addMember(Member mem){
        try {
            Statement statement = DB.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("INSERT OR REPLACE INTO team(id, fName, lName) VALUES(" + mem.getID() + ", '" + mem.getFirstName() + "' , '" + mem.getLastName() + "')");

        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeMember(Member mem) {
        if (mem != null) {
            try {
                Statement statement = DB.createStatement();
                statement.setQueryTimeout(30);

                statement.executeUpdate("DELETE FROM team WHERE id = " + mem.getID());
                System.out.println("DROP TABLE IF EXISTS ID_" + mem.getID());
                statement.executeUpdate("DROP TABLE IF EXISTS ID_" + mem.getID());

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }
}
