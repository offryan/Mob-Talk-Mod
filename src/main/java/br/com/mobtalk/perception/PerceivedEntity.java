package br.com.mobtalk.perception;

import java.util.UUID;

import net.minecraft.world.entity.Entity;

/**
 * Representa uma entidade percebida por um mob.
 *
 * Esta classe não representa a entidade em si.
 * Ela representa a informação que o sistema de percepção
 * conseguiu obter sobre aquela entidade.
 *
 * Futuramente essa estrutura poderá crescer para incluir:
 *
 * - hostilidade;
 * - amizade;
 * - medo;
 * - confiança;
 * - visibilidade;
 * - linha de visão;
 * - ameaça;
 * - distância;
 * - direção;
 * - espécie;
 * - histórico;
 * - etc.
 */
public record PerceivedEntity(
        UUID entityId,
        String entityType,
        double distance,
        boolean alive
) {

    /**
     * Cria uma percepção a partir de uma entidade do Minecraft.
     */
    public static PerceivedEntity from(
            Entity observer,
            Entity target
    ) {

        return new PerceivedEntity(
                target.getUUID(),
                target.getType().toString(),
                observer.distanceTo(target),
                target.isAlive()
        );
    }
}