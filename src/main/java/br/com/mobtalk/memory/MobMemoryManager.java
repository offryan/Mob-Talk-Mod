package br.com.mobtalk.memory;

import br.com.mobtalk.MobTalkAttachments;

import net.minecraft.world.entity.Mob;

/**
 * Gerencia a memória individual dos mobs.
 *
 * Cada Mob possui sua própria instância de MobMemory,
 * guardada como attachment do NeoForge na própria
 * entidade. Assim a memória é gravada no NBT do mob e
 * sobrevive ao descarregamento do chunk, à troca de
 * dimensão e ao reinício do mundo.
 */
public final class MobMemoryManager {

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

        return mob.getData(
                MobTalkAttachments.MEMORY
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

        mob.removeData(
                MobTalkAttachments.MEMORY
        );
    }
}
