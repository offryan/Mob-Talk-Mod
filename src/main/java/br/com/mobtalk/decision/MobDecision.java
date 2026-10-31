package br.com.mobtalk.decision;

import java.util.UUID;

/**
 * Representa a decisão atual tomada pelo sistema
 * cognitivo de um mob.
 *
 * Uma decisão ainda não executa nenhuma ação.
 *
 * Ela representa somente o resultado do raciocínio.
 */
public record MobDecision(
        MobDecisionType type,
        UUID targetId,
        double confidence
) {

    /**
     * Cria uma decisão neutra.
     */
    public static MobDecision none() {

        return new MobDecision(
                MobDecisionType.NONE,
                null,
                0.0D
        );
    }

    /**
     * Cria uma decisão direcionada a um alvo.
     *
     * @param type tipo da decisão
     * @param targetId entidade relacionada à decisão
     * @param confidence confiança na decisão
     */
    public static MobDecision of(
            MobDecisionType type,
            UUID targetId,
            double confidence
    ) {

        return new MobDecision(
                type,
                targetId,
                clamp(confidence)
        );
    }

    /**
     * Garante que a confiança permaneça
     * entre 0.0 e 1.0.
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