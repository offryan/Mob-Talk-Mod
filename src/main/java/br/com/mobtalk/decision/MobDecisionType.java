package br.com.mobtalk.decision;

/**
 * Representa o resultado final de uma avaliação
 * de decisão do MobTalk.
 *
 * A decisão é diferente da intenção.
 *
 * INTENÇÃO:
 * "Quero atacar."
 *
 * DECISÃO:
 * "Vou atacar."
 *
 * ou:
 *
 * "Não vou atacar."
 *
 * A decisão representa o que o sistema considera
 * apropriado fazer diante do contexto atual.
 */
public enum MobDecisionType {

    /**
     * Nenhuma ação relevante deve ser executada.
     */
    NONE,

    /**
     * Continuar observando a situação.
     */
    OBSERVE,

    /**
     * Aproximar-se do alvo.
     */
    APPROACH,

    /**
     * Atacar o alvo.
     */
    ATTACK,

    /**
     * Evitar o alvo.
     */
    AVOID,

    /**
     * Fugir da situação.
     */
    FLEE,

    /**
     * Proteger uma entidade.
     */
    PROTECT
}