package tv.banko.batterydisplay.device;

import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.hid4java.HidServicesSpecification;
import tv.banko.batterydisplay.BatteryDisplay;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceManager {

    private final BatteryDisplay display;

    private final HidServices services;

    private DeviceState state;
    private int batteryLevel;
    private int notConnectedSince;

    public DeviceManager(BatteryDisplay display) {
        this.display = display;
        this.batteryLevel = 0;
        this.notConnectedSince = 10;
        this.state = DeviceState.DISCONNECTED;

        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();

        hidServicesSpecification.setAutoStart(false);

        this.services = HidManager.getHidServices(hidServicesSpecification);

        this.services.start();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::update, 0, 1, TimeUnit.SECONDS);
    }

    public void update() {
        try {
            Optional<HidDevice> optional = this.getDevice(3);

            if (optional.isEmpty()) {
                this.notConnectedSince++;
                this.updateDisplay();
                return;
            }

            HidDevice hidDevice = optional.get();

            if (!hidDevice.isOpen()) {
                hidDevice.open();
            }

            boolean wireless = this.isWireless(hidDevice);
            byte command = (byte) 0x92;

            if (wireless) {
                command = (byte) (command | 0b01000000);
            }

            hidDevice.write(new byte[]{command}, 1, (byte) 0x00);

            Byte[] bytes = hidDevice.read(2, 200);

            if (bytes[1] == 0) {
                this.notConnectedSince++;
                this.updateDisplay();
                return;
            }

            this.notConnectedSince = 0;

            byte unsigned = bytes[1];

            if (!wireless) {
                unsigned = toUnsigned(unsigned);
            }

            this.batteryLevel = (((unsigned & ~0b10000000) - 1) * 5);
            this.state = (bytes[1] & 0b10000000) != 0 ? DeviceState.CHARGING : DeviceState.WIRELESS;

            this.updateDisplay();
        } catch (Exception e) {
            this.notConnectedSince++;
            this.updateDisplay();
        }
    }

    private void updateDisplay() {
        if (!this.isConnected()) {
            this.state = DeviceState.DISCONNECTED;
            this.batteryLevel = 0;
        }

        this.display.getScreen().sendEvent(this.batteryLevel, this.state);
    }

    public boolean isConnected() {
        return this.notConnectedSince < 10;
    }

    public Optional<HidDevice> getDevice(int interfaceNumber) {
        Optional<Object> optionalVendorId = display.getConfig().get("device-vendor-id");
        Optional<Object> optionalProductIdWireless = display.getConfig().get("device-product-id-wireless");
        Optional<Object> optionalProductIdWired = display.getConfig().get("device-product-id-wired");

        if (optionalVendorId.isEmpty()) {
            throw new NullPointerException("device-vendor-id is null");
        }

        if (optionalProductIdWireless.isEmpty()) {
            throw new NullPointerException("device-product-id-wireless is null");
        }

        if (optionalProductIdWired.isEmpty()) {
            throw new NullPointerException("device-product-id-wired is null");
        }

        Optional<HidDevice> optionalWireless = this.getDevice((int) optionalVendorId.get(), (int) optionalProductIdWireless.get(), interfaceNumber);

        if (optionalWireless.isPresent()) {
            return optionalWireless;
        }

        return this.getDevice((int) optionalVendorId.get(), (int) optionalProductIdWired.get(), interfaceNumber);
    }

    public Optional<HidDevice> getKeyboard(int interfaceNumber) {
        Optional<Object> optionalVendorId = display.getConfig().get("keyboard-vendor-id");
        Optional<Object> optionalProductId = display.getConfig().get("keyboard-product-id");

        if (optionalVendorId.isEmpty()) {
            throw new NullPointerException("device-vendor-id is null");
        }

        if (optionalProductId.isEmpty()) {
            throw new NullPointerException("device-product-id-wireless is null");
        }

        return this.getDevice((int) optionalVendorId.get(), (int) optionalProductId.get(), interfaceNumber);
    }

    public Optional<HidDevice> getDevice(int vendorId, int productId, int interfaceNumber) {
        return this.services.getAttachedHidDevices().stream()
                .filter(hidDevice -> this.compareHex(hidDevice.getVendorId(), vendorId)
                        && this.compareHex(hidDevice.getProductId(), productId)
                        && this.compareHex(hidDevice.getInterfaceNumber(), interfaceNumber))
                .findFirst();
    }

    public boolean isWireless(HidDevice device) {
        return display.getConfig().get("device-product-id-wireless").orElse(" ").equals(device.getProductId());
    }

    private byte toUnsigned(int value) {
        return (byte) (value + 128);
    }

    private boolean compareHex(int value1, int value2) {
        return Integer.toHexString(value1).equals(Integer.toHexString(value2));
    }
}
