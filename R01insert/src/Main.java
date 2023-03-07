import com.microsoft.sqlserver.jdbc.SQLServerResultSet;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Scanner SQLScanner = new Scanner(System.in);
        System.out.println("Enter DB IP, DB port, DB name, user account, password, owner : ");
        String DB_IP = SQLScanner.next();
        String DB_port = SQLScanner.next();
        String DB_name = SQLScanner.next();
        String user_account = SQLScanner.next();
        String password = SQLScanner.next();

        int min = 1120201;
        int max = 1120302;

        String connectionUrl =
                "jdbc:sqlserver://" + DB_IP + ":" + DB_port + ";"
                        +"databaseName=" + DB_name + ";"
                        +"user=" + user_account + ";"
                        +"password=" + password + ";"
                        +"encrypt=true;"
                        +"trustServerCertificate=true;";

        String tableName = "FISH.R01";
        String columnName1 = "CRMYY";
        String columnName2 = "CRMID";
        String columnName3 = "CRMNO";
        String columnName4 = "CHKNO";
        String columnName5 = "SDDT_6B";



        try (Connection con = DriverManager.getConnection(connectionUrl);) {
            //Class.forName("com.mysql.jdbc.Driver");
            System.out.println("successed connect to MSSQL");
            String sql = "INSERT INTO " + tableName + " (" + columnName1 + ", " + columnName2 +  ", " + columnName3 +  ", " + columnName4 +  ", " + columnName5 + ") VALUES (?, ?, ?, ?, ?)";
            System.out.println("SQL = " + sql);
            PreparedStatement pstmt = con.prepareStatement(sql);


            for (int i = 1; i <= 10000; i++) {
                pstmt.setString(1, Integer.toString(i));
                pstmt.setString(2, Integer.toString(i));
                pstmt.setString(3, Integer.toString(i));
                pstmt.setString(4, Integer.toString(i));

                Random rand = new Random();
                int randomNum = rand.nextInt((max - min) + 1) + min;
                pstmt.setString(5, Integer.toString(randomNum));
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

