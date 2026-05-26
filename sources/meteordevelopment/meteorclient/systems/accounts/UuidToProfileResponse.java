package meteordevelopment.meteorclient.systems.accounts;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/UuidToProfileResponse.class */
public class UuidToProfileResponse {
    public Property[] properties;

    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/systems/accounts/UuidToProfileResponse$Property.class */
    public static class Property {
        public String name;
        public String value;
    }

    public String getPropertyValue(String name) {
        for (Property property : this.properties) {
            if (property.name.equals(name)) {
                return property.value;
            }
        }
        return null;
    }
}
