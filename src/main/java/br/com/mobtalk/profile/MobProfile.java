package br.com.mobtalk.profile;

import br.com.mobtalk.intent.MobIntentType;
import br.com.mobtalk.threat.MobThreatLevel;

/**
 * Representa o perfil cognitivo de um mob.
 *
 * O perfil define como o mob tende a reagir
 * diante de diferentes níveis de ameaça.
 *
 * Esta classe não executa comportamentos.
 * Ela apenas fornece uma interpretação de alto nível
 * para o sistema de intenção.
 */
public record MobProfile(
        MobProfileType type
) {

    /**
     * Cria um perfil neutro.
     */
    public static MobProfile neutral() {
        return new MobProfile(
                MobProfileType.NEUTRAL
        );
    }

    /**
     * Cria um perfil passivo.
     */
    public static MobProfile passive() {
        return new MobProfile(
                MobProfileType.PASSIVE
        );
    }

    /**
     * Cria um perfil agressivo.
     */
    public static MobProfile aggressive() {
        return new MobProfile(
                MobProfileType.AGGRESSIVE
        );
    }

    /**
     * Cria um perfil defensivo.
     */
    public static MobProfile defensive() {
        return new MobProfile(
                MobProfileType.DEFENSIVE
        );
    }

    /**
     * Determina a intenção apropriada diante
     * de determinado nível de ameaça.
     *
     * Este método não considera o alvo nem executa
     * nenhuma ação física.
     *
     * Ele apenas traduz:
     *
     * ameaça + perfil → intenção
     */
    public MobIntentType determineThreatResponse(
            MobThreatLevel threatLevel
    ) {

        if (threatLevel == null) {
            return MobIntentType.NONE;
        }

        return switch (type) {

            case NEUTRAL ->
                    determineNeutralResponse(
                            threatLevel
                    );

            case PASSIVE ->
                    determinePassiveResponse(
                            threatLevel
                    );

            case AGGRESSIVE ->
                    determineAggressiveResponse(
                            threatLevel
                    );

            case DEFENSIVE ->
                    determineDefensiveResponse(
                            threatLevel
                    );
        };
    }

    /**
     * Resposta de um perfil neutro.
     */
    private static MobIntentType determineNeutralResponse(
            MobThreatLevel threatLevel
    ) {

        return switch (threatLevel) {

            case NONE ->
                    MobIntentType.NONE;

            case LOW ->
                    MobIntentType.OBSERVE;

            case MODERATE ->
                    MobIntentType.OBSERVE;

            case HIGH ->
                    MobIntentType.OBSERVE;

            case CRITICAL ->
                    MobIntentType.AVOID;
        };
    }

    /**
     * Resposta de um perfil passivo.
     *
     * Criaturas passivas tendem a evitar
     * ameaças significativas.
     */
    private static MobIntentType determinePassiveResponse(
            MobThreatLevel threatLevel
    ) {

        return switch (threatLevel) {

            case NONE ->
                    MobIntentType.NONE;

            case LOW ->
                    MobIntentType.OBSERVE;

            case MODERATE ->
                    MobIntentType.AVOID;

            case HIGH ->
                    MobIntentType.FLEE;

            case CRITICAL ->
                    MobIntentType.FLEE;
        };
    }

    /**
     * Resposta de um perfil agressivo.
     *
     * Criaturas agressivas tendem a confrontar
     * ameaças elevadas.
     */
    private static MobIntentType determineAggressiveResponse(
            MobThreatLevel threatLevel
    ) {

        return switch (threatLevel) {

            case NONE ->
                    MobIntentType.NONE;

            case LOW ->
                    MobIntentType.OBSERVE;

            case MODERATE ->
                    MobIntentType.OBSERVE;

            case HIGH ->
                    MobIntentType.ATTACK;

            case CRITICAL ->
                    MobIntentType.ATTACK;
        };
    }

    /**
     * Resposta de um perfil defensivo.
     *
     * O perfil defensivo reage à ameaça,
     * mas evita confronto direto sempre que possível.
     */
    private static MobIntentType determineDefensiveResponse(
            MobThreatLevel threatLevel
    ) {

        return switch (threatLevel) {

            case NONE ->
                    MobIntentType.NONE;

            case LOW ->
                    MobIntentType.OBSERVE;

            case MODERATE ->
                    MobIntentType.AVOID;

            case HIGH ->
                    MobIntentType.AVOID;

            case CRITICAL ->
                    MobIntentType.FLEE;
        };
    }
}