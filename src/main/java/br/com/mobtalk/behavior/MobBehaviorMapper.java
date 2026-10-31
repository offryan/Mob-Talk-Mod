package br.com.mobtalk.behavior;

import br.com.mobtalk.decision.MobDecision;

/**
 * Converte uma decisão cognitiva em um comportamento.
 *
 * Fluxo:
 *
 * DECISÃO
 *    ↓
 * MAPEAMENTO
 *    ↓
 * COMPORTAMENTO
 *
 * Nenhuma ação física é executada aqui.
 */
public final class MobBehaviorMapper {

    private MobBehaviorMapper() {
        // Classe utilitária.
    }

    /**
     * Converte uma decisão em comportamento.
     *
     * @param decision decisão atual do mob
     * @return comportamento correspondente
     */
    public static MobBehavior map(
            MobDecision decision
    ) {

        if (decision == null) {
            return MobBehavior.none();
        }

        return switch (decision.type()) {

            case NONE ->
                    MobBehavior.none();

            case OBSERVE ->
                    MobBehavior.of(
                            MobBehaviorType.OBSERVE,
                            decision.targetId(),
                            decision.confidence()
                    );

            case APPROACH ->
                    MobBehavior.of(
                            MobBehaviorType.APPROACH,
                            decision.targetId(),
                            decision.confidence()
                    );

            case ATTACK ->
                    MobBehavior.of(
                            MobBehaviorType.ATTACK,
                            decision.targetId(),
                            decision.confidence()
                    );

            case AVOID ->
                    MobBehavior.of(
                            MobBehaviorType.AVOID,
                            decision.targetId(),
                            decision.confidence()
                    );

            case FLEE ->
                    MobBehavior.of(
                            MobBehaviorType.FLEE,
                            decision.targetId(),
                            decision.confidence()
                    );

            case PROTECT ->
                    MobBehavior.of(
                            MobBehaviorType.PROTECT,
                            decision.targetId(),
                            decision.confidence()
                    );
        };
    }
}