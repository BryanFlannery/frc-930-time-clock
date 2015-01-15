package Team;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Admin on 9/25/2014.
 */
public class Entry {

    public enum TIME_TYPE { //Time types used by the team
        SHOP_HOURS, FUNDRASING_HOURS, VOLUNTEER_HOURS
    }

    private int id;
    private StringProperty date;
    private StringProperty timeIn;
    private StringProperty timeOut;
    private StringProperty type;

    public Entry(int id, String date, String timeIn, String timeOut, String type) {
        this.id = id;
        this.date = new SimpleStringProperty(date);
        this.timeIn = new SimpleStringProperty(timeIn);
        this.timeOut = new SimpleStringProperty(timeOut);
        this.type = new SimpleStringProperty(type);
    }

    public Entry() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) { this.id = id; }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getTimeIn() {
        return timeIn.get();
    }

    public void setTimeIn(String timeIn) {
        this.timeIn.set(timeIn);
    }

    public StringProperty timeInProperty() {
        return timeIn;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getTimeOut() {
        return timeOut.get();
    }

    public void setTimeOut(String timeOut) {
        this.timeOut.set(timeOut);
    }

    public StringProperty timeOutProperty() {
        return timeOut;
    }
}
