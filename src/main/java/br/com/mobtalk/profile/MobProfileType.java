package br.com.mobtalk.profile;

/**
 * Define o perfil cognitivo geral de um mob.
 *
 * O perfil representa a tendência comportamental
 * do mob diante de diferentes situações.
 *
 * Ele não executa ações diretamente.
 *
 * Sua função é fornecer uma orientação para que
 * o sistema de intenção possa interpretar ameaças
 * de maneira diferente dependendo da natureza do mob.
 */
public enum MobProfileType {

    /**
     * Perfil neutro.
     *
     * Utilizado quando ainda não existe uma
     * característica comportamental específica.
     */
    NEUTRAL,

    /**
     * Perfil de criatura predominantemente pacífica.
     *
     * Tende a evitar ameaças elevadas.
     */
    PASSIVE,

    /**
     * Perfil de criatura que reage agressivamente
     * quando ameaçada.
     */
    AGGRESSIVE,

    /**
     * Perfil de criatura que prioriza distância
     * e sobrevivência.
     */
    DEFENSIVE
}