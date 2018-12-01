package client;

import java.io.IOException;
import java.util.Map;

public class WebtorrentWraper {

    private String webtorrentPath;

    public WebtorrentWraper(){

        if(System.getProperty("os.name").contains("Linux")){
            webtorrentPath = "/usr/local/bin/webtorrent";
        }else if (System.getProperty("os.name").contains("Windows")){
            String[] paths = System.getenv("PATH").split(";");
            for(String path :paths){
                if (path.endsWith("npm")){
                    webtorrentPath = path+"/webtorrent.cmd";
                    break;
                }
            }
        }

    }

    public String createTorrent(String torrentName,String pathToFile,String outPath,String announce){

        try {
            Process exec = Runtime.getRuntime().exec(
                    webtorrentPath +
                    " create "+pathToFile+
                    " --announce "+announce+
                    " --out "+outPath+"/"+torrentName+".torrent");
            exec.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return outPath+"/"+torrentName+".torrent";
    }

    public void downloadTorrent(String pathToTorrent,String outDir){
        try {
            Process exec = Runtime.getRuntime().exec(
                    webtorrentPath +
                            " download "+ pathToTorrent +
                            " --out "+ outDir);
            exec.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void seedTorrent(String pathToTorrent,String outDir){
        try {
            Process exec = Runtime.getRuntime().exec(
                    webtorrentPath +
                            " download "+ pathToTorrent +
                            " --out "+ outDir+
                            " --keep-seeding"
            );
            exec.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
