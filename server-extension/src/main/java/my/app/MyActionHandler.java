package my.app;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Dummy Rest action route
 */
@OskariActionRoute("MyAction")
public class MyActionHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(MyActionHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        LOG.info(params.getUser(), "accessing route", getName());
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        ResponseHelper.writeResponse(params, "Hello " + params.getUser().getFullName());
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        throw new ActionException("This will be logged including stack trace");
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        throw new ActionParamsException("Notify there was something wrong with the params");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        throw new ActionDeniedException("Not deleting anything");
    }


}
