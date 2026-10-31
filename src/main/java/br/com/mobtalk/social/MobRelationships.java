package br.com.mobtalk.social;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Conjunto de relações sociais de um mob.
 *
 * É guardado como attachment da entidade, para que os
 * relacionamentos sobrevivam ao descarregamento do chunk,
 * à troca de dimensão e ao reinício do mundo.
 *
 * O acesso é feito pelo MobRelationshipManager.
 */
public final class MobRelationships {

    /**
     * Versão do formato gravado no mundo.
     *
     * Deve ser incrementada quando o formato mudar, para
     * que relações gravadas em versões antigas possam ser
     * convertidas na leitura.
     */
    public static final int SCHEMA_VERSION = 1;

    /**
     * Formato de gravação das relações no mundo.
     */
    public static final Codec<MobRelationships> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT
                            .optionalFieldOf("version", 1)
                            .forGetter(relationships -> SCHEMA_VERSION),
                    MobRelationship.CODEC
                            .listOf()
                            .fieldOf("entries")
                            .forGetter(relationships -> List.copyOf(
                                    relationships.entries.values()
                            ))
            ).apply(instance, MobRelationships::fromSaved));

    private final Map<UUID, MobRelationship> entries =
            new HashMap<>();

    /**
     * Reconstrói as relações a partir dos dados gravados.
     *
     * A versão 1 é o formato atual e não precisa de
     * conversão.
     */
    private static MobRelationships fromSaved(
            int version,
            List<MobRelationship> saved
    ) {

        MobRelationships relationships =
                new MobRelationships();

        for (MobRelationship relationship : saved) {
            relationships.entries.put(
                    relationship.entityId(),
                    relationship
            );
        }

        return relationships;
    }

    /**
     * Indica se não há nenhuma relação.
     *
     * Conjuntos vazios não são gravados no mundo.
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Mapa interno das relações, por UUID da entidade.
     */
    Map<UUID, MobRelationship> entries() {
        return entries;
    }
}
