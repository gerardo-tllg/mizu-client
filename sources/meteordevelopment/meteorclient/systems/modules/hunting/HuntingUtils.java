package meteordevelopment.meteorclient.systems.modules.hunting;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import javax.net.ssl.HttpsURLConnection;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.jetbrains.annotations.Nullable;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/modules/hunting/HuntingUtils.class */
public class HuntingUtils {
    public static void firework() {
        firework(class_310.method_1551(), false);
    }

    public static int firework(class_310 mc, boolean elytraRequired) {
        if (mc.field_1724 == null) {
            return -1;
        }
        int elytraSwapSlot = -1;
        if (elytraRequired && !mc.field_1724.method_31548().method_5438(38).method_31574(class_1802.field_8833)) {
            FindItemResult itemResult = InvUtils.findInHotbar(class_1802.field_8833);
            if (!itemResult.found()) {
                return -1;
            }
            elytraSwapSlot = itemResult.slot();
            InvUtils.swap(itemResult.slot(), true);
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            InvUtils.swapBack();
            mc.method_1562().method_52787(new class_2848(mc.field_1724, class_2848.class_2849.field_12982));
        }
        FindItemResult itemResult2 = InvUtils.findInHotbar(class_1802.field_8639);
        if (!itemResult2.found()) {
            return -1;
        }
        if (itemResult2.isOffhand()) {
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5810);
            mc.field_1724.method_6104(class_1268.field_5810);
        } else {
            InvUtils.swap(itemResult2.slot(), true);
            mc.field_1761.method_2919(mc.field_1724, class_1268.field_5808);
            mc.field_1724.method_6104(class_1268.field_5808);
            InvUtils.swapBack();
        }
        if (elytraSwapSlot != -1) {
            return elytraSwapSlot;
        }
        return 200;
    }

    public static void setPressed(class_304 key, boolean pressed) {
        key.method_23481(pressed);
        Input.setKeyState(key, pressed);
    }

    public static int emptyInvSlots(class_310 mc) {
        if (mc.field_1724 == null) {
            return 0;
        }
        int airCount = 0;
        for (int i = 0; i < 36; i++) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909() == class_1802.field_8162) {
                airCount++;
            }
        }
        return airCount;
    }

    public static class_243 positionInDirection(class_243 pos, double yaw, double distance) {
        class_243 offset = yawToDirection(yaw).method_1021(distance);
        return pos.method_1019(offset);
    }

    public static class_243 yawToDirection(double yaw) {
        double yaw2 = (yaw * 3.141592653589793d) / 180.0d;
        double x = -Math.sin(yaw2);
        double z = Math.cos(yaw2);
        return new class_243(x, 0.0d, z);
    }

    public static double distancePointToDirection(class_243 point, class_243 direction, @Nullable class_243 start) {
        if (start == null) {
            start = class_243.field_1353;
        }
        class_243 point2 = point.method_18806(new class_243(1.0d, 0.0d, 1.0d));
        class_243 start2 = start.method_18806(new class_243(1.0d, 0.0d, 1.0d));
        class_243 direction2 = direction.method_18806(new class_243(1.0d, 0.0d, 1.0d));
        class_243 directionVec = point2.method_1020(start2);
        double projectionLength = directionVec.method_1026(direction2) / direction2.method_1027();
        class_243 projection = direction2.method_1021(projectionLength);
        class_243 perp = directionVec.method_1020(projection);
        return perp.method_1033();
    }

    public static double angleOnAxis(double yaw) {
        if (yaw < 0.0d) {
            yaw += 360.0d;
        }
        return Math.round(yaw / 45.0d) * 45;
    }

    public static class_243 normalizedPositionOnAxis(class_243 pos) {
        double angle = -Math.atan2(pos.field_1352, pos.field_1350);
        double angleDeg = Math.toDegrees(angle);
        return positionInDirection(new class_243(0.0d, 0.0d, 0.0d), angleOnAxis(angleDeg), 1.0d);
    }

    public static int totalInvCount(class_310 mc, class_1792 item) {
        if (mc.field_1724 == null) {
            return 0;
        }
        int itemCount = 0;
        for (int i = 0; i < 36; i++) {
            class_1799 stack = mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7909() == item) {
                itemCount += stack.method_7947();
            }
        }
        return itemCount;
    }

    public static float smoothRotation(double current, double target, double rotationScaling) {
        double difference = angleDifference(target, current);
        return (float) (current + (difference * rotationScaling));
    }

    public static double angleDifference(double target, double current) {
        double diff = (((target - current) + 180.0d) % 360.0d) - 180.0d;
        return diff < -180.0d ? diff + 360.0d : diff;
    }

    public static void sendWebhook(String webhookURL, String title, String message, String pingID, String playerName) {
        String json = "" + "{\"embeds\": [{\"title\": \"" + title + "\",\"description\": \"" + message + "\",\"color\": 15258703,\"footer\": {\"text\": \"From: " + playerName + "\"}}]}";
        sendRequest(webhookURL, json);
        if (pingID != null) {
            String json2 = "{\"content\": \"<@" + pingID + ">\"}";
            sendRequest(webhookURL, json2);
        }
    }

    public static void sendWebhook(String webhookURL, String jsonObject, String pingID) {
        sendRequest(webhookURL, jsonObject);
        if (pingID != null) {
            String jsonObject2 = "{\"content\": \"<@" + pingID + ">\"}";
            sendRequest(webhookURL, jsonObject2);
        }
    }

    private static void sendRequest(String webhookURL, String json) {
        try {
            URL url = new URL(webhookURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.addRequestProperty("Content-Type", "application/json");
            con.addRequestProperty("User-Agent", "Mozilla");
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            OutputStream stream = con.getOutputStream();
            stream.write(json.getBytes());
            stream.flush();
            stream.close();
            con.getInputStream().close();
            con.disconnect();
        } catch (MalformedURLException | UnknownServiceException e) {
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }
}
