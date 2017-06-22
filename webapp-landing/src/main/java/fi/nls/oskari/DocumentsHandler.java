package fi.nls.oskari;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Support for old document links, now available under Jetty/resources/legacy-docs
 * Maybe move legacy links handling to another webapp?
 */
@Controller
public class DocumentsHandler {

    private Map<String, LegacyDocument> docs = new HashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    private String basePath = "/legacy";

    @RequestMapping("/documents/**")
    public String documentsPath(HttpServletRequest request) throws Exception {
        request.getRequestURI();
        return "login";
    }

    /*
    /documents/108478/f22964f8-cc49-421e-bf2e-084d54be6a04
     */
    @RequestMapping("/documents/108478/{uuid}")
    public void documentsPath(@PathVariable("uuid") String uuid, HttpServletResponse response) throws Exception {
        writeDocument(uuid, response);
    }

    /*
    /c/document_library/get_file?uuid=2c4a9801-b9e6-473f-aebe-58b9ab3d7935&groupId=108478
     */
    @RequestMapping("/c/document_library/get_file")
    public void documentsParam(@RequestParam("uuid") String uuid, HttpServletResponse response) throws Exception {
        writeDocument(uuid, response);
    }

    protected void writeDocument(String uuid, HttpServletResponse response) throws Exception {
        LegacyDocument doc = findDocument(uuid);
        if(doc.mimeType != null) {
            response.setContentType(doc.mimeType);
        }
        response.setHeader("Content-Disposition", "inline; filename=\"" + doc.filename + "\"");
        try (InputStream is = getClass().getResourceAsStream(doc.path)) {
            response.setContentLength(is.available());
            FileCopyUtils.copy(is, response.getOutputStream());
        }
    }

    protected LegacyDocument findDocument(String uuid) throws Exception {
        if (docs.isEmpty()) {
            readList();
        }
        LegacyDocument doc = docs.get(uuid);
        if (doc == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        return doc;
    }

    private void readList() throws IOException {
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put(".pdf", "application/pdf");
        mimeTypes.put(".doc", "application/msword");
        mimeTypes.put(".docx", "application/msword");
        mimeTypes.put(".ppt", "application/mspowerpoint");
        mimeTypes.put(".pptx", "application/mspowerpoint");
        mimeTypes.put(".zip", "application/zip");
        mimeTypes.put(".txt", "plain/text");
        mimeTypes.put(".xls", "application/excel");
        mimeTypes.put(".xlsx", "application/excel");
        mimeTypes.put(".gpx", "application/gpx+xml");
        mimeTypes.put(".html", "text/html");
        mimeTypes.put(".xml", "text/xml");
        mimeTypes.put(".png", "image/png");
        mimeTypes.put(".jpg", "image/jpg");

        LegacyDocument[] list = objectMapper.readValue(getClass().getResourceAsStream(basePath + "/liferay_docs.json"), LegacyDocument[].class);
        for(LegacyDocument doc : list) {
            // setup path for classpath resource
            doc.path = basePath + "/docs/" + doc.path;
            doc.mimeType = getMimeType(doc.filename, mimeTypes);
            docs.put(doc.uuid, doc);
        }
    }
    private String getMimeType(String filename, Map<String, String> types) {
        for( Map.Entry<String, String> e : types.entrySet()) {
            if(filename.toLowerCase().endsWith(e.getKey())) {
                return e.getValue();
            }
        }
        System.out.println("Mimetype missing for: " + filename);
        return null;
    }
}