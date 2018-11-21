package client;

import java.io.IOException;
import java.util.Map;

public class WebtorrentWraper {


    public String createTorrent(String torrentName,String pathToFile,String outPath,String announce){

        String npmPath=".";
        String[] paths = System.getenv("PATH").split(";");
        for(String path :paths){
            if (path.contains("npm")){
                npmPath = path;
                break;
            }
        }

        try {
            Process exec = Runtime.getRuntime().exec(
                    npmPath+"/webtorrent.cmd "+
                    "create "+pathToFile+" "+
                    "--announce "+announce+" "+
                    "--out "+outPath+"/"+torrentName+".torrent",
                    new String[]{System.getenv("PATH")}
            );
            exec.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return "OK";
    }


    public static void main(String[] args) {

        new WebtorrentWraper().createTorrent("elo","D:\\Test\\A\\down\\todo.txt","D:\\Test\\A",
                "http://localhost:8080/announce");


    }


}
