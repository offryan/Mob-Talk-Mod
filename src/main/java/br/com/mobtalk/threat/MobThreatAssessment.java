package br.com.mobtalk.threat;

import java.util.UUID;

/**
 * Representa a avaliação de ameaça de uma entidade
 * em relação a um determinado mob.
 *
 * O assessment não executa nenhuma ação.
 * Ele apenas representa a interpretação atual
 * do nível de ameaça.
 *
 * A pontuação final é composta por três partes,
 * guardadas separadamente para diagnóstico:
 *
 * - imediata: acontecimentos recentes do contexto;
 * - histórica: ataques lembrados pela memória;
 * - social: hostilidade do relacionamento.
 */
public record MobThreatAssessment(
        UUID targetId,
        MobThreatLevel level,
        double score,
        int recentAttackCount,
        double recentDamage,
        double highestRecentDamage,
        boolean repeatedAttack,
        long lastAttackAge,
        double immediateScore,
        double historicalScore,
        double socialScore
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
                -1L,
                0.0D,
                0.0D,
                0.0D
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
            long lastAttackAge,
            double immediateScore,
            double historicalScore,
            double socialScore
    ) {

        return new MobThreatAssessment(
                targetId,
                level,
                clamp(score),
                recentAttackCount,
                recentDamage,
                highestRecentDamage,
                repeatedAttack,
                lastAttackAge,
                immediateScore,
                historicalScore,
                socialScore
        );
    }

    /**
     * Indica se a ameaça vem de um ataque recente,
     * registrado no contexto.
     */
    public boolean hasRecentAttack() {
        return recentAttackCount > 0;
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
