package dev.syoritohatsuki.duckyupdater.mixin.client;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.StringUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Unique
    private static Boolean alreadyShowed = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void printUpdates(CallbackInfo ci) {
        if (alreadyShowed) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            AtomicBoolean firstLine = new AtomicBoolean(true);

            DuckyUpdater.getUpdateDataHashMap().forEach(((pair, updateData) -> {
                if (firstLine.get()) {
                    sendMessage(Text.literal("Updates available").styled(style ->
                            style.withBold(true).withColor(Formatting.YELLOW)), false);
                    firstLine.set(false);
                }
                sendMessage(Text.literal(" - ").append(StringUtil.updateText(pair, updateData)).styled(style ->
                        style.withHoverEvent(
                                new HoverEvent.ShowText(Text.literal(updateData.changelog()))
                        ).withClickEvent(new ClickEvent.OpenUrl(URI.create(updateData.fileUrl())))
                ), false);
            }));
        });

        alreadyShowed = true;
    }
}
