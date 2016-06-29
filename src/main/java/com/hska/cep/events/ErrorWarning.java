package com.hska.cep.events;

/**
 * Created by wipatrick on 21.06.16.
 */
public class ErrorWarning {

    private int sensorID;
    private int errorCode;

    public ErrorWarning(int sensorID, int errorCode) {
        this.sensorID = sensorID;
        this.errorCode = errorCode;
    }

    public ErrorWarning() {
        this(-1, -1);
    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSensorID(int sensorID) {
        this.sensorID = sensorID;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemperatureWarning) {
            ErrorWarning other = (ErrorWarning) obj;

            return sensorID == other.sensorID && errorCode == other.errorCode;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 41 * sensorID + errorCode;
    }

    @Override
    public String toString() {
        return "ErrorWarning(" + getSensorID() + ", " + errorCode + ")";
    }
}
