package com.parrotwars.network;

import com.parrotwars.ParrotWars;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(ParrotWars.MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
        int id = 0;
        CHANNEL.registerMessage(id++, OrderPacket.class, OrderPacket::encode, OrderPacket::decode, OrderPacket::handle);
        CHANNEL.registerMessage(id++, CountryActionPacket.class, CountryActionPacket::encode,
                CountryActionPacket::decode, CountryActionPacket::handle);
        CHANNEL.registerMessage(id++, CountrySyncPacket.class, CountrySyncPacket::encode,
                CountrySyncPacket::decode, CountrySyncPacket::handle);
    }
}
