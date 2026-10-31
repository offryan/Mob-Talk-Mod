package br.com.mobtalk;

import java.util.function.Supplier;

import br.com.mobtalk.memory.MobMemory;
import br.com.mobtalk.social.MobRelationships;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Dados do MobTalk gravados junto com cada mob.
 *
 * Os attachments do NeoForge ficam no NBT da entidade:
 * acompanham o mob no descarregamento do chunk, na troca
 * de dimensão e no reinício do mundo.
 *
 * Só o conhecimento duradouro é gravado. Contexto,
 * ameaça, intenção, decisão e comportamento são
 * temporários e se recalculam no primeiro ciclo.
 */
public final class MobTalkAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(
                    NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    MobTalk.MODID
            );

    /**
     * Memória individual do mob.
     *
     * Memórias vazias não são gravadas.
     */
    public static final Supplier<AttachmentType<MobMemory>> MEMORY =
            ATTACHMENT_TYPES.register(
                    "memory",
                    () -> AttachmentType
                            .builder(() -> new MobMemory())
                            .serialize(
                                    MobMemory.CODEC,
                                    memory -> !memory.isEmpty()
                            )
                            .build()
            );

    /**
     * Relações sociais do mob.
     *
     * Conjuntos vazios não são gravados.
     */
    public static final Supplier<AttachmentType<MobRelationships>> RELATIONSHIPS =
            ATTACHMENT_TYPES.register(
                    "relationships",
                    () -> AttachmentType
                            .builder(() -> new MobRelationships())
                            .serialize(
                                    MobRelationships.CODEC,
                                    relationships -> !relationships.isEmpty()
                            )
                            .build()
            );

    private MobTalkAttachments() {
        // Classe utilitária.
    }

    /**
     * Registra os attachments no barramento do mod.
     */
    public static void register(
            IEventBus modEventBus
    ) {

        ATTACHMENT_TYPES.register(modEventBus);
    }
}
