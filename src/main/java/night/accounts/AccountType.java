package night.accounts;

public enum AccountType {
   OFFLINE("Offline"),
   MICROSOFT("Microsoft"),
   SESSION("Session");

   private final String displayName;

   AccountType(String var3) {
      this.displayName = var3;
   }

   public String getDisplayName() {
      return this.displayName;
   }
}
