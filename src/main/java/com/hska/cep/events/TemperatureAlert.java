package com.hska.cep.events;

/**
 * Created by wipatrick on 21.06.16.
 */
public class TemperatureAlert {
    private int sensorId;

    public TemperatureAlert(int rackID) {
        this.sensorId = sensorId;
    }

    public TemperatureAlert() {
        this(-1);
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public int getSensorId() {
        return sensorId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemperatureAlert) {
            TemperatureAlert other = (TemperatureAlert) obj;
            return sensorId == other.sensorId;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return sensorId;
    }

    @Override
    public String toString() {
        return "TemperatureAlert(" + getSensorId() + ")";
    }
}
