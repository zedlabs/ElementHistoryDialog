package me.zed.elementhistorydialog;

public class OsmApiException extends Exception {

    private int code;
    
    /**
     * Construct a new exception
     *
     * @param msg the error message
     * @param code the error code
     */
    public OsmApiException(final String msg, int code) {
        super(msg);
        this.code = code;
    }
    
    /**
     * Get the error code
     * 
     * @return the error code
     */
    public int getErrorCode() {
        return code;
    }
}
