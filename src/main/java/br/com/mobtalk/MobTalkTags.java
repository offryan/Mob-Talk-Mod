package br.com.mobtalk;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Tags de dados usadas pelo MobTalk.
 *
 * As tags ficam em arquivos JSON do mod e podem ser
 * estendidas por modpacks e datapacks sem alterar código.
 */
public final class MobTalkTags {

    /**
     * Tipos de entidade que o MobTalk não controla.
     *
     * Arquivo: data/mobtalk/tags/entity_type/ignored.json
     *
     * Usado para chefes e mobs com IA especial, em que a
     * influência do MobTalk atrapalharia o comportamento
     * original.
     */
    public static final TagKey<EntityType<?>> IGNORED =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(
                            MobTalk.MODID,
                            "ignored"
                    )
            );

    private MobTalkTags() {
        // Classe utilitária.
    }

    /**
     * Indica se o mob está fora do controle do MobTalk.
     */
    public static boolean isIgnored(
            Mob mob
    ) {

        return mob.getType().is(IGNORED);
    }
}
