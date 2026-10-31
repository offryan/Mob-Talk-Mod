package br.com.mobtalk.threat;

import java.util.UUID;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.context.MobContextInterpreter;
import br.com.mobtalk.social.MobRelationship;
import br.com.mobtalk.social.MobRelationshipManager;

import net.minecraft.world.entity.Mob;

/**
 * Avalia o nível de ameaça que uma entidade representa
 * para um mob.
 *
 * O avaliador não toma decisões e não executa comportamentos.
 *
 * Sua responsabilidade é transformar informações cognitivas
 * em uma avaliação de ameaça.
 */
public final class MobThreatEvaluator {

    private MobThreatEvaluator() {
        // Classe utilitária.
    }

    /**
     * Avalia a ameaça representada por uma entidade.
     */
    public static MobThreatAssessment evaluate(
            Mob mob,
            UUID targetId
    ) {

        if (mob == null || targetId == null) {
            return MobThreatAssessment.none(
                    targetId
            );
        }

        MobRelationship relationship =
                MobRelationshipManager.getRelationship(
                        mob,
                        targetId
                );

        double hostility =
                getHostility(
                        relationship
                );

        double familiarity =
                getFamiliarity(
                        relationship
                );

        double recentDamage =
                MobContextInterpreter.getRecentDamage(
                        mob,
                        targetId
                );

        double highestRecentDamage =
                MobContextInterpreter.getHighestRecentDamage(
                        mob,
                        targetId
                );

        int recentAttackCount =
                MobContextInterpreter.countRecentDamages(
                        mob,
                        targetId
                );

        long lastAttackAge =
                MobContextInterpreter.getLastDamageAge(
                        mob,
                        targetId
                );

        boolean repeatedAttack =
                MobContextInterpreter.hasRepeatedRecentDamage(
                        mob,
                        targetId
                );

        double score =
                calculateThreatScore(
                        hostility,
                        familiarity,
                        recentDamage,
                        highestRecentDamage,
                        recentAttackCount,
                        lastAttackAge,
                        repeatedAttack
                );

        MobThreatLevel level =
                determineThreatLevel(
                        score
                );

        MobThreatAssessment assessment =
                MobThreatAssessment.of(
                        targetId,
                        level,
                        score,
                        recentAttackCount,
                        recentDamage,
                        highestRecentDamage,
                        repeatedAttack,
                        lastAttackAge
                );

        MobTalk.LOGGER.debug(
                "MobTalk AMEAÇA: {} | alvo={} | nível={} | score={} | ataques={} | dano={} | maiorDano={} | repetido={} | idade={} ticks",
                mob.getType(),
                targetId,
                level,
                String.format(
                        "%.2f",
                        score
                ),
                recentAttackCount,
                String.format(
                        "%.2f",
                        recentDamage
                ),
                String.format(
                        "%.2f",
                        highestRecentDamage
                ),
                repeatedAttack,
                lastAttackAge
        );

        return assessment;
    }

    /**
     * Obtém a hostilidade atual da relação.
     */
    private static double getHostility(
            MobRelationship relationship
    ) {

        if (relationship == null) {
            return 0.0D;
        }

        return relationship.hostility();
    }

    /**
     * Obtém a familiaridade atual da relação.
     */
    private static double getFamiliarity(
            MobRelationship relationship
    ) {

        if (relationship == null) {
            return 0.0D;
        }

        return relationship.familiarity();
    }

    /**
     * Calcula a pontuação de ameaça.
     *
     * A pontuação combina:
     *
     * - hostilidade histórica;
     * - familiaridade;
     * - dano recente;
     * - maior dano recente;
     * - quantidade de ataques;
     * - recência;
     * - repetição dos ataques.
     */
    private static double calculateThreatScore(
            double hostility,
            double familiarity,
            double recentDamage,
            double highestRecentDamage,
            int recentAttackCount,
            long lastAttackAge,
            boolean repeatedAttack
    ) {

        double hostilityScore =
                hostility * 0.30D;

        double familiarityScore =
                familiarity * 0.05D;

        double damageScore =
                clamp(
                        recentDamage / 10.0D
                ) * 0.20D;

        double highestDamageScore =
                clamp(
                        highestRecentDamage / 10.0D
                ) * 0.10D;

        double attackCountScore =
                clamp(
                        recentAttackCount / 5.0D
                ) * 0.10D;

        double recencyScore =
                calculateRecencyScore(
                        lastAttackAge
                ) * 0.10D;

        double repetitionScore =
                repeatedAttack
                        ? 0.15D
                        : 0.0D;

        double score =
                hostilityScore
                + familiarityScore
                + damageScore
                + highestDamageScore
                + attackCountScore
                + recencyScore
                + repetitionScore;

        return clamp(score);
    }

    /**
     * Calcula quanto um acontecimento recente
     * ainda influencia a avaliação de ameaça.
     */
    private static double calculateRecencyScore(
            long age
    ) {

        if (age < 0L) {
            return 0.0D;
        }

        return clamp(
                1.0D
                        - (
                        age
                                / 200.0D
                )
        );
    }

    /**
     * Converte a pontuação em um nível qualitativo.
     */
    private static MobThreatLevel determineThreatLevel(
            double score
    ) {

        if (score >= 0.80D) {
            return MobThreatLevel.CRITICAL;
        }

        if (score >= 0.60D) {
            return MobThreatLevel.HIGH;
        }

        if (score >= 0.35D) {
            return MobThreatLevel.MODERATE;
        }

        if (score > 0.0D) {
            return MobThreatLevel.LOW;
        }

        return MobThreatLevel.NONE;
    }

    /**
     * Mantém um valor entre 0 e 1.
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