package br.com.mobtalk.interaction;

import java.util.UUID;

/**
 * Representa uma interação entre duas entidades.
 *
 * Uma interação é diferente de uma simples percepção.
 *
 * Percepção significa:
 *
 * "Eu percebi você."
 *
 * Interação significa:
 *
 * "Algo aconteceu entre nós."
 *
 * Exemplos futuros de interação:
 *
 * - ataque;
 * - dano recebido;
 * - morte de um aliado;
 * - ajuda;
 * - troca de itens;
 * - proteção;
 * - fuga;
 * - ameaça;
 * - comunicação;
 * - aproximação;
 * - etc.
 */
public record MobInteraction(
        UUID observerId,
        UUID otherEntityId,
        MobInteractionType type,
        long gameTime,
        double intensity
) {

    /**
     * Tipos de interação atualmente suportados
     * pelo sistema.
     */
    public enum MobInteractionType {

        /**
         * A entidade sofreu dano causado
         * por outra entidade.
         */
        DAMAGED_BY,

        /**
         * A entidade causou dano em outra entidade.
         */
        DAMAGED_OTHER
    }
}