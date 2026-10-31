package br.com.mobtalk.social;

import java.util.UUID;

/**
 * Representa a relação de um mob com uma entidade específica.
 *
 * Esta classe não executa comportamentos.
 *
 * Ela apenas representa como o mob atualmente avalia
 * determinada entidade.
 *
 * Os valores variam entre 0.0 e 1.0.
 */
public record MobRelationship(
        UUID entityId,
        double familiarity,
        double hostility,
        double trust
) {

    /**
     * Cria uma relação inicial.
     *
     * Uma entidade desconhecida começa com:
     *
     * familiaridade = 0
     * hostilidade   = 0
     * confiança     = 0
     */
    public static MobRelationship initial(
            UUID entityId
    ) {

        return new MobRelationship(
                entityId,
                0.0D,
                0.0D,
                0.0D
        );
    }

    /**
     * Aumenta a familiaridade com a entidade.
     *
     * O valor máximo é 1.0.
     */
    public MobRelationship increaseFamiliarity(
            double amount
    ) {

        return new MobRelationship(
                entityId,
                clamp(familiarity + amount),
                hostility,
                trust
        );
    }

    /**
     * Aumenta a hostilidade em relação à entidade.
     *
     * O valor máximo é 1.0.
     */
    public MobRelationship increaseHostility(
            double amount
    ) {

        return new MobRelationship(
                entityId,
                familiarity,
                clamp(hostility + amount),
                trust
        );
    }

    /**
     * Aumenta a confiança na entidade.
     *
     * O valor máximo é 1.0.
     */
    public MobRelationship increaseTrust(
            double amount
    ) {

        return new MobRelationship(
                entityId,
                familiarity,
                hostility,
                clamp(trust + amount)
        );
    }

    /**
     * Garante que um valor permaneça entre 0 e 1.
     */
    private static double clamp(
            double value
    ) {

        return Math.max(
                0.0D,
                Math.min(1.0D, value)
        );
    }
}