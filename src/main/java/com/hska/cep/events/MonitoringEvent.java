package com.hska.cep.events;

/**
 * Created by wipatrick on 20.06.16.
 */
public abstract class MonitoringEvent {

    private int sensorID;
    private long timestamp;

    public MonitoringEvent(int sensorID, long timestamp) {
        this.sensorID = sensorID;
        this.timestamp = timestamp;
    };

    public int getSensorID () { return this.sensorID;}

    public void setSensorID(int sensorID) { this.sensorID = sensorID;}

    public long getTimestamp() { return timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MonitoringEvent) {
            MonitoringEvent monitoringEvent = (MonitoringEvent) obj;
            return monitoringEvent.canEquals(this) && sensorID == monitoringEvent.sensorID && timestamp == monitoringEvent.timestamp;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return sensorID + (int) timestamp;
    }

    public boolean canEquals(Object obj) {
        return obj instanceof MonitoringEvent;
    }
}
