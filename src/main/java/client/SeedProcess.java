package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SeedProcess {

    private final String command;
    String file;
    String progress;
    String status;

    public SeedProcess(String command) {
        this.command = command;
    }

    Process process;

    @Override
    public String toString() {
            return this.file + " " + this.progress + " " + this.status;
    }

    public void start() {
        try {
            process = Runtime.getRuntime().exec(command);
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains("Seeding:"))
                    this.file = line;
                else if (line.contains("Speed:"))
                    this.progress = line;
                else if (line.contains("Running"))
                    this.status = line;
            }
            input.close();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
