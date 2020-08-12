package UpdateFunc;

/**
 * @author Xiaoyao.L
 * @date 2020/8/12 18:44
 * @project LocalUpdate
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FwChecker {

    private static FileWriter file;
    //private static String baseUrl = "https://iotprocessorstorage.blob.core.windows.net/fileshare"; // 固件存放位置，起始位置，目录下有两个文件一个版本号，一个是固件压缩包

    public static int ReadCurrentVersion() throws Exception {
        int version = 0;
        JSONParser parser = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader("StaticInformation.json"));
            JSONObject jsonObject = (JSONObject) obj;
            String softwareVersion = (String) jsonObject.get("firmwareVersion");
            if (!softwareVersion.isEmpty()) {
                softwareVersion = softwareVersion.replace(".", "");
                version = Integer.parseInt(softwareVersion);

            }

        } catch (IOException e) {
            return 0;
        }

        return version;

    }

    public static int Check(int currentVersion, String versionUrl) throws IOException {

        try {
            URL url = new URL(versionUrl);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String versionLine;
            versionLine = in.readLine();
            int latestVersion = Integer.parseInt((versionLine.replaceFirst("^0+(?!$)", "")));

            if (latestVersion > currentVersion) {
                return latestVersion;
            }
            return currentVersion;
        } catch (Exception e) {

            return currentVersion;
        }

    }

    public static boolean Download(String fwFileLocation, String tempLocation) throws IOException {
        try {
            File tempFile = new File(tempLocation);
            boolean exists = tempFile.exists();
            FileOutputStream fileOutputStream;
            if (!exists) {
                tempFile.getParentFile().mkdirs();
                tempFile.createNewFile();
            }

            URL url = new URL(fwFileLocation);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            fileOutputStream = new FileOutputStream(tempFile);
            FileChannel fileChannel = fileOutputStream.getChannel();
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            fileOutputStream.close();

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static boolean Update(String tempFile, String targetLocation, int latestVersion, String staticFileLocation)
            throws IOException {

        try {
            //System.out.println("unpacking...");
            Runtime runtime = Runtime.getRuntime();
            //Process process1 =runtime.exec(new String[]{"rm" ,"/home/root/PC-LCD/UI_interface"});
            //Process process2 =runtime.exec(new String[]{"rm" ,"/home/root/PC-LCD/ocpp.jar"});

            byte[] buffer = new byte[4096];
            File destinationFolder = new File(targetLocation);
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = CheckFile(destinationFolder, zipEntry);
                if(!newFile.canWrite())
                {
                	newFile.setReadable(true);
                }

                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            //System.out.println("update config...");
            WriteUpdatedVersion(latestVersion, staticFileLocation);

            //System.out.println("update successfully, will take effect from next reboot");

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private static File CheckFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static void WriteUpdatedVersion(int version, String staticFileLocation) throws Exception, IOException {

        JSONParser parser = new JSONParser();
        try {

            Object obj = parser.parse(new FileReader(staticFileLocation));
            JSONObject jsonObject = (JSONObject) obj;
            jsonObject.put("firmwareVersion", Integer.toString(version));
            file = new FileWriter(staticFileLocation);
            file.write(jsonObject.toJSONString());

        } catch (IOException e) {
            //System.out.println("failed to update version in config file...");
        }

        finally {
            file.flush();
            file.close();
        }

    }
}

