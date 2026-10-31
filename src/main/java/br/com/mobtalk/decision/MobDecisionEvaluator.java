package br.com.mobtalk.decision;

import br.com.mobtalk.intent.MobIntent;
import br.com.mobtalk.intent.MobIntentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Avalia uma intenção considerando o contexto atual
 * do mob e do ambiente.
 *
 * Fluxo:
 *
 * INTENÇÃO
 *     +
 * CONTEXTO
 *     ↓
 * DECISÃO
 *
 * Esta classe não executa comportamentos.
 */
public final class MobDecisionEvaluator {

    /**
     * Distância máxima considerada adequada para
     * um ataque direto.
     */
    private static final double ATTACK_DISTANCE = 3.0D;

    /**
     * Percentual de vida abaixo do qual o mob é
     * considerado gravemente ferido.
     */
    private static final double CRITICAL_HEALTH_RATIO = 0.25D;

    private MobDecisionEvaluator() {
        // Classe utilitária.
    }

    /**
     * Avalia a intenção atual do mob.
     *
     * @param mob mob que está tomando a decisão
     * @param intent intenção atualmente calculada
     * @return decisão resultante
     */
    public static MobDecision evaluate(
            Mob mob,
            MobIntent intent
    ) {

        if (mob == null || intent == null) {
            return MobDecision.none();
        }

        if (intent.type() == MobIntentType.NONE) {
            return MobDecision.none();
        }

        Entity target =
                findTarget(
                        mob,
                        intent
                );

        /*
         * Algumas intenções futuras poderão existir
         * sem um alvo específico.
         */
        if (requiresTarget(intent.type())
                && target == null) {

            return MobDecision.none();
        }

        if (target != null && !target.isAlive()) {
            return MobDecision.none();
        }

        return switch (intent.type()) {

            case ATTACK ->
                    evaluateAttack(
                            mob,
                            target,
                            intent
                    );

            case OBSERVE ->
                    MobDecision.of(
                            MobDecisionType.OBSERVE,
                            intent.targetId(),
                            intent.priority()
                    );

            case APPROACH ->
                    MobDecision.of(
                            MobDecisionType.APPROACH,
                            intent.targetId(),
                            intent.priority()
                    );

            case AVOID ->
                    MobDecision.of(
                            MobDecisionType.AVOID,
                            intent.targetId(),
                            intent.priority()
                    );

            case FLEE ->
                    MobDecision.of(
                            MobDecisionType.FLEE,
                            intent.targetId(),
                            intent.priority()
                    );

            case PROTECT ->
                    MobDecision.of(
                            MobDecisionType.PROTECT,
                            intent.targetId(),
                            intent.priority()
                    );

            case INVESTIGATE ->
                    MobDecision.of(
                            MobDecisionType.OBSERVE,
                            intent.targetId(),
                            intent.priority()
                    );

            case NONE ->
                    MobDecision.none();
        };
    }

    /**
     * Avalia especificamente uma intenção de ataque.
     *
     * O mob não deve simplesmente atacar porque
     * possui hostilidade suficiente.
     *
     * O contexto também importa.
     */
    private static MobDecision evaluateAttack(
            Mob mob,
            Entity target,
            MobIntent intent
    ) {

        double healthRatio =
                getHealthRatio(mob);

        /*
         * Se o mob estiver gravemente ferido,
         * ele não considera o ataque a melhor decisão.
         *
         * Por enquanto usamos FLEE.
         *
         * Futuramente teremos uma decisão de fuga
         * mais sofisticada.
         */
        if (healthRatio <= CRITICAL_HEALTH_RATIO) {

            return MobDecision.of(
                    MobDecisionType.FLEE,
                    target.getUUID(),
                    calculateFleeConfidence(
                            healthRatio,
                            intent.priority()
                    )
            );
        }

        double distance =
                mob.distanceTo(target);

        /*
         * Se o alvo estiver próximo o suficiente,
         * atacar passa a ser uma decisão válida.
         */
        if (distance <= ATTACK_DISTANCE) {

            return MobDecision.of(
                    MobDecisionType.ATTACK,
                    target.getUUID(),
                    calculateAttackConfidence(
                            intent.priority(),
                            healthRatio,
                            distance
                    )
            );
        }

        /*
         * O mob quer atacar, mas ainda está longe.
         *
         * Portanto, a decisão imediata é aproximar-se.
         */
        return MobDecision.of(
                MobDecisionType.APPROACH,
                target.getUUID(),
                calculateApproachConfidence(
                        intent.priority(),
                        distance
                )
        );
    }

    /**
     * Obtém o alvo associado à intenção.
     *
     * O sistema de decisão funciona no lado servidor,
     * portanto utilizamos ServerLevel para localizar
     * uma entidade pelo UUID.
     */
    private static Entity findTarget(
            Mob mob,
            MobIntent intent
    ) {

        if (intent.targetId() == null) {
            return null;
        }

        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        return serverLevel.getEntity(
                intent.targetId()
        );
    }

    /**
     * Determina se determinado tipo de intenção
     * necessita de um alvo.
     */
    private static boolean requiresTarget(
            MobIntentType type
    ) {

        return switch (type) {

            case NONE -> false;

            case INVESTIGATE,
                    OBSERVE,
                    APPROACH,
                    AVOID,
                    FLEE,
                    ATTACK,
                    PROTECT -> true;
        };
    }

    /**
     * Calcula a proporção atual de vida do mob.
     */
    private static double getHealthRatio(
            Mob mob
    ) {

        float maxHealth =
                mob.getMaxHealth();

        if (maxHealth <= 0.0F) {
            return 0.0D;
        }

        return mob.getHealth() / maxHealth;
    }

    /**
     * Calcula a confiança de uma decisão de ataque.
     */
    private static double calculateAttackConfidence(
            double priority,
            double healthRatio,
            double distance
    ) {

        double distanceFactor =
                1.0D - Math.min(
                        distance / ATTACK_DISTANCE,
                        1.0D
                );

        double healthFactor =
                Math.min(
                        healthRatio,
                        1.0D
                );

        return priority * 0.60D
                + healthFactor * 0.20D
                + distanceFactor * 0.20D;
    }

    /**
     * Calcula a confiança de uma aproximação.
     */
    private static double calculateApproachConfidence(
            double priority,
            double distance
    ) {

        double distanceFactor =
                Math.min(
                        distance / 16.0D,
                        1.0D
                );

        return priority * 0.70D
                + distanceFactor * 0.30D;
    }

    /**
     * Calcula a confiança de uma fuga.
     */
    private static double calculateFleeConfidence(
            double healthRatio,
            double priority
    ) {

        double injuryFactor =
                1.0D - healthRatio;

        return injuryFactor * 0.70D
                + priority * 0.30D;
    }
}