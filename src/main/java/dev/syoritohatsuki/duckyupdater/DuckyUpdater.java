package dev.syoritohatsuki.duckyupdater;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import dev.syoritohatsuki.duckyupdater.dto.MetaData;
import dev.syoritohatsuki.duckyupdater.dto.ProjectVersion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;

public class DuckyUpdater {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String URL = "https://api.modrinth.com/v2/";

    private static final HashSet<MetaData> MODRINTH_ID_LIST = new HashSet<>();

    static void checkForUpdate(String modrinthId, String modId) {
        checkForUpdate(modrinthId, modId, true);
    }

    static void checkForUpdate(String modrinthId, String modId, Boolean onlyFeatured) {
        checkForUpdate(modrinthId, modId, "release", onlyFeatured);
    }

    static void checkForUpdate(String modrinthId, String modId, String type, Boolean onlyFeatured) {
        MODRINTH_ID_LIST.add(new MetaData(modrinthId, modId, type, onlyFeatured));
    }

    /**
     * <b>ONLY IN MIXIN USE!!!<b/>
     *
     * @param minecraftVersion required for request data only for current version
     * @return Set of all projects that have update available
     */
    public static HashSet<Pair<ProjectVersion, Pair<String, String>>> check(String minecraftVersion) {
        HashSet<Pair<ProjectVersion, Pair<String, String>>> projectVersionsSet = new HashSet<>();
        MODRINTH_ID_LIST.forEach(metaData -> {
            var url = URI.create(URL + "project/" + metaData.modrinthId() +
                    "/version?loaders=[%22fabric%22]" +
                    "&game_versions=[%22" + minecraftVersion + "%22]" +
                    "&featured=" + metaData.onlyFeatured());

            LOGGER.info(url.toString());

            try {
                ProjectVersion[] projectVersions = new Gson().fromJson(
                        HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                                .uri(url)
                                .GET()
                                .build(), HttpResponse.BodyHandlers.ofString()).body(), ProjectVersion[].class);

                LOGGER.info(String.valueOf(projectVersions[0].version_type.equals(metaData.type())));
                if (projectVersions[0].version_type.equals(metaData.type())) {

                    var modNameAndVersion = getModNameAndVersion(metaData.modId());

                    if (!projectVersions[0].version_number.contains(modNameAndVersion.getRight()))
                        projectVersionsSet.add(new Pair<>(projectVersions[0], modNameAndVersion));

                }
            } catch (Exception exception) {
                LOGGER.error(exception.getMessage());
            }
        });
        return projectVersionsSet;
    }

    private static Pair<String, String> getModNameAndVersion(String modId) {

        var metadata = FabricLoader.getInstance()
                .getModContainer(modId)
                .orElseThrow()
                .getMetadata();

        return new Pair<>(metadata.getName(), metadata.getVersion().getFriendlyString());
    }
}