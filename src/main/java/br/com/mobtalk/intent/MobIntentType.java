package br.com.mobtalk.intent;

/**
 * Representa os possíveis objetivos que um mob
 * pode desenvolver diante de uma situação.
 *
 * Esta enumeração não executa nenhuma ação.
 *
 * Ela apenas descreve uma intenção.
 *
 * Futuramente novas intenções poderão ser adicionadas.
 */
public enum MobIntentType {

    /**
     * Não existe uma intenção relevante no momento.
     */
    NONE,

    /**
     * Investigar uma entidade ou situação.
     */
    INVESTIGATE,

    /**
     * Observar uma entidade à distância.
     */
    OBSERVE,

    /**
     * Aproximar-se de uma entidade.
     */
    APPROACH,

    /**
     * Evitar determinada entidade.
     */
    AVOID,

    /**
     * Fugir de uma ameaça.
     */
    FLEE,

    /**
     * Atacar uma entidade.
     */
    ATTACK,

    /**
     * Proteger outra entidade.
     */
    PROTECT
}