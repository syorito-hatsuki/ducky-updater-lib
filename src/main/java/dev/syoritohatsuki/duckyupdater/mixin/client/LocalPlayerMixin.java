package dev.syoritohatsuki.duckyupdater.mixin.client;

import dev.syoritohatsuki.duckyupdater.DuckyUpdater;
import dev.syoritohatsuki.duckyupdater.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    @Final
    protected Minecraft minecraft;
    @Unique
    private static Boolean alreadyShowed = false;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void printUpdates(CallbackInfo ci) {
        if (alreadyShowed) return;

        try (var executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {
                AtomicBoolean firstLine = new AtomicBoolean(true);

                DuckyUpdater.getUpdateDataHashMap()
                        .forEach(((pair, updateData) -> Minecraft.getInstance().execute(() -> {
                            if (firstLine.get()) {
                                sendMessageWithoutOverlay(Component.literal("Updates available").withStyle(style ->
                                        style.withBold(true).withColor(ChatFormatting.YELLOW)));
                                firstLine.set(false);
                            }
                            sendMessageWithoutOverlay(Component.literal(" - ")
                                    .append(StringUtil.updateText(pair, updateData))
                                    .withStyle(style -> style.withHoverEvent(
                                            new HoverEvent.ShowText(Component.literal(updateData.changelog()))
                                    ).withClickEvent(
                                            new ClickEvent.OpenUrl(URI.create(updateData.fileUrl()))
                                    ))
                            );
                        })));
            });
        }

        alreadyShowed = true;
    }

    @Unique
    private void sendMessageWithoutOverlay(Component component) {
        minecraft.gui.chatListener().handleSystemMessage(component, false);
    }
}
