import com.microsoft.sqlserver.jdbc.SQLServerResultSet;
import java.io.IOException;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner SQLScanner = new Scanner(System.in);
        System.out.println("Enter DB IP, DB port, DB name, user account, password, owner, table, source drive, destination drive and prefix folder : ");
        String DB_IP = SQLScanner.next();
        String DB_port = SQLScanner.next();
        String DB_name = SQLScanner.next();
        String user_account = SQLScanner.next();
        String password = SQLScanner.next();

        String connectionUrl =
                "jdbc:sqlserver://" + DB_IP + ":" + DB_port + ";"
                        +"databaseName=" + DB_name + ";"
                        +"user=" + user_account + ";"
                        +"password=" + password + ";"
                        +"encrypt=true;"
                        +"trustServerCertificate=true;";

        String tableName = "FISH.TestTable";
        String columnName1 = "CRMYY";
        String columnName2 = "CRMID";
        String columnName3 = "CRMNO";
        String columnName4 = "CHKNO";
        String columnName5 = "ORGNO";
        String columnName6 = "FILENM";



        try (Connection con = DriverManager.getConnection(connectionUrl);) {
            //Class.forName("com.mysql.jdbc.Driver");
            System.out.println("successed connect to MSSQL");
            String sql = "INSERT INTO " + tableName + " (" + columnName1 + ", " + columnName2 +  ", " + columnName3 +  ", " + columnName4 +  ", " + columnName5 +  ", " + columnName6 + ") VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(sql);


            for (int i = 1; i <= 10000; i++) {
                pstmt.setString(1, Integer.toString(i));
                pstmt.setString(2, Integer.toString(i));
                pstmt.setString(3, Integer.toString(i));
                pstmt.setString(4, Integer.toString(i));
                pstmt.setString(5, "NNN");
                pstmt.setString(6, "/nw/FISH/type/test" + i + ".txt");
                pstmt.executeUpdate();
            }

            System.out.println("Data inserted successfully.");

        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            System.out.println("failed connect to MSSQL");
            e.printStackTrace();
        }
    }
}
