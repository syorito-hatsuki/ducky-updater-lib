package dev.syoritohatsuki.duckyupdater;

import dev.syoritohatsuki.duckyupdater.dto.UpdateData;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

public final class StringUtil {

    public final static String BOLD = "\u001B[1m";
    public final static String BRIGHT_GRAY = "\u001B[37m";
    public final static String BRIGHT_GREEN = "\u001B[92m";
    public final static String BRIGHT_RED = "\u001B[91m";
    public final static String GRAY = "\u001B[90m";
    public final static String RESET = "\u001B[0m";
    public final static String YELLOW = "\u001B[33m";

    public static String buildUrl(ModContainer modContainer) {
        var featured = false;

        var duckyUpdaterObject = modContainer.getMetadata().getCustomValue("duckyupdater").getAsObject();
        if (duckyUpdaterObject == null) return null;

        var modrinthId = duckyUpdaterObject.get("modrinthId");
        if (modrinthId == null) return null;

        var featuredObject = duckyUpdaterObject.get("featured");
        if (featuredObject != null) featured = featuredObject.getAsBoolean();

        return "https://api.modrinth.com/v2/project/" + modrinthId.getAsString() + "/version?loaders=[%22fabric%22]&game_versions=[%22" + SharedConstants.getGameVersion().name() + "%22]&featured=" + featured;
    }

    public static String match(char[] oldVersion, char[] newVersion) {
        var result = new StringBuilder();
        try {
            var index = 0;
            while (oldVersion[index] == newVersion[index]) {
                result.append(oldVersion[index]);
                index++;
            }
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        return result.toString();
    }

    public static String userAgent(ModContainer modContainer) {
        return "syorito-hatsuki/ducky-updater-lib/ + " + modContainer.getMetadata().getVersion().getFriendlyString() + "(syorito-hatsuki.dev)";
    }

    public static MutableText updateText(Pair<String, String> pair, UpdateData updateData) {
        final String common = StringUtil.match(pair.getRight().toCharArray(), updateData.remoteVersion().toCharArray());

        final String oldVersion = pair.getRight().replace(common, "");
        final String newVersion = updateData.remoteVersion().replace(common, "");

        return Text.literal(pair.getLeft())
                .append(Text.literal(" [").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(common).formatted(Formatting.GRAY))
                .append(Text.literal(oldVersion).formatted(Formatting.RED))
                .append(Text.literal(" -> ").formatted(Formatting.DARK_GRAY))
                .append(Text.literal(common).formatted(Formatting.GRAY))
                .append(Text.literal(newVersion).formatted(Formatting.GREEN))
                .append(Text.literal("]").formatted(Formatting.DARK_GRAY));
    }
}