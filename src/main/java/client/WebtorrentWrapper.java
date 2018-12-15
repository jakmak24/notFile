package client;

import server.Server;

import java.io.IOException;


public class WebtorrentWrapper {

    private String webtorrentPath;

    public WebtorrentWrapper(){

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


    public DownloadProcess downloadTorrent(String pathToTorrent, String outDir){
            return  new DownloadProcess(
                    webtorrentPath +
                            " download "+ pathToTorrent +
                            " --out "+ outDir);
    }

    public SeedProcess seedTorrent(String pathToTorrent,String outDir){
        return new SeedProcess(
                    webtorrentPath +
                            " download "+ pathToTorrent +
                            " --out "+ outDir+
                            " --keep-seeding"
            );
    }



}
