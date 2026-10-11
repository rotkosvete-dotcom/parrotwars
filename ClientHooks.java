package com.parrotwars.client;

import com.parrotwars.network.CountrySyncPacket;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

/** Клиентская часть сетевых пакетов. */
public class ClientHooks {
    public static List<CountrySyncPacket.View> views = new ArrayList<>();

    public static void onSync(CountrySyncPacket msg) {
        views = msg.views;
        if (msg.open) {
            Minecraft.getInstance().setScreen(new StrategyMapScreen());
        }
    }
}
