package meteordevelopment.meteorclient.addons;

import javax.annotation.Nullable;
import meteordevelopment.meteorclient.utils.network.Http;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/addons/GithubRepo.class */
public record GithubRepo(String owner, String name, String branch, @Nullable String accessToken) {

    public GithubRepo(String owner, String name, @Nullable String accessToken) {
        this(owner, name, "master", accessToken);
    }

    public GithubRepo(String owner, String name) {
        this(owner, name, "master", null);
    }

    public String getOwnerName() {
        return this.owner + "/" + this.name;
    }

    public void authenticate(Http.Request request) {
        if (this.accessToken != null && !this.accessToken.isBlank()) {
            request.bearer(this.accessToken);
            return;
        }
        String personalAuthToken = System.getenv("meteor.github.authorization");
        if (personalAuthToken != null && !personalAuthToken.isBlank()) {
            request.bearer(personalAuthToken);
        }
    }
}
