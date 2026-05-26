package meteordevelopment.meteorclient.addons;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.ObjectMethods;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.utils.network.Http;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/addons/GithubRepo.class */
public final class GithubRepo extends Record {
    private final String owner;
    private final String name;
    private final String branch;

    @Nullable
    private final String accessToken;

    public GithubRepo(String owner, String name, String branch, @Nullable String accessToken) {
        this.owner = owner;
        this.name = name;
        this.branch = branch;
        this.accessToken = accessToken;
    }

    @Override // java.lang.Record
    public final String toString() {
        return (String) ObjectMethods.bootstrap(MethodHandles.lookup(), "toString", MethodType.methodType(String.class, GithubRepo.class), GithubRepo.class, "owner;name;branch;accessToken", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->owner:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->name:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->branch:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->accessToken:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final int hashCode() {
        return (int) ObjectMethods.bootstrap(MethodHandles.lookup(), "hashCode", MethodType.methodType(Integer.TYPE, GithubRepo.class), GithubRepo.class, "owner;name;branch;accessToken", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->owner:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->name:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->branch:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->accessToken:Ljava/lang/String;").dynamicInvoker().invoke(this) /* invoke-custom */;
    }

    @Override // java.lang.Record
    public final boolean equals(Object o) {
        return (boolean) ObjectMethods.bootstrap(MethodHandles.lookup(), "equals", MethodType.methodType(Boolean.TYPE, GithubRepo.class, Object.class), GithubRepo.class, "owner;name;branch;accessToken", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->owner:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->name:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->branch:Ljava/lang/String;", "FIELD:Lmeteordevelopment/meteorclient/addons/GithubRepo;->accessToken:Ljava/lang/String;").dynamicInvoker().invoke(this, o) /* invoke-custom */;
    }

    public String owner() {
        return this.owner;
    }

    public String name() {
        return this.name;
    }

    public String branch() {
        return this.branch;
    }

    @Nullable
    public String accessToken() {
        return this.accessToken;
    }

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
