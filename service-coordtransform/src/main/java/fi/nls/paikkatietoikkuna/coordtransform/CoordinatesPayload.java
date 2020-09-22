package fi.nls.paikkatietoikkuna.coordtransform;

import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class CoordinatesPayload {

    private boolean partialParse = false;
    private List<Coordinate> coords = new ArrayList<>();
    private CoordTransFileSettings exportSettings;
    private List<String> headerRows = new ArrayList<>();
    private List<String> ids = new ArrayList<>();
    private List<String> lineEnds = new ArrayList<>();


    public void setPartialParse(boolean isPartial) {
        partialParse = isPartial;
    }

    public boolean hasMore() {
        return partialParse;
    }

    public void addCoordinates(List<Coordinate> list) {
        coords.addAll(list);
    }

    public void addCoordinate(Coordinate c) {
        coords.add(c);
    }

    public List<Coordinate> getCoords() {
        return coords;
    }
    public int size() {
        return coords.size();
    }
    public boolean isEmpty() {
        return coords.isEmpty();
    }

    public CoordTransFileSettings getExportSettings() {
        return exportSettings;
    }

    public void setExportSettings(CoordTransFileSettings exportSettings) {
        this.exportSettings = exportSettings;
    }
    public List<String> getLineEnds() {
        return lineEnds;
    }

    public void addLineEnd(String lineEnd) {
        this.lineEnds.add(lineEnd);
    }
    public List<String> getHeaderRows() {
        return headerRows;
    }

    public void addHeaderRow(String row) {
        this.headerRows.add(row);
    }

    public List<String> getIds() {
        return ids;
    }

    public void addId(String id) {
        this.ids.add(id);
    }
}
