package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import fi.nls.oskari.control.ActionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Test;

public class CoordinateTransformationActionHandlerTest {

    @Test
    public void testParseInputCoordinates() throws IOException, ActionException {
        double[] expect = getRandomDoubleArray(1024, 0.0, 1000.0);
        int dimension = 2;
        byte[] jsonBytes = createJsonArrayOfArrays(expect, dimension);
        CoordinateTransformationActionHandler handler = new CoordinateTransformationActionHandler();
        double[] actual = handler.parseInputCoordinates(new ByteArrayInputStream(jsonBytes), 2);
        Assert.assertArrayEquals(expect, actual, 0.00001);
    }

    private byte[] createJsonArrayOfArrays(double[] actual, int dimension) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator json = new JsonFactory().createGenerator(baos);
        json.writeStartArray();
        for (int i = 0; i < actual.length; i += dimension) {
            json.writeArray(actual, i, dimension);
        }
        json.writeEndArray();
        json.close();
        return baos.toByteArray();
    }

    private double[] getRandomDoubleArray(int size, double min, double max) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double[] actual = new double[size];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = r.nextDouble(min, max);
        }
        return actual;
    }

}
