package br.com.mobtalk.cognition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import br.com.mobtalk.MobTalk;
import br.com.mobtalk.behavior.MobBehavior;
import br.com.mobtalk.behavior.MobBehaviorExecutor;
import br.com.mobtalk.behavior.MobBehaviorManager;
import br.com.mobtalk.behavior.MobBehaviorMapper;
import br.com.mobtalk.context.MobContextManager;
import br.com.mobtalk.decision.MobDecision;
import br.com.mobtalk.decision.MobDecisionEvaluator;
import br.com.mobtalk.decision.MobDecisionManager;
import br.com.mobtalk.intent.MobIntent;
import br.com.mobtalk.intent.MobIntentEvaluator;
import br.com.mobtalk.intent.MobIntentManager;
import br.com.mobtalk.memory.MemoryEntry;
import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.memory.MobMemoryManager;
import br.com.mobtalk.perception.MobPerception;
import br.com.mobtalk.social.MobRelationshipManager;
import br.com.mobtalk.threat.MobThreatAssessment;
import br.com.mobtalk.threat.MobThreatEvaluator;
import br.com.mobtalk.threat.MobThreatManager;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Cérebro central do MobTalk.
 *
 * Este sistema coordena o ciclo cognitivo completo dos mobs
 * em uma ordem determinística.
 *
 * Fluxo:
 *
 * PERCEPÇÃO
 *     ↓
 * MEMÓRIA
 *     ↓
 * RELACIONAMENTO
 *     ↓
 * CONTEXTO
 *     ↓
 * AMEAÇA
 *     ↓
 * INTENÇÃO
 *     ↓
 * DECISÃO
 *     ↓
 * COMPORTAMENTO
 *     ↓
 * EXECUTOR
 *     ↓
 * AÇÃO
 */
@EventBusSubscriber(modid = MobTalk.MODID)
public final class MobCognitionTicker {

    /**
     * Intervalo entre ciclos cognitivos.
     *
     * 10 ticks = aproximadamente 0,5 segundo
     * em um servidor rodando a 20 TPS.
     */
    public static final int COGNITION_INTERVAL_TICKS = 10;

    private MobCognitionTicker() {
        // Classe utilitária.
    }

    /**
     * Entrada principal do sistema cognitivo.
     */
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

        if (!shouldThink(mob)) {
            return;
        }

