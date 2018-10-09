package fi.nls.paikkatietoikkuna.coordtransform;

/**
 * Created by zakar on 03/10/2018.
 */
public enum TransformationType {
    F2A,
    F2F,
    F2R,
    A2A,
    A2F;

    public boolean isFileInput() {
        return toString().charAt(0) == 'F';
    }

    public boolean isFileOutput() {
        return toString().charAt(2) == 'F';
    }

    public boolean isTransform() {
        return toString().charAt(2) != 'R';
    }
}
