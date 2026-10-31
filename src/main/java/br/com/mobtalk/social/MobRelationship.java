package br.com.mobtalk.social;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;

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
     * Formato de gravação da relação no mundo.
     */
    public static final Codec<MobRelationship> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC
                            .fieldOf("entity")
                            .forGetter(MobRelationship::entityId),
                    Codec.DOUBLE
                            .fieldOf("familiarity")
                            .forGetter(MobRelationship::familiarity),
                    Codec.DOUBLE
                            .fieldOf("hostility")
                            .forGetter(MobRelationship::hostility),
                    Codec.DOUBLE
                            .fieldOf("trust")
                            .forGetter(MobRelationship::trust)
            ).apply(instance, MobRelationship::new));

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
     * Abaixo deste valor a hostilidade é considerada
     * esquecida e passa a ser zero.
     */
    public static final double HOSTILITY_FORGET_THRESHOLD = 0.01D;

    /**
     * Reduz a hostilidade multiplicando-a por um fator.
     *
     * Valores abaixo do limite de esquecimento viram zero,
     * para que a hostilidade realmente termine em vez de
     * se aproximar de zero para sempre.
     *
     * @param factor fator entre 0 e 1
     */
    public MobRelationship decayHostility(
            double factor
    ) {

        double decayed =
                clamp(hostility * factor);

        if (decayed < HOSTILITY_FORGET_THRESHOLD) {
            decayed = 0.0D;
        }

        return new MobRelationship(
                entityId,
                familiarity,
                decayed,
                trust
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