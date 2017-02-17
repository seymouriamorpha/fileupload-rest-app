package com.epam.client;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

public class Client {
    private static final Logger LOG = Logger.getLogger(Client.class);
    private static final String SERVICE_URL = "http://localhost:8080/fileupload-rest-app-service/fileupdownloadService/";
    private WebClient client;
    private String name;
    private boolean active;

    public Client(String name) {
        client = WebClient.create(SERVICE_URL);
        client.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.MULTIPART_FORM_DATA);
        this.name = name;
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void uploadFile(Resourse resourse, Client otherClient) throws IOException {
        while (active) {
            if (resourse.getOwner() != this) {
                try {
                    wait(10);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            if (otherClient.isActive()) {
                System.out.println(getName() + " : handover the resource to the client " + otherClient.getName());
                resourse.setOwner(otherClient);
                continue;
            }
            System.out.println(getName() + ": working on the common resource");
            active = false;
            resourse.setOwner(otherClient);
            File file = new File(resourse.getFilepath());
            if (!file.exists()) {
                LOG.warn("File: " + resourse.getFilepath() + " doesn't exist!");
                return;
            }
            ContentDisposition cd = new ContentDisposition("attachment;filename=" + file.getName());
            Attachment att = new Attachment("file", FileUtils.openInputStream(file), cd);
            Response response = client.post(att);
            if (doesResponseContainError(response)) {
                return;
            }
            if (response.getStatus()==Response.Status.OK.getStatusCode()) {
                LOG.info("File: " + file.getName() + " successfully uploaded to server");
            }
        }
    }

    public void downloadFile(String filename, String output) throws IOException {
        resetPath();
        client.path("/" + filename + "/");
        Response response = client.get();
        if (doesResponseContainError(response)) {
            return;
        }
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            Attachment att = response.readEntity(Attachment.class);
            File file = new File(output + filename);
            FileUtils.copyInputStreamToFile(att.getDataHandler().getInputStream(), file);
            LOG.info("File: " + filename + " successfully downloaded from server to " + output);
        } else {
            LOG.warn("Server can't get File: " + filename + " from server");
        }
    }

    private void resetPath() {
        client.reset();
        client.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.MULTIPART_FORM_DATA);
    }

    private static boolean doesResponseContainError(Response response) {
        Response.StatusType statusInfo = response.getStatusInfo();
        LOG.info("STATUS " + statusInfo.getReasonPhrase());
        if (statusInfo.getFamily() == Response.Status.Family.SERVER_ERROR) {
            LOG.error("ERROR on the server: " + response.readEntity(Exception.class).getMessage());
            return true;
        }
        return false;
    }

}
