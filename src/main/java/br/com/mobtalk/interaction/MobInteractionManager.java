package br.com.mobtalk.interaction;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.context.MobContextEvent;
import br.com.mobtalk.context.MobContextManager;
import br.com.mobtalk.memory.MemoryEntry;
import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.memory.MobMemoryManager;
import br.com.mobtalk.social.MobRelationship;
import br.com.mobtalk.social.MobRelationshipManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Gerencia as interações entre mobs e outras entidades.
 *
 * As interações representam acontecimentos relevantes
 * que podem alterar memória, relacionamentos e contexto.
 */
public final class MobInteractionManager {

    private MobInteractionManager() {
        // Classe utilitária.
    }

    /**
     * Registra que um mob foi danificado por outra entidade.
     *
     * Essa interação:
     *
     * - registra o acontecimento no contexto;
     * - atualiza a memória da entidade;
     * - acumula o histórico de dano;
     * - aumenta a familiaridade;
     * - aumenta a hostilidade.
     */
    public static void onDamagedBy(
            Mob mob,
            Entity attacker,
            float damage
    ) {

        if (mob == null || attacker == null) {
            return;
        }

        if (attacker == mob) {
            return;
        }

        if (damage <= 0.0F) {
            return;
        }

        long gameTime =
                mob.level().getGameTime();

        MobInteraction interaction =
                new MobInteraction(
                        mob.getUUID(),
                        attacker.getUUID(),
                        MobInteraction.MobInteractionType.DAMAGED_BY,
                        gameTime,
                        damage
                );

        processInteraction(
                mob,
                attacker,
                interaction
        );
    }

    /**
     * Processa uma interação e atualiza os estados
     * cognitivos associados ao acontecimento.
     */
    private static void processInteraction(
            Mob mob,
            Entity otherEntity,
            MobInteraction interaction
    ) {

        rememberEntity(
                mob,
                otherEntity,
                interaction.gameTime()
        );

        registerDamageMemory(
                mob,
                otherEntity,
                interaction
        );

        registerContextEvent(
                mob,
                otherEntity,
                interaction
        );

        MobRelationship relationship =
                MobRelationshipManager.getRelationship(
                        mob,
                        otherEntity.getUUID()
                );

        if (relationship == null) {
            MobTalk.LOGGER.warn(
                    "MobTalk: relacionamento nulo para {} -> {}",
                    mob.getType(),
                    otherEntity.getType()
            );

            return;
        }

        double hostilityIncrease =
                calculateHostilityIncrease(
                        interaction.intensity()
                );

        MobRelationship updatedRelationship =
                relationship
                        .increaseFamiliarity(0.05D)
                        .increaseHostility(
                                hostilityIncrease
                        );

        MobRelationshipManager.setRelationship(
                mob,
                updatedRelationship
        );

        /*
         * Diagnóstico detalhado do estado social.
         */
        MobTalk.LOGGER.info(
                "MobTalk RELACIONAMENTO: {} -> {} | familiaridade={} | hostilidade={} | confiança={}",
                mob.getType(),
                otherEntity.getType(),
                String.format(
                        "%.2f",
                        updatedRelationship.familiarity()
                ),
                String.format(
                        "%.2f",
                        updatedRelationship.hostility()
                ),
                String.format(
                        "%.2f",
                        updatedRelationship.trust()
                )
        );

        /*
         * Diagnóstico da interação.
         */
        MobTalk.LOGGER.debug(
                "MobTalk: interação {} entre {} e {} | intensidade={}",
                interaction.type(),
                mob.getType(),
                otherEntity.getType(),
                interaction.intensity()
        );
    }

    /**
     * Registra o dano recebido no histórico persistente
     * em memória do mob.
     *
     * Diferentemente do contexto, que representa um
     * acontecimento recente, essa informação é acumulada
     * na memória da entidade.
     */
    private static void registerDamageMemory(
            Mob mob,
            Entity otherEntity,
            MobInteraction interaction
    ) {

        if (interaction.type()
                != MobInteraction.MobInteractionType.DAMAGED_BY) {
            return;
        }

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return;
        }

        memory.rememberDamage(
                otherEntity.getUUID(),
                otherEntity.getType().toString(),
                interaction.intensity(),
                interaction.gameTime(),
                mob.distanceTo(otherEntity),
                otherEntity.isAlive()
        );

        MemoryEntry updatedMemory =
                memory.getMemory(
                        otherEntity.getUUID()
                );

        if (updatedMemory == null) {
            return;
        }

        MobTalk.LOGGER.debug(
                "MobTalk MEMÓRIA DE COMBATE: {} -> {} | ataques={} | danoTotal={} | maiorDano={} | últimoAtaque={} ticks",
                mob.getType(),
                otherEntity.getType(),
                updatedMemory.attackCount(),
                String.format(
                        "%.2f",
                        updatedMemory.totalDamageReceived()
                ),
                String.format(
                        "%.2f",
                        updatedMemory.highestDamageReceived()
                ),
                updatedMemory.getLastAttackAge(
                        interaction.gameTime()
                )
        );
    }

    /**
     * Registra a interação como um acontecimento
     * no contexto cognitivo do mob.
     */
    private static void registerContextEvent(
            Mob mob,
            Entity otherEntity,
            MobInteraction interaction
    ) {

        MobContextEvent.MobContextEventType eventType =
                switch (interaction.type()) {

                    case DAMAGED_BY ->
                            MobContextEvent.MobContextEventType.DAMAGED_BY;

                    case DAMAGED_OTHER ->
                            MobContextEvent.MobContextEventType.DAMAGED_OTHER;
                };

        MobContextEvent contextEvent =
                new MobContextEvent(
                        mob.getUUID(),
                        otherEntity.getUUID(),
                        eventType,
                        interaction.gameTime(),
                        interaction.intensity()
                );

        MobContextManager.addEvent(
                mob,
                contextEvent
        );

        MobTalk.LOGGER.debug(
                "MobTalk CONTEXTO: {} registrou {} envolvendo {} | intensidade={}",
                mob.getType(),
                eventType,
                otherEntity.getType(),
                String.format(
                        "%.2f",
                        interaction.intensity()
                )
        );
    }

    /**
     * Registra a entidade na memória do mob.
     *
     * Essa operação atualiza as informações de observação
     * sem apagar o histórico acumulado de interações.
     */
    private static void rememberEntity(
            Mob mob,
            Entity entity,
            long gameTime
    ) {

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return;
        }

        MemoryEntry entry =
                MemoryEntry.initial(
                        entity.getUUID(),
                        entity.getType().toString(),
                        gameTime,
                        mob.distanceTo(entity),
                        entity.isAlive()
                );

        memory.remember(
                entry
        );

        MobTalk.LOGGER.debug(
                "MobTalk MEMÓRIA: {} lembrou de {} | distância={} | memórias={}",
                mob.getType(),
                entity.getType(),
                String.format(
                        "%.2f",
                        mob.distanceTo(entity)
                ),
                memory.size()
        );
    }

    /**
     * Calcula quanto a hostilidade aumenta
     * em resposta ao dano recebido.
     */
    private static double calculateHostilityIncrease(
            double damage
    ) {

        double increase =
                0.05D + (damage * 0.01D);

        return Math.min(
                increase,
                0.20D
        );
    }
}