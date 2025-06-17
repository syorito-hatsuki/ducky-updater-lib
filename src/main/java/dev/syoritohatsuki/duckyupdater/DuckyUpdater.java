package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.concurrent.Executors;

public final class DuckyUpdater {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new Gson();
    private static final HashMap<Pair<String, String>, UpdateData> UPDATE_DATA_HASH_MAP = new HashMap<>();

    public static void fetchUpdates() {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
                if (modContainer.getMetadata().getCustomValue("duckyupdater") == null) return;

                try (var client = HttpClient.newHttpClient()) {
                    var url = StringUtil.buildUrl(modContainer);
                    if (url == null) return;

                    var jsonArray = GSON.fromJson(
                            client.send(HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .setHeader("User-Agent", StringUtil.userAgent(modContainer))
                                    .GET()
                                    .build(), HttpResponse.BodyHandlers.ofString()).body(), JsonElement.class
                    ).getAsJsonArray();

                    if (jsonArray.isEmpty()) return;

                    var json = jsonArray.get(0).getAsJsonObject();

                    System.out.println(json.toString());

                    var remoteVersion = json.get("version_number").getAsString();
                    if (remoteVersion.equals(modContainer.getMetadata().getVersion().getFriendlyString())) return;

                    var updateData = new UpdateData(remoteVersion, json.get("changelog").getAsString(), json.get("version_type").getAsString(), json.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());

                    var type = "release";
                    var typeObject = modContainer.getMetadata().getCustomValue("duckyupdater").getAsObject().get("type");
                    if (typeObject != null) type = typeObject.getAsString();

                    if (!updateData.type().equals(type)) return;

                    UPDATE_DATA_HASH_MAP.put(new Pair<>(modContainer.getMetadata().getName(), modContainer.getMetadata().getVersion().getFriendlyString()), updateData);
                } catch (Exception e) {
                    if (e instanceof JsonSyntaxException) return;
                    LOGGER.warn("Can't get update for {}", modContainer.getMetadata().getId(), e);
                }
            }));
        } catch (Exception e) {
            LOGGER.warn("Can't fetch updates", e);
        }
    }

    public static HashMap<Pair<String, String>, UpdateData> getUpdateDataHashMap() {
        return UPDATE_DATA_HASH_MAP;
    }
}
