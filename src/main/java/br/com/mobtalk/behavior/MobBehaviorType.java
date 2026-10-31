package br.com.mobtalk.behavior;

/**
 * Representa os comportamentos que podem ser executados
 * pelo sistema MobTalk.
 *
 * Esta enumeração descreve um comportamento desejado.
 *
 * A execução física do comportamento será conectada
 * posteriormente à IA vanilla do Minecraft.
 */
public enum MobBehaviorType {

    /**
     * Nenhum comportamento especial.
     */
    NONE,

    /**
     * Observar o ambiente ou um alvo.
     */
    OBSERVE,

    /**
     * Aproximar-se de um alvo.
     */
    APPROACH,

    /**
     * Atacar um alvo.
     */
    ATTACK,

    /**
     * Evitar um alvo.
     */
    AVOID,

    /**
     * Fugir de uma ameaça.
     */
    FLEE,

    /**
     * Proteger uma entidade.
     */
    PROTECT
}