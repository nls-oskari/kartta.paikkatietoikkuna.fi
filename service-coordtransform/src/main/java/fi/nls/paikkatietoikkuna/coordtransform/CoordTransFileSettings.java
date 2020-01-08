package fi.nls.paikkatietoikkuna.coordtransform;

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
}
    
    