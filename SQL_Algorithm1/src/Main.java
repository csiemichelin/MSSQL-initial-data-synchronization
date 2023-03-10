import com.microsoft.sqlserver.jdbc.SQLServerResultSet;

import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.logging.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String [] argv) throws IOException  {
        // input string
        Scanner SQLScanner = new Scanner(System.in);
        System.out.println("Enter DB IP, DB port, DB name, user account, password, owner, table, source drive, destination drive and prefix folder : ");
        String DB_IP = SQLScanner.next();
        String DB_port = SQLScanner.next();
        String DB_name = SQLScanner.next();
        String user_account = SQLScanner.next();
        String password = SQLScanner.next();
        String owner = SQLScanner.next();
        String table = SQLScanner.next();
        String source_drive = SQLScanner.next();
        String destination_drive = SQLScanner.next();
        String prefix_folder = SQLScanner.next();
        String PublicCRMYY = "";
        String PublicCRMID = "";
        String PublicCRMNO = "";
        String PublicCHKNO = "";
        String PublicFILENM = "";

        // Create a variable for the connection string.
        String connectionUrl =
                "jdbc:sqlserver://" + DB_IP + ":" + DB_port + ";"
                        +"databaseName=" + DB_name + ";"
                        +"user=" + user_account + ";"
                        +"password=" + password + ";"
                        +"encrypt=true;"
                        +"trustServerCertificate=true;";

        try (Connection con = DriverManager.getConnection(connectionUrl);) {
            System.out.println("successed connect to MSSQL");

            int TotalFileNum = 0;
            int SegmentNum = 1;
            int InitialSegmentNum = 1;
            int totalErrorCount = 0;
            int dataIndex = 0;

            //Select
            String TestTable = "SELECT * FROM " + owner + "." + table;
            Statement stmt1 = con.createStatement();
            ResultSet rs1 = stmt1.executeQuery(TestTable);

            String R01 = "SELECT * FROM " + owner + ".R01 WHERE CRMYY=? AND CRMID=? AND CRMNO=? AND CHKNO=?";
            PreparedStatement stmt2 = con.prepareStatement(R01);

            String Order = "SELECT CRMYY, CRMID, CRMNO, CHKNO FROM " + owner + ".R01 ORDER BY SDDT_6B ASC";
            //??????statement???????????????????????????????????????absolute?????????
            Statement stmt3 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs3 = stmt3.executeQuery(Order);

            //Initial config file
            File file = new File("C:\\Alg1Config.txt");
            FileWriter fw = new FileWriter("C:\\Alg1Config.txt", true);


            // ??????????????????
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            cal.add(Calendar.YEAR, -1911); // ??????????????????????????????
            SimpleDateFormat sdf = new SimpleDateFormat("yyy/MM/dd");
            String rocDate = sdf.format(cal.getTime());
            System.out.println("???????????????????????????" + rocDate);
            String Time = "";

            try{
                if(!file.exists() || file.length() == 0){
                    System.out.println("File created " + file.getName());
                    Time = rocDate.replace("/", "");
                    //System.out.println("test Time: " + Time);
                    SegmentNum = 1;
                    totalErrorCount = 0;
                    InitialSegmentNum = 1;
                    fw.write(Time + "\n");
                    fw.flush(); //??????????????????

                }
                else{
                    BufferedReader br1 = new BufferedReader(new FileReader(file));
                    String firstLine = br1.readLine();
                    String secondLine = br1.readLine();
                    String searchStr1 = "SegmentNum = ";
                    String searchStr2 = "ErrorCount = ";
                    int maxNum = 0;
                    if(firstLine != null && !firstLine.isEmpty()){
                        Time = firstLine;
                        if(secondLine != null && !secondLine.isEmpty()){
                            String line;
                            BufferedReader br2 = new BufferedReader(new FileReader(file));
                            while ((line = br2.readLine()) != null) {
                                if (line.contains(searchStr1)) {
                                    // ???????????????"SegmentNum = "?????????
                                    String[] parts = line.split("\\s+");
                                    for (int i = 0; i < parts.length; i++) {
                                        if (parts[i].equals("=")) {
                                            // ?????????"SegmentNum = "???????????????????????????
                                            if (i+1 < parts.length) {
                                                // ??????"SegmentNum = "??????????????????
                                                int num = Integer.parseInt(parts[i+1]);
                                                if (num > maxNum) {
                                                    maxNum = num;
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                else if (line.contains(searchStr2)) {
                                    // ???????????????"ErrorCount = "?????????
                                    String[] parts = line.split("\\s+");
                                    for (int i = 0; i < parts.length; i++) {
                                        if (parts[i].equals("=")) {
                                            // ?????????"ErrorCount = "???????????????????????????
                                            if (i+1 < parts.length) {
                                                // ??????"ErrorCount = "??????????????????
                                                int num = Integer.parseInt(parts[i+1]);
                                                totalErrorCount += num;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }

                            SegmentNum = maxNum + 1;
                            InitialSegmentNum = SegmentNum;
                        }
                        else{
                            SegmentNum = 1;
                            totalErrorCount = 0;
                        }
                    }
                }
                System.out.println("Time: " + Time);
                System.out.println("SegmentNum: " + SegmentNum);
                System.out.println("totalErrorCount: " + totalErrorCount);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //?????????????????????????????????????????????
            while (rs1.next()) {
                String CRMYY = rs1.getString("CRMYY");
                String CRMID = rs1.getString("CRMID");
                String CRMNO = rs1.getString("CRMNO");
                String CHKNO = rs1.getString("CHKNO");
                String ORGNO = rs1.getString("ORGNO");
                String FILENM = rs1.getString("FILENM");

                if(ORGNO.startsWith("N")){
                    //?????????????????????????????????sql string??????WHERE
                    stmt2.setString(1, CRMYY);
                    stmt2.setString(2, CRMID);
                    stmt2.setString(3, CRMNO);
                    stmt2.setString(4, CHKNO);
                    ResultSet rs2 = stmt2.executeQuery();
                    //???R01????????????????????????SDDT_6B
                    while (rs2.next()) {
                        String SDDT_6B = rs2.getString("SDDT_6B");
                        String CRMYY2 = rs1.getString("CRMYY");
                        String CRMID2 = rs1.getString("CRMID");
                        String CRMNO2 = rs1.getString("CRMNO");
                        String CHKNO2 = rs1.getString("CHKNO");
                        //?????????????????????????????????
                        if (rs2.getString("CRMYY").equals(CRMYY) && rs2.getString("CRMID").equals(CRMID) && rs2.getString("CRMNO").equals(CRMNO) && rs2.getString("CHKNO").equals(CHKNO)) {
                            if ((SDDT_6B.length() == 7 || SDDT_6B.length() == 6) && Integer.valueOf(SDDT_6B) <= Integer.valueOf(Time)) {
                                TotalFileNum++;
                            }
                            else {
                                try (FileWriter writer = new FileWriter("C:\\Alg1Log.txt", true)) {
                                    writer.write("over time of CRMYY: " + CRMYY2 + ", CRMID: " + CRMID2 + ", CRMNO: " + CRMNO2 + ", CHKNO: " + CHKNO2 + ", SDDT_6B: " + SDDT_6B + ", Time: " + Integer.valueOf(Time) + "\n");
                                    writer.flush();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else {
                    System.out.println("ORGNO is not N started");
                    try (FileWriter writer = new FileWriter("C:\\Alg1Log.txt", true)) {
                        writer.write("ORGNO is not N started  of CRMYY: " + CRMYY + ", CRMID: " + CRMID + ", CRMNO: " + CRMNO + ", CHKNO: " + CHKNO + "\n");
                        writer.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            //??????????????????
            System.out.println("Total File number: " + TotalFileNum);
            int total_count = (InitialSegmentNum-1) * 1000;
            System.out.println("Total count: " + total_count + ", InitialSegmentNum: " + InitialSegmentNum);
            //???????????????table?????????????????????????????????
            //&& total_count <= 5025
            while(total_count <= TotalFileNum ){
                //dataindex???????????????segment?????????
                dataIndex = (SegmentNum - 1) * 1000;

                //???SDDT_6B????????????
                System.out.println("dataIndex = " + dataIndex);
                List<String> idList = new ArrayList<>();
                // ??????ResultSet?????????dataIndex
                // ?????????ResultSet????????????????????????101???
                rs3.absolute(dataIndex);
                // ??????ResultSet???????????????dataIndex??????dataIndex+1000???id?????????List???
                int timer = 0;
                while (rs3.next() && timer < 1000) {
                    String CRMYY = rs3.getString("CRMYY");
                    String CRMID = rs3.getString("CRMID");
                    String CRMNO = rs3.getString("CRMNO");
                    String CHKNO = rs3.getString("CHKNO");

                    // ?????????????????????????????????????????????Map???Key
                    String key = CRMYY + "," + CRMID + "," + CRMNO + "," + CHKNO;

                    // ???Key?????????List???
                    idList.add(key);
                    timer++;
                }

                // ????????????StringBuilder?????????????????????SQL????????????
                StringBuilder sb = new StringBuilder();
                // ?????????Key?????????SQL????????????
                for (int i = 0; i < idList.size(); i++) {
                    sb.append("SELECT * FROM " + owner + "." + table +  " WHERE ");
                    sb.append("(");
                    sb.append("CRMYY=");
                    sb.append(idList.get(i).split(",")[0]);
                    sb.append(" AND CRMID='");
                    sb.append(idList.get(i).split(",")[1]);
                    sb.append("'");
                    sb.append(" AND CRMNO=");
                    sb.append(idList.get(i).split(",")[2]);
                    sb.append(" AND CHKNO=");
                    sb.append(idList.get(i).split(",")[3]);
                    sb.append(")");
                    if (i < idList.size() - 1) {
                        sb.append(" UNION ALL ");
                    }
                }
                //System.out.println("SB = " + sb.toString());
                Statement stmt4 = con.createStatement();
                ResultSet rs4 = stmt4.executeQuery(sb.toString());
                int counter = 0;
                int ErrorCount = 0;
                while(rs4.next()) {
                    String FILENM = rs4.getString("FILENM");
                    PublicCHKNO = rs4.getString("CHKNO");
                    PublicCRMYY = rs4.getString("CRMYY");
                    PublicCRMID = rs4.getString("CRMID");
                    PublicCRMNO = rs4.getString("CRMNO");
                    PublicFILENM = FILENM;
                    //System.out.println("FILENM = " + FILENM);
                    StringBuilder sourcepath = new StringBuilder(FILENM);
                    boolean funcbool = ChangeFullPath(source_drive, destination_drive, prefix_folder, owner, sourcepath);
                    //??????FILENM????????????????????????
                    if(funcbool == false){
                        counter++;
                        ErrorCount++;
                        totalErrorCount++;
                        total_count++;
                        // ?????????????????????????????????

                        try (FileWriter writer = new FileWriter("C:\\Alg1Log.txt", true)) {
                            writer.write("An error occurred in FILENM format\n");
                            writer.flush();
                            writer.write("CRMYY: " + PublicCRMYY + ", " + "CRMID: " + PublicCRMID + ", " + "CRMNO: " + PublicCRMNO + ", " + "CHKNO: " + PublicCHKNO + ", " + "FILENM: " + PublicFILENM + "\n");
                            writer.flush();
                            if(total_count == TotalFileNum ){
                                break;
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if(counter == 1000){
                            String strSegment = Integer.toString(SegmentNum);
                            fw.write("SegmentNum = " + strSegment + "\n");
                            fw.flush();
                            String strErrorcount = Integer.toString(ErrorCount);
                            fw.write("ErrorCount = " + ErrorCount + "\n");
                            fw.flush();
                        }

                        continue;
                    }
                    if(total_count == TotalFileNum ){
                        break;
                    }
                    //?????????????????????????????????
                    //System.out.println("source full path = " + sourcepath.toString());
                    //???????????????????????????
                    String DestFullPath = "";
                    if(!sourcepath.toString().isEmpty()){
                        DestFullPath = sourcepath.toString();
                        StringBuilder sbdest = new StringBuilder(DestFullPath);
                        sbdest.setCharAt(0, destination_drive.charAt(0));
                        DestFullPath = sbdest.toString();
                        //System.out.println("destination full path = " + DestFullPath);
                    }

                    // ????????????????????????????????????????????????????????????????????????
                    File targetFile = new File(DestFullPath);
                    File sourceFile = new File(sourcepath.toString());
                    File targetDir = targetFile.getParentFile();
                    if (!targetDir.exists() && sourceFile.exists()) {
                        targetDir.mkdirs();
                    }
                    //??????????????????????????????
                    try {
                        //System.out.println("source full path = " + sourcepath.toString());
                        //System.out.println("destination full path = " + DestFullPath);
                        File DestFill = new File(DestFullPath.toString());

                        if (DestFill.exists()) {
                            System.out.println(DestFill.toString() + " is already exist???");
                        }
                        // Create a file input stream to read the source file
                        FileInputStream inputStream = new FileInputStream(sourcepath.toString());

                        // Create a file output stream to write the target file
                        FileOutputStream outputStream = new FileOutputStream(DestFullPath);

                        // Copy the contents of the source file to the target file
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }

                        // Close the streams
                        inputStream.close();
                        outputStream.close();

                        //System.out.println("File copied successfully.");
                    } catch (IOException e) {
                        counter++;
                        total_count++;
                        ErrorCount++;
                        totalErrorCount++;

                        // ?????????????????????????????????
                        try (FileWriter writer = new FileWriter("C:\\Alg1Log.txt", true)) {
                            writer.write("An error occurred while copying file: " + e.getMessage() + "\n");
                            writer.flush();
                            writer.write("CRMYY: " + PublicCRMYY + ", " + "CRMID: " + PublicCRMID + ", " + "CRMNO: " + PublicCRMNO + ", " + "CHKNO: " + PublicCHKNO + ", " + "FILENM: " + PublicFILENM + ", " + sourcepath.toString() + "\n");
                            writer.flush();
                            if(total_count == TotalFileNum ){
                                break;
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if(counter == 1000){
                            String strSegment = Integer.toString(SegmentNum);
                            fw.write("SegmentNum = " + strSegment + "\n");
                            fw.flush();
                            String strErrorcount = Integer.toString(ErrorCount);
                            fw.write("ErrorCount = " + ErrorCount + "\n");
                            fw.flush();
                        }
                        e.printStackTrace();
                        continue;
                    }
                    //????????????????????????????????????
                    File file1 = new File(sourcepath.toString());
                    File file2 = new File(DestFullPath);
                    //System.out.println("File path is = " + DestFullPath);
                    if (file1.length() == file2.length()) {
                        System.out.println("The file sizes are the same.");
                    } else {
                        System.out.println("The file sizes are different.");
                    }

                    counter++;
                    total_count++;
                    if(total_count == TotalFileNum ){
                        break;
                    }
                    if(counter == 1000){
                        String strSegment = Integer.toString(SegmentNum);
                        fw.write("SegmentNum = " + strSegment + "\n");
                        fw.flush();
                        String strErrorcount = Integer.toString(ErrorCount);
                        fw.write("ErrorCount = " + ErrorCount + "\n");
                        fw.flush();
                        SegmentNum++;
                        counter = 0;
                        ErrorCount = 0;
                    }
                    System.out.println("total_count = " + total_count + ", " + "TotalFileNum = " + TotalFileNum);
                }
                //???????????????????????????????????????
                String Filepath = destination_drive + ":\\\\" + prefix_folder + "\\\\nw\\\\" + owner + "\\\\type";
                System.out.println("File path = " + Filepath);
                File directory = new File(Filepath);
                int count = countFiles(directory);
                System.out.println("The directory contains " + count + " files.");
                if(count == TotalFileNum - totalErrorCount){
                    System.out.println("Copy Successfully!");
                }
                if(total_count == TotalFileNum ){
                    break;
                }
            }
            //???????????????????????????????????????
            fw.flush();
            fw.close();
        }
        // Handle any errors that may have occurred.
        catch (SQLException | IOException e) {
            System.out.println("failed connect to MSSQL");
            e.printStackTrace();
        }
    }
    //???????????????????????????????????????
    public static int countFiles(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFiles(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }
    //???????????????????????????
    public static boolean  ChangeFullPath(String SourceDrive, String DestDrive, String Prefix, String schema, StringBuilder OriginalPath){
        String result = "";
        String stringpath = OriginalPath.toString();
        if(stringpath.matches("^[nN][wW].*")){
            //?????????slash????????????
            result = stringpath.replaceAll("/+", "\\\\");
            result = result.replaceAll("\\\\+", "\\\\");
            result = result.replaceAll("(?i)[nN][wW]", SourceDrive + ":\\\\" + Prefix + "\\\\nw");
        } else if (stringpath.matches("^/+[nN][wW].*")) {
            //?????????slash????????????
            result = stringpath.replaceAll("/+", "\\\\");
            result = result.replace("\\\\+", "\\\\");
            result = result.replaceAll("\\\\(?i)[nN][wW]", SourceDrive + ":\\\\" + Prefix + "\\\\nw");
        } else if (stringpath.matches("^[a-zA-Z]:/+[tT][yY][pP][eE].*")) {
            //?????????slash????????????
            result = stringpath.replaceAll("/+", "\\\\");
            result = result.replaceAll("\\\\+", "\\\\");
            result = result.replaceAll("(?i)[a-z]:\\\\", SourceDrive + ":\\\\" + Prefix + "\\\\nw\\\\" + schema + "\\\\");
        }
        else if (stringpath.matches("^[a-zA-Z]:\\\\+[tT][yY][pP][eE].*")) {
            //?????????slash????????????
            result = stringpath.replaceAll("/+", "\\\\");
            result = result.replaceAll("\\\\+", "\\\\");
            result = result.replaceAll("(?i)[a-z]:\\\\", SourceDrive + ":\\\\" + Prefix + "\\\\nw\\\\" + schema + "\\\\");
        }
        else return false;

        OriginalPath.setLength(0); // ?????? StringBuilder ??????
        OriginalPath.append(result);
        return true;
    };
}