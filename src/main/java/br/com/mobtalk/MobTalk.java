package br.com.mobtalk;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Classe principal do MobTalk.
 *
 * Esta classe é o ponto de entrada do mod.
 *
 * O MobTalk será construído gradualmente em camadas:
 *
 * 1. Comunicação;
 * 2. Percepção;
 * 3. Memória;
 * 4. Relações;
 * 5. Interação;
 * 6. Intenção;
 * 7. Tomada de decisão;
 * 8. Comportamento;
 * 9. Execução;
 * 10. Inteligência social.
 */
@Mod(MobTalk.MODID)
public class MobTalk {

    public static final String MODID = "mobtalk";

    public static final Logger LOGGER =
            LogUtils.getLogger();

    public MobTalk(
            IEventBus modEventBus,
            ModContainer modContainer
    ) {

        LOGGER.info("=================================");
        LOGGER.info("MobTalk iniciado!");
        LOGGER.info("Sistema cognitivo carregado.");
        LOGGER.info("=================================");
    }
}