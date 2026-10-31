package br.com.mobtalk.communication;

import java.util.UUID;

/**
 * Representa uma mensagem enviada de uma entidade para outra
 * dentro do sistema de comunicação do MobTalk.
 *
 * Nesta primeira versão, a mensagem é apenas uma estrutura de dados.
 * Futuramente ela poderá conter:
 *
 * - intenção;
 * - emoção;
 * - prioridade;
 * - informação sobre perigo;
 * - alvo;
 * - contexto;
 * - distância;
 * - memória;
 * - etc.
 */
public record MobMessage(
        UUID senderId,
        UUID receiverId,
        String content,
        long gameTime
) {

    /**
     * Cria uma mensagem sem destinatário específico.
     *
     * Útil para mensagens que posteriormente poderão ser
     * interpretadas por qualquer mob dentro de determinado alcance.
     */
    public static MobMessage broadcast(
            UUID senderId,
            String content,
            long gameTime
    ) {
        return new MobMessage(
                senderId,
                null,
                content,
                gameTime
        );
    }
}