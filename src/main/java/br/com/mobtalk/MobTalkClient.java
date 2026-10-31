package br.com.mobtalk;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Parte exclusivamente cliente do MobTalk.
 *
 * Esta classe não será carregada em um servidor dedicado.
 */
@Mod(value = MobTalk.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(
        modid = MobTalk.MODID,
        value = Dist.CLIENT
)
public class MobTalkClient {

    public MobTalkClient(ModContainer container) {
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MobTalk.LOGGER.info("MobTalk - lado cliente iniciado!");
    }
}