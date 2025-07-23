package com.animaleconomy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.EntityType;

import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.extension.annotation.DataBuilderProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;

@PluginInfo(name = "AnimalEconomy", iconName = "dragon", iconFamily = Family.SOLID, color = Color.BROWN)
public class PlanAPI implements DataExtension {
	
	public void initialize() {
        try {
            ExtensionService.getInstance().register(this);
        } catch (IllegalStateException planIsNotEnabled) {
        } catch (IllegalArgumentException dataExtensionImplementationIsInvalid) {
        }
	}
	
	@Override
	public CallEvents[] callExtensionMethodsOn() {
	    return new CallEvents[]{CallEvents.PLAYER_JOIN, CallEvents.PLAYER_LEAVE, CallEvents.SERVER_PERIODICAL};
	}
	
    @DataBuilderProvider
    public ExtensionDataBuilder playerData(UUID playerUUID) {

        Table.Factory plots = Table.builder()
                .columnOne("Mob Type", Icon.called("dragon").of(Family.REGULAR).build())
                .columnTwo("Heads", Icon.called("count").build());

        Map<EntityType, Integer> mobs = Brain.getInstance().getAnimalEconomy().getAllKills(playerUUID);
        
        int total = 0;
        
        for (Entry<EntityType, Integer> mob : mobs.entrySet()) {
            plots.addRow(mob.getKey().name(), mob.getValue());
            total += mob.getValue();
        }

        return newExtensionDataBuilder()
                .addValue(String.class, valueBuilder("Mob heads")
                        .description("How many mob heads the player has.")
                        .icon(Icon.called("dragon").of(Family.REGULAR).of(Color.BROWN).build())
                        .showInPlayerTable()
                        .buildString(String.valueOf(total)))
                .addTable("animaleconomy", plots.build(), Color.GREEN);
    }
	

}
