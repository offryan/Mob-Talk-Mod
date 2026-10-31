package br.com.mobtalk.intent;

import java.util.UUID;

/**
 * Representa a intenção atual de um mob.
 *
 * Uma intenção não é um comportamento.
 *
 * Exemplo:
 *
 * INTENÇÃO:
 *     ATTACK
 *
 * não significa que o mob já atacou.
 *
 * Significa apenas:
 *
 * "A avaliação atual indica que atacar seria
 * uma ação desejada."
 *
 * A camada de decisão e comportamento será
 * responsável por transformar essa intenção
 * em uma ação real.
 */
public record MobIntent(
        MobIntentType type,
        UUID targetId,
        double priority
) {

    /**
     * Cria uma intenção sem alvo.
     */
    public static MobIntent none() {

        return new MobIntent(
                MobIntentType.NONE,
                null,
                0.0D
        );
    }

    /**
     * Cria uma intenção direcionada a uma entidade.
     */
    public static MobIntent of(
            MobIntentType type,
            UUID targetId,
            double priority
    ) {

        return new MobIntent(
                type,
                targetId,
                clampPriority(priority)
        );
    }

    /**
     * Garante que a prioridade permaneça entre
     * 0.0 e 1.0.
     */
    private static double clampPriority(
            double value
    ) {

        return Math.max(
                0.0D,
                Math.min(1.0D, value)
        );
    }
}