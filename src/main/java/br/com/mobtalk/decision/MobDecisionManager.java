package br.com.mobtalk.decision;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia a decisão atual de cada mob.
 *
 * Assim como memória, relacionamento e intenção,
 * cada mob possui seu próprio estado.
 */
public final class MobDecisionManager {

    private static final Map<Mob, MobDecision> DECISIONS =
            new WeakHashMap<>();

    private MobDecisionManager() {
        // Classe utilitária.
    }

    /**
     * Obtém a decisão atual de um mob.
     */
    public static MobDecision getDecision(
            Mob mob
    ) {

        if (mob == null) {
            return MobDecision.none();
        }

        return DECISIONS.getOrDefault(
                mob,
                MobDecision.none()
        );
    }

    /**
     * Define a decisão atual de um mob.
     */
    public static void setDecision(
            Mob mob,
            MobDecision decision
    ) {

        if (mob == null || decision == null) {
            return;
        }

        DECISIONS.put(
                mob,
                decision
        );
    }

    /**
     * Remove a decisão de um mob.
     */
    public static void removeDecision(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        DECISIONS.remove(mob);
    }

    /**
     * Limpa todas as decisões armazenadas.
     */
    public static void clear() {
        DECISIONS.clear();
    }
}