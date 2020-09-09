package UpdateFunc;

import java.io.IOException;
import java.text.SimpleDateFormat;

import static UpdateFunc.FwChecker.ReadCurrentVersion;

/**
 * @author Xiaoyao.L
 * @date 2020/8/12 19:23
 * @project LocalUpdate
 */
public class main {
    private static String baseUrl = "https://iotprocessorstorage.blob.core.windows.net/fileshare"; // 固件存放位置，起始位置，目录下有两个文件一个版本号，一个是固件压缩包
    //private final static String staticFileLocation = "StaticInformation.json";
    private static String staticFileLocation = "/home/xiaoyao/tmp/StaticInformation.json";
/*    private final static String fwTempLocation = "tmp/tempfwfile.zip";
    private final static String fwDestination = "tmp";*/
    private final static String fwTempLocation = "/tmp/tempfwfile.zip";
    private final static String fwDestination = "/home/root/PC-LCD";
        public static void main(String[] args) throws Exception {

            //System.out.println("checking version...");
            int currentVersion = ReadCurrentVersion();
            
            String versionFileLocation = baseUrl + "/fwversion";
            int latestVersion = FwChecker.Check(currentVersion, versionFileLocation);
            boolean foundNew = latestVersion > currentVersion;



            if (foundNew) {
                String fwurl = baseUrl + "/firmware.zip";
                boolean isDownloaded = FwChecker.Download(fwurl, fwTempLocation);
                //System.out.println("downloading ...");
                if (isDownloaded) {
                    //System.out.println("downloaded ...");
                }
                //System.out.println("updating ...");

                boolean isUpdated = FwChecker.Update(fwTempLocation, fwDestination, latestVersion, staticFileLocation);
                if (isUpdated) {
                    System.out.println("updated");
                }
            } else {
                System.out.println("no new version found");
            }
        }
}


