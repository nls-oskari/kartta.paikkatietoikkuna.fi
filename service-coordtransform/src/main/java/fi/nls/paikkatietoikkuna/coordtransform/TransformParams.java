package fi.nls.paikkatietoikkuna.coordtransform;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransformParams {

    protected static final String PARAM_TRANSFORM_TYPE = "transformType";
    protected static final String PARAM_SOURCE_CRS = "sourceCrs";
    protected static final String PARAM_SOURCE_H_CRS = "sourceHeightCrs";
    protected static final String PARAM_TARGET_CRS = "targetCrs";
    protected static final String PARAM_TARGET_H_CRS = "targetHeightCrs";
    protected static final String PARAM_SOURCE_DIMENSION = "sourceDimension";
    protected static final String PARAM_TARGET_DIMENSION = "targetDimension";
    protected static final String KEY_IMPORT_SETTINGS = "importSettings";
    protected static final String KEY_EXPORT_SETTINGS = "exportSettings";

    public final TransformationType type;
    public final String sourceCRS;
    public final String targetCRS;
    public final FileItem file;
    public final Map<String, String> formParams;
    public final CoordTransFileSettings importSettings;
    public final CoordTransFileSettings exportSettings;
    public final ActionParameters actionParameters;
    public final int inputDimensions;
    public final int outputDimensions;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * 1024;
    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private static final int MB = 1024 * 1024;
    private final int maxFileSize = PropertyUtil.getOptional("coordtransform.max.filesize.mb", 50) * MB;

    public TransformParams(ActionParameters params) throws ActionParamsException {
        actionParameters = params;
        try {
            type = TransformationType.valueOf(params.getRequiredParam(PARAM_TRANSFORM_TYPE));
        } catch (IllegalArgumentException e) {
            throw new ActionParamsException("Unknown transform type");
        }
        if (type.isTransform() == false) {
            sourceCRS = null;
            targetCRS = null;
        } else {
            sourceCRS = getSourceCrs(params);
            targetCRS = getTargetCrs(params);
        }

        inputDimensions = params.getHttpParam(PARAM_SOURCE_DIMENSION, 2);
        outputDimensions = params.getHttpParam(PARAM_TARGET_DIMENSION, 2);
        List<FileItem> fileItems = null;
        if (isMultipart(params.getRequest())) {
            fileItems = getFileItems(params.getRequest());
            formParams = getFormParams(fileItems);
        } else {
            formParams = null;
            if(type.isFileInput()) {
                throw new ActionParamsException("Expected multipart request for file input");
            }
        }
        if (type.isFileInput()) {
            file = getFile(fileItems);
            importSettings = getFileSettings(formParams.get(KEY_IMPORT_SETTINGS), KEY_IMPORT_SETTINGS);
        } else {
            file = null;
            importSettings = null;
        }

        if (type.isFileOutput()) {
            String settingsJSON;
            if(formParams == null) {
                settingsJSON = params.getHttpParam(KEY_EXPORT_SETTINGS);
            } else {
                settingsJSON = formParams.get(KEY_EXPORT_SETTINGS);
            }
            exportSettings = getFileSettings(settingsJSON, KEY_EXPORT_SETTINGS);
        } else {
            exportSettings = null;
        }
    }


    private boolean isMultipart(HttpServletRequest request) {
        return request.getContentType() != null &&
                request.getContentType().toLowerCase().indexOf("multipart/form-data") != -1;
    }

    private CoordTransFileSettings getFileSettings(String settingsJSON, String errorKey) throws ActionParamsException {
        try {
            return CoordinateTransformationActionHandler.mapper.readValue(settingsJSON, CoordTransFileSettings.class);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid file settings: " + errorKey, createErrorResponse("invalid_file_settings", e));
        }
    }

    private String getSourceCrs(ActionParameters params) throws ActionParamsException {
        String sourceCrs = params.getRequiredParam(PARAM_SOURCE_CRS);
        String sourceHeightCrs = params.getHttpParam(PARAM_SOURCE_H_CRS);
        if (sourceHeightCrs != null && !sourceHeightCrs.isEmpty()) {
            return sourceCrs + ',' + sourceHeightCrs;
        }
        return sourceCrs;
    }

    private String getTargetCrs(ActionParameters params) throws ActionParamsException {
        String targetCrs = params.getRequiredParam(PARAM_TARGET_CRS);
        String targetHeightCrs = params.getHttpParam(PARAM_TARGET_H_CRS);
        if (targetHeightCrs != null && !targetHeightCrs.isEmpty()) {
            return targetCrs + ',' + targetHeightCrs;
        }
        return targetCrs;
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionParamsException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(maxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionParamsException("Failed to read files from request", e);
        }
    }

    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }

    private FileItem getFile(List<FileItem> fileItems) throws ActionParamsException {
        return fileItems.stream()
                .filter(f -> !f.isFormField())
                .findAny() // If there are more files we'll get the file or fail miserably
                .orElseThrow(() -> new ActionParamsException("No file entry", createErrorResponse("no_file")));
    }

    protected static JSONObject createErrorResponse(String errorKey, Exception e) {
        JSONObject jsonError = JSONHelper.createJSONObject(CoordinateTransformationActionHandler.KEY_FOR_ERRORS, errorKey);
        if (e != null) {
            JSONHelper.putValue(jsonError, "exception", e.getMessage());
        }
        return jsonError;
    }

    protected static JSONObject createErrorResponse(String errorKey) {
        return createErrorResponse(errorKey, null);
    }

}
