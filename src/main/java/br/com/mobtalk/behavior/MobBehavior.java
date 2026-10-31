package br.com.mobtalk.behavior;

import java.util.UUID;

/**
 * Representa o comportamento atual de um mob.
 *
 * Um comportamento é a tradução da decisão cognitiva
 * para uma ação que posteriormente poderá ser executada
 * pelo sistema de IA.
 *
 * Neste momento o comportamento ainda não controla
 * diretamente o mob.
 */
public record MobBehavior(
        MobBehaviorType type,
        UUID targetId,
        double priority
) {

    /**
     * Cria um comportamento neutro.
     */
    public static MobBehavior none() {

        return new MobBehavior(
                MobBehaviorType.NONE,
                null,
                0.0D
        );
    }

    /**
     * Cria um comportamento direcionado a um alvo.
     */
    public static MobBehavior of(
            MobBehaviorType type,
            UUID targetId,
            double priority
    ) {

        return new MobBehavior(
                type,
                targetId,
                clamp(priority)
        );
    }

    /**
     * Mantém a prioridade entre 0 e 1.
     */
    private static double clamp(
            double value
    ) {

        return Math.max(
                0.0D,
                Math.min(1.0D, value)
        );
    }
}