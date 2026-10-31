package br.com.mobtalk.memory;

import java.util.UUID;

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
     * Registra um novo dano recebido da entidade.
     *
     * O acontecimento é incorporado ao conhecimento
     * acumulado da memória.
     */
    public MemoryEntry registerDamage(
            double damage,
            long gameTime
    ) {

        if (damage <= 0.0D) {
            return this;
        }

        return new MemoryEntry(
                entityId,
                entityType,
                lastSeenGameTime,
                lastKnownDistance,
                alive,
                attackCount + 1,
                totalDamageReceived + damage,
                Math.max(
                        highestDamageReceived,
                        damage
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
}