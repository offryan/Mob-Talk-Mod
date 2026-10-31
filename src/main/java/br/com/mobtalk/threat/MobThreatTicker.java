package br.com.mobtalk.threat;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.memory.MemoryEntry;
import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.memory.MobMemoryManager;

import java.util.Collection;

import net.minecraft.world.entity.Mob;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Executa periodicamente a avaliação de ameaça
 * dos mobs que possuem entidades registradas em memória.
 */
@EventBusSubscriber(modid = MobTalk.MODID)
public final class MobThreatTicker {

    public static final int THREAT_INTERVAL_TICKS = 10;

    private MobThreatTicker() {
        // Classe utilitária.
    }

    @SubscribeEvent
    public static void onEntityTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        if (mob.level().isClientSide()) {
            return;
        }

        if (!mob.isAlive()) {
            return;
        }

        if (!shouldEvaluate(mob)) {
            return;
        }

        evaluateThreats(mob);
    }

    private static boolean shouldEvaluate(
            Mob mob
    ) {

        return Math.floorMod(
                mob.tickCount + mob.getId(),
                THREAT_INTERVAL_TICKS
        ) == 0;
    }

    private static void evaluateThreats(
            Mob mob
    ) {

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return;
        }

        Collection<MemoryEntry> memories =
                memory.getMemories();

        if (memories.isEmpty()) {
            return;
        }

        for (MemoryEntry memoryEntry : memories) {

            MobThreatAssessment assessment =
                    MobThreatEvaluator.evaluate(
                            mob,
                            memoryEntry.entityId()
                    );

            MobThreatManager.setThreat(
                    mob,
                    assessment
            );
        }
    }
}