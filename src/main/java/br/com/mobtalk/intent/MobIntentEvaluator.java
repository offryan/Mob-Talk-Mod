package br.com.mobtalk.intent;

import java.util.UUID;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.context.MobContextInterpreter;
import br.com.mobtalk.memory.MemoryEntry;
import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.memory.MobMemoryManager;
import br.com.mobtalk.profile.MobProfile;
import br.com.mobtalk.profile.MobProfileManager;
import br.com.mobtalk.social.MobRelationship;
import br.com.mobtalk.social.MobRelationshipManager;
import br.com.mobtalk.threat.MobThreatAssessment;
import br.com.mobtalk.threat.MobThreatManager;

import net.minecraft.world.entity.Mob;

/**
 * Avalia a intenção cognitiva de um mob.
 *
 * A intenção representa aquilo que o mob deseja fazer
 * diante da situação atual.
 *
 * O avaliador combina:
 *
 * - memória;
 * - contexto;
 * - relacionamento;
 * - ameaça;
 * - perfil comportamental;
 * - estabilidade da intenção.
 *
 * O avaliador não executa ações físicas.
 */
public final class MobIntentEvaluator {

    /**
     * Pontuação mínima para um mob continuar em FLEE
     * depois de já ter entrado nesse estado.
     *
     * Isso cria hysteresis e evita oscilações rápidas
     * entre FLEE e AVOID.
     */
    private static final double FLEE_EXIT_SCORE = 0.40D;

    /**
     * Pontuação mínima para manter AVOID.
     */
    private static final double AVOID_EXIT_SCORE = 0.20D;

    private MobIntentEvaluator() {
        // Classe utilitária.
    }

    /**
     * Avalia a intenção atual do mob.
     */
    public static MobIntent evaluate(
            Mob mob
    ) {

        if (mob == null) {
            return MobIntent.none();
        }

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return MobIntent.none();
        }

        MemoryEntry bestCandidate =
                findBestCandidate(
                        mob,
                        memory
                );

        if (bestCandidate == null) {
            return MobIntent.none();
        }

        UUID targetId =
                bestCandidate.entityId();

        MobRelationship relationship =
                MobRelationshipManager.getRelationship(
                        mob,
                        targetId
                );

        double hostility =
                getHostility(
                        relationship
                );

        double trust =
                getTrust(
                        relationship
                );

        MobThreatAssessment threat =
                MobThreatManager.getThreat(
                        mob,
                        targetId
                );

        MobProfile profile =
                MobProfileManager.getProfile(
                        mob
                );

        MobIntent previousIntent =
                MobIntentManager.getIntent(
                        mob
                );

        MobIntent threatResponse =
                evaluateThreatResponse(
                        profile,
                        threat,
                        previousIntent,
                        targetId
                );

        if (threatResponse.type()
                != MobIntentType.NONE) {

            return threatResponse;
        }

        /*
         * A partir daqui permanecem as regras cognitivas
         * complementares.
         */

        if (hostility >= 0.70D) {

            double priority =
                    calculateHostilityPriority(
                            hostility,
                            threat
                    );

            return MobIntent.of(
                    MobIntentType.ATTACK,
                    targetId,
                    priority
            );
        }

        if (MobContextInterpreter.wasRecentlyDamagedBy(
                mob,
                targetId
        )) {

            if (MobContextInterpreter.hasRepeatedRecentDamage(
                    mob,
                    targetId
            )) {

                double priority =
                        calculateRepeatedAttackPriority(
                                mob,
                                targetId,
                                hostility,
                                threat
                        );

                return MobIntent.of(
                        MobIntentType.OBSERVE,
                        targetId,
                        priority
                );
            }

            double priority =
                    calculateRecentAttackPriority(
                            mob,
                            targetId,
                            hostility,
                            threat
                    );

            return MobIntent.of(
                    MobIntentType.OBSERVE,
                    targetId,
                    priority
            );
        }

        if (hostility >= 0.30D) {

            double priority =
                    Math.min(
                            1.0D,
                            0.35D
                                    + hostility * 0.50D
                    );

            return MobIntent.of(
                    MobIntentType.OBSERVE,
                    targetId,
                    priority
            );
        }

        if (trust >= 0.60D) {

            double priority =
                    Math.min(
                            1.0D,
                            0.40D
                                    + trust * 0.50D
                    );

            return MobIntent.of(
                    MobIntentType.APPROACH,
                    targetId,
                    priority
            );
        }

        return MobIntent.none();
    }

    /**
     * Avalia a ameaça levando em consideração
     * a intenção anterior.
     *
     * Isso impede que o mob mude de ideia
     * imediatamente quando a ameaça oscila
     * próximo de um limite.
     */
    private static MobIntent evaluateThreatResponse(
            MobProfile profile,
            MobThreatAssessment threat,
            MobIntent previousIntent,
            UUID targetId
    ) {

        if (profile == null
                || threat == null
                || targetId == null) {

            return MobIntent.none();
        }

        /*
         * HYSTERESIS DE FLEE
         *
         * Se o mob já estava fugindo do mesmo alvo,
         * ele continua fugindo enquanto a ameaça
         * permanecer acima do limite de saída.
         *
         * A fuga só se mantém enquanto houver ataque
         * recente: hostilidade e memória histórica,
         * sozinhas, não sustentam uma fuga.
         */
        if (isSameTargetIntent(
                previousIntent,
                targetId
        )
                && previousIntent.type()
                == MobIntentType.FLEE
                && threat.hasRecentAttack()
                && threat.score() > FLEE_EXIT_SCORE) {

            double priority =
                    calculateThreatPriority(
                            threat
                    );

            if (MobTalk.LOGGER.isDebugEnabled()) {
                MobTalk.LOGGER.debug(
                        "MobTalk INTENÇÃO ESTÁVEL: mantendo FLEE | ameaça={} | score={} | alvo={}",
                        threat.level(),
                        String.format(
                                "%.2f",
                                threat.score()
                        ),
                        targetId
                );
            }

            return MobIntent.of(
                    MobIntentType.FLEE,
                    targetId,
                    priority
            );
        }

        /*
         * HYSTERESIS DE AVOID
         *
         * Se o mob estava evitando o mesmo alvo,
         * mantemos AVOID enquanto ainda houver
         * uma ameaça significativa.
         */
        if (isSameTargetIntent(
                previousIntent,
                targetId
        )
                && previousIntent.type()
                == MobIntentType.AVOID
                && threat.score() > AVOID_EXIT_SCORE) {

            MobIntentType profileResponse =
                    profile.determineThreatResponse(
                            threat.level()
                    );

            /*
             * Se o perfil agora exige FLEE,
             * não seguramos AVOID.
             */
            if (profileResponse
                    == MobIntentType.FLEE) {

                return createThreatIntent(
                        profileResponse,
                        targetId,
                        threat,
                        profile
                );
            }

            /*
             * Caso a ameaça ainda seja relevante,
             * mantemos AVOID.
             */
            if (profileResponse
                    == MobIntentType.AVOID
                    || profileResponse
                    == MobIntentType.OBSERVE) {

                return createThreatIntent(
                        MobIntentType.AVOID,
                        targetId,
                        threat,
                        profile
                );
            }
        }

        MobIntentType threatIntent =
                profile.determineThreatResponse(
                        threat.level()
                );

        return createThreatIntent(
                threatIntent,
                targetId,
                threat,
                profile
        );
    }

    /**
     * Verifica se uma intenção possui o mesmo alvo
     * que a ameaça atualmente avaliada.
     */
    private static boolean isSameTargetIntent(
            MobIntent intent,
            UUID targetId
    ) {

        if (intent == null
                || targetId == null) {

            return false;
        }

        return targetId.equals(
                intent.targetId()
        );
    }

    /**
     * Cria a intenção derivada da avaliação de ameaça.
     */
    private static MobIntent createThreatIntent(
            MobIntentType threatIntent,
            UUID targetId,
            MobThreatAssessment threat,
            MobProfile profile
    ) {

        if (threatIntent == null
                || threat == null
                || profile == null) {

            return MobIntent.none();
        }

        if (threatIntent == MobIntentType.NONE) {
            return MobIntent.none();
        }

        double priority =
                calculateThreatPriority(
                        threat
                );

        if (MobTalk.LOGGER.isDebugEnabled()) {
            MobTalk.LOGGER.debug(
                    "MobTalk INTENÇÃO AMEAÇA: perfil={} | ameaça={} | score={} | resposta={} | prioridade={}",
                    profile.type(),
                    threat.level(),
                    String.format(
                            "%.2f",
                            threat.score()
                    ),
                    threatIntent,
                    String.format(
                            "%.2f",
                            priority
                    )
            );
        }

        return MobIntent.of(
                threatIntent,
                targetId,
                priority
        );
    }

    /**
     * Calcula a prioridade derivada da ameaça.
     */
    private static double calculateThreatPriority(
            MobThreatAssessment threat
    ) {

        double base =
                threat.score();

        return switch (threat.level()) {

            case NONE ->
                    0.0D;

            case LOW ->
                    Math.max(
                            0.20D,
                            base
                    );

            case MODERATE ->
                    Math.max(
                            0.40D,
                            base
                    );

            case HIGH ->
                    Math.max(
                            0.60D,
                            base
                    );

            case CRITICAL ->
                    Math.max(
                            0.80D,
                            base
                    );
        };
    }

    /**
     * Calcula a prioridade de ataque baseada
     * na hostilidade e na ameaça atual.
     */
    private static double calculateHostilityPriority(
            double hostility,
            MobThreatAssessment threat
    ) {

        double threatScore =
                threat == null
                        ? 0.0D
                        : threat.score();

        return Math.min(
                1.0D,
                0.55D
                        + hostility * 0.30D
                        + threatScore * 0.15D
        );
    }

    /**
     * Calcula a prioridade de uma reação a um ataque recente.
     */
    private static double calculateRecentAttackPriority(
            Mob mob,
            UUID targetId,
            double hostility,
            MobThreatAssessment threat
    ) {

        long age =
                MobContextInterpreter.getLastDamageAge(
                        mob,
                        targetId
                );

        double recencyScore =
                calculateRecencyScore(
                        age
                );

        double threatScore =
                threat == null
                        ? 0.0D
                        : threat.score();

        return Math.min(
                1.0D,
                0.25D
                        + recencyScore * 0.35D
                        + hostility * 0.20D
                        + threatScore * 0.20D
        );
    }

    /**
     * Calcula a prioridade de uma sequência de ataques.
     */
    private static double calculateRepeatedAttackPriority(
            Mob mob,
            UUID targetId,
            double hostility,
            MobThreatAssessment threat
    ) {

        MobContextInterpreter.DamageHistory history =
                MobContextInterpreter.getDamageHistory(
                        mob,
                        targetId
                );

        double repetitionScore =
                clamp(
                        history.damageCount() / 5.0D
                );

        double totalDamageScore =
                clamp(
                        history.totalDamage() / 10.0D
                );

        double highestDamageScore =
                clamp(
                        history.highestDamage() / 10.0D
                );

        double recencyScore =
                calculateRecencyScore(
                        history.lastDamageAge()
                );

        double threatScore =
                threat == null
                        ? 0.0D
                        : threat.score();

        double priority =
                repetitionScore * 0.25D
                        + totalDamageScore * 0.20D
                        + highestDamageScore * 0.10D
                        + recencyScore * 0.20D
                        + hostility * 0.10D
                        + threatScore * 0.15D;

        return clamp(priority);
    }

    /**
     * Calcula o quanto um acontecimento recente
     * ainda influencia a intenção.
     */
    private static double calculateRecencyScore(
            long age
    ) {

        if (age < 0L) {
            return 0.0D;
        }

        return clamp(
                1.0D
                        - age / 200.0D
        );
    }

    /**
     * Encontra o melhor candidato atualmente registrado
     * na memória do mob.
     */
    private static MemoryEntry findBestCandidate(
            Mob mob,
            MobMemory memory
    ) {

        MemoryEntry bestCandidate = null;

        double bestScore = -1.0D;

        for (MemoryEntry entry :
                memory.getMemories()) {

            if (entry == null) {
                continue;
            }

            double score =
                    calculateCandidateScore(
                            mob,
                            entry
                    );

            if (score > bestScore) {

                bestScore = score;
                bestCandidate = entry;
            }
        }

        return bestCandidate;
    }

    /**
     * Calcula a relevância cognitiva de uma entidade
     * atualmente presente na memória.
     */
    private static double calculateCandidateScore(
            Mob mob,
            MemoryEntry entry
    ) {

        double score = 0.0D;

        MobRelationship relationship =
                MobRelationshipManager.getRelationship(
                        mob,
                        entry.entityId()
                );

        if (relationship != null) {

            score +=
                    relationship.hostility()
                            * 0.40D;

            score +=
                    relationship.trust()
                            * 0.20D;

            score +=
                    relationship.familiarity()
                            * 0.10D;
        }

        MobThreatAssessment threat =
                MobThreatManager.getThreat(
                        mob,
                        entry.entityId()
                );

        score +=
                threat.score()
                        * 0.30D;

        return score;
    }

    private static double getHostility(
            MobRelationship relationship
    ) {

        if (relationship == null) {
            return 0.0D;
        }

        return relationship.hostility();
    }

    private static double getTrust(
            MobRelationship relationship
    ) {

        if (relationship == null) {
            return 0.0D;
        }

        return relationship.trust();
    }

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