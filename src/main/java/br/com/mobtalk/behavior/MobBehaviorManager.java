package br.com.mobtalk.behavior;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia o comportamento atual de cada mob.
 *
 * Cada mob possui seu próprio estado comportamental.
 *
 * A execução real do comportamento será adicionada
 * posteriormente.
 */
public final class MobBehaviorManager {

    private static final Map<Mob, MobBehavior> BEHAVIORS =
            new WeakHashMap<>();

    private MobBehaviorManager() {
        // Classe utilitária.
    }

    /**
     * Obtém o comportamento atual de um mob.
     */
    public static MobBehavior getBehavior(
            Mob mob
    ) {

        if (mob == null) {
            return MobBehavior.none();
        }

        return BEHAVIORS.getOrDefault(
                mob,
                MobBehavior.none()
        );
    }

    /**
     * Define o comportamento atual de um mob.
     */
    public static void setBehavior(
            Mob mob,
            MobBehavior behavior
    ) {

        if (mob == null || behavior == null) {
            return;
        }

        BEHAVIORS.put(
                mob,
                behavior
        );
    }

    /**
     * Remove o comportamento de um mob.
     */
    public static void removeBehavior(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        BEHAVIORS.remove(mob);
    }

    /**
     * Limpa todos os comportamentos armazenados.
     */
    public static void clear() {
        BEHAVIORS.clear();
    }
}