package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DownloadProcess {
    private final String command;
    String downloading;
    String server;
    String downloadTo;
    String progress;
    String time;
    boolean finished = false;

    public DownloadProcess(String command) {
        this.command = command;
    }

    Process process;

    @Override
    public String toString() {
        if(! finished)
            return this.downloading + " " + this.server + " " + this.downloadTo + " " + this.progress + " " + this.time;
        else
            return this.downloading + " finished";
    }

    public void start() {
        try {
            process = Runtime.getRuntime().exec(command);

            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains("Downloading:"))
                    this.downloading = line;
                else if (line.contains("Server"))
                    this.server = line;
                else if (line.contains("Downloading to:"))
                    this.downloadTo = line;
                else if (line.contains("Speed:"))
                    this.progress = line;
                else if (line.contains("Running"))
                    this.time = line;
            }
            input.close();
            process.waitFor();
            finished = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
