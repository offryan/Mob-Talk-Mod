package br.com.mobtalk.communication;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import br.com.mobtalk.MobTalk;

/**
 * Sistema central de comunicação do MobTalk.
 *
 * Nesta primeira etapa, ele é responsável apenas por:
 *
 * 1. Criar mensagens;
 * 2. Publicar eventos de comunicação;
 * 3. Permitir que outros sistemas reajam às mensagens.
 *
 * A inteligência propriamente dita dos mobs será adicionada
 * posteriormente.
 */
@EventBusSubscriber(modid = MobTalk.MODID)

public final class MobCommunication {

    private MobCommunication() {
        // Classe utilitária.
    }

    /**
     * Envia uma mensagem de uma entidade para outra.
     *
     * @param sender entidade que está enviando a mensagem
     * @param receiver entidade que receberá a mensagem
     * @param content conteúdo da mensagem
     */
    public static void send(
            Entity sender,
            Entity receiver,
            String content
    ) {
        if (sender == null) {
            MobTalk.LOGGER.warn(
                    "Tentativa de comunicação sem remetente."
            );
            return;
        }

        if (receiver == null) {
            MobTalk.LOGGER.warn(
                    "Tentativa de comunicação sem destinatário."
            );
            return;
        }

        if (content == null || content.isBlank()) {
            MobTalk.LOGGER.warn(
                    "Tentativa de enviar uma mensagem vazia."
            );
            return;
        }

        MobMessage message = new MobMessage(
                sender.getUUID(),
                receiver.getUUID(),
                content,
                sender.level().getGameTime()
        );

        NeoForge.EVENT_BUS.post(
                new MobCommunicationEvent(
                        sender,
                        receiver,
                        message
                )
        );
    }

    /**
     * Envia uma mensagem para qualquer entidade que futuramente
     * esteja dentro de um determinado contexto de comunicação.
     */
    public static void broadcast(
            Entity sender,
            String content
    ) {
        if (sender == null) {
            MobTalk.LOGGER.warn(
                    "Tentativa de broadcast sem remetente."
            );
            return;
        }

        if (content == null || content.isBlank()) {
            MobTalk.LOGGER.warn(
                    "Tentativa de broadcast com mensagem vazia."
            );
            return;
        }

        MobMessage message = MobMessage.broadcast(
                sender.getUUID(),
                content,
                sender.level().getGameTime()
        );

        NeoForge.EVENT_BUS.post(
                new MobCommunicationEvent(
                        sender,
                        null,
                        message
                )
        );
    }

    /**
     * Recebe os eventos de comunicação publicados pelo próprio sistema.
     *
     * Por enquanto apenas registramos no log.
     *
     * Mais tarde este será um dos pontos onde poderemos conectar:
     *
     * - memória;
     * - emoções;
     * - relações;
     * - inteligência;
     * - tomada de decisão;
     * - comportamento de grupo.
     */
    @SubscribeEvent
    public static void onMobCommunication(
            MobCommunicationEvent event
    ) {
        MobMessage message = event.getMessage();

        if (event.getReceiver() != null) {
            MobTalk.LOGGER.info(
                    "MobTalk: {} -> {} | \"{}\"",
                    event.getSender().getType(),
                    event.getReceiver().getType(),
                    message.content()
            );
        } else {
            MobTalk.LOGGER.info(
                    "MobTalk: {} -> TODOS | \"{}\"",
                    event.getSender().getType(),
                    message.content()
            );
        }
    }

    /**
     * Evento disparado quando uma mensagem é enviada.
     */
    public static class MobCommunicationEvent extends Event {

        private final Entity sender;
        private final Entity receiver;
        private final MobMessage message;

        public MobCommunicationEvent(
                Entity sender,
                Entity receiver,
                MobMessage message
        ) {
            this.sender = sender;
            this.receiver = receiver;
            this.message = message;
        }

        public Entity getSender() {
            return sender;
        }

        public Entity getReceiver() {
            return receiver;
        }

        public MobMessage getMessage() {
            return message;
        }
    }
}