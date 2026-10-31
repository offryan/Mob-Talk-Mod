package br.com.mobtalk.memory;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia a memória individual dos mobs.
 *
 * Cada Mob possui sua própria instância de MobMemory.
 *
 * WeakHashMap é utilizado para que a referência da memória
 * possa ser liberada quando a entidade deixar de existir
 * e não houver mais referências para ela.
 *
 * Este sistema é inicialmente mantido em memória.
 *
 * A persistência em NBT será adicionada posteriormente,
 * quando definirmos quais memórias realmente precisam
 * sobreviver ao descarregamento do mundo.
 */
public final class MobMemoryManager {

    private static final Map<Mob, MobMemory> MEMORIES =
            new WeakHashMap<>();

    private MobMemoryManager() {
        // Classe utilitária.
    }

    /**
     * Obtém a memória pertencente a um mob.
     *
     * Caso o mob ainda não possua uma memória,
     * uma nova será criada.
     */
    public static MobMemory getMemory(
            Mob mob
    ) {

        if (mob == null) {
            return null;
        }

        return MEMORIES.computeIfAbsent(
                mob,
                ignored -> new MobMemory()
        );
    }

    /**
     * Remove a memória associada a um mob.
     */
    public static void removeMemory(
            Mob mob
    ) {

        if (mob == null) {
            return;
        }

        MEMORIES.remove(mob);
    }

    /**
     * Limpa todas as memórias atualmente gerenciadas.
     *
     * Útil futuramente para gerenciamento de mundos
     * e encerramento de sessões.
     */
    public static void clear() {
        MEMORIES.clear();
    }
}