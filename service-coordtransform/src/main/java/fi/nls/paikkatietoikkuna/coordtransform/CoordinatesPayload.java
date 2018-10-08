package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class CoordinatesPayload {

    private boolean partialParse = false;
    private List<Coordinate> coords = new ArrayList<>();
    private CoordTransFileSettings exportSettings;


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

    public CoordTransFileSettings getExportSettings() {
        return exportSettings;
    }

    public void setExportSettings(CoordTransFileSettings exportSettings) {
        this.exportSettings = exportSettings;
    }
}
