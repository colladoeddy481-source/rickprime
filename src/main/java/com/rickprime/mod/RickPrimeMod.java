package com.rickprime.mod;

import com.mojang.blaze3d.platform.InputConstants;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod(RickPrimeMod.MODID)
public class RickPrimeMod {
    public static final String MODID = "rickprime";

    public RickPrimeMod() {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        public static final KeyMapping REGEN_KEY = new KeyMapping(
                "key.rickprime.regen",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "key.categories.rickprime"
        );

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(REGEN_KEY);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                new ResourceLocation(MODID, "animation_layer"),
                50,
                RickPrimeMod::registerPlayerAnimation
            );
        }
    }

    private static IAnimation registerPlayerAnimation(AbstractClientPlayer player) {
        return new ModifierLayer<>();
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ForgeClientEvents {
        private static boolean isBrokenDown = false;

        @SubscribeEvent
        public static void onPlayerHurt(LivingHurtEvent event) {
            if (Minecraft.getInstance().player != null && event.getEntity().getId() == Minecraft.getInstance().player.getId()) {
                float currentHealth = Minecraft.getInstance().player.getHealth();
                float incomingDamage = event.getAmount();

                if (currentHealth - incomingDamage <= 1.0f && !isBrokenDown) {
                    event.setCanceled(true);
                    Minecraft.getInstance().player.setHealth(2.0f);
                    isBrokenDown = true;
                    playAnimation("animation10");
                } else if (isBrokenDown) {
                    event.setCanceled(true);
                } else {
                    if (currentHealth / Minecraft.getInstance().player.getMaxHealth() <= 0.5f) {
                        playAnimation("animation8");
                    } else {
                        String[] lightHits = {"animation", "animation2", "animation3", "animation4", "animation5"};
                        int randomIdx = (int) (Math.random() * lightHits.length);
                        playAnimation(lightHits[randomIdx]);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (ClientModEvents.REGEN_KEY.consumeClick() && isBrokenDown) {
                if (Minecraft.getInstance().player != null) {
                    isBrokenDown = false;
                    playAnimation("animation9");
                    Minecraft.getInstance().player.heal(10.0f);
                }
            }
        }
    }

    public static void playAnimation(String animName) {
        if (Minecraft.getInstance().player == null) return;
        var animationContainer = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData(Minecraft.getInstance().player)
                .get(new ResourceLocation(MODID, "animation_layer"));

        if (animationContainer != null) {
            var anim = PlayerAnimationRegistry.getAnimation(new ResourceLocation(MODID, animName));
            if (anim != null) {
                animationContainer.setAnimation(new KeyframeAnimationPlayer(anim));
            }
        }
    }
}
