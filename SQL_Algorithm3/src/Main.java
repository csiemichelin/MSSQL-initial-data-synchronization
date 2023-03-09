import java.io.*;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.Files;
import java.util.Scanner;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) throws IOException {
        // input string
        Scanner SQLScanner = new Scanner(System.in);
        System.out.println("Enter source root folder path and destination drive: ");
        String SourceFolderPath = SQLScanner.next();
        String DestDrive = SQLScanner.next();
        String DestFolderPath = replaceChars(SourceFolderPath, DestDrive);
        // 設定原始目錄和目標目錄
        File srcDir = new File(SourceFolderPath);
        File destDir = new File(DestFolderPath);
        //遞迴掃描資料夾下的檔案
        scanDirectory(srcDir, destDir);
        //遞迴掃描資料夾下的檔案是否都是archive bit皆為0
        boolean cheackbool = checkArchiveBit(srcDir, destDir);
        if (cheackbool == true) {
            System.out.println("All files are copied completely!");
        }
        else {
            try (FileWriter writer = new FileWriter("C:\\Alg3Log.txt", true)) {
                writer.write("Not all files are copied completely!");
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
    //目的路徑轉換
    public static String replaceChars(String str, String destdrive) {
        if (str == null || str.isEmpty()) {
            // 如果字串為 null 或空字串，回傳原字串
            return str;
        } else {
            // 將字串轉成 char 陣列
            char[] chars = str.toCharArray();

            chars[0] = destdrive.charAt(0);

            // 將 char 陣列轉回字串
            return new String(chars);
        }
    }
    //掃描目錄下的檔案
    private static void scanDirectory(File srcDir, File destDir) throws IOException {
        // 確認原始目錄存在且是目錄
        if (!srcDir.isDirectory()) {
            System.err.println(srcDir.getAbsolutePath() + " not a Directory");
            try (FileWriter writer = new FileWriter("C:\\Alg3Log.txt", true)) {
                writer.write(srcDir.getAbsolutePath() + " not a Directory");
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (!destDir.exists()) { // 如果目錄不存在，則創建目錄
            destDir.mkdirs();
        }

        // 取得原始目錄下的所有檔案和目錄
        File[] files = srcDir.listFiles();

        // 遍歷所有檔案和目錄
        for (File file : files) {
            // 如果是目錄，遞迴呼叫掃描方法
            if (file.isDirectory()) {
                scanDirectory(file, new File(destDir, file.getName()));
            }
            // 如果是檔案，執行複製動作
            else {
                File destFile = new File(destDir, file.getName());
                //初始archive bit
                DosFileAttributeView attrs = Files.getFileAttributeView(file.toPath(), DosFileAttributeView.class);
                // 如果目標檔案已經存在且大小一樣和檢查archive bit是否為0，跳過
                if (destFile.exists()) {
                    if (destFile.length() == file.length() && attrs.readAttributes().isArchive() != true) {
                        System.out.println(destFile.getAbsolutePath() + " is aleady exist!");
                        continue;
                    } else {
                        //將archive bit設為0
                        attrs.setArchive(false);
                        copyFile(file, destFile);
                        //比對兩個檔案是否一樣
                        if (destFile.exists() && destFile.length() == file.length()) {
                            System.out.println(destFile + " copy is finished!");
                        } else {
                            System.out.println(destFile + " copy error!");
                            try (FileWriter writer = new FileWriter("C:\\Alg3Log.txt", true)) {
                                writer.write(destFile + " copy error!");
                                writer.flush();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                else {
                    //將archive bit設為0
                    attrs.setArchive(false);
                    copyFile(file, destFile);
                    //比對兩個檔案是否一樣
                    if (destFile.exists() && destFile.length() == file.length()) {
                        System.out.println(destFile + " copy is finished!");
                    }
                    else {
                        System.out.println(destFile + " copy error!");
                        try (FileWriter writer = new FileWriter("C:\\Alg3Log.txt", true)) {
                            writer.write(destFile + " copy error!");
                            writer.flush();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    //複製檔案
    private static void copyFile(File srcFile, File destFile) throws IOException {
        // 建立檔案輸入流和輸出流
        FileInputStream inStream = new FileInputStream(srcFile);
        FileOutputStream outStream = new FileOutputStream(destFile);

        // 取得檔案通道
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();

        // 複製檔案
        inChannel.transferTo(0, inChannel.size(), outChannel);

        // 關閉資源
        inChannel.close();
        outChannel.close();
        inStream.close();
        outStream.close();
    }
    //檢查全部檔案archive blt是否為0
    private static boolean checkArchiveBit(File srcDir, File destDir) throws IOException {
        // 取得原始目錄下的所有檔案和目錄
        File[] files = srcDir.listFiles();

        // 遍歷所有檔案
        for (File file : files) {
            //初始archive bit
            DosFileAttributeView attrs = Files.getFileAttributeView(file.toPath(), DosFileAttributeView.class);
            // 如果是目錄，遞迴呼叫檢查方法
            if (file.isDirectory()) {
                // 如果子目錄中有 archive bit 不為 0 的檔案，回傳 true
                if (!checkArchiveBit(file, destDir)) {
                    return false;
                }
            } else {
                // 如果是檔案，檢查 archive bit 是否為 0
                if ((attrs.readAttributes().isArchive() != false)) {
                    // 如果 archive bit 不為 0，回傳 false
                    return false;
                }
                // 檢查目的檔案是否已存在
                String destFilePath = destDir.getAbsolutePath() + File.separator + file.getName();
                File destFile = new File(destFilePath);
                if (!destFile.exists() || !destFile.isFile() || destFile.length() != file.length()) {
                    // 如果目的檔案已存在且檔案大小不一樣
                    return false;
                }
            }
        }
        // 如果所有檔案的 archive bit 都為 0，回傳 true
        return true;
    }

}