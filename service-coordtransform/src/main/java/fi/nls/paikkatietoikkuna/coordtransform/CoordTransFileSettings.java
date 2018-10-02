package fi.nls.paikkatietoikkuna.coordtransform;

import java.util.ArrayList;
import java.util.List;

public class CoordTransFileSettings {
    private String fileName;
    private String unit;
    private String lineSeparator;
    private String coordinateSeparator;
    private int headerLineCount;
    private int decimalCount;
    private char decimalSeparator;
    private boolean axisFlip;
    private boolean prefixId;
    private boolean writeHeader;
    private boolean writeLineEndings;
    private boolean writeCardinals;

    private boolean hasMoreCoordinates = false;
    private List<String> headerRows = new ArrayList<>();
    private List<String> ids = new ArrayList<>();
    private List<String> lineEnds = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getHeaderLineCount() {
        return headerLineCount;
    }

    public void setHeaderLineCount(int headerLineCount) {
        this.headerLineCount = headerLineCount;
    }

    public boolean isAxisFlip() {
        return axisFlip;
    }

    public void setAxisFlip(boolean axisFlip) {
        this.axisFlip = axisFlip;
    }

    public boolean isPrefixId() {
        return prefixId;
    }

    public void setPrefixId(boolean prefixId) {
        this.prefixId = prefixId;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<String> getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(List<String> headerRows) {
        this.headerRows = headerRows;
    }

    public void addHeaderRow(String row) {
        this.headerRows.add(row);
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public void addId(String id) {
        this.ids.add(id);
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    public String getCoordinateSeparator() {
        return coordinateSeparator;
    }

    public void setCoordinateSeparator(String coordinateSeparator) {
        this.coordinateSeparator = coordinateSeparator;
    }

    public boolean isWriteHeader() {
        return writeHeader;
    }

    public void setWriteHeader(boolean writeHeader) {
        this.writeHeader = writeHeader;
    }

    public boolean isWriteLineEndings() {
        return writeLineEndings;
    }

    public void setWriteLineEndings(boolean writeLineEndings) {
        this.writeLineEndings = writeLineEndings;
    }

    public List<String> getLineEnds() {
        return lineEnds;
    }

    public void setLineEnds(List<String> lineEnds) {
        this.lineEnds = lineEnds;
    }

    public void addLineEnd(String lineEnd) {
        this.lineEnds.add(lineEnd);
    }

    public boolean isWriteCardinals() {
        return writeCardinals;
    }

    public void setWriteCardinals(boolean writeCardinals) {
        this.writeCardinals = writeCardinals;
    }

    public int getDecimalCount() {
        return decimalCount;
    }

    public void setDecimalCount(int decimalCount) {
        this.decimalCount = decimalCount;
    }

    public void copyArrays(CoordTransFileSettings from) {
        headerRows = from.getHeaderRows();
        ids = from.getIds();
        lineEnds = from.getLineEnds();
    }

    public boolean isHasMoreCoordinates() {
        return hasMoreCoordinates;
    }

    public void setHasMoreCoordinates(boolean hasMoreCoordinates) {
        this.hasMoreCoordinates = hasMoreCoordinates;
    }
}
    
    