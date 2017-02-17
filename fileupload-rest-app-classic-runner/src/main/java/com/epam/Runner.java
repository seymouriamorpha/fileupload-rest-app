package com.epam;

import com.epam.client.Client;
import com.epam.client.Resourse;
import com.epam.utils.PropertiesReader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Runner {
    private static Logger log = Logger.getLogger(Runner.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        final Client client1 = new Client("Client 1");
        final Client client2 = new Client("Client 2");

        final PropertiesReader propertiesReader = PropertiesReader
                .getInstance(Runner.class.getClassLoader().getResourceAsStream("config.properties"));

        Set<String> keys = propertiesReader.getPropertyKeys();
        if(keys.isEmpty()) {
            log.warn("There are no files to upload");
        }

        log.info("\n================= POST file to server ==================");

        String file = propertiesReader.getProperty("file1");
        final Resourse resourse = new Resourse(file, client1);

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                try {
                    client1.uploadFile(resourse, client2);
                } catch (IOException e) {
                    log.error("IO error: " + e.getMessage());
                }
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    client2.uploadFile(resourse, client1);
                } catch (IOException e) {
                    log.error("IO error: " + e.getMessage());
                }
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        log.info("\n================= GET all files from server ==================");
        final String output = propertiesReader.getProperty("output");
        for(final String key: keys){
            if(key.startsWith("file")){
                final String filename = new File(propertiesReader.getProperty(key)).getName();
                new Thread(){
                    public void run(){
                        try {
                            log.info("Starting download file: " + filename);
                            client1.downloadFile(filename, output);
                        } catch (IOException e) {
                            log.error("IO error: " + e.getMessage());
                        }
                    }
                }.start();
            }
        }
    }
}
