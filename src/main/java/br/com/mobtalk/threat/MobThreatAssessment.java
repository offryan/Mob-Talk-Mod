package br.com.mobtalk.threat;

import java.util.UUID;

/**
 * Representa a avaliação de ameaça de uma entidade
 * em relação a um determinado mob.
 *
 * O assessment não executa nenhuma ação.
 * Ele apenas representa a interpretação atual
 * do nível de ameaça.
 */
public record MobThreatAssessment(
        UUID targetId,
        MobThreatLevel level,
        double score,
        int recentAttackCount,
        double recentDamage,
        double highestRecentDamage,
        boolean repeatedAttack,
        long lastAttackAge
) {

    /**
     * Cria uma avaliação sem ameaça.
     */
    public static MobThreatAssessment none(
            UUID targetId
    ) {

        return new MobThreatAssessment(
                targetId,
                MobThreatLevel.NONE,
                0.0D,
                0,
                0.0D,
                0.0D,
                false,
                -1L
        );
    }

    /**
     * Cria uma avaliação de ameaça.
     */
    public static MobThreatAssessment of(
            UUID targetId,
            MobThreatLevel level,
            double score,
            int recentAttackCount,
            double recentDamage,
            double highestRecentDamage,
            boolean repeatedAttack,
            long lastAttackAge
    ) {

        return new MobThreatAssessment(
                targetId,
                level,
                clamp(score),
                recentAttackCount,
                recentDamage,
                highestRecentDamage,
                repeatedAttack,
                lastAttackAge
        );
    }

    /**
     * Mantém a pontuação entre 0 e 1.
     */
    private static double clamp(
            double value
    ) {

        return Math.max(
                0.0D,
                Math.min(
                        1.0D,
                        value
                )
        );
    }
}