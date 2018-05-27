package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.control.ActionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class CoordinateTransformationActionHandlerTest {

    @Test
    public void testParseInputCoordinates() throws IOException, ActionException {
        int n = 100;
        int dimension = 3;
        List<Coordinate> expected = getRandomCoordinates(n, 0.0, 1000.0);
        byte[] jsonBytes = createJsonArrayOfArrays(expected, dimension);
        CoordinateTransformationActionHandler handler = new CoordinateTransformationActionHandler();
        List<Coordinate> actual = handler.parseInputCoordinates(new ByteArrayInputStream(jsonBytes), dimension, false);
        for (int i = 0; i < n; i++) {
            Coordinate e = expected.get(i);
            Coordinate a = actual.get(i);
            assertEquals(e.x, a.x, 0.00001);
            assertEquals(e.y, a.y, 0.00001);
            assertEquals(e.z, a.z, 0.00001);
        }
    }

    @Test
    public void testCreateFileSettings (){
        CoordTransFile file = getFileSettings();
        assertEquals("test.txt", file.getFileName());
        assertEquals('.', file.getDecimalSeparator());
        assertEquals("tab", file.getCoordinateSeparator());
        assertEquals(2, file.getHeaderLineCount());
        assertEquals(5, file.getDecimalCount());
        assertEquals("degree", file.getUnit());
        assertEquals(true, file.isAxisFlip());
        assertEquals(true, file.isPrefixId());
        assertEquals(false, file.isWriteCardinals());
        assertEquals(true, file.isWriteLineEndings());
        assertEquals(true, file.isWriteHeader());
    }
    private CoordTransFile getFileSettings (){
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"fileName\":\"test.txt\","
                + "\"unit\":\"degree\","
                + "\"decimalSeparator\":\".\","
                + "\"coordinateSeparator\":\"tab\","
                + "\"prefixId\":true,"
                + "\"writeHeader\":true,"
                + "\"axisFlip\":true,"
                + "\"writeCardinals\":false,"
                + "\"writeLineEndings\":true,"
                + "\"decimalCount\":5,"
                + "\"headerLineCount\":2}";
        try {
             return mapper.readValue(json, CoordTransFile.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Coordinate> getRandomCoordinates(int n, double min, double max) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Coordinate> coordinates = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Coordinate coord = new Coordinate(
                    random.nextDouble(min, max),
                    random.nextDouble(min, max),
                    random.nextDouble(min, max));
            coordinates.add(coord);
        }
        return coordinates;
    }

    private byte[] createJsonArrayOfArrays(List<Coordinate> coords, int dimension) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator json = new JsonFactory().createGenerator(baos);
        json.writeStartArray();
        for (Coordinate coord : coords) {
            json.writeStartArray();
            json.writeNumber(coord.x);
            json.writeNumber(coord.y);
            if (dimension == 3) {
                json.writeNumber(coord.z);
            }
            json.writeEndArray();
        }
        json.writeEndArray();
        json.close();
        return baos.toByteArray();
    }
}
