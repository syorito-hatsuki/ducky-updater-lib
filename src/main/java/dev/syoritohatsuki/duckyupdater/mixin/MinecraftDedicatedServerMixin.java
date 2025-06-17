package dev.syoritohatsuki.duckyupdater.mixin;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.StringUtil;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.syoritohatsuki.duckyupdater.StringUtil.*;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MinecraftDedicatedServerMixin {

    @Shadow
    @Final
    static Logger LOGGER;

    @Inject(method = "setupServer", at = @At("TAIL"))
    protected void runServerReturn(CallbackInfoReturnable<Boolean> cir) {
        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {

                var firstLine = new AtomicBoolean(true);

                DuckyUpdater.getUpdateDataHashMap().forEach((ducky, updateData) -> {

                    if (firstLine.get()) {
                        DuckyUpdater.LOGGER.info("");
                        DuckyUpdater.LOGGER.info("{}{}Updates available{}", BOLD, YELLOW, RESET);
                        firstLine.set(false);
                    }

                    var oldVersion = ducky.getRight();
                    var newVersion = updateData.remoteVersion();
                    var common = StringUtil.match(oldVersion.toCharArray(), newVersion.toCharArray());

                    DuckyUpdater.LOGGER.info("\t- {} {}[{}{}{}{}{} -> {}{}{}{}{}]{}", ducky.getLeft(), GRAY, BRIGHT_GRAY, common, BRIGHT_RED, oldVersion.replace(common, ""), GRAY, BRIGHT_GRAY, common, BRIGHT_GREEN, newVersion.replace(common, ""), GRAY, RESET);

                });

                if (!firstLine.get()) DuckyUpdater.LOGGER.info("");

            });
        } catch (Exception e) {
            LOGGER.warn("Can't fetch updates", e);
        }
    }
}