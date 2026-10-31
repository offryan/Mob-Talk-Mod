package br.com.mobtalk.memory;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

/**
 * Representa uma informação armazenada na memória de um mob.
 *
 * Uma entrada de memória representa o conhecimento atual
 * que um mob possui sobre uma determinada entidade.
 *
 * A memória não representa acontecimentos individuais.
 * Ela representa o estado acumulado desse conhecimento.
 *
 * Exemplos de informações armazenadas:
 *
 * - identidade da entidade;
 * - tipo da entidade;
 * - última vez em que foi percebida;
 * - última distância conhecida;
 * - estado de vida conhecido;
 * - quantidade de ataques sofridos;
 * - dano acumulado recebido;
 * - maior dano recebido;
 * - momento do último ataque.
 *
 * O contexto continua responsável pelos acontecimentos recentes.
 * A memória representa o conhecimento acumulado.
 */
public record MemoryEntry(
        UUID entityId,
        String entityType,
        long lastSeenGameTime,
        double lastKnownDistance,
        boolean alive,
        int attackCount,
        double totalDamageReceived,
        double highestDamageReceived,
        long lastAttackGameTime
) {

    /**
     * Formato de gravação da entrada no mundo.
     *
     * Os tempos usam o relógio do mundo, que também é
     * salvo, então idades e decaimentos continuam
     * corretos depois de recarregar.
     */
    public static final Codec<MemoryEntry> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC
                            .fieldOf("entity")
                            .forGetter(MemoryEntry::entityId),
                    Codec.STRING
                            .fieldOf("type")
                            .forGetter(MemoryEntry::entityType),
                    Codec.LONG
                            .fieldOf("last_seen")
                            .forGetter(MemoryEntry::lastSeenGameTime),
                    Codec.DOUBLE
                            .fieldOf("distance")
                            .forGetter(MemoryEntry::lastKnownDistance),
                    Codec.BOOL
                            .fieldOf("alive")
                            .forGetter(MemoryEntry::alive),
                    Codec.INT
                            .fieldOf("attacks")
                            .forGetter(MemoryEntry::attackCount),
                    Codec.DOUBLE
                            .fieldOf("total_damage")
                            .forGetter(MemoryEntry::totalDamageReceived),
                    Codec.DOUBLE
                            .fieldOf("highest_damage")
                            .forGetter(MemoryEntry::highestDamageReceived),
                    Codec.LONG
                            .fieldOf("last_attack")
                            .forGetter(MemoryEntry::lastAttackGameTime)
            ).apply(instance, MemoryEntry::new));

    /**
     * Cria uma memória inicial para uma entidade
     * que acabou de ser percebida.
     */
    public static MemoryEntry initial(
            UUID entityId,
            String entityType,
            long gameTime,
            double distance,
            boolean alive
    ) {

        return new MemoryEntry(
                entityId,
                entityType,
                gameTime,
                distance,
                alive,
                0,
                0.0D,
                0.0D,
                -1L
        );
    }

    /**
     * Atualiza as informações de percepção da entidade.
     *
     * As informações históricas de combate são preservadas.
     */
    public MemoryEntry updateObservation(
            long gameTime,
            double distance,
            boolean alive
    ) {

        return new MemoryEntry(
                entityId,
                entityType,
                gameTime,
                distance,
                alive,
                attackCount,
                totalDamageReceived,
                highestDamageReceived,
                lastAttackGameTime
        );
    }

    /**
     * Registra um novo ataque recebido da entidade.
     *
     * O acontecimento é incorporado ao conhecimento
     * acumulado da memória.
     *
     * O ataque é contado mesmo quando o dano efetivo
     * é zero (absorvido por armadura, resistência ou
     * escudo): a intenção de ferir também é lembrada.
     *
     * @param damage dano efetivamente recebido
     * @param gameTime momento do ataque
     */
    public MemoryEntry registerDamage(
            double damage,
            long gameTime
    ) {

        double effectiveDamage =
                Math.max(
                        0.0D,
                        damage
                );

        return new MemoryEntry(
                entityId,
                entityType,
                lastSeenGameTime,
                lastKnownDistance,
                alive,
                attackCount + 1,
                totalDamageReceived + effectiveDamage,
                Math.max(
                        highestDamageReceived,
                        effectiveDamage
                ),
                gameTime
        );
    }

    /**
     * Retorna a idade do último ataque conhecido.
     *
     * Caso nenhum ataque tenha sido registrado,
     * retorna -1.
     */
    public long getLastAttackAge(
            long currentGameTime
    ) {

        if (lastAttackGameTime < 0L) {
            return -1L;
        }

        return Math.max(
                0L,
                currentGameTime - lastAttackGameTime
        );
    }

    /**
     * Indica se existe histórico de ataque
     * dessa entidade contra o mob.
     */
    public boolean hasAttackHistory() {
        return attackCount > 0;
    }

    /**
     * Indica se a entidade realizou ataques
     * recentemente.
     */
    public boolean wasRecentlyAttacked(
            long currentGameTime,
            long maxAge
    ) {

        long age =
                getLastAttackAge(
                        currentGameTime
                );

        return age >= 0L && age <= maxAge;
    }

    /**
     * Indica quanto o histórico de ataques ainda é
     * lembrado com força, entre 0 e 1.
     *
     * O valor cai pela metade a cada meia-vida desde
     * o último ataque. Sem histórico, retorna 0.
     *
     * @param currentGameTime tempo atual do mundo
     * @param halfLifeTicks ticks para a lembrança cair pela metade
     */
    public double getAttackRecall(
            long currentGameTime,
            long halfLifeTicks
    ) {

        long age =
                getLastAttackAge(
                        currentGameTime
                );

        if (age < 0L || halfLifeTicks <= 0L) {
            return 0.0D;
        }

        return Math.pow(
                0.5D,
                (double) age / halfLifeTicks
        );
    }
}