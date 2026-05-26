package meteordevelopment.meteorclient.addons;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/addons/AddonManager.class */
public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList();

    public static void init() {
        MeteorClient.ADDON = new MeteorAddon() { // from class: meteordevelopment.meteorclient.addons.AddonManager.1
            @Override // meteordevelopment.meteorclient.addons.MeteorAddon
            public void onInitialize() {
            }

            @Override // meteordevelopment.meteorclient.addons.MeteorAddon
            public String getPackage() {
                return "meteordevelopment.meteorclient";
            }

            @Override // meteordevelopment.meteorclient.addons.MeteorAddon
            public String getWebsite() {
                return "https://meteorclient.com";
            }

            @Override // meteordevelopment.meteorclient.addons.MeteorAddon
            public GithubRepo getRepo() {
                return new GithubRepo("MeteorDevelopment", MeteorClient.MOD_ID);
            }

            @Override // meteordevelopment.meteorclient.addons.MeteorAddon
            public String getCommit() {
                String commit = MeteorClient.MOD_META.getCustomValue("meteor-client:commit").getAsString();
                if (commit.isEmpty()) {
                    return null;
                }
                return commit;
            }
        };
        ModMetadata metadata = ((ModContainer) FabricLoader.getInstance().getModContainer(MeteorClient.MOD_ID).get()).getMetadata();
        MeteorClient.ADDON.name = metadata.getName();
        MeteorClient.ADDON.authors = new String[metadata.getAuthors().size()];
        if (metadata.containsCustomValue("meteor-client:color")) {
            MeteorClient.ADDON.color.parse(metadata.getCustomValue("meteor-client:color").getAsString());
        }
        int i = 0;
        for (Person author : metadata.getAuthors()) {
            int i2 = i;
            i++;
            MeteorClient.ADDON.authors[i2] = author.getName();
        }
        ADDONS.add(MeteorClient.ADDON);
        for (EntrypointContainer<MeteorAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("meteor", MeteorAddon.class)) {
            ModMetadata metadata2 = entrypoint.getProvider().getMetadata();
            try {
                MeteorAddon addon = (MeteorAddon) entrypoint.getEntrypoint();
                addon.name = metadata2.getName();
                if (metadata2.getAuthors().isEmpty()) {
                    throw new RuntimeException("Addon \"%s\" requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
                }
                addon.authors = new String[metadata2.getAuthors().size()];
                if (metadata2.containsCustomValue("meteor-client:color")) {
                    addon.color.parse(metadata2.getCustomValue("meteor-client:color").getAsString());
                }
                int i3 = 0;
                for (Person author2 : metadata2.getAuthors()) {
                    int i4 = i3;
                    i3++;
                    addon.authors[i4] = author2.getName();
                }
                ADDONS.add(addon);
            } catch (Throwable throwable) {
                throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata2.getName()), throwable);
            }
        }
    }
}
