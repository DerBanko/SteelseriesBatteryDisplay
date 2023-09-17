package tv.banko.batterydisplay.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import tv.banko.batterydisplay.device.DeviceState;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class OLEDScreen {

    private final String address;
    private final OkHttpClient client;
    private boolean showNoBattery;

    public OLEDScreen() {
        this.address = this.readAddress();
        this.client = new OkHttpClient();
        this.showNoBattery = false;

        this.sendBindEvent();
    }

    public void sendEvent(int batteryLevel, DeviceState state) {
        Request request = new Request.Builder()
                .url(this.address + "/game_event")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(this.createEventObject(batteryLevel, state).toString().getBytes()))
                .build();
        this.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() == 200) {
                    response.close();
                    return;
                }

                System.out.println(response.code());
                response.close();
            }
        });
    }

    private JsonObject createEventObject(int batteryLevel, DeviceState state) {
        JsonObject frame = new JsonObject();
        frame.addProperty("text", state.getText());

        JsonObject data = new JsonObject();

        if (this.showNoBattery) {
            this.showNoBattery = false;
            data.addProperty("value", 0);
        } else {
            data.addProperty("value", batteryLevel);

            if (state == DeviceState.WIRELESS && batteryLevel <= 20) {
                this.showNoBattery = true;
            }
        }

        data.add("frame", frame);

        JsonObject object = new JsonObject();
        object.addProperty("game", "BATTERY_DISPLAY");
        object.addProperty("event", "BATTERY_UPDATE");
        object.addProperty("icon-id", 1);
        object.addProperty("min_value", 0);
        object.addProperty("max_value", 100);
        object.add("data", data);

        return object;
    }

    private void sendBindEvent() {
        Request request = new Request.Builder()
                .url(this.address + "/bind_game_event")
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(this.createBindEventObject().toString().getBytes()))
                .build();
        this.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    response.close();
                    return;
                }

                assert response.body() != null;
                System.out.println(response.code() + ": " + response.body().string());
                response.close();
            }
        });
    }

    private JsonObject createBindEventObject() {
        JsonObject line1 = new JsonObject();
        line1.addProperty("has-text", true);
        line1.addProperty("context-frame-key", "text");

        JsonObject line2 = new JsonObject();
        line2.addProperty("has-progress-bar", true);

        JsonArray lines = new JsonArray();
        lines.add(line1);
        lines.add(line2);

        JsonObject data = new JsonObject();
        data.add("lines", lines);

        JsonArray datas = new JsonArray();
        datas.add(data);

        JsonObject handler = new JsonObject();
        handler.addProperty("device-type", "keyboard");
        handler.addProperty("zone", "one");
        handler.addProperty("mode", "screen");
        handler.add("datas", datas);

        JsonArray handlers = new JsonArray();
        handlers.add(handler);

        JsonObject object = new JsonObject();
        object.addProperty("game", "BATTERY_DISPLAY");
        object.addProperty("event", "BATTERY_UPDATE");
        object.addProperty("icon-id", 1);
        object.addProperty("min_value", 0);
        object.addProperty("max_value", 100);
        object.add("handlers", handlers);

        return object;
    }

    private String readAddress() {
        String jsonAddressStr = "";
        String corePropsFileName;

        if (System.getProperty("os.name").startsWith("Windows")) {
            corePropsFileName = System.getenv("PROGRAMDATA") + "\\SteelSeries\\SteelSeries Engine 3\\coreProps.json";
        } else {
            corePropsFileName = "/Library/Application Support/SteelSeries Engine 3/coreProps.json";
        }
        try {
            BufferedReader coreProps = new BufferedReader(new FileReader(corePropsFileName));
            jsonAddressStr = coreProps.readLine();
            coreProps.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!jsonAddressStr.isEmpty()) {
            JsonObject obj = JsonParser.parseString(jsonAddressStr).getAsJsonObject();
            return "http://" + obj.get("address").getAsString();
        }

        return null;
    }

}
