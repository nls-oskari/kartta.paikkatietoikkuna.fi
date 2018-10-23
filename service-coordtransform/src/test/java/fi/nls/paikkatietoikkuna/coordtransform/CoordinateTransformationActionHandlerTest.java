package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;

import fi.nls.oskari.control.ActionException;

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
        CoordTransFileSettings file = getFileSettings();
        assertEquals("test.txt", file.getFileName());
        assertEquals('.', file.getDecimalSeparator());
        assertEquals("tab", file.getCoordinateSeparator());
        assertEquals("win", file.getLineSeparator());
        assertEquals(2, file.getHeaderLineCount());
        assertEquals(5, file.getDecimalCount());
        assertEquals("degree", file.getUnit());
        assertEquals(true, file.isAxisFlip());
        assertEquals(true, file.isPrefixId());
        assertEquals(false, file.isWriteCardinals());
        assertEquals(true, file.isWriteLineEndings());
        assertEquals(true, file.isWriteHeader());
    }
    private CoordTransFileSettings getFileSettings (){
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
                + "\"lineSeparator\":\"win\","
                + "\"decimalCount\":5,"
                + "\"headerLineCount\":2}";
        try {
            return mapper.readValue(json, CoordTransFileSettings.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Coordinate> getRandomCoordinates(int n, double min, double max) {
        return getRandomCoordinates(n, min, max, min, max, min, max);
    }

    private List<Coordinate> getRandomCoordinates(int n,
            double minX, double maxX,
            double minY, double maxY,
            double minZ, double maxZ) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Coordinate> coordinates = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Coordinate coord;
            if (minZ == 0 && maxZ == 0) {
                coord = new Coordinate(
                        random.nextDouble(minX, maxX),
                        random.nextDouble(minY, maxY));
            } else {
                coord = new Coordinate(
                        random.nextDouble(minX, maxX),
                        random.nextDouble(minY, maxY),
                        random.nextDouble(minZ, maxZ));
            }
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

    @Test
    @Ignore("Requires connection to external service")
    public void transformTest() throws ActionException {
        CoordinateTransformationActionHandler handler = new CoordinateTransformationActionHandler("https://coordtrans.maanmittauslaitos.fi/CoordTrans-1.0/CoordTrans");
        int n = 1000;

        List<Coordinate> coordinates = getRandomCoordinates(n, 300000, 600000, 6700000, 6730000, 0, 0);
        List<Coordinate> originals = coordinates.stream().map(c -> new Coordinate(c)).collect(Collectors.toList());
        handler.transform("EPSG:3067", "EPSG:4258", 2, coordinates);
        assertEquals(originals.size(), coordinates.size());
        for (int i = 0; i < originals.size(); i++) {
            Coordinate original = originals.get(i);
            Coordinate transformed = coordinates.get(i);
            assertNotEquals(original.x, transformed.x, 0);
            assertNotEquals(original.y, transformed.y, 0);
        }

        handler.transform("EPSG:4258", "EPSG:3067", 2, coordinates);
        assertEquals(originals.size(), coordinates.size());
        for (int i = 0; i < originals.size(); i++) {
            Coordinate original = originals.get(i);
            Coordinate transformed = coordinates.get(i);
            // Allow 1mm error in transformation
            assertEquals(original.x, transformed.x, 0.001);
            assertEquals(original.y, transformed.y, 0.001);
        }
    }

}
