package br.com.mobtalk.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa a memória de um mob.
 *
 * A memória mantém informações sobre entidades que o mob
 * percebeu ou com as quais interagiu anteriormente.
 *
 * A memória representa conhecimento acumulado.
 *
 * Diferentemente do contexto, que registra acontecimentos
 * recentes, a memória mantém o estado conhecido sobre
 * determinada entidade.
 *
 * Esta classe não decide o que fazer com as informações.
 *
 * Sua responsabilidade é somente:
 *
 * PERCEPÇÃO / INTERAÇÃO → MEMÓRIA
 */
public final class MobMemory {

    /**
     * Tempo máximo padrão que uma memória permanece sem
     * ser atualizada pela percepção.
     *
     * 600 ticks = aproximadamente 30 segundos.
     */
    public static final long DEFAULT_MEMORY_DURATION_TICKS = 600L;

    /**
     * Armazena as memórias utilizando o UUID da entidade
     * percebida como identificador.
     */
    private final Map<UUID, MemoryEntry> memories =
            new HashMap<>();

    /**
     * Adiciona ou atualiza uma memória.
     *
     * Se a entidade ainda não estiver na memória,
     * uma nova entrada será criada.
     *
     * Se a entidade já existir, as informações de
     * observação serão atualizadas enquanto o histórico
     * acumulado da entidade será preservado.
     */
    public void remember(
            MemoryEntry entry
    ) {

        if (entry == null || entry.entityId() == null) {
            return;
        }

        MemoryEntry previous =
                memories.get(
                        entry.entityId()
                );

        if (previous == null) {
            memories.put(
                    entry.entityId(),
                    entry
            );

            return;
        }

        MemoryEntry updated =
                new MemoryEntry(
                        entry.entityId(),
                        entry.entityType(),
                        entry.lastSeenGameTime(),
                        entry.lastKnownDistance(),
                        entry.alive(),
                        previous.attackCount(),
                        previous.totalDamageReceived(),
                        previous.highestDamageReceived(),
                        previous.lastAttackGameTime()
                );

        memories.put(
                entry.entityId(),
                updated
        );
    }

    /**
     * Registra dano recebido de uma entidade.
     *
     * O histórico de combate é acumulado na memória
     * existente da entidade.
     *
     * Caso ainda não exista memória para a entidade,
     * uma nova memória será criada.
     *
     * @param entityId UUID da entidade que causou o dano
     * @param entityType tipo da entidade que causou o dano
     * @param damage quantidade de dano recebido
     * @param gameTime momento do ataque
     * @param distance distância atual entre as entidades
     * @param alive estado atual conhecido da entidade
     */
    public void rememberDamage(
            UUID entityId,
            String entityType,
            double damage,
            long gameTime,
            double distance,
            boolean alive
    ) {

        if (entityId == null) {
            return;
        }

        if (damage <= 0.0D) {
            return;
        }

        MemoryEntry memory =
                memories.get(
                        entityId
                );

        if (memory == null) {
            memory =
                    MemoryEntry.initial(
                            entityId,
                            entityType,
                            gameTime,
                            distance,
                            alive
                    );
        }

        MemoryEntry updated =
                memory.registerDamage(
                        damage,
                        gameTime
                );

        memories.put(
                entityId,
                updated
        );
    }

    /**
     * Recupera uma memória específica.
     *
     * @param entityId UUID da entidade procurada
     * @return memória encontrada ou null
     */
    public MemoryEntry getMemory(
            UUID entityId
    ) {

        if (entityId == null) {
            return null;
        }

        return memories.get(
                entityId
        );
    }

    /**
     * Verifica se o mob possui memória sobre determinada entidade.
     */
    public boolean remembers(
            UUID entityId
    ) {

        if (entityId == null) {
            return false;
        }

        return memories.containsKey(
                entityId
        );
    }

    /**
     * Retorna uma cópia das memórias atualmente armazenadas.
     *
     * A cópia impede que código externo altere diretamente
     * o mapa interno da memória.
     */
    public Collection<MemoryEntry> getMemories() {
        return new ArrayList<>(
                memories.values()
        );
    }

    /**
     * Retorna a quantidade de memórias armazenadas.
     */
    public int size() {
        return memories.size();
    }

    /**
     * Remove uma memória específica.
     */
    public void forget(
            UUID entityId
    ) {

        if (entityId == null) {
            return;
        }

        memories.remove(
                entityId
        );
    }

    /**
     * Remove todas as memórias.
     */
    public void clear() {
        memories.clear();
    }

    /**
     * Remove memórias que ficaram antigas demais.
     *
     * A expiração continua baseada na última percepção
     * da entidade.
     *
     * Isso significa que o histórico de combate poderá
     * ser mantido enquanto a memória geral da entidade
     * ainda estiver válida.
     *
     * @param currentGameTime tempo atual do mundo
     */
    public void forgetExpired(
            long currentGameTime
    ) {

        memories.entrySet().removeIf(entry ->
                isExpired(
                        entry.getValue(),
                        currentGameTime
                )
        );
    }

    /**
     * Determina se uma memória ultrapassou seu tempo
     * máximo de retenção.
     */
    private boolean isExpired(
            MemoryEntry memory,
            long currentGameTime
    ) {

        long age =
                currentGameTime
                        - memory.lastSeenGameTime();

        return age > DEFAULT_MEMORY_DURATION_TICKS;
    }
}