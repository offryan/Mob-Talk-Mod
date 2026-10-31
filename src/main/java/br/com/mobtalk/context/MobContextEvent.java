package br.com.mobtalk.context;

import java.util.UUID;

/**
 * Representa um acontecimento relevante observado
 * pelo sistema cognitivo do MobTalk.
 *
 * Um evento de contexto possui:
 *
 * - quem foi afetado;
 * - quem causou o acontecimento;
 * - qual acontecimento ocorreu;
 * - quando ocorreu;
 * - intensidade do acontecimento.
 */
public record MobContextEvent(
        UUID observerId,
        UUID otherEntityId,
        MobContextEventType type,
        long gameTime,
        double intensity
) {

    /**
     * Tipos de acontecimentos reconhecidos
     * pelo sistema de contexto.
     */
    public enum MobContextEventType {

        /**
         * O mob sofreu dano causado por outra entidade.
         */
        DAMAGED_BY,

        /**
         * O mob causou dano em outra entidade.
         */
        DAMAGED_OTHER,

        /**
         * Uma entidade entrou na percepção do mob.
         */
        ENTITY_ENTERED_PERCEPTION,

        /**
         * Uma entidade deixou de ser percebida pelo mob.
         */
        ENTITY_LEFT_PERCEPTION,

        /**
         * Uma entidade percebida morreu.
         */
        ENTITY_DIED
    }
}