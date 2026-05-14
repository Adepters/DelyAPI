package me.dely.delyApi.loot;

import java.io.File;

public interface LootContainer {
    File getLootFile();
    int getMinLoot();
    int getMaxLoot();
}
