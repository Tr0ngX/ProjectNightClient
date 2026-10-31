package meteordevelopment.discordipc;

public enum Opcode {
   Handshake,
   Frame,
   Close,
   Ping,
   Pong;

   private static final Opcode[] VALUES = values();

   public static Opcode valueOf(int var0) {
      return VALUES[var0];
   }
}
