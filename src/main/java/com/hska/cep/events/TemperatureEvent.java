package com.hska.cep.events;

/**
 * Created by wipatrick on 20.06.16.
 */
public class TemperatureEvent extends MonitoringEvent {

    //private long timestamp;
    private double temperature;

    public TemperatureEvent(int sensorId, long timestamp, double temperature) {
        super(sensorId, timestamp);

        this.temperature = temperature;
    }

//    public long getTimestamp() { return timestamp; }
//
//    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemperatureEvent) {
            TemperatureEvent other = (TemperatureEvent) obj;

            return other.canEquals(this) && super.equals(other) && temperature == other.temperature;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 41 * super.hashCode() + Double.hashCode(temperature);
    }

    @Override
    public boolean canEquals(Object obj){
        return obj instanceof TemperatureEvent;
    }

    @Override
    public String toString() {
        return "TemperatureEvent(" + getSensorID() + ", " + getTimestamp() + "," + temperature + ")";
    }
}
