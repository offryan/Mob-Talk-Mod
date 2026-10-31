package br.com.mobtalk.context;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia o contexto cognitivo de cada mob.
 *
 * Cada mob possui seu próprio contexto independente.
 */
public final class MobContextManager {

    private static final Map<Mob, MobContext> CONTEXTS =
            new WeakHashMap<>();

    private MobContextManager() {
        // Classe utilitária.
    }

    /**
     * Obtém o contexto de um mob.
     *
     * Caso ainda não exista um contexto para o mob,
     * um novo contexto é criado automaticamente.
     */
    public static MobContext getContext(
            Mob mob
    ) {

        if (mob == null) {
            return null;
        }

        return CONTEXTS.computeIfAbsent(
                mob,
                ignored -> new MobContext()
        );
    }

    /**
     * Adiciona um evento diretamente ao contexto
     * de um mob.
     */
    public static void addEvent(
            Mob mob,
            MobContextEvent event
    ) {

        if (mob == null || event == null) {
            return;
        }

        MobContext context =
                getContext(mob);

        if (context == null) {
            return;
        }

        context.addEvent(event);
    }

    /**
     * Remove o contexto associado a um mob.
     */
    public static void removeContext(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        CONTEXTS.remove(mob);
    }

    /**
     * Limpa todos os contextos mantidos pelo sistema.
     */
    public static void clear() {

        CONTEXTS.clear();
    }
}