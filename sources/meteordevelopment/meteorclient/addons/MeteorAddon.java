package meteordevelopment.meteorclient.addons;

import meteordevelopment.meteorclient.utils.render.color.Color;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/addons/MeteorAddon.class */
public abstract class MeteorAddon {
    public String name;
    public String[] authors;
    public final Color color = new Color(255, 255, 255);

    public abstract void onInitialize();

    public abstract String getPackage();

    public void onRegisterCategories() {
    }

    public String getWebsite() {
        return null;
    }

    public GithubRepo getRepo() {
        return null;
    }

    public String getCommit() {
        return null;
    }
}
