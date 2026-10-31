package br.com.mobtalk.threat;

import java.util.UUID;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.context.MobContextInterpreter;
import br.com.mobtalk.memory.MemoryEntry;
import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.memory.MobMemoryManager;
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
 *
 * A pontuação é formada por:
 *
 * - social: hostilidade do relacionamento;
 * - imediata: ataques recentes registrados no contexto;
 * - histórica: ataques lembrados pela memória, que
 *   perdem força com o tempo.
 *
 * Imediata e histórica descrevem os mesmos ataques em
 * escalas de tempo diferentes, por isso entra apenas a
 * maior das duas: um golpe nunca é contado duas vezes.
 */
public final class MobThreatEvaluator {

    /**
     * Ticks para a lembrança de um ataque cair pela metade.
     *
     * 2400 ticks = aproximadamente 2 minutos.
     */
    public static final long HISTORY_HALF_LIFE_TICKS = 2400L;

    /**
     * Contribuição máxima da memória histórica.
     *
     * Somada à hostilidade máxima (0,30), fica abaixo do
     * limiar de HIGH (0,60): o histórico pode levar um
     * mob a evitar, mas fugir exige um ataque recente.
     */
    public static final double HISTORY_MAX_SCORE = 0.25D;

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

        double socialScore =
                hostility * 0.30D;

        double immediateScore =
                calculateImmediateScore(
                        recentDamage,
                        highestRecentDamage,
                        recentAttackCount,
                        lastAttackAge,
                        repeatedAttack
                );

        double historicalScore =
                calculateHistoricalScore(
                        getMemoryEntry(
                                mob,
                                targetId
                        ),
                        mob.level().getGameTime()
                );

        double score =
                clamp(
                        socialScore
                                + Math.max(
                                        immediateScore,
                                        historicalScore
                                )
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
                        lastAttackAge,
                        immediateScore,
                        historicalScore,
                        socialScore
                );

        /*
         * A avaliação roda para cada entidade lembrada em
         * todo ciclo: a formatação só acontece quando o
         * log de diagnóstico está ativo.
         */
        if (MobTalk.LOGGER.isDebugEnabled()) {
            MobTalk.LOGGER.debug(
                    "MobTalk AMEAÇA: {} | alvo={} | nível={} | score={} | imediato={} | histórico={} | social={} | ataques={} | dano={} | maiorDano={} | repetido={} | idade={} ticks",
                    mob.getType(),
                    targetId,
                    level,
                    String.format(
                            "%.2f",
                            score
                    ),
                    String.format(
                            "%.2f",
                            immediateScore
                    ),
                    String.format(
                            "%.2f",
                            historicalScore
                    ),
                    String.format(
                            "%.2f",
                            socialScore
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
        }

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
     * Obtém a memória que o mob tem da entidade.
     */
    private static MemoryEntry getMemoryEntry(
            Mob mob,
            UUID targetId
    ) {

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return null;
        }

        return memory.getMemory(
                targetId
        );
    }

    /**
     * Calcula a parte imediata da ameaça.
     *
     * Combina, dentro da janela do contexto:
     *
     * - dano recente;
     * - maior dano recente;
     * - quantidade de ataques;
     * - recência;
     * - repetição dos ataques.
     *
     * A familiaridade não entra no cálculo: conhecer
     * uma entidade não a torna ameaçadora. Sem agressão,
     * a ameaça é NONE e a IA vanilla segue no controle.
     */
    private static double calculateImmediateScore(
            double recentDamage,
            double highestRecentDamage,
            int recentAttackCount,
            long lastAttackAge,
            boolean repeatedAttack
    ) {

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

        return damageScore
                + highestDamageScore
                + attackCountScore
                + recencyScore
                + repetitionScore;
    }

    /**
     * Calcula a parte histórica da ameaça.
     *
     * Metade vem da quantidade de ataques lembrados e
     * metade do dano total efetivo. O resultado perde
     * força pela meia-vida desde o último ataque.
     */
    private static double calculateHistoricalScore(
            MemoryEntry entry,
            long currentGameTime
    ) {

        if (entry == null || !entry.hasAttackHistory()) {
            return 0.0D;
        }

        double attackFactor =
                clamp(
                        entry.attackCount() / 5.0D
                );

        double damageFactor =
                clamp(
                        entry.totalDamageReceived() / 20.0D
                );

        double recall =
                entry.getAttackRecall(
                        currentGameTime,
                        HISTORY_HALF_LIFE_TICKS
                );

        return HISTORY_MAX_SCORE
                * (attackFactor * 0.5D + damageFactor * 0.5D)
                * recall;
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
