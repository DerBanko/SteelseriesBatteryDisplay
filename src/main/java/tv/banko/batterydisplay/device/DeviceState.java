package tv.banko.batterydisplay.device;

public enum DeviceState {

    WIRELESS("Wireless"),
    CHARGING("Charging"),
    DISCONNECTED("Not connected!");

    private final String text;

    DeviceState(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