        processCognition(mob);
    }

    /**
     * Determina quando o mob deve executar
     * um novo ciclo cognitivo.
     *
     * O deslocamento pelo ID distribui os ciclos
     * entre diferentes mobs, evitando que todos
     * processem simultaneamente.
     */
    private static boolean shouldThink(
            Mob mob
    ) {

        return Math.floorMod(
                mob.tickCount + mob.getId(),
                COGNITION_INTERVAL_TICKS
        ) == 0;
    }

    /**
     * Executa o ciclo cognitivo completo.
     */
    private static void processCognition(
            Mob mob
    ) {

        MobTalk.LOGGER.debug(
                "MobTalk COGNIÇÃO: iniciando ciclo para {} | id={}",
                mob.getType(),
                mob.getId()
        );

        /*
         * =====================================================
         * 1. PERCEPÇÃO
         * =====================================================
         *
         * A percepção identifica entidades próximas.
         *
         * O sistema de percepção também atualiza:
         *
         * - memória;
         * - familiaridade;
         * - relacionamentos.
         */
        updatePerception(mob);

        /*
         * =====================================================
         * 2. CONTEXTO
         * =====================================================
         *
         * Acontecimentos recentes que ultrapassaram sua
         * duração deixam de influenciar a interpretação.
         *
         * O conhecimento acumulado continua na memória.
         */
        MobContextManager.removeExpiredEvents(
                mob,
                mob.level().getGameTime()
        );

        /*
         * =====================================================
         * 3. AMEAÇA
         * =====================================================
         *
         * Cada entidade lembrada é avaliada uma única vez
         * por ciclo. A intenção consulta o resultado
         * armazenado em vez de recalcular.
         */
        updateThreats(mob);

        /*
         * =====================================================
         * 4. INTENÇÃO
         * =====================================================
         *
         * O mob interpreta o que deseja fazer
         * com base no estado cognitivo atual.
         */
        MobIntent previousIntent =
                MobIntentManager.getIntent(mob);

        MobIntent currentIntent =
                MobIntentEvaluator.evaluate(mob);

        MobIntentManager.setIntent(
                mob,
                currentIntent
        );

        logIntentChange(
                mob,
                previousIntent,
                currentIntent
        );

        /*
         * =====================================================
         * 5. DECISÃO
         * =====================================================
         *
         * A decisão transforma a intenção em uma
         * resposta operacional.
         */
        MobDecision previousDecision =
                MobDecisionManager.getDecision(mob);

        MobDecision currentDecision =
                MobDecisionEvaluator.evaluate(
                        mob,
                        currentIntent
                );

        MobDecisionManager.setDecision(
                mob,
                currentDecision
        );

        logDecisionChange(
                mob,
                previousDecision,
                currentDecision
        );

        /*
         * =====================================================
         * 6. COMPORTAMENTO
         * =====================================================
         *
         * O comportamento representa aquilo que o mob
         * efetivamente tentará executar.
         */
        MobBehavior previousBehavior =
                MobBehaviorManager.getBehavior(mob);

        MobBehavior currentBehavior =
                MobBehaviorMapper.map(
                        currentDecision
                );

        MobBehaviorManager.setBehavior(
                mob,
                currentBehavior
        );

        logBehaviorChange(
                mob,
                previousBehavior,
                currentBehavior
        );

        /*
         * =====================================================
         * 7. EXECUTOR
         * =====================================================
         *
         * Finalmente o comportamento é convertido
         * em ações reais utilizando a API vanilla.
         */
        MobBehaviorExecutor.execute(
                mob,
                currentBehavior
        );

        MobTalk.LOGGER.debug(
                "MobTalk COGNIÇÃO: ciclo concluído para {} | intenção={} | decisão={} | comportamento={}",
                mob.getType(),
                currentIntent.type(),
                currentDecision.type(),
                currentBehavior.type()
        );
    }

    /**
     * Avalia a ameaça de cada entidade lembrada
     * e armazena o resultado do ciclo.
     *
     * Avaliações de entidades que saíram da memória
     * são descartadas junto com o ciclo anterior.
     */
    private static void updateThreats(
            Mob mob
    ) {

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            return;
        }

        Map<UUID, MobThreatAssessment> assessments =
                new HashMap<>();

        for (MemoryEntry entry : memory.getMemories()) {

            MobThreatAssessment assessment =
                    MobThreatEvaluator.evaluate(
                            mob,
                            entry.entityId()
                    );

            assessments.put(
                    entry.entityId(),
                    assessment
            );
        }

        MobThreatManager.setThreats(
                mob,
                assessments
        );
    }

    /**
     * Atualiza a percepção e a memória do mob.
     *
     * A percepção atualiza somente as informações
     * relacionadas à observação.
     *
     * O histórico de interações armazenado na memória
     * é preservado.
     */
    private static void updatePerception(
            Mob mob
    ) {

        MobMemory memory =
                MobMemoryManager.getMemory(mob);

        if (memory == null) {
            MobTalk.LOGGER.warn(
                    "MobTalk: memória nula para {}",
                    mob.getType()
            );

            return;
        }

        long currentGameTime =
                mob.level().getGameTime();

        /*
         * Remove memórias antigas antes de registrar
         * novas percepções.
         */
        memory.forgetExpired(
                currentGameTime
        );

        var perceivedEntities =
                MobPerception.perceive(
                        mob,
                        MobPerception.DEFAULT_PERCEPTION_RADIUS
                );

        MobTalk.LOGGER.debug(
                "MobTalk PERCEPÇÃO: {} percebeu {} entidades",
                mob.getType(),
                perceivedEntities.size()
        );

        /*
         * Cada entidade percebida atualiza sua memória
         * de observação.
         *
         * O método remember() preserva automaticamente
         * o histórico acumulado da entidade.
         */
        perceivedEntities.forEach(perceived -> {

            MemoryEntry entry =
                    MemoryEntry.initial(
                            perceived.entityId(),
                            perceived.entityType(),
                            currentGameTime,
                            perceived.distance(),
                            perceived.alive()
                    );

            memory.remember(
                    entry
            );

            var relationship =
                    MobRelationshipManager.getRelationship(
                            mob,
                            perceived.entityId()
                    );

            if (relationship == null) {
                return;
            }

            var updatedRelationship =
                    relationship.increaseFamiliarity(
                            0.01D
                    );

            MobRelationshipManager.setRelationship(
                    mob,
                    updatedRelationship
            );
        });
    }

    /**
     * Registra mudanças de intenção.
     */
    private static void logIntentChange(
            Mob mob,
            MobIntent previousIntent,
            MobIntent currentIntent
    ) {

        if (previousIntent.type()
                == currentIntent.type()
                && sameTarget(
                        previousIntent.targetId(),
                        currentIntent.targetId()
                )) {
            return;
        }

        MobTalk.LOGGER.info(
                "MobTalk INTENÇÃO: {} -> {} | alvo={} | prioridade={}",
                mob.getType(),
                currentIntent.type(),
                currentIntent.targetId(),
                String.format(
                        "%.2f",
                        currentIntent.priority()
                )
        );
    }

    /**
     * Registra mudanças de decisão.
     */
    private static void logDecisionChange(
            Mob mob,
            MobDecision previousDecision,
            MobDecision currentDecision
    ) {

        if (previousDecision.type()
                == currentDecision.type()
                && sameTarget(
                        previousDecision.targetId(),
                        currentDecision.targetId()
                )) {
            return;
        }

        MobTalk.LOGGER.info(
                "MobTalk DECISÃO: {} -> {} | alvo={} | confiança={}",
                mob.getType(),
                currentDecision.type(),
                currentDecision.targetId(),
                String.format(
                        "%.2f",
                        currentDecision.confidence()
                )
        );
    }

    /**
     * Registra mudanças de comportamento.
     */
    private static void logBehaviorChange(
            Mob mob,
            MobBehavior previousBehavior,
            MobBehavior currentBehavior
    ) {

        if (previousBehavior.type()
                == currentBehavior.type()
                && sameTarget(
                        previousBehavior.targetId(),
                        currentBehavior.targetId()
                )) {
            return;
        }

        MobTalk.LOGGER.info(
                "MobTalk COMPORTAMENTO: {} -> {} | alvo={} | prioridade={}",
                mob.getType(),
                currentBehavior.type(),
                currentBehavior.targetId(),
                String.format(
                        "%.2f",
                        currentBehavior.priority()
                )
        );
    }

    /**
     * Compara UUIDs de maneira segura.
     */
    private static boolean sameTarget(
            UUID first,
            UUID second
    ) {

        if (first == null) {
            return second == null;
        }

        return first.equals(second);
    }
}