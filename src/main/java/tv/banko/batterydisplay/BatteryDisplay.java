package tv.banko.batterydisplay;

import tv.banko.batterydisplay.device.DeviceManager;
import tv.banko.batterydisplay.config.Config;
import tv.banko.batterydisplay.screen.OLEDScreen;
import tv.banko.batterydisplay.tray.BatterySystemTray;

import java.io.IOException;

public class BatteryDisplay {

    private final Config config;
    private final DeviceManager api;
    private final OLEDScreen screen;
    private final BatterySystemTray tray;

    public BatteryDisplay() {
        this.config = new Config();
        this.screen = new OLEDScreen();
        this.api = new DeviceManager(this);
        try {
            this.tray = new BatterySystemTray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Config getConfig() {
        return config;
    }

    public DeviceManager getApi() {
        return api;
    }

    public OLEDScreen getScreen() {
        return screen;
    }

    public BatterySystemTray getTray() {
        return tray;
    }

    public static void main(String[] args) {
        new BatteryDisplay();
    }

}
