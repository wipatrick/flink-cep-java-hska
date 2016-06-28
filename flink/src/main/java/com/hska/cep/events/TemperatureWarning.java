package com.hska.cep.events;

/**
 * Created by wipatrick on 20.06.16.
 */
public class TemperatureWarning {

    private int sensorID;
    private double averageTemperature;

    public TemperatureWarning(int sensorID, double averageTemperature) {
        this.sensorID = sensorID;
        this.averageTemperature = averageTemperature;
    }

    public TemperatureWarning() {
        this(-1, -1);
    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSensorID(int sensorID) {
        this.sensorID = sensorID;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemperatureWarning) {
            TemperatureWarning other = (TemperatureWarning) obj;

            return sensorID == other.sensorID && averageTemperature == other.averageTemperature;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 41 * sensorID + Double.hashCode(averageTemperature);
    }

    @Override
    public String toString() {
        return "TemperatureWarning(" + getSensorID() + ", " + averageTemperature + ")";
    }
}
