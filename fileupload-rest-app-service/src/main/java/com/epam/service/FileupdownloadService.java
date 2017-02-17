package com.epam.service;

import com.epam.utils.PropertiesReader;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

@Path("/fileupdownloadService/")
@Produces(MediaType.MULTIPART_FORM_DATA)
@Consumes(MediaType.MULTIPART_FORM_DATA)
public class FileupdownloadService {

    private static Logger log = Logger.getLogger(FileupdownloadService.class);
    private static String SAVE_PATH;

    static {
        try {
            SAVE_PATH = PropertiesReader
                    .getInstance(FileupdownloadService.class.getClassLoader()
                    .getResourceAsStream("config.properties")).getProperty("savePath");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @POST
    public Response uploadFile(Attachment attachment) {
        try {
            String filename = attachment.getContentDisposition().getParameter("filename");
            log.info("<<<<<<<<<<< Invoking uploadFile method, filename is: " + filename+ " >>>>>>>>>>>");
            File file = new File(SAVE_PATH + filename);
            FileUtils.copyInputStreamToFile(attachment.getDataHandler().getInputStream(), file);
            return Response.ok().build();
        } catch (IOException e) {
            log.error(e.getMessage());
            return Response.serverError().entity(e).build();
        }
    }

    @GET
    @Path("/{filename}/")
    public Response downloadFile(@PathParam("filename") String filename) {
        try {
            log.info("<<<<<<<<<<< Invoking downloadFile method, filename is: " + filename + " >>>>>>>>>>>");
            File file = new File(SAVE_PATH + filename);
            if (!file.exists()) {
                return Response.noContent().build();
            }
            ContentDisposition cd = new ContentDisposition("attachment;filename=" + filename);
            Attachment att = new Attachment("file", FileUtils.openInputStream(file), cd);
            return Response.ok(att).build();
        } catch (Exception e) {
            return Response.serverError().entity(e).build();
        }
    }

}
