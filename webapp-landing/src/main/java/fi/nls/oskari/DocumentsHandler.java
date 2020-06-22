package fi.nls.oskari;

import fi.nls.oskari.domain.LegacyDocument;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import static fi.nls.oskari.Helper.*;

/**
 * Support for old document links, now available under Jetty/resources/legacy-docs
 */
@Controller
public class DocumentsHandler {

    private static final Logger LOG = LogManager.getLogger("Documents");

    private Map<String, LegacyDocument> docs = new HashMap<>();

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
        LegacyDocument doc = docs.get(uuid);
        if (doc == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try (InputStream is = getClass().getResourceAsStream(doc.path)) {
            if (is == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                LOG.warn("Resource not found", doc.uuid);
                return;
            }
            if (doc.mimeType != null) {
                response.setContentType(doc.mimeType);
            }
            response.setHeader("Content-Disposition", "attachment; filename=\"" + doc.filename + "\"");
            response.setContentLength(is.available());
            FileCopyUtils.copy(is, response.getOutputStream());
            LOG.info(doc.uuid);
        }
    }

    @PostConstruct
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

        LegacyDocument[] list = getMapper().readValue(getClass().getResourceAsStream(getBasePath() + "/liferay_docs.json"), LegacyDocument[].class);
        for(LegacyDocument doc : list) {
            // setup path for classpath resource
            doc.path = getBasePath() + "/docs/" + doc.path;
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