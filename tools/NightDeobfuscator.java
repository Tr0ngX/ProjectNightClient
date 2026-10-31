import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class NightDeobfuscator {

    // Hash helper methods from aa.class
    public static int aa_a(int x) {
        if (x == 0) return 0;
        return (((x * 31) >>> 4) % x) ^ (x >>> 16);
    }

    public static int aa_b(int x) {
        return ((x & (7 << 29)) >> 29) | (x << 3);
    }

    public static int aa_c(int x) {
        return ((x & (7 << 29)) >> 29) | (x << 3);
    }

    // Variant 1 & 2 string decryption (UTF_16)
    public static String decryptStringV12(byte[] encrypted, byte[] key2, int varS) {
        byte[] data = encrypted.clone();
        byte[] k1 = Integer.toString(varS).getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < data.length; i++) {
            data[i] ^= k1[i % k1.length];
            data[i] ^= key2[i % key2.length];
        }
        return new String(data, StandardCharsets.UTF_16);
    }

    // Variant 3 string decryption (UTF_16BE blob substring)
    public static String decryptStringV3(byte[] descBytes, String blobString, int varS) {
        byte[] k1 = Integer.toString(varS).getBytes(StandardCharsets.UTF_8);
        int len = ((descBytes[0] & 0xFF) << 24) | ((descBytes[1] & 0xFF) << 16) | ((descBytes[2] & 0xFF) << 8) | (descBytes[3] & 0xFF);
        int offset = ((descBytes[4] & 0xFF) << 24) | ((descBytes[5] & 0xFF) << 16) | ((descBytes[6] & 0xFF) << 8) | (descBytes[7] & 0xFF);
        String sub = blobString.substring(offset, offset + len);
        byte[] data = sub.getBytes(StandardCharsets.UTF_16BE);
        for (int i = 0; i < data.length; i++) {
            data[i] ^= k1[i % k1.length];
        }
        return new String(data, StandardCharsets.UTF_16BE);
    }

    private static boolean isIntPush(AbstractInsnNode insn) {
        if (insn == null) return false;
        int op = insn.getOpcode();
        if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5) return true;
        if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) return true;
        if (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Integer) return true;
        return false;
    }

    private static int getIntVal(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5) return op - Opcodes.ICONST_0;
        if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) return ((IntInsnNode) insn).operand;
        if (op == Opcodes.LDC) return (Integer) ((LdcInsnNode) insn).cst;
        throw new IllegalArgumentException("Not an int push: " + op);
    }

    private static AbstractInsnNode makeIntPush(int val) {
        if (val >= -1 && val <= 5) return new InsnNode(Opcodes.ICONST_0 + val);
        if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) return new IntInsnNode(Opcodes.BIPUSH, val);
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) return new IntInsnNode(Opcodes.SIPUSH, val);
        return new LdcInsnNode(val);
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode insn) {
        AbstractInsnNode cur = insn == null ? null : insn.getNext();
        while (cur != null && (cur instanceof LabelNode || cur instanceof LineNumberNode || cur instanceof FrameNode)) {
            cur = cur.getNext();
        }
        return cur;
    }

    private static AbstractInsnNode prevReal(AbstractInsnNode insn) {
        AbstractInsnNode cur = insn == null ? null : insn.getPrevious();
        while (cur != null && (cur instanceof LabelNode || cur instanceof LineNumberNode || cur instanceof FrameNode)) {
            cur = cur.getPrevious();
        }
        return cur;
    }

    private static AbstractInsnNode firstReal(InsnList insns) {
        AbstractInsnNode cur = insns.getFirst();
        if (cur != null && (cur instanceof LabelNode || cur instanceof LineNumberNode || cur instanceof FrameNode)) {
            return nextReal(cur);
        }
        return cur;
    }

    private static byte[] extractByteArrayMethod(MethodNode mn) {
        if ((mn.access & Opcodes.ACC_PRIVATE) == 0 || (mn.access & Opcodes.ACC_STATIC) == 0) return null;
        if (!mn.desc.equals("()[B")) return null;
        AbstractInsnNode cur = firstReal(mn.instructions);
        if (!isIntPush(cur)) return null;
        int len = getIntVal(cur);
        if (len < 0 || len > 65536) return null;
        AbstractInsnNode n1 = nextReal(cur);
        if (n1 == null || n1.getOpcode() != Opcodes.NEWARRAY || ((IntInsnNode) n1).operand != Opcodes.T_BYTE) return null;
        byte[] result = new byte[len];
        cur = nextReal(n1);
        for (int i = 0; i < len; i++) {
            if (cur == null || cur.getOpcode() != Opcodes.DUP) return null;
            AbstractInsnNode idxNode = nextReal(cur);
            if (!isIntPush(idxNode) || getIntVal(idxNode) != i) return null;
            AbstractInsnNode valNode = nextReal(idxNode);
            if (!isIntPush(valNode)) return null;
            int val = getIntVal(valNode);
            AbstractInsnNode next = nextReal(valNode);
            if (next != null && next.getOpcode() == Opcodes.I2B) {
                val = (byte) val;
                next = nextReal(next);
            }
            if (next == null || next.getOpcode() != Opcodes.BASTORE) return null;
            result[i] = (byte) val;
            cur = nextReal(next);
        }
        if (cur == null || cur.getOpcode() != Opcodes.ARETURN) return null;
        return result;
    }

    private static boolean isXorHelper(MethodNode mn) {
        if ((mn.access & Opcodes.ACC_STATIC) == 0) return false;
        if (!mn.desc.equals("(II)I")) return false;
        if (mn.instructions.size() > 25) return false;
        boolean hasIxor = false;
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof MethodInsnNode || insn instanceof FieldInsnNode || insn instanceof JumpInsnNode) return false;
            int op = insn.getOpcode();
            if (op == Opcodes.IADD || op == Opcodes.ISUB || op == Opcodes.IMUL || op == Opcodes.IDIV
                || op == Opcodes.IAND || op == Opcodes.IOR || op == Opcodes.ISHL || op == Opcodes.ISHR || op == Opcodes.IUSHR) {
                return false;
            }
            if (op == Opcodes.IXOR) hasIxor = true;
        }
        return hasIxor;
    }

    // Returns 1 for V1 ([BI with UTF_16), 2 for V2 ([B[BI with UTF_16), 3 for V3 ([BI with UTF_16BE), 0 otherwise
    private static int getStringDecryptorVariant(MethodNode mn) {
        if ((mn.access & Opcodes.ACC_STATIC) == 0) return 0;
        if (!mn.desc.equals("([BI)Ljava/lang/String;") && !mn.desc.equals("([B[BI)Ljava/lang/String;")) return 0;
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof FieldInsnNode fi) {
                if (fi.name.equals("UTF_16")) {
                    return mn.desc.equals("([BI)Ljava/lang/String;") ? 1 : 2;
                }
                if (fi.name.equals("UTF_16BE") && mn.desc.equals("([BI)Ljava/lang/String;")) {
                    return 3;
                }
            }
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        Path inDir = Paths.get(args[0]);
        Path outDir = Paths.get(args[1]);
        Files.createDirectories(outDir);

        List<Path> classFiles = Files.walk(inDir).filter(p -> p.toString().endsWith(".class")).toList();
        int totalClasses = 0;
        int obfClasses = 0;
        int totalMethodsDeobf = 0;
        int totalStringsDecrypted = 0;

        for (Path p : classFiles) {
            Path rel = inDir.relativize(p);
            if (rel.toString().equals("aa.class") || rel.toString().equals("nq.class")) {
                continue;
            }
            totalClasses++;
            ClassReader cr = new ClassReader(Files.readAllBytes(p));
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_FRAMES);

            Map<String, byte[]> byteMethods = new HashMap<>();
            Set<String> xorMethods = new HashSet<>();
            Set<String> strDecryptor1 = new HashSet<>(); // V1: ([BI) UTF_16
            Set<String> strDecryptor2 = new HashSet<>(); // V2: ([B[BI) UTF_16
            Set<String> strDecryptor3 = new HashSet<>(); // V3: ([BI) UTF_16BE

            for (MethodNode mn : cn.methods) {
                byte[] arr = extractByteArrayMethod(mn);
                if (arr != null) {
                    byteMethods.put(mn.name + mn.desc, arr);
                } else if (isXorHelper(mn)) {
                    xorMethods.add(mn.name + mn.desc);
                } else {
                    int v = getStringDecryptorVariant(mn);
                    if (v == 1) strDecryptor1.add(mn.name + mn.desc);
                    else if (v == 2) strDecryptor2.add(mn.name + mn.desc);
                    else if (v == 3) strDecryptor3.add(mn.name + mn.desc);
                }
            }

            // Find static byte[] key field (V1) or static String blob field (V3)
            String staticKeyFieldName = null;
            byte[] staticKeyBytes = null;
            String staticBlobFieldName = null;
            String staticBlobString = null;

            for (MethodNode mn : cn.methods) {
                if (strDecryptor1.contains(mn.name + mn.desc)) {
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.GETSTATIC && fi.owner.equals(cn.name) && fi.desc.equals("[B")) {
                            staticKeyFieldName = fi.name;
                            break;
                        }
                    }
                }
                if (strDecryptor3.contains(mn.name + mn.desc)) {
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.GETSTATIC && fi.owner.equals(cn.name) && fi.desc.equals("Ljava/lang/String;")) {
                            staticBlobFieldName = fi.name;
                            break;
                        }
                    }
                }
            }

            for (MethodNode mn : cn.methods) {
                if (mn.name.equals("<clinit>")) {
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (staticKeyFieldName != null && insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.PUTSTATIC && fi.owner.equals(cn.name) && fi.name.equals(staticKeyFieldName)) {
                            AbstractInsnNode prev = prevReal(insn);
                            if (prev instanceof MethodInsnNode mi && mi.owner.equals(cn.name) && byteMethods.containsKey(mi.name + mi.desc)) {
                                staticKeyBytes = byteMethods.get(mi.name + mi.desc);
                            }
                        }
                        if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.PUTSTATIC && fi.owner.equals(cn.name) && fi.desc.equals("Ljava/lang/String;")) {
                            // Check for: INVOKESTATIC byteMethod()[B; INVOKESTATIC ByteBuffer.wrap; INVOKEVIRTUAL asCharBuffer; INVOKEVIRTUAL toString; PUTSTATIC d
                            AbstractInsnNode p1 = prevReal(insn);
                            AbstractInsnNode p2 = prevReal(p1);
                            AbstractInsnNode p3 = prevReal(p2);
                            AbstractInsnNode p4 = prevReal(p3);
                            if (p1 instanceof MethodInsnNode m1 && m1.name.equals("toString")
                                && p2 instanceof MethodInsnNode m2 && m2.name.equals("asCharBuffer")
                                && p3 instanceof MethodInsnNode m3 && m3.name.equals("wrap")
                                && p4 instanceof MethodInsnNode m4 && m4.owner.equals(cn.name) && byteMethods.containsKey(m4.name + m4.desc)) {
                                staticBlobFieldName = fi.name;
                                byte[] raw = byteMethods.get(m4.name + m4.desc);
                                staticBlobString = ByteBuffer.wrap(raw).asCharBuffer().toString();
                            }
                        }
                    }
                }
            }

            boolean classWasObf = !byteMethods.isEmpty() || !xorMethods.isEmpty()
                || !strDecryptor1.isEmpty() || !strDecryptor2.isEmpty() || !strDecryptor3.isEmpty() || staticBlobFieldName != null;

            // Clean up junk fields & <clinit> / <init> artifacts BEFORE deobfuscateMethod
            cleanupJunkFieldsAndInits(cn, byteMethods, staticKeyFieldName, staticBlobFieldName);

            // Deobfuscate each method that uses var_S
            for (MethodNode mn : cn.methods) {
                if (byteMethods.containsKey(mn.name + mn.desc) || xorMethods.contains(mn.name + mn.desc)
                    || strDecryptor1.contains(mn.name + mn.desc) || strDecryptor2.contains(mn.name + mn.desc)
                    || strDecryptor3.contains(mn.name + mn.desc)) {
                    continue;
                }
                int[] res = deobfuscateMethod(cn, mn, byteMethods, xorMethods, strDecryptor1, strDecryptor2, strDecryptor3, staticKeyBytes, staticBlobString);
                if (res[0] > 0) {
                    classWasObf = true;
                    totalMethodsDeobf++;
                    totalStringsDecrypted += res[1];
                }
            }

            // Also check for Variant 4 single-byte XOR decryptor: private static ([B)Ljava/lang/String;
            int v4Count = inlineVariant4ByteXorStrings(cn);
            if (v4Count > 0) {
                totalStringsDecrypted += v4Count;
                for (MethodNode mn : cn.methods) {
                    removeUnreachableAfterRewrite(mn);
                    simplifyJumpsAndGotos(mn);
                }
            }

            // Remove synthetic methods
            cn.methods.removeIf(mn ->
                byteMethods.containsKey(mn.name + mn.desc) ||
                xorMethods.contains(mn.name + mn.desc) ||
                strDecryptor1.contains(mn.name + mn.desc) ||
                strDecryptor2.contains(mn.name + mn.desc) ||
                strDecryptor3.contains(mn.name + mn.desc) ||
                (mn.name.equals("<clinit>") && isEmptyClinit(mn))
            );

            for (MethodNode mn : cn.methods) {
                cleanUpStackSpillsAndLinearize(cn.name, mn);
            }

            // Restore stripped abstract method on PacketHandler (mo) functional interface
            if ("mo".equals(cn.name) && (cn.access & Opcodes.ACC_INTERFACE) != 0 && cn.methods.isEmpty()) {
                MethodNode handleMethod = new MethodNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT,
                    "handle",
                    "(Lmn;Lnet/minecraft/network/Connection;)V",
                    "(TT;Lnet/minecraft/network/Connection;)V",
                    null
                );
                cn.methods.add(handleMethod);
            }

            // Restore stripped ACC_RECORD and RecordComponents attribute for java/lang/Record subclasses
            if ("java/lang/Record".equals(cn.superName)) {
                cn.access |= Opcodes.ACC_RECORD;
                if (cn.recordComponents == null) {
                    cn.recordComponents = new ArrayList<>();
                }
                boolean rebuildComponents = cn.recordComponents.isEmpty();
                for (FieldNode fn : cn.fields) {
                    if ((fn.access & Opcodes.ACC_STATIC) == 0) {
                        fn.access = (fn.access & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
                        if (rebuildComponents) {
                            RecordComponentNode rcn = new RecordComponentNode(fn.name, fn.desc, fn.signature);
                            rcn.visibleAnnotations = fn.visibleAnnotations;
                            rcn.invisibleAnnotations = fn.invisibleAnnotations;
                            rcn.visibleTypeAnnotations = fn.visibleTypeAnnotations;
                            rcn.invisibleTypeAnnotations = fn.invisibleTypeAnnotations;
                            cn.recordComponents.add(rcn);
                        }
                    }
                }
            }

            if (classWasObf) obfClasses++;

            Path outFile = outDir.resolve(rel);
            Files.createDirectories(outFile.getParent());
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            Files.write(outFile, cw.toByteArray());
        }

        System.out.println("Processed " + totalClasses + " classes (" + obfClasses + " obfuscated).");
        System.out.println("Deobfuscated " + totalMethodsDeobf + " methods, decrypted " + totalStringsDecrypted + " strings.");
    }

    // Detects and inlines Variant 4 single-byte XOR string decryptors: `private static String a(byte[])`
    private static int inlineVariant4ByteXorStrings(ClassNode cn) {
        MethodNode v4Method = null;
        Integer xorKey = null;
        for (MethodNode mn : cn.methods) {
            if ((mn.access & Opcodes.ACC_PRIVATE) != 0 && (mn.access & Opcodes.ACC_STATIC) != 0 && mn.desc.equals("([B)Ljava/lang/String;")) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn.getOpcode() == Opcodes.IXOR) {
                        AbstractInsnNode p = prevReal(insn);
                        for (int k = 0; k < 5 && p != null; k++) {
                            if (isIntPush(p)) {
                                xorKey = getIntVal(p);
                                v4Method = mn;
                                break;
                            }
                            p = prevReal(p);
                        }
                    }
                }
            }
        }
        if (v4Method == null || xorKey == null) return 0;

        int inlined = 0;
        String v4Name = v4Method.name;
        int key = xorKey;

        for (MethodNode mn : cn.methods) {
            if (mn == v4Method) continue;
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (insn instanceof MethodInsnNode mi && mi.getOpcode() == Opcodes.INVOKESTATIC
                    && mi.owner.equals(cn.name) && mi.name.equals(v4Name) && mi.desc.equals("([B)Ljava/lang/String;")) {
                    List<AbstractInsnNode> toRem = new ArrayList<>();
                    byte[] arr = matchVerboseByteArrayConstruction(mi, toRem);
                    if (arr != null) {
                        char[] chars = new char[arr.length];
                        for (int i = 0; i < arr.length; i++) {
                            chars[i] = (char) (arr[i] ^ key);
                        }
                        String dec = new String(chars);
                        for (AbstractInsnNode r : toRem) mn.instructions.remove(r);
                        mn.instructions.set(mi, new LdcInsnNode(dec));
                        inlined++;
                    } else {
                        System.out.println("FAILED V4 match in " + cn.name + "." + mn.name + mn.desc);
                    }
                }
            }
        }

        boolean stillCalled = false;
        for (MethodNode mn : cn.methods) {
            if (mn == v4Method) continue;
            for (AbstractInsnNode insn : mn.instructions) {
                if (insn instanceof MethodInsnNode mi && mi.owner.equals(cn.name) && mi.name.equals(v4Name) && mi.desc.equals("([B)Ljava/lang/String;")) {
                    stillCalled = true;
                    break;
                }
            }
        }
        if (!stillCalled) {
            cn.methods.remove(v4Method);
        }
        return inlined;
    }

    private static byte[] matchVerboseByteArrayConstruction(MethodInsnNode callInsn, List<AbstractInsnNode> toRem) {
        AbstractInsnNode cur = prevReal(callInsn);
        if (!(cur instanceof VarInsnNode v1 && v1.getOpcode() == Opcodes.ALOAD)) return null;
        toRem.add(cur);
        cur = prevReal(cur);
        while (cur instanceof JumpInsnNode ji && ji.getOpcode() == Opcodes.GOTO) {
            toRem.add(cur);
            cur = prevReal(cur);
        }
        if (cur instanceof VarInsnNode v2 && v2.getOpcode() == Opcodes.ASTORE && v2.var == v1.var) {
            AbstractInsnNode prev = prevReal(cur);
            if (prev instanceof VarInsnNode v3 && v3.getOpcode() == Opcodes.ALOAD) {
                toRem.add(cur);
                toRem.add(prev);
                cur = prevReal(prev);
            }
        }

        List<Byte> revBytes = new ArrayList<>();
        while (true) {
            while (cur instanceof JumpInsnNode ji && ji.getOpcode() == Opcodes.GOTO) {
                toRem.add(cur);
                cur = prevReal(cur);
            }
            if (cur == null || cur.getOpcode() != Opcodes.BASTORE) break;
            AbstractInsnNode iLoadVal = prevReal(cur);
            AbstractInsnNode iLoadIdx = prevReal(iLoadVal);
            AbstractInsnNode aLoadArr = prevReal(iLoadIdx);
            AbstractInsnNode iStoreVal = prevReal(aLoadArr);
            AbstractInsnNode pushVal = prevReal(iStoreVal);
            AbstractInsnNode iStoreIdx = prevReal(pushVal);
            AbstractInsnNode pushIdx = prevReal(iStoreIdx);
            if (!(iLoadVal instanceof VarInsnNode && iLoadIdx instanceof VarInsnNode && aLoadArr instanceof VarInsnNode
                && iStoreVal instanceof VarInsnNode && isIntPush(pushVal) && iStoreIdx instanceof VarInsnNode && isIntPush(pushIdx))) {
                return null;
            }
            revBytes.add((byte) getIntVal(pushVal));
            toRem.addAll(Arrays.asList(cur, iLoadVal, iLoadIdx, aLoadArr, iStoreVal, pushVal, iStoreIdx, pushIdx));
            cur = prevReal(pushIdx);
        }

        while (cur instanceof JumpInsnNode ji && ji.getOpcode() == Opcodes.GOTO) {
            toRem.add(cur);
            cur = prevReal(cur);
        }
        if (!(cur instanceof VarInsnNode aStoreArr && aStoreArr.getOpcode() == Opcodes.ASTORE)) return null;
        AbstractInsnNode newArr = prevReal(cur);
        AbstractInsnNode iLoadLen = prevReal(newArr);
        AbstractInsnNode iStoreLen = prevReal(iLoadLen);
        AbstractInsnNode pushLen = prevReal(iStoreLen);
        if (!(newArr instanceof IntInsnNode ii && ii.getOpcode() == Opcodes.NEWARRAY && ii.operand == Opcodes.T_BYTE
            && iLoadLen instanceof VarInsnNode && iStoreLen instanceof VarInsnNode && isIntPush(pushLen))) {
            return null;
        }
        int len = getIntVal(pushLen);
        if (len != revBytes.size()) return null;
        toRem.addAll(Arrays.asList(cur, newArr, iLoadLen, iStoreLen, pushLen));

        byte[] res = new byte[len];
        for (int i = 0; i < len; i++) {
            res[i] = revBytes.get(len - 1 - i);
        }
        return res;
    }

    private static boolean isEmptyClinit(MethodNode mn) {
        AbstractInsnNode f = firstReal(mn.instructions);
        return f != null && f.getOpcode() == Opcodes.RETURN;
    }

    private static Integer matchConstXorInit(String className, Set<String> xorMethods, VarInsnNode istoreInsn, List<AbstractInsnNode> matchedInsns) {
        AbstractInsnNode cur = prevReal(istoreInsn);
        if (!isXorOp(className, xorMethods, cur)) return null;

        List<AbstractInsnNode> collected = new ArrayList<>();
        collected.add(istoreInsn);

        int neededOperands = 1;
        int xorValue = 0;
        int opCount = 0;

        while (neededOperands > 0 && cur != null) {
            if (isXorOp(className, xorMethods, cur)) {
                collected.add(cur);
                neededOperands++;
                opCount++;
                cur = prevReal(cur);
            } else if (isIntPush(cur)) {
                collected.add(cur);
                xorValue ^= getIntVal(cur);
                neededOperands--;
                cur = prevReal(cur);
            } else if (cur instanceof MethodInsnNode mi && mi.owner.equals("java/lang/Integer") && mi.name.equals("parseInt")) {
                AbstractInsnNode prev = prevReal(cur);
                if (prev instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                    try {
                        int parsed = Integer.parseInt(s);
                        collected.add(cur);
                        collected.add(prev);
                        xorValue ^= parsed;
                        neededOperands--;
                        cur = prevReal(prev);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        if (neededOperands == 0 && opCount >= 1) {
            matchedInsns.addAll(collected);
            return xorValue;
        }
        return null;
    }

    private static int[] findEntryVarS(ClassNode cn, MethodNode mn, Set<String> xorMethods) {
        AbstractInsnNode first = firstReal(mn.instructions);
        if (first == null) return null;
        AbstractInsnNode cur = first;
        for (int steps = 0; steps < 15 && cur != null; steps++) {
            if (cur instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ISTORE) {
                List<AbstractInsnNode> matched = new ArrayList<>();
                Integer val = matchConstXorInit(cn.name, xorMethods, vi, matched);
                if (val != null && matched.contains(first)) {
                    int varS = vi.var;
                    boolean confirmed = false;
                    for (AbstractInsnNode insn : mn.instructions) {
                        if (insn instanceof MethodInsnNode mi) {
                            if (mi.owner.equals("aa") || (mi.owner.equals(cn.name) && xorMethods.contains(mi.name + mi.desc))) {
                                confirmed = true;
                                break;
                            }
                        }
                        if (insn instanceof VarInsnNode v && v.getOpcode() == Opcodes.ILOAD && v.var == varS) {
                            AbstractInsnNode n1 = nextReal(v);
                            AbstractInsnNode n2 = nextReal(n1);
                            if ((n1 != null && n1.getOpcode() == Opcodes.IXOR) || (n2 != null && n2.getOpcode() == Opcodes.IXOR) || (n1 instanceof MethodInsnNode)) {
                                confirmed = true;
                                break;
                            }
                        }
                    }
                    if (!confirmed) return null;
                    return new int[]{varS, val, mn.instructions.indexOf(vi)};
                }
            }
            cur = nextReal(cur);
        }
        return null;
    }

    private static boolean isXorOp(String className, Set<String> xorMethods, AbstractInsnNode insn) {
        if (insn == null) return false;
        if (insn.getOpcode() == Opcodes.IXOR) return true;
        if (insn instanceof MethodInsnNode mi && mi.owner.equals(className) && xorMethods.contains(mi.name + mi.desc)) return true;
        return false;
    }

    private static int[] deobfuscateMethod(
        ClassNode cn,
        MethodNode mn,
        Map<String, byte[]> byteMethods,
        Set<String> xorMethods,
        Set<String> strDecryptor1,
        Set<String> strDecryptor2,
        Set<String> strDecryptor3,
        byte[] staticKeyBytes,
        String staticBlobString
    ) {
        int[] entry = findEntryVarS(cn, mn, xorMethods);
        if (entry == null) return new int[]{0, 0};

        int varS = entry[0];
        int initVal = entry[1];
        int entryEndIdx = entry[2];

        AbstractInsnNode[] insns = mn.instructions.toArray();
        int n = insns.length;

        Integer[] stateBefore = new Integer[n];
        boolean[] visited = new boolean[n];

        Map<Integer, Integer> reinitValueAtIstore = new HashMap<>();
        Map<Integer, List<AbstractInsnNode>> reinitInsnsAtIstore = new HashMap<>();
        for (int i = entryEndIdx + 1; i < n; i++) {
            if (insns[i] instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ISTORE && vi.var == varS) {
                if (getPrecedingXorUpdateConst(cn.name, xorMethods, vi, varS) == null) {
                    List<AbstractInsnNode> matched = new ArrayList<>();
                    Integer val = matchConstXorInit(cn.name, xorMethods, vi, matched);
                    if (val != null) {
                        reinitValueAtIstore.put(i, val);
                        reinitInsnsAtIstore.put(i, matched);
                    }
                }
            }
        }

        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{entryEndIdx + 1, initVal});

        Map<TryCatchBlockNode, LabelNode> redirectedCatchHandlers = new HashMap<>();
        Set<TryCatchBlockNode> deadCatchBlocks = new HashSet<>();

        Runnable propagate = () -> {
            while (!queue.isEmpty()) {
                int[] item = queue.poll();
                int idx = item[0];
                int curS = item[1];
                if (idx < 0 || idx >= n) continue;
                if (visited[idx]) {
                    continue;
                }
                visited[idx] = true;
                stateBefore[idx] = curS;

                AbstractInsnNode insn = insns[idx];
                int nextS = curS;

                if (insn instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ISTORE && vi.var == varS) {
                    Integer xorConst = getPrecedingXorUpdateConst(cn.name, xorMethods, insn, varS);
                    if (xorConst != null) {
                        nextS = curS ^ xorConst;
                    } else if (reinitValueAtIstore.containsKey(idx)) {
                        nextS = reinitValueAtIstore.get(idx);
                    }
                }

                int op = insn.getOpcode();
                if (op == Opcodes.GOTO) {
                    LabelNode target = ((JumpInsnNode) insn).label;
                    queue.add(new int[]{mn.instructions.indexOf(target), nextS});
                } else if (insn instanceof JumpInsnNode ji) {
                    Integer opaqueTaken = evalOpaqueJump(ji, varS, nextS);
                    if (opaqueTaken != null) {
                        if (opaqueTaken == 1) {
                            queue.add(new int[]{mn.instructions.indexOf(ji.label), nextS});
                        } else {
                            queue.add(new int[]{idx + 1, nextS});
                        }
                    } else {
                        queue.add(new int[]{idx + 1, nextS});
                        queue.add(new int[]{mn.instructions.indexOf(ji.label), nextS});
                    }
                } else if (insn instanceof LookupSwitchInsnNode lsi) {
                    Integer switchFn = getAaSwitchFunc(lsi, varS);
                    if (switchFn != null) {
                        int key = (switchFn == 1) ? aa_a(nextS) : aa_b(nextS);
                        LabelNode target = lsi.dflt;
                        for (int i = 0; i < lsi.keys.size(); i++) {
                            if (lsi.keys.get(i) == key) {
                                target = lsi.labels.get(i);
                                break;
                            }
                        }
                        queue.add(new int[]{mn.instructions.indexOf(target), nextS});
                    } else {
                        queue.add(new int[]{mn.instructions.indexOf(lsi.dflt), nextS});
                        for (LabelNode lbl : lsi.labels) {
                            queue.add(new int[]{mn.instructions.indexOf(lbl), nextS});
                        }
                    }
                } else if (insn instanceof TableSwitchInsnNode tsi) {
                    Integer switchFn = getAaSwitchFunc(tsi, varS);
                    if (switchFn != null) {
                        int key = (switchFn == 1) ? aa_a(nextS) : aa_b(nextS);
                        LabelNode target = (key >= tsi.min && key <= tsi.max) ? tsi.labels.get(key - tsi.min) : tsi.dflt;
                        queue.add(new int[]{mn.instructions.indexOf(target), nextS});
                    } else {
                        queue.add(new int[]{mn.instructions.indexOf(tsi.dflt), nextS});
                        for (LabelNode lbl : tsi.labels) {
                            queue.add(new int[]{mn.instructions.indexOf(lbl), nextS});
                        }
                    }
                } else if (op == Opcodes.IRETURN || op == Opcodes.LRETURN || op == Opcodes.FRETURN
                        || op == Opcodes.DRETURN || op == Opcodes.ARETURN || op == Opcodes.RETURN
                        || op == Opcodes.ATHROW) {
                    // Terminal instruction
                } else {
                    queue.add(new int[]{idx + 1, nextS});
                }
            }
        };

        boolean changed = true;
        while (changed) {
            propagate.run();
            changed = false;
            for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                int startIdx = mn.instructions.indexOf(tcb.start);
                int endIdx = mn.instructions.indexOf(tcb.end);
                int handlerIdx = mn.instructions.indexOf(tcb.handler);

                Set<Integer> tryStates = new LinkedHashSet<>();
                for (int i = startIdx; i < endIdx; i++) {
                    if (visited[i] && stateBefore[i] != null && !(insns[i] instanceof LabelNode || insns[i] instanceof LineNumberNode || insns[i] instanceof FrameNode)) {
                        tryStates.add(stateBefore[i]);
                    }
                }
                if (tryStates.isEmpty()) continue;

                AbstractInsnNode h0 = nextReal(tcb.handler);
                AbstractInsnNode h1 = nextReal(h0);
                AbstractInsnNode h2 = nextReal(h1);
                if (h0 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS
                    && h1 instanceof MethodInsnNode mi && mi.owner.equals("aa") && mi.name.equals("b")
                    && (h2 instanceof LookupSwitchInsnNode || h2 instanceof TableSwitchInsnNode)) {

                    for (int s : tryStates) {
                        int key = aa_b(s);
                        LabelNode caseTarget = null;
                        if (h2 instanceof LookupSwitchInsnNode lsi) {
                            caseTarget = lsi.dflt;
                            for (int k = 0; k < lsi.keys.size(); k++) {
                                if (lsi.keys.get(k) == key) {
                                    caseTarget = lsi.labels.get(k);
                                    break;
                                }
                            }
                        } else if (h2 instanceof TableSwitchInsnNode tsi) {
                            caseTarget = (key >= tsi.min && key <= tsi.max) ? tsi.labels.get(key - tsi.min) : tsi.dflt;
                        }
                        if (caseTarget != null) {
                            LabelNode realCatch = traceCatchDispatcherStub(cn.name, xorMethods, caseTarget, varS);
                            if (realCatch != null) {
                                int caseIdx = mn.instructions.indexOf(caseTarget);
                                if (!visited[caseIdx]) {
                                    queue.add(new int[]{caseIdx, s});
                                    changed = true;
                                }
                                redirectedCatchHandlers.put(tcb, realCatch);
                            }
                        }
                    }
                } else {
                    int firstTryState = tryStates.iterator().next();
                    if (!visited[handlerIdx]) {
                        queue.add(new int[]{handlerIdx, firstTryState});
                        changed = true;
                    }
                }
            }
        }

        for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
            if (redirectedCatchHandlers.containsKey(tcb)) {
                tcb.handler = redirectedCatchHandlers.get(tcb);
            } else {
                int handlerIdx = mn.instructions.indexOf(tcb.handler);
                if (!visited[handlerIdx]) {
                    deadCatchBlocks.add(tcb);
                }
            }
        }
        mn.tryCatchBlocks.removeAll(deadCatchBlocks);

        int decryptedCount = 0;
        Set<AbstractInsnNode> toRemove = new HashSet<>();

        for (int i = 0; i <= entryEndIdx; i++) {
            if (!(insns[i] instanceof LabelNode || insns[i] instanceof LineNumberNode || insns[i] instanceof FrameNode)) {
                toRemove.add(insns[i]);
            }
        }

        for (List<AbstractInsnNode> rInsns : reinitInsnsAtIstore.values()) {
            toRemove.addAll(rInsns);
        }

        for (int i = 0; i < n; i++) {
            if (!visited[i] && !(insns[i] instanceof LabelNode)) {
                toRemove.add(insns[i]);
            }
        }

        Set<Integer> tempSwitchVars = new HashSet<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i] || toRemove.contains(insns[i])) continue;
            AbstractInsnNode insn = insns[i];
            int curS = stateBefore[i];

            // 1. Opaque switch on varS
            if (insn instanceof LookupSwitchInsnNode || insn instanceof TableSwitchInsnNode) {
                Integer switchFn = getAaSwitchFunc(insn, varS);
                if (switchFn != null) {
                    AbstractInsnNode p1 = prevReal(insn);
                    AbstractInsnNode p2 = prevReal(p1);
                    int key = (switchFn == 1) ? aa_a(curS) : aa_b(curS);
                    LabelNode target = null;
                    if (insn instanceof LookupSwitchInsnNode lsi) {
                        target = lsi.dflt;
                        for (int k = 0; k < lsi.keys.size(); k++) {
                            if (lsi.keys.get(k) == key) {
                                target = lsi.labels.get(k);
                                break;
                            }
                        }
                    } else if (insn instanceof TableSwitchInsnNode tsi) {
                        target = (key >= tsi.min && key <= tsi.max) ? tsi.labels.get(key - tsi.min) : tsi.dflt;
                    }
                    toRemove.add(p2);
                    toRemove.add(p1);
                    mn.instructions.set(insn, new JumpInsnNode(Opcodes.GOTO, target));
                    continue;
                }
            }

            // 2. Opaque jump on aa.c
            if (insn instanceof JumpInsnNode ji && ji.getOpcode() != Opcodes.GOTO) {
                Integer opaqueTaken = evalOpaqueJump(ji, varS, curS);
                if (opaqueTaken != null) {
                    AbstractInsnNode p1 = prevReal(ji);
                    AbstractInsnNode p2 = prevReal(p1);
                    AbstractInsnNode p3 = prevReal(p2);
                    toRemove.add(p3);
                    toRemove.add(p2);
                    toRemove.add(p1);
                    if (opaqueTaken == 1) {
                        mn.instructions.set(ji, new JumpInsnNode(Opcodes.GOTO, ji.label));
                    } else {
                        toRemove.add(ji);
                    }
                    continue;
                }
            }

            // 3. String decryption (V1, V2, V3)
            if (insn instanceof MethodInsnNode mi && mi.owner.equals(cn.name)) {
                String sig = mi.name + mi.desc;
                if (strDecryptor1.contains(sig)) {
                    AbstractInsnNode p1 = prevReal(insn);
                    AbstractInsnNode p2 = prevReal(p1);
                    if (p1 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS
                        && p2 instanceof MethodInsnNode bm && bm.owner.equals(cn.name) && byteMethods.containsKey(bm.name + bm.desc)) {
                        byte[] enc = byteMethods.get(bm.name + bm.desc);
                        String dec = decryptStringV12(enc, staticKeyBytes, curS);
                        toRemove.add(p2);
                        toRemove.add(p1);
                        mn.instructions.set(insn, new LdcInsnNode(dec));
                        decryptedCount++;
                        continue;
                    }
                } else if (strDecryptor2.contains(sig)) {
                    AbstractInsnNode p1 = prevReal(insn);
                    AbstractInsnNode p2 = prevReal(p1);
                    AbstractInsnNode p3 = prevReal(p2);
                    if (p1 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS
                        && p2 instanceof MethodInsnNode bm2 && bm2.owner.equals(cn.name) && byteMethods.containsKey(bm2.name + bm2.desc)
                        && p3 instanceof MethodInsnNode bm1 && bm1.owner.equals(cn.name) && byteMethods.containsKey(bm1.name + bm1.desc)) {
                        byte[] enc = byteMethods.get(bm1.name + bm1.desc);
                        byte[] k2 = byteMethods.get(bm2.name + bm2.desc);
                        String dec = decryptStringV12(enc, k2, curS);
                        toRemove.add(p3);
                        toRemove.add(p2);
                        toRemove.add(p1);
                        mn.instructions.set(insn, new LdcInsnNode(dec));
                        decryptedCount++;
                        continue;
                    }
                } else if (strDecryptor3.contains(sig)) {
                    AbstractInsnNode p1 = prevReal(insn);
                    AbstractInsnNode p2 = prevReal(p1);
                    if (p1 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS
                        && p2 instanceof MethodInsnNode bm && bm.owner.equals(cn.name) && byteMethods.containsKey(bm.name + bm.desc)) {
                        byte[] descBytes = byteMethods.get(bm.name + bm.desc);
                        String dec = decryptStringV3(descBytes, staticBlobString, curS);
                        toRemove.add(p2);
                        toRemove.add(p1);
                        mn.instructions.set(insn, new LdcInsnNode(dec));
                        decryptedCount++;
                        continue;
                    }
                }
            }

            // 4 & 5. XOR with varS (either state update or obfuscated constant)
            if (isXorOp(cn.name, xorMethods, insn)) {
                AbstractInsnNode p1 = prevReal(insn);
                AbstractInsnNode p2 = prevReal(p1);
                Integer xorConst = null;
                if (p1 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS && isIntPush(p2)) {
                    xorConst = getIntVal(p2);
                } else if (isIntPush(p1) && p2 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS) {
                    xorConst = getIntVal(p1);
                }
                if (xorConst != null) {
                    AbstractInsnNode next = nextReal(insn);
                    if (next instanceof VarInsnNode nvi && nvi.getOpcode() == Opcodes.ISTORE && nvi.var == varS) {
                        toRemove.add(p2);
                        toRemove.add(p1);
                        toRemove.add(insn);
                        toRemove.add(next);
                    } else {
                        int folded = xorConst ^ curS;
                        toRemove.add(p2);
                        toRemove.add(p1);
                        if (next != null && next.getOpcode() == Opcodes.I2B) {
                            folded = (byte) folded;
                            toRemove.add(next);
                        } else if (next != null && next.getOpcode() == Opcodes.I2S) {
                            folded = (short) folded;
                            toRemove.add(next);
                        }
                        mn.instructions.set(insn, makeIntPush(folded));
                    }
                }
            }
        }

        // Now that all strings in `mn` are decrypted into plain LdcInsnNode(String), unobfuscate all real switches!
        for (int i = 0; i < n; i++) {
            if (!visited[i] || toRemove.contains(insns[i])) continue;
            AbstractInsnNode insn = insns[i];
            if ((insn instanceof LookupSwitchInsnNode || insn instanceof TableSwitchInsnNode) && mn.instructions.contains(insn)) {
                int curS = stateBefore[i];
                unobfuscateRealSwitch(cn.name, xorMethods, mn, insn, varS, curS, toRemove, tempSwitchVars);
            }
        }

        for (AbstractInsnNode rem : toRemove) {
            mn.instructions.remove(rem);
        }

        removeUnreachableAfterRewrite(mn);

        simplifyJumpsAndGotos(mn);

        if (mn.localVariables != null) {
            mn.localVariables.removeIf(lv -> lv.index == varS || tempSwitchVars.contains(lv.index));
        }

        return new int[]{1, decryptedCount};
    }

    private static boolean unobfuscateRealSwitch(
        String className,
        Set<String> xorMethods,
        MethodNode mn,
        AbstractInsnNode switchInsn,
        int varS,
        int curS,
        Set<AbstractInsnNode> toRemove,
        Set<Integer> tempSwitchVars
    ) {
        AbstractInsnNode p1 = prevReal(switchInsn);
        if (!(p1 instanceof MethodInsnNode mi && mi.owner.equals("aa") && mi.name.equals("a"))) return false;
        AbstractInsnNode p2 = prevReal(p1);
        if (!(p2 instanceof VarInsnNode viLoadTemp && viLoadTemp.getOpcode() == Opcodes.ILOAD)) return false;
        int tempVar = viLoadTemp.var;
        AbstractInsnNode p3 = prevReal(p2);
        if (!(p3 instanceof VarInsnNode viStoreTemp && viStoreTemp.getOpcode() == Opcodes.ISTORE && viStoreTemp.var == tempVar)) return false;
        AbstractInsnNode p4 = prevReal(p3);
        if (!isXorOp(className, xorMethods, p4)) return false;
        AbstractInsnNode p5 = prevReal(p4);
        if (!(p5 instanceof VarInsnNode viLoadS && viLoadS.getOpcode() == Opcodes.ILOAD && viLoadS.var == varS)) return false;

        toRemove.add(p5);
        toRemove.add(p4);
        toRemove.add(p3);
        toRemove.add(p2);
        toRemove.add(p1);
        tempSwitchVars.add(tempVar);

        List<Integer> hashedKeys = new ArrayList<>();
        List<LabelNode> targets = new ArrayList<>();
        LabelNode dflt;
        if (switchInsn instanceof LookupSwitchInsnNode lsi) {
            hashedKeys.addAll(lsi.keys);
            targets.addAll(lsi.labels);
            dflt = lsi.dflt;
        } else {
            TableSwitchInsnNode tsi = (TableSwitchInsnNode) switchInsn;
            for (int k = tsi.min; k <= tsi.max; k++) {
                hashedKeys.add(k);
                targets.add(tsi.labels.get(k - tsi.min));
            }
            dflt = tsi.dflt;
        }

        AbstractInsnNode p6 = prevReal(p5);
        AbstractInsnNode p7_0 = prevReal(p6);
        AbstractInsnNode p8_0 = prevReal(p7_0);
        boolean isTypeSwitch = (p6 instanceof InvokeDynamicInsnNode idi0 && idi0.name.equals("typeSwitch"))
            || (p8_0 instanceof InvokeDynamicInsnNode idi1 && idi1.name.equals("typeSwitch"));
        boolean isEnumArraySwitch = (p6 != null && p6.getOpcode() == Opcodes.IALOAD)
            || (p8_0 != null && p8_0.getOpcode() == Opcodes.IALOAD);

        Map<Integer, LabelNode> newCases = new TreeMap<>();
        for (int i = 0; i < hashedKeys.size(); i++) {
            int hKey = hashedKeys.get(i);
            LabelNode target = targets.get(i);
            if (target == dflt) continue;

            Integer origKey = null;
            if (isEnumArraySwitch) {
                for (int v = 1; v <= 1024; v++) {
                    if (aa_a(v ^ curS) == hKey) {
                        origKey = v;
                        break;
                    }
                }
            }
            if (origKey == null && !isTypeSwitch && !isEnumArraySwitch) {
                origKey = findStringEqualsHashCodeInMethod(mn, hKey, curS);
            }
            if (origKey == null) {
                origKey = invertSmallKey(hKey, curS);
            }
            if (origKey == null) {
                continue;
            }
            newCases.put(origKey, target);
        }

        if (isTypeSwitch) {
            AbstractInsnNode p7 = prevReal(p6); // iconst_0
            AbstractInsnNode p8 = prevReal(p7); // aload 123
            AbstractInsnNode p9 = prevReal(p8); // astore 123
            AbstractInsnNode p10 = prevReal(p9); // aload 46
            AbstractInsnNode p11 = prevReal(p10); // astore 100
            AbstractInsnNode p12 = prevReal(p11); // invokestatic Objects.requireNonNull
            if (p7 != null && p8 instanceof VarInsnNode vl123 && p9 instanceof VarInsnNode vs123 && vl123.var == vs123.var
                && p10 instanceof VarInsnNode vl46 && p11 instanceof VarInsnNode vs100 && vs100.getOpcode() == Opcodes.ASTORE
                && p12 instanceof MethodInsnNode req && req.owner.equals("java/util/Objects") && req.name.equals("requireNonNull")) {
                AbstractInsnNode p13 = prevReal(p12); // aload 46
                if (p13 instanceof VarInsnNode vl46_0 && vl46_0.var == vl46.var) {
                    mn.instructions.set(p11, new InsnNode(Opcodes.POP));
                }
            }
            // Note: SwitchBootstraps.typeSwitch returns -1 when the selector is null!
            // Because -1 < -128..1024 was inverted to -1, `newCases` currently contains `-1 -> dflt` or `-1 -> target`.
            // Standard javac `lookupswitch` on `typeSwitch` does NOT include `-1` as a case key (unless `case null`), or if `-1` is present with `Objects.requireNonNull` before `typeSwitch`, Vineflower's `SwitchPatternMatchProcessor` expects only non-negative case keys `0, 1, ...`!
            newCases.remove(-1);
        }

        List<Integer> sortedKeys = new ArrayList<>(newCases.keySet());
        List<LabelNode> sortedLabels = new ArrayList<>();
        for (int k : sortedKeys) sortedLabels.add(newCases.get(k));

        LookupSwitchInsnNode newSwitch = new LookupSwitchInsnNode(dflt, sortedKeys.stream().mapToInt(Integer::intValue).toArray(), sortedLabels.toArray(new LabelNode[0]));
        mn.instructions.set(switchInsn, newSwitch);
        return true;
    }

    private static Integer findStringEqualsHashCodeInMethod(MethodNode mn, int expectedHashedKey, int switchS) {
        for (AbstractInsnNode insn : mn.instructions) {
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                int hc = s.hashCode();
                if (aa_a(hc ^ switchS) == expectedHashedKey) {
                    return hc;
                }
            }
        }
        return null;
    }

    private static Integer invertSmallKey(int expectedHashedKey, int curS) {
        for (int v = -128; v <= 1024; v++) {
            if (aa_a(v ^ curS) == expectedHashedKey) return v;
        }
        for (int v = -100000; v <= 100000; v++) {
            if (aa_a(v ^ curS) == expectedHashedKey) return v;
        }
        return null;
    }

    private static void removeUnreachableAfterRewrite(MethodNode mn) {
        AbstractInsnNode[] insns = mn.instructions.toArray();
        int n = insns.length;
        if (n == 0) return;
        boolean[] reach = new boolean[n];
        Deque<Integer> q = new ArrayDeque<>();
        q.add(0);
        for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
            q.add(mn.instructions.indexOf(tcb.handler));
        }
        while (!q.isEmpty()) {
            int idx = q.poll();
            if (idx < 0 || idx >= n || reach[idx]) continue;
            reach[idx] = true;
            AbstractInsnNode insn = insns[idx];
            int op = insn.getOpcode();
            if (op == Opcodes.GOTO) {
                q.add(mn.instructions.indexOf(((JumpInsnNode) insn).label));
            } else if (insn instanceof JumpInsnNode ji) {
                q.add(idx + 1);
                q.add(mn.instructions.indexOf(ji.label));
            } else if (insn instanceof LookupSwitchInsnNode lsi) {
                q.add(mn.instructions.indexOf(lsi.dflt));
                for (LabelNode l : lsi.labels) q.add(mn.instructions.indexOf(l));
            } else if (insn instanceof TableSwitchInsnNode tsi) {
                q.add(mn.instructions.indexOf(tsi.dflt));
                for (LabelNode l : tsi.labels) q.add(mn.instructions.indexOf(l));
            } else if (op == Opcodes.IRETURN || op == Opcodes.LRETURN || op == Opcodes.FRETURN
                    || op == Opcodes.DRETURN || op == Opcodes.ARETURN || op == Opcodes.RETURN
                    || op == Opcodes.ATHROW) {
                // stop
            } else {
                q.add(idx + 1);
            }
        }
        for (int i = 0; i < n; i++) {
            if (!reach[i] && !(insns[i] instanceof LabelNode)) {
                mn.instructions.remove(insns[i]);
            }
        }
    }

    private static Integer getPrecedingXorUpdateConst(String className, Set<String> xorMethods, AbstractInsnNode istoreInsn, int varS) {
        AbstractInsnNode p1 = prevReal(istoreInsn);
        if (!isXorOp(className, xorMethods, p1)) return null;
        AbstractInsnNode p2 = prevReal(p1);
        AbstractInsnNode p3 = prevReal(p2);
        if (p2 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS && isIntPush(p3)) {
            return getIntVal(p3);
        }
        if (isIntPush(p2) && p3 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS) {
            return getIntVal(p2);
        }
        return null;
    }

    private static Integer evalOpaqueJump(JumpInsnNode ji, int varS, int curS) {
        int op = ji.getOpcode();
        if (op != Opcodes.IF_ICMPEQ && op != Opcodes.IF_ICMPNE) return null;
        AbstractInsnNode p1 = prevReal(ji);
        if (!isIntPush(p1)) return null;
        AbstractInsnNode p2 = prevReal(p1);
        if (!(p2 instanceof MethodInsnNode mi && mi.owner.equals("aa") && mi.name.equals("c"))) return null;
        AbstractInsnNode p3 = prevReal(p2);
        if (!(p3 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS)) return null;

        int expected = getIntVal(p1);
        int actual = aa_c(curS);
        boolean eq = (actual == expected);
        if (op == Opcodes.IF_ICMPEQ) return eq ? 1 : 0;
        else return (!eq) ? 1 : 0;
    }

    private static Integer getAaSwitchFunc(AbstractInsnNode switchInsn, int varS) {
        AbstractInsnNode p1 = prevReal(switchInsn);
        if (!(p1 instanceof MethodInsnNode mi && mi.owner.equals("aa"))) return null;
        AbstractInsnNode p2 = prevReal(p1);
        if (!(p2 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ILOAD && vi.var == varS)) return null;
        if (mi.name.equals("a")) return 1;
        if (mi.name.equals("b")) return 2;
        return null;
    }

    private static LabelNode traceCatchDispatcherStub(String className, Set<String> xorMethods, LabelNode caseTarget, int varS) {
        AbstractInsnNode i0 = nextReal(caseTarget);
        AbstractInsnNode i1 = nextReal(i0);
        AbstractInsnNode i2 = nextReal(i1);
        AbstractInsnNode i3 = nextReal(i2);
        AbstractInsnNode i4 = nextReal(i3);
        if (i3 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ISTORE && vi.var == varS
            && isXorOp(className, xorMethods, i2)) {
            if (i4 instanceof JumpInsnNode ji && ji.getOpcode() == Opcodes.GOTO) {
                return ji.label;
            }
            if (i4 instanceof VarInsnNode vs && vs.getOpcode() == Opcodes.ASTORE) {
                return caseTarget;
            }
        }
        return null;
    }

    private static void simplifyJumpsAndGotos(MethodNode mn) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (insn.getOpcode() == Opcodes.GOTO) {
                    JumpInsnNode ji = (JumpInsnNode) insn;
                    AbstractInsnNode next = nextReal(ji);
                    AbstractInsnNode targetReal = (ji.label instanceof LabelNode) ? nextReal(ji.label) : ji.label;
                    if (next == targetReal) {
                        mn.instructions.remove(ji);
                        changed = true;
                        continue;
                    }
                    if (targetReal != null && targetReal.getOpcode() == Opcodes.GOTO) {
                        LabelNode nextTarget = ((JumpInsnNode) targetReal).label;
                        if (nextTarget != ji.label) {
                            ji.label = nextTarget;
                            changed = true;
                        }
                    }
                } else if (insn instanceof JumpInsnNode ji && (ji.getOpcode() == Opcodes.IF_ICMPEQ || ji.getOpcode() == Opcodes.IF_ICMPNE)) {
                    AbstractInsnNode prev = prevReal(ji);
                    if (prev != null && prev.getOpcode() == Opcodes.ICONST_0) {
                        mn.instructions.remove(prev);
                        ji.setOpcode(ji.getOpcode() == Opcodes.IF_ICMPEQ ? Opcodes.IFEQ : Opcodes.IFNE);
                        changed = true;
                    }
                }
            }
        }
    }

    private static void cleanupJunkFieldsAndInits(ClassNode cn, Map<String, byte[]> byteMethods, String staticKeyFieldName, String staticBlobFieldName) {
        Set<String> junkFields = new HashSet<>();
        if (staticKeyFieldName != null) {
            junkFields.add(staticKeyFieldName + ":[B");
        }
        if (staticBlobFieldName != null) {
            junkFields.add(staticBlobFieldName + ":Ljava/lang/String;");
        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<clinit>")) {
                List<AbstractInsnNode> toRem = new ArrayList<>();
                for (AbstractInsnNode insn : mn.instructions.toArray()) {
                    if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.PUTSTATIC && fi.owner.equals(cn.name)) {
                        if (fi.desc.equals("[B")) {
                            AbstractInsnNode prev = prevReal(fi);
                            if (prev instanceof MethodInsnNode mi && mi.owner.equals(cn.name) && byteMethods.containsKey(mi.name + mi.desc)) {
                                junkFields.add(fi.name + ":[B");
                                toRem.add(prev);
                                toRem.add(fi);
                            }
                        }
                        if (fi.name.equals(staticBlobFieldName) && fi.desc.equals("Ljava/lang/String;")) {
                            AbstractInsnNode p1 = prevReal(fi);
                            AbstractInsnNode p2 = prevReal(p1);
                            AbstractInsnNode p3 = prevReal(p2);
                            AbstractInsnNode p4 = prevReal(p3);
                            if (p1 instanceof MethodInsnNode && p2 instanceof MethodInsnNode && p3 instanceof MethodInsnNode
                                && p4 instanceof MethodInsnNode m4 && byteMethods.containsKey(m4.name + m4.desc)) {
                                toRem.addAll(Arrays.asList(p4, p3, p2, p1, fi));
                            }
                        }
                        if (fi.desc.equals("I")) {
                            List<AbstractInsnNode> randInsns = matchRandomSeedInit(fi);
                            if (randInsns != null) {
                                junkFields.add(fi.name + ":I");
                                toRem.addAll(randInsns);
                            }
                        }
                        if (fi.desc.equals("[Ljava/lang/String;")) {
                            List<AbstractInsnNode> arrInsns = matchJunkStringArrayInit(cn.name, fi);
                            if (arrInsns != null) {
                                junkFields.add(fi.name + ":[Ljava/lang/String;");
                                toRem.addAll(arrInsns);
                            }
                        }
                    }
                }
                for (AbstractInsnNode r : toRem) mn.instructions.remove(r);
            }
        }

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("<init>")) {
                List<AbstractInsnNode> toRem = new ArrayList<>();
                for (AbstractInsnNode insn : mn.instructions.toArray()) {
                    if (insn instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.PUTFIELD && fi.owner.equals(cn.name) && fi.desc.equals("I")) {
                        AbstractInsnNode p1 = prevReal(fi);
                        AbstractInsnNode p2 = prevReal(p1);
                        AbstractInsnNode p3 = prevReal(p2);
                        AbstractInsnNode p4 = prevReal(p3);
                        if (p1 != null && p1.getOpcode() == Opcodes.IXOR
                            && p2 instanceof FieldInsnNode gf && gf.getOpcode() == Opcodes.GETSTATIC && gf.owner.equals(cn.name) && junkFields.contains(gf.name + ":I")
                            && isIntPush(p3)
                            && p4 instanceof VarInsnNode vi && vi.getOpcode() == Opcodes.ALOAD && vi.var == 0) {
                            junkFields.add(fi.name + ":I");
                            toRem.addAll(Arrays.asList(p4, p3, p2, p1, fi));
                        }
                    }
                }
                for (AbstractInsnNode r : toRem) mn.instructions.remove(r);
            }
        }

        cn.fields.removeIf(fn -> junkFields.contains(fn.name + ":" + fn.desc));
    }

    private static List<AbstractInsnNode> matchRandomSeedInit(FieldInsnNode putStaticInsn) {
        AbstractInsnNode p1 = prevReal(putStaticInsn);
        if (p1 == null || p1.getOpcode() != Opcodes.IXOR) return null;
        AbstractInsnNode p2 = prevReal(p1);
        AbstractInsnNode p3 = prevReal(p2);

        if (p2 instanceof VarInsnNode viLoad && viLoad.getOpcode() == Opcodes.ILOAD && isIntPush(p3)) {
            AbstractInsnNode p4 = prevReal(p3);
            if (p4 instanceof VarInsnNode viStore && viStore.getOpcode() == Opcodes.ISTORE && viStore.var == viLoad.var) {
                AbstractInsnNode p5 = prevReal(p4);
                AbstractInsnNode p6 = prevReal(p5);
                AbstractInsnNode p7 = prevReal(p6);
                AbstractInsnNode p8 = prevReal(p7);
                AbstractInsnNode p9 = prevReal(p8);
                if (p5 instanceof MethodInsnNode m5 && m5.owner.equals("java/util/Random") && m5.name.equals("nextInt")
                    && p6 instanceof MethodInsnNode m6 && m6.owner.equals("java/util/Random") && m6.name.equals("<init>")
                    && p7 instanceof LdcInsnNode
                    && p8 != null && p8.getOpcode() == Opcodes.DUP
                    && p9 instanceof TypeInsnNode t9 && t9.desc.equals("java/util/Random")) {
                    return Arrays.asList(p9, p8, p7, p6, p5, p4, p3, p2, p1, putStaticInsn);
                }
            }
        }

        if (p2 instanceof MethodInsnNode m2 && m2.owner.equals("java/util/Random") && m2.name.equals("nextInt")) {
            AbstractInsnNode p4 = prevReal(p3);
            AbstractInsnNode p5 = prevReal(p4);
            AbstractInsnNode p6 = prevReal(p5);
            AbstractInsnNode p7 = prevReal(p6);
            if (p3 instanceof MethodInsnNode m3 && m3.owner.equals("java/util/Random") && m3.name.equals("<init>")
                && p4 instanceof LdcInsnNode
                && p5 != null && p5.getOpcode() == Opcodes.DUP
                && p6 instanceof TypeInsnNode t6 && t6.desc.equals("java/util/Random")
                && isIntPush(p7)) {
                return Arrays.asList(p7, p6, p5, p4, p3, p2, p1, putStaticInsn);
            }
        }
        return null;
    }

    private static List<AbstractInsnNode> matchJunkStringArrayInit(String className, FieldInsnNode putStaticInsn) {
        AbstractInsnNode prev1 = prevReal(putStaticInsn);
        if (prev1 instanceof TypeInsnNode tn && tn.getOpcode() == Opcodes.ANEWARRAY && tn.desc.equals("java/lang/String")) {
            AbstractInsnNode lenNode = prevReal(prev1);
            if (isIntPush(lenNode)) {
                int len = getIntVal(lenNode);
                List<AbstractInsnNode> collected = new ArrayList<>();
                collected.add(lenNode);
                collected.add(prev1);
                collected.add(putStaticInsn);
                AbstractInsnNode cur = nextReal(putStaticInsn);
                int count = 0;
                while (cur instanceof FieldInsnNode gf && gf.getOpcode() == Opcodes.GETSTATIC && gf.owner.equals(className) && gf.name.equals(putStaticInsn.name)) {
                    AbstractInsnNode idxNode = nextReal(gf);
                    AbstractInsnNode sNode = nextReal(idxNode);
                    AbstractInsnNode aastoreNode = nextReal(sNode);
                    if (!isIntPush(idxNode) || !(sNode instanceof LdcInsnNode ldc && ldc.cst instanceof String s)
                        || aastoreNode == null || aastoreNode.getOpcode() != Opcodes.AASTORE) {
                        break;
                    }
                    boolean hasHighChar = false;
                    for (int i = 0; i < s.length(); i++) {
                        if (s.charAt(i) > 127) { hasHighChar = true; break; }
                    }
                    if (!hasHighChar) return null;
                    collected.add(gf);
                    collected.add(idxNode);
                    collected.add(sNode);
                    collected.add(aastoreNode);
                    cur = nextReal(aastoreNode);
                    count++;
                }
                if (count == len && count >= 5) {
                    return collected;
                }
            }
        }

        List<AbstractInsnNode> collected = new ArrayList<>();
        collected.add(putStaticInsn);
        AbstractInsnNode cur = prev1;
        int count = 0;
        while (cur != null && cur.getOpcode() == Opcodes.AASTORE) {
            AbstractInsnNode sNode = prevReal(cur);
            AbstractInsnNode idxNode = prevReal(sNode);
            AbstractInsnNode dupNode = prevReal(idxNode);
            if (!(sNode instanceof LdcInsnNode ldc && ldc.cst instanceof String s)) return null;
            boolean hasHighChar = false;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) > 127) {
                    hasHighChar = true;
                    break;
                }
            }
            if (!hasHighChar) return null;
            if (!isIntPush(idxNode)) return null;
            if (dupNode == null || dupNode.getOpcode() != Opcodes.DUP) return null;
            collected.add(cur);
            collected.add(sNode);
            collected.add(idxNode);
            collected.add(dupNode);
            cur = prevReal(dupNode);
            count++;
        }
        if (count >= 5 && cur instanceof TypeInsnNode tn2 && tn2.getOpcode() == Opcodes.ANEWARRAY && tn2.desc.equals("java/lang/String")) {
            AbstractInsnNode lenNode = prevReal(cur);
            if (isIntPush(lenNode) && getIntVal(lenNode) == count) {
                collected.add(cur);
                collected.add(lenNode);
                return collected;
            }
        }
        return null;
    }

    private static int getFirstSyntheticLocalIndex(MethodNode mn) {
        int idx = ((mn.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        for (Type argType : Type.getArgumentTypes(mn.desc)) {
            idx += argType.getSize();
        }
        return idx;
    }

    private static boolean isStoreOp(int op) {
        return op == Opcodes.ISTORE || op == Opcodes.LSTORE || op == Opcodes.FSTORE || op == Opcodes.DSTORE || op == Opcodes.ASTORE;
    }

    private static boolean isLoadOp(int op) {
        return op == Opcodes.ILOAD || op == Opcodes.LLOAD || op == Opcodes.FLOAD || op == Opcodes.DLOAD || op == Opcodes.ALOAD;
    }

    private static boolean isMovableSingleSlotPush(AbstractInsnNode insn) {
        if (insn == null) return false;
        if (isIntPush(insn)) return true;
        int op = insn.getOpcode();
        if (op == Opcodes.ACONST_NULL || op == Opcodes.FCONST_0 || op == Opcodes.FCONST_1 || op == Opcodes.FCONST_2) return true;
        if (insn instanceof LdcInsnNode ldc) {
            return !(ldc.cst instanceof Long || ldc.cst instanceof Double);
        }
        if (op == Opcodes.ILOAD || op == Opcodes.FLOAD || op == Opcodes.ALOAD) return true;
        if (op == Opcodes.GETSTATIC && insn instanceof FieldInsnNode fi) {
            int size = Type.getType(fi.desc).getSize();
            return size == 1;
        }
        return false;
    }

    private static AbstractInsnNode clonePushInsn(AbstractInsnNode insn) {
        if (insn instanceof InsnNode in) return new InsnNode(in.getOpcode());
        if (insn instanceof IntInsnNode iin) return new IntInsnNode(iin.getOpcode(), iin.operand);
        if (insn instanceof LdcInsnNode ldc) return new LdcInsnNode(ldc.cst);
        if (insn instanceof VarInsnNode vin) return new VarInsnNode(vin.getOpcode(), vin.var);
        if (insn instanceof FieldInsnNode fin) return new FieldInsnNode(fin.getOpcode(), fin.owner, fin.name, fin.desc);
        throw new IllegalArgumentException("Cannot clone " + insn);
    }

    private static void cleanUpStackSpillsAndLinearize(String cnName, MethodNode mn) {
        if (mn.instructions.size() == 0) return;
        int minSynthVar = getFirstSyntheticLocalIndex(mn);

        boolean changed = true;
        int iterGuard = 0;
        while (changed && iterGuard++ < 5000) {
            changed = false;

            // Count reads, writes, iincs per local variable
            Map<Integer, List<VarInsnNode>> loads = new HashMap<>();
            Map<Integer, List<VarInsnNode>> stores = new HashMap<>();
            Set<Integer> iincVars = new HashSet<>();

            for (AbstractInsnNode insn : mn.instructions) {
                if (insn instanceof VarInsnNode vi) {
                    if (isLoadOp(vi.getOpcode())) {
                        loads.computeIfAbsent(vi.var, k -> new ArrayList<>()).add(vi);
                    } else if (isStoreOp(vi.getOpcode())) {
                        stores.computeIfAbsent(vi.var, k -> new ArrayList<>()).add(vi);
                    }
                } else if (insn instanceof IincInsnNode iinc) {
                    iincVars.add(iinc.var);
                }
            }

            // Pass 0: Remove dead store `XSTORE v` when `v >= minSynthVar` has 0 loads (`vLoads.isEmpty()`)
            for (Map.Entry<Integer, List<VarInsnNode>> entry : stores.entrySet()) {
                int v = entry.getKey();
                if (v < minSynthVar || iincVars.contains(v)) continue;
                if (!loads.getOrDefault(v, Collections.emptyList()).isEmpty()) continue;
                // Only replace `XSTORE v` with `POP` / `POP2` if `XSTORE v` is NOT a catch handler's exception `ASTORE` (or even if it isn't, wait: if `prevReal(vs)` is a method call or `checkcast`, replacing `XSTORE v` with `POP` or removing `checkcast; astore v`!)
                for (VarInsnNode vs : entry.getValue()) {
                    AbstractInsnNode prev = prevReal(vs);
                    // Do not touch catch block entry `ASTORE`
                    if (prev == null) continue;
                    boolean isCatchEntry = false;
                    for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                        if (nextReal(tcb.handler) == vs) {
                            isCatchEntry = true;
                            break;
                        }
                    }
                    if (isCatchEntry) continue;

                    // If `prev` is a method call or `ARRAYLENGTH`/`BALOAD`, replace `XSTORE v` with `POP` / `POP2`
                    if (prev instanceof MethodInsnNode) {
                        int popOp = (vs.getOpcode() == Opcodes.LSTORE || vs.getOpcode() == Opcodes.DSTORE) ? Opcodes.POP2 : Opcodes.POP;
                        mn.instructions.set(vs, new InsnNode(popOp));
                        changed = true;
                        break;
                    }
                }
                if (changed) break;
            }
            if (changed) continue;

            // Pass A: Remove adjacent `XSTORE v; XLOAD v` when `v` has 1 store and 1 load
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (!mn.instructions.contains(insn)) continue;
                if (insn instanceof VarInsnNode vs && isStoreOp(vs.getOpcode()) && vs.var >= minSynthVar && !iincVars.contains(vs.var)) {
                    List<VarInsnNode> vLoads = loads.getOrDefault(vs.var, Collections.emptyList());
                    List<VarInsnNode> vStores = stores.getOrDefault(vs.var, Collections.emptyList());
                    if (vStores.size() == 1 && vLoads.size() == 1) {
                        AbstractInsnNode next = nextReal(vs);
                        if (next == vLoads.get(0)) {
                            // Ensure no label or frame or exception boundary between vs and next
                            boolean hasLabelBetween = false;
                            for (AbstractInsnNode cur = vs.getNext(); cur != null && cur != next; cur = cur.getNext()) {
                                if (cur instanceof LabelNode) {
                                    hasLabelBetween = true;
                                    break;
                                }
                            }
                            if (!hasLabelBetween) {
                                mn.instructions.remove(vs);
                                mn.instructions.remove(next);
                                changed = true;
                                break;
                            }
                        }
                    }
                }
            }
            if (changed) continue;

            // Pass B: Propagate single-store movable push (`ALOAD p / ILOAD p / LDC / SIPUSH / GETSTATIC; XSTORE v`) to all `XLOAD v`
            for (Map.Entry<Integer, List<VarInsnNode>> entry : stores.entrySet()) {
                int v = entry.getKey();
                if (v < minSynthVar || iincVars.contains(v)) continue;
                if (entry.getValue().size() != 1) continue;
                VarInsnNode vs = entry.getValue().get(0);
                List<VarInsnNode> vLoads = loads.getOrDefault(v, Collections.emptyList());
                if (vLoads.isEmpty()) continue;

                AbstractInsnNode pushInsn = prevReal(vs);
                if (pushInsn == null || !isMovableSingleSlotPush(pushInsn)) continue;

                // Ensure no label between pushInsn and vs
                boolean labelBetween = false;
                for (AbstractInsnNode c = pushInsn.getNext(); c != null && c != vs; c = c.getNext()) {
                    if (c instanceof LabelNode) { labelBetween = true; break; }
                }
                if (labelBetween) continue;

                // If pushInsn is a local load `XLOAD u`, ensure `u` is NEVER modified (`XSTORE u` or `IINC u`) anywhere in the method, OR `u < minSynthVar` and not modified
                if (pushInsn instanceof VarInsnNode uLoad) {
                    int u = uLoad.var;
                    if (iincVars.contains(u) || stores.containsKey(u)) {
                        continue;
                    }
                }
                // If pushInsn is GETSTATIC, only propagate if vLoads.size() == 1 and in same basic block
                if (pushInsn.getOpcode() == Opcodes.GETSTATIC) {
                    if (vLoads.size() != 1) continue;
                    VarInsnNode vl = vLoads.get(0);
                    boolean safeBlock = true;
                    for (AbstractInsnNode c = vs.getNext(); c != null && c != vl; c = c.getNext()) {
                        if (c instanceof LabelNode || c instanceof JumpInsnNode || c instanceof InvokeDynamicInsnNode
                            || c instanceof LookupSwitchInsnNode || c instanceof TableSwitchInsnNode) {
                            safeBlock = false;
                            break;
                        }
                        if (c instanceof FieldInsnNode fi && (fi.getOpcode() == Opcodes.PUTSTATIC || fi.getOpcode() == Opcodes.PUTFIELD)) {
                            safeBlock = false;
                            break;
                        }
                    }
                    if (!safeBlock) continue;
                }

                for (VarInsnNode vl : vLoads) {
                    mn.instructions.set(vl, clonePushInsn(pushInsn));
                }
                mn.instructions.remove(pushInsn);
                mn.instructions.remove(vs);
                changed = true;
                break;
            }
            // Pass B2: Propagate local-to-local copy `XLOAD u; XSTORE v` -> replace all `XLOAD v` with `XLOAD u` if neither `u` nor `v` is modified between `vs` and any `vl`
            for (Map.Entry<Integer, List<VarInsnNode>> entry : stores.entrySet()) {
                int v = entry.getKey();
                if (v < minSynthVar || iincVars.contains(v)) continue;
                if (entry.getValue().size() != 1) continue;
                VarInsnNode vs = entry.getValue().get(0);
                List<VarInsnNode> vLoads = loads.getOrDefault(v, Collections.emptyList());
                if (vLoads.isEmpty()) continue;

                AbstractInsnNode prev = prevReal(vs);
                if (!(prev instanceof VarInsnNode uLoad && isLoadOp(uLoad.getOpcode()))) continue;
                boolean labelBetween = false;
                for (AbstractInsnNode c = prev.getNext(); c != null && c != vs; c = c.getNext()) {
                    if (c instanceof LabelNode) { labelBetween = true; break; }
                }
                if (labelBetween) continue;

                int u = uLoad.var;
                if (iincVars.contains(u)) continue;
                // Check if `u` is only stored once, BEFORE `uLoad` (and no back-edge can re-execute `u`'s store between `uLoad` and `vLoads`)
                List<VarInsnNode> uStores = stores.getOrDefault(u, Collections.emptyList());
                if (uStores.size() == 1) {
                    VarInsnNode us = uStores.get(0);
                    int usIdx = mn.instructions.indexOf(us);
                    int vsIdx = mn.instructions.indexOf(vs);
                    // If `us` is immediately before `uLoad` (`XSTORE u; XLOAD u; XSTORE v`), or `usIdx < vsIdx` with no backward jumps targeting between `usIdx` and max `vlIdx`
                    boolean adjStore = (prevReal(uLoad) == us);
                    if (adjStore) {
                        for (VarInsnNode vl : vLoads) {
                            mn.instructions.set(vl, new VarInsnNode(uLoad.getOpcode(), u));
                        }
                        mn.instructions.remove(uLoad);
                        mn.instructions.remove(vs);
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) continue;

            // Pass C2: If a single-value expression (such as `INVOKEDYNAMIC`, `INVOKESTATIC`, `INVOKEVIRTUAL`, `INVOKEINTERFACE`, `GETFIELD`, `CHECKCAST`, etc., with no labels/jumps) is stored into a single-use `ASTORE v` (or in `<clinit>`), inline that expression chunk directly at `ALOAD v` PROVIDED the intervening instructions between `vs` and `vl` only evaluate into other local variables without side effects or labels!
            for (Map.Entry<Integer, List<VarInsnNode>> entry : stores.entrySet()) {
                int v = entry.getKey();
                if (v < minSynthVar || iincVars.contains(v)) continue;
                if (entry.getValue().size() != 1) continue;
                List<VarInsnNode> vLoads = loads.getOrDefault(v, Collections.emptyList());
                if (vLoads.size() != 1) continue;
                VarInsnNode vs = entry.getValue().get(0);
                VarInsnNode vl = vLoads.get(0);
                if (vs.getOpcode() != Opcodes.ASTORE) continue;

                // Outside `<clinit>`, only inline if `prevReal(vs)` is an `InvokeDynamicInsnNode` (lambda/method reference) or `MethodInsnNode` returning `Stream`/`Optional`/`Comparator`/`Predicate`/`Function`/`ToDoubleFunction`/`ToIntFunction`
                boolean shouldInline = mn.name.equals("<clinit>");
                if (!shouldInline) {
                    AbstractInsnNode pvs = prevReal(vs);
                    if (pvs instanceof InvokeDynamicInsnNode) {
                        shouldInline = true;
                    } else if (pvs instanceof MethodInsnNode pmi) {
                        String retDesc = Type.getReturnType(pmi.desc).getDescriptor();
                        if (retDesc.startsWith("Ljava/util/stream/") || retDesc.startsWith("Ljava/util/function/")
                            || retDesc.equals("Ljava/util/Comparator;") || retDesc.equals("Ljava/util/Optional;")) {
                            shouldInline = true;
                        }
                    }
                }
                if (!shouldInline) continue;

                int vsIdx = mn.instructions.indexOf(vs);
                int vlIdx = mn.instructions.indexOf(vl);
                if (vsIdx < 0 || vlIdx <= vsIdx || vlIdx - vsIdx > 30) continue;

                // Ensure no labels, jumps, switches, returns, throws, or field/array/method mutations between `vs` and `vl`
                boolean canInlineAcross = mn.name.equals("<clinit>");
                if (!canInlineAcross) {
                    canInlineAcross = true;
                    for (AbstractInsnNode c = vs.getNext(); c != null && c != vl; c = c.getNext()) {
                        if (c instanceof LineNumberNode || c instanceof FrameNode) continue;
                        if (c instanceof LabelNode || c instanceof JumpInsnNode
                            || c instanceof LookupSwitchInsnNode || c instanceof TableSwitchInsnNode) {
                            canInlineAcross = false;
                            break;
                        }
                        int cop = c.getOpcode();
                        if (cop == Opcodes.PUTFIELD || cop == Opcodes.PUTSTATIC
                            || cop == Opcodes.IASTORE || cop == Opcodes.LASTORE || cop == Opcodes.FASTORE
                            || cop == Opcodes.DASTORE || cop == Opcodes.AASTORE || cop == Opcodes.BASTORE
                            || cop == Opcodes.CASTORE || cop == Opcodes.SASTORE
                            || cop == Opcodes.ATHROW || cop == Opcodes.MONITORENTER || cop == Opcodes.MONITOREXIT
                            || (cop >= Opcodes.IRETURN && cop <= Opcodes.RETURN)) {
                            canInlineAcross = false;
                            break;
                        }
                    }
                }
                if (!canInlineAcross) continue;

                // Walk backwards from `vs` using stack height delta until net stack delta is +1 (the expression that produced `vs`)
                List<AbstractInsnNode> expr = extractSingleValueExpressionBefore(mn, vs);
                if (expr != null && !expr.isEmpty()) {
                    // Check that between `vs` and `vl`, the stack height is >= 0 everywhere
                    int curStack = 0;
                    boolean validStackBetween = true;
                    Set<Integer> writtenBetween = new HashSet<>();
                    for (AbstractInsnNode c = vs.getNext(); c != null && c != vl; c = c.getNext()) {
                        if (c instanceof LineNumberNode || c instanceof FrameNode) continue;
                        if (c instanceof VarInsnNode vin && isStoreOp(vin.getOpcode())) {
                            writtenBetween.add(vin.var);
                            int sz = (vin.getOpcode() == Opcodes.LSTORE || vin.getOpcode() == Opcodes.DSTORE) ? 2 : 1;
                            curStack -= sz;
                            if (curStack < 0) {
                                validStackBetween = false;
                                break;
                            }
                        } else if (c instanceof IincInsnNode iinc) {
                            writtenBetween.add(iinc.var);
                        } else {
                            Integer d = singleInsnStackDelta(c);
                            if (d == null) {
                                validStackBetween = false;
                                break;
                            }
                            curStack += d;
                            if (curStack < 0) {
                                validStackBetween = false;
                                break;
                            }
                        }
                    }
                    if (!validStackBetween) continue;

                    boolean conflict = false;
                    for (AbstractInsnNode eNode : expr) {
                        if (eNode instanceof VarInsnNode evin && isLoadOp(evin.getOpcode()) && writtenBetween.contains(evin.var)) {
                            conflict = true;
                            break;
                        }
                    }
                    if (!conflict) {
                        for (AbstractInsnNode eNode : expr) {
                            mn.instructions.remove(eNode);
                            mn.instructions.insertBefore(vl, eNode);
                        }
                        mn.instructions.remove(vs);
                        mn.instructions.remove(vl);
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) continue;
            for (Map.Entry<Integer, List<VarInsnNode>> entry : stores.entrySet()) {
                int v = entry.getKey();
                if (v < minSynthVar || iincVars.contains(v)) continue;
                if (entry.getValue().size() != 1) continue;
                VarInsnNode vs = entry.getValue().get(0);
                if (vs.getOpcode() != Opcodes.ASTORE) continue;
                AbstractInsnNode prev = prevReal(vs);
                if (!(prev instanceof TypeInsnNode newInsn && newInsn.getOpcode() == Opcodes.NEW)) continue;

                boolean labelBetween = false;
                for (AbstractInsnNode c = prev.getNext(); c != null && c != vs; c = c.getNext()) {
                    if (c instanceof LabelNode) { labelBetween = true; break; }
                }
                if (labelBetween) continue;

                List<VarInsnNode> vLoads = loads.getOrDefault(v, Collections.emptyList());
                if (vLoads.isEmpty()) continue;

                VarInsnNode firstLoad = vLoads.get(0);
                // Ensure `firstLoad` is at stack depth 0 relative to `vs` (all instructions between `vs` and `firstLoad` only evaluate arguments into local variables and leave 0 items on the stack, or `vs` is immediately followed by `firstLoad`)
                List<AbstractInsnNode> between = new ArrayList<>();
                boolean noLabelsBetween = true;
                for (AbstractInsnNode c = vs.getNext(); c != null && c != firstLoad; c = c.getNext()) {
                    if (c instanceof LineNumberNode || c instanceof FrameNode) continue;
                    if (c instanceof LabelNode) {
                        noLabelsBetween = false;
                        break;
                    }
                    between.add(c);
                }

                if (vLoads.size() == 1 && noLabelsBetween) {
                    if (between.isEmpty()) {
                        mn.instructions.remove(vs);
                        mn.instructions.remove(firstLoad);
                        changed = true;
                        break;
                    } else if (mn.instructions.indexOf(prev) < mn.instructions.indexOf(firstLoad)) {
                        // Check if `firstLoad` is followed by args + `INVOKESPECIAL newInsn.desc.<init>` (void constructor call without DUP, or DUP was inside)
                        mn.instructions.insertBefore(firstLoad, new TypeInsnNode(Opcodes.NEW, newInsn.desc));
                        mn.instructions.remove(prev);
                        mn.instructions.remove(vs);
                        mn.instructions.remove(firstLoad);
                        changed = true;
                        break;
                    }
                } else if (vLoads.size() == 2) {
                    VarInsnNode secondLoad = vLoads.get(1);
                    AbstractInsnNode beforeSecond = prevReal(secondLoad);
                    MethodInsnNode initCall = null;
                    // Search backwards from `secondLoad` up to 4 instructions for `INVOKESPECIAL newInsn.desc.<init>`
                    AbstractInsnNode scan = prevReal(secondLoad);
                    for (int step = 0; step < 5 && scan != null; step++) {
                        if (scan instanceof MethodInsnNode m && m.getOpcode() == Opcodes.INVOKESPECIAL && m.owner.equals(newInsn.desc) && m.name.equals("<init>")) {
                            initCall = m;
                            break;
                        }
                        scan = prevReal(scan);
                    }
                    if (initCall != null) {
                        boolean canMoveAfterInit = true;
                        List<AbstractInsnNode> afterInitInsns = new ArrayList<>();
                        for (AbstractInsnNode c = initCall.getNext(); c != null && c != secondLoad; c = c.getNext()) {
                            if (c instanceof LineNumberNode || c instanceof FrameNode) continue;
                            if (c instanceof LabelNode) {
                                canMoveAfterInit = false;
                                break;
                            }
                            afterInitInsns.add(c);
                        }
                        // All instructions in `afterInitInsns` must be movable single-slot pushes (like `aload 20` receiver) OR `push + store` pairs!
                        if (canMoveAfterInit && !afterInitInsns.isEmpty()) {
                            for (int k = 0; k < afterInitInsns.size(); k++) {
                                AbstractInsnNode nNode = afterInitInsns.get(k);
                                if (isMovableSingleSlotPush(nNode)) {
                                    // If followed by a store to `otherVar != v`, consume both; otherwise it's a push onto the stack before `secondLoad` (which we can also move before `firstLoad`!)
                                    if (k + 1 < afterInitInsns.size() && afterInitInsns.get(k + 1) instanceof VarInsnNode svn && isStoreOp(svn.getOpcode()) && svn.var != v) {
                                        k++;
                                    }
                                } else {
                                    canMoveAfterInit = false;
                                    break;
                                }
                            }
                        }
                        if (canMoveAfterInit) {
                            if (between.isEmpty() && noLabelsBetween) {
                                // `NEW T; ASTORE v; ALOAD v; ...; INVOKESPECIAL T.<init>; [afterInitInsns]; ALOAD v`
                                for (AbstractInsnNode ai : afterInitInsns) {
                                    mn.instructions.remove(ai);
                                    mn.instructions.insertBefore(prev, ai);
                                }
                                mn.instructions.set(vs, new InsnNode(Opcodes.DUP));
                                mn.instructions.remove(firstLoad);
                                mn.instructions.remove(secondLoad);
                                changed = true;
                                break;
                            } else if (mn.instructions.indexOf(prev) < mn.instructions.indexOf(firstLoad)) {
                                // `NEW T; ASTORE v; [arg evaluation into locals]; ALOAD v; [push args from locals]; INVOKESPECIAL T.<init>; [optional push; store pairs]; ALOAD v`
                                boolean cleanArgPush = true;
                                for (AbstractInsnNode c = firstLoad.getNext(); c != null && c != initCall; c = c.getNext()) {
                                    if (c instanceof LabelNode || c instanceof JumpInsnNode || c instanceof LookupSwitchInsnNode || c instanceof TableSwitchInsnNode) {
                                        cleanArgPush = false;
                                        break;
                                    }
                                }
                                if (cleanArgPush) {
                                    // Also check if the instruction right BEFORE `firstLoad` is a single `XLOAD otherVar` (like `aload 20` receiver for `var20.send(new Pos(...))`) that was pushed only AFTER `prev` (`NEW T; ASTORE v`)!
                                    // Wait: in `gw`, `aload 20` is NOT before `firstLoad` (`aload 23`); `aload 20` is right BEFORE `secondLoad` (`aload 20; aload 23; invokevirtual send`)!
                                    // Why was `aload 20` right before `secondLoad` (`aload 23`)?
                                    // Look at `gw`:
                                    // 397: invokespecial Pos.<init>
                                    // 400: aload 20
                                    // 402: aload 23  <-- `secondLoad`!
                                    // 404: invokevirtual send
                                    // BINGO! Look at lines 397-402 of `gw`:
                                    // `397: invokespecial Pos.<init>`
                                    // `400: aload 20` (`beforeSecond` is `ALOAD 20`, NOT `initCall`!)
                                    // `402: aload 23` (`secondLoad`)!
                                    // Why did `beforeSecond` equal `ALOAD 20` instead of `initCall`?
                                    // Because Pass B propagated `aload 20; astore X; ...; aload X` into `aload 20` right before `aload 23; invokevirtual send`!
                                    // So between `initCall` (`397`) and `secondLoad` (`402`), there is `400: aload 20` (a single movable push instruction on the stack)!
                                    for (AbstractInsnNode ai : afterInitInsns) {
                                        mn.instructions.remove(ai);
                                        mn.instructions.insertBefore(firstLoad, ai);
                                    }
                                    mn.instructions.insertBefore(firstLoad, new TypeInsnNode(Opcodes.NEW, newInsn.desc));
                                    mn.instructions.insertBefore(firstLoad, new InsnNode(Opcodes.DUP));
                                    mn.instructions.remove(prev);
                                    mn.instructions.remove(vs);
                                    mn.instructions.remove(firstLoad);
                                    mn.instructions.remove(secondLoad);
                                    changed = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fix `invokestatic Objects.requireNonNull(Object); astore v` before `invokedynamic typeSwitch` -> `invokestatic Objects.requireNonNull(Object); pop`
        int nextSyntheticVar = mn.maxLocals + 50;
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (insn instanceof VarInsnNode vi && vi.var + 2 >= nextSyntheticVar) {
                nextSyntheticVar = vi.var + 3;
            }
        }
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (insn instanceof InvokeDynamicInsnNode idi && (idi.name.equals("typeSwitch") || idi.name.equals("enumSwitch"))) {
                AbstractInsnNode cur = prevReal(insn);
                for (int step = 0; step < 12 && cur != null; step++) {
                    if (cur instanceof MethodInsnNode mi && mi.owner.equals("java/util/Objects") && mi.name.equals("requireNonNull")) {
                        AbstractInsnNode afterReq = nextReal(cur);
                        if (afterReq instanceof VarInsnNode vs && vs.getOpcode() == Opcodes.ASTORE) {
                            mn.instructions.set(vs, new InsnNode(Opcodes.POP));
                        }
                        break;
                    }
                    cur = prevReal(cur);
                }
                // Also ensure there are NO redundant `ASTORE v; ALOAD v` on the `typeSwitch` selector!
                AbstractInsnNode idxPush = prevReal(insn);
                AbstractInsnNode selLoad = prevReal(idxPush);
                if (selLoad instanceof VarInsnNode sv && sv.getOpcode() == Opcodes.ALOAD) {
                    int selVar = sv.var;
                    // Look backwards for `ALOAD origVar; [Objects.requireNonNull; POP]; [ALOAD origVar; ASTORE selVar]`
                    AbstractInsnNode prev1 = prevReal(selLoad);
                    if (prev1 instanceof VarInsnNode ps && ps.getOpcode() == Opcodes.ASTORE && ps.var == selVar) {
                        AbstractInsnNode origLoad = prevReal(ps);
                        if (origLoad instanceof VarInsnNode ol && ol.getOpcode() == Opcodes.ALOAD) {
                            int origVar = ol.var;
                            for (AbstractInsnNode x : mn.instructions.toArray()) {
                                if (x instanceof VarInsnNode vx && vx.getOpcode() == Opcodes.ALOAD && vx.var == selVar) {
                                    vx.var = origVar;
                                }
                            }
                            mn.instructions.remove(origLoad);
                            mn.instructions.remove(ps);
                            selVar = origVar;
                        }
                    }
                    // Also check if `selVar` itself was just copied from `origVar2` right before `Objects.requireNonNull`:
                    // `ALOAD origVar2; ASTORE selVar; ALOAD selVar; INVOKESTATIC Objects.requireNonNull; POP; ALOAD selVar`
                    AbstractInsnNode pPop = prevReal(selLoad);
                    if (pPop != null && pPop.getOpcode() == Opcodes.POP) {
                        AbstractInsnNode pReq = prevReal(pPop);
                        AbstractInsnNode pLoadReq = prevReal(pReq);
                        AbstractInsnNode pStoreSel = prevReal(pLoadReq);
                        AbstractInsnNode pLoadOrig = prevReal(pStoreSel);
                        if (pLoadReq instanceof VarInsnNode lr && lr.getOpcode() == Opcodes.ALOAD && lr.var == selVar
                            && pStoreSel instanceof VarInsnNode ss && ss.getOpcode() == Opcodes.ASTORE && ss.var == selVar
                            && pLoadOrig instanceof VarInsnNode lo && lo.getOpcode() == Opcodes.ALOAD) {
                            int origVar2 = lo.var;
                            for (AbstractInsnNode x : mn.instructions.toArray()) {
                                if (x instanceof VarInsnNode vx && vx.getOpcode() == Opcodes.ALOAD && vx.var == selVar) {
                                    vx.var = origVar2;
                                }
                            }
                            mn.instructions.remove(pLoadOrig);
                            mn.instructions.remove(pStoreSel);
                        }
                    }
                    // Vineflower's SwitchPatternMatchProcessor requires the 2nd argument of typeSwitch/enumSwitch
                    // to be a VarExprent (ILOAD idxVar) initialized to 0 before the switch, NEVER a constant int push!
                    if (isIntPush(idxPush)) {
                        int idxVar = nextSyntheticVar++;
                        int initVal = getIntVal(idxPush);
                        InsnList initList = new InsnList();
                        initList.add(makeIntPush(initVal));
                        initList.add(new VarInsnNode(Opcodes.ISTORE, idxVar));
                        mn.instructions.insertBefore(selLoad, initList);
                        mn.instructions.set(idxPush, new VarInsnNode(Opcodes.ILOAD, idxVar));
                    }
                }
                AbstractInsnNode next = nextReal(insn);
                if (next instanceof LookupSwitchInsnNode lsi) {
                    for (int k = lsi.keys.size() - 1; k >= 0; k--) {
                        if (lsi.keys.get(k) < 0) {
                            lsi.keys.remove(k);
                            lsi.labels.remove(k);
                        }
                    }
                    // Convert LookupSwitchInsnNode to TableSwitchInsnNode if keys are contiguous 0..K-1!
                    if (!lsi.keys.isEmpty() && lsi.keys.get(0) == 0 && lsi.keys.get(lsi.keys.size() - 1) == lsi.keys.size() - 1) {
                        TableSwitchInsnNode tsi = new TableSwitchInsnNode(0, lsi.keys.size() - 1, lsi.dflt, lsi.labels.toArray(new LabelNode[0]));
                        mn.instructions.set(lsi, tsi);
                    }
                }
            }
        }

        // Normalize spilled String.equals comparisons in String switches:
        // `aload S; astore V1; ldc "lit"; astore V2; aload V1; aload V2; invokevirtual String.equals`
        // -> `aload S; ldc "lit"; invokevirtual String.equals`
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (insn instanceof MethodInsnNode mi && mi.getOpcode() == Opcodes.INVOKEVIRTUAL
                && mi.owner.equals("java/lang/String") && mi.name.equals("equals")
                && mi.desc.equals("(Ljava/lang/Object;)Z")) {
                AbstractInsnNode p1 = prevReal(insn);
                AbstractInsnNode p2 = prevReal(p1);
                AbstractInsnNode p3 = prevReal(p2);
                AbstractInsnNode p4 = prevReal(p3);
                AbstractInsnNode p5 = prevReal(p4);
                AbstractInsnNode p6 = prevReal(p5);
                if (p1 instanceof VarInsnNode vl2 && vl2.getOpcode() == Opcodes.ALOAD
                    && p2 instanceof VarInsnNode vl1 && vl1.getOpcode() == Opcodes.ALOAD
                    && p3 instanceof VarInsnNode vs2 && vs2.getOpcode() == Opcodes.ASTORE && vs2.var == vl2.var
                    && p4 instanceof LdcInsnNode ldc && ldc.cst instanceof String
                    && p5 instanceof VarInsnNode vs1 && vs1.getOpcode() == Opcodes.ASTORE && vs1.var == vl1.var
                    && p6 instanceof VarInsnNode origLoad && origLoad.getOpcode() == Opcodes.ALOAD) {
                    mn.instructions.remove(vs1);
                    mn.instructions.remove(vs2);
                    mn.instructions.remove(vl1);
                    mn.instructions.remove(vl2);
                } else if (p1 instanceof VarInsnNode vl2b && vl2b.getOpcode() == Opcodes.ALOAD
                    && p2 instanceof VarInsnNode vs2b && vs2b.getOpcode() == Opcodes.ASTORE && vs2b.var == vl2b.var
                    && p3 instanceof LdcInsnNode ldcb && ldcb.cst instanceof String) {
                    mn.instructions.remove(vs2b);
                    mn.instructions.remove(vl2b);
                }
            }
        }

        // Normalize spilled enum switchmap initializations (<clinit>) AND enum switch lookups (call sites):
        // `<enumExpr>; invokevirtual Enum.ordinal()I; istore V; getstatic SwitchMap.a:[I; iload V; [iaload / iconst_K; iastore]`
        // -> `getstatic SwitchMap.a:[I; <enumExpr>; invokevirtual Enum.ordinal()I; [iaload / iconst_K; iastore]`
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (insn instanceof MethodInsnNode mi && mi.getOpcode() == Opcodes.INVOKEVIRTUAL
                && mi.name.equals("ordinal") && mi.desc.equals("()I")) {
                AbstractInsnNode nStore = nextReal(insn);
                AbstractInsnNode nArr = nextReal(nStore);
                AbstractInsnNode nLoad = nextReal(nArr);
                if (nStore instanceof VarInsnNode vs && vs.getOpcode() == Opcodes.ISTORE
                    && nArr instanceof FieldInsnNode fa && fa.getOpcode() == Opcodes.GETSTATIC && fa.desc.equals("[I")
                    && nLoad instanceof VarInsnNode vl && vl.getOpcode() == Opcodes.ILOAD && vl.var == vs.var) {
                    List<AbstractInsnNode> expr = extractSingleValueExpressionBefore(mn, vs);
                    if (expr != null && !expr.isEmpty()) {
                        mn.instructions.remove(fa);
                        mn.instructions.insertBefore(expr.get(0), fa);
                        mn.instructions.remove(vs);
                        mn.instructions.remove(vl);
                    }
                }
            }
        }

        // Linearize basic blocks by following unconditional GOTOs and clean up redundant GOTOs/TryCatchBlocks
        linearizeBlocksAndCleanTryCatch(mn);
        removeUnreachableAfterRewrite(mn);
        simplifyJumpsAndGotos(mn);
        linearizeBlocksAndCleanTryCatch(mn);
    }

    private static void linearizeBlocksAndCleanTryCatch(MethodNode mn) {
        simplifyJumpsAndGotos(mn);

        // Repeatedly inline unconditional GOTO targets if the target block has ONLY 1 predecessor (this GOTO)
        // Wait: even more general: if block A ends with `GOTO L`, and `L` is NOT immediately following `GOTO L`,
        // and no instruction falls through into `L` (i.e. instruction before `L` is an unconditional jump/return/athrow),
        // we can move the contiguous chunk starting at `L` (up to the next unconditional jump/return/athrow) right after `GOTO L` and delete `GOTO L`,
        // PROVIDED moving that chunk does not split an active TryCatchBlock range!
        // Wait: why did TryCatchBlock ranges get split in the first place?
        // Look at `be.findClass`:
        // 042: ifnonnull 121
        // 045..056: new ClassNotFoundException ... athrow
        // 057..0c2: try body ... areturn
        // 121: goto 057
        // Look at 121: `121: goto 057`! Why did `042: ifnonnull 121` jump to `121: goto 057`?
        // Because `simplifyJumpsAndGotos` ONLY redirected `GOTO -> GOTO`, NOT `IFxxx -> GOTO`!
        boolean changed = true;
        while (changed) {
            changed = false;
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                if (insn instanceof JumpInsnNode ji && ji.getOpcode() != Opcodes.GOTO) {
                    AbstractInsnNode targetReal = nextReal(ji.label);
                    if (targetReal instanceof JumpInsnNode nextGoto && nextGoto.getOpcode() == Opcodes.GOTO) {
                        if (nextGoto.label != ji.label) {
                            ji.label = nextGoto.label;
                            changed = true;
                        }
                    }
                } else if (insn instanceof LookupSwitchInsnNode lsi) {
                    AbstractInsnNode dReal = nextReal(lsi.dflt);
                    if (dReal instanceof JumpInsnNode dg && dg.getOpcode() == Opcodes.GOTO && dg.label != lsi.dflt) {
                        lsi.dflt = dg.label;
                        changed = true;
                    }
                    for (int i = 0; i < lsi.labels.size(); i++) {
                        LabelNode l = lsi.labels.get(i);
                        AbstractInsnNode lReal = nextReal(l);
                        if (lReal instanceof JumpInsnNode lg && lg.getOpcode() == Opcodes.GOTO && lg.label != l) {
                            lsi.labels.set(i, lg.label);
                            changed = true;
                        }
                    }
                } else if (insn instanceof TableSwitchInsnNode tsi) {
                    AbstractInsnNode dReal = nextReal(tsi.dflt);
                    if (dReal instanceof JumpInsnNode dg && dg.getOpcode() == Opcodes.GOTO && dg.label != tsi.dflt) {
                        tsi.dflt = dg.label;
                        changed = true;
                    }
                    for (int i = 0; i < tsi.labels.size(); i++) {
                        LabelNode l = tsi.labels.get(i);
                        AbstractInsnNode lReal = nextReal(l);
                        if (lReal instanceof JumpInsnNode lg && lg.getOpcode() == Opcodes.GOTO && lg.label != l) {
                            tsi.labels.set(i, lg.label);
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                removeUnreachableAfterRewrite(mn);
                simplifyJumpsAndGotos(mn);
            }
        }

        // Clean up empty or redundant TryCatchBlockNodes
        if (mn.tryCatchBlocks != null && !mn.tryCatchBlocks.isEmpty()) {
            mn.tryCatchBlocks.removeIf(tcb -> {
                int s = mn.instructions.indexOf(tcb.start);
                int e = mn.instructions.indexOf(tcb.end);
                if (s < 0 || e < 0 || s >= e) return true;
                for (int i = s; i < e; i++) {
                    AbstractInsnNode in = mn.instructions.get(i);
                    if (!(in instanceof LabelNode || in instanceof LineNumberNode || in instanceof FrameNode)) {
                        return false;
                    }
                }
                return true;
            });

            // Normalize synchronized monitorexit handlers (`astore X; aload Y; monitorexit; aload X; athrow`) to `type = null` (<any>)
            for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                if ("java/lang/Throwable".equals(tcb.type)) {
                    AbstractInsnNode h0 = nextReal(tcb.handler);
                    AbstractInsnNode h1 = nextReal(h0);
                    AbstractInsnNode h2 = nextReal(h1);
                    AbstractInsnNode h3 = nextReal(h2);
                    AbstractInsnNode h4 = nextReal(h3);
                    if (h0 instanceof VarInsnNode vs && vs.getOpcode() == Opcodes.ASTORE
                        && h1 instanceof VarInsnNode vl && vl.getOpcode() == Opcodes.ALOAD
                        && h2 != null && h2.getOpcode() == Opcodes.MONITOREXIT
                        && h3 instanceof VarInsnNode vl2 && vl2.getOpcode() == Opcodes.ALOAD && vl2.var == vs.var
                        && h4 != null && h4.getOpcode() == Opcodes.ATHROW) {
                        tcb.type = null;
                    }
                }
            }

            boolean merged = true;
            while (merged) {
                merged = false;
                for (int i = 0; i < mn.tryCatchBlocks.size(); i++) {
                    TryCatchBlockNode a = mn.tryCatchBlocks.get(i);
                    for (int j = i + 1; j < mn.tryCatchBlocks.size(); j++) {
                        TryCatchBlockNode b = mn.tryCatchBlocks.get(j);
                        if (a.handler == b.handler && Objects.equals(a.type, b.type)) {
                            int as = mn.instructions.indexOf(a.start);
                            int ae = mn.instructions.indexOf(a.end);
                            int bs = mn.instructions.indexOf(b.start);
                            int be = mn.instructions.indexOf(b.end);
                            AbstractInsnNode hReal = nextReal(a.handler);
                            int hRealIdx = (hReal != null) ? mn.instructions.indexOf(hReal) : mn.instructions.indexOf(a.handler);
                            int minStart = Math.min(as, bs);
                            int maxEnd = Math.max(ae, be);
                            // Never merge two TryCatchBlockNodes if the merged range [minStart, maxEnd) would contain its own handler!
                            if (hRealIdx > minStart && hRealIdx < maxEnd) {
                                continue;
                            }
                            if (nextReal(a.start) == hReal || nextReal(b.start) == hReal) {
                                continue;
                            }
                            // Check if [as, ae] and [bs, be] overlap or are separated only by Labels/LineNumbers/Frames or a small gap without side effects (like a return/throw/astore)
                            if (ae >= bs && be >= as) {
                                a.start = (as <= bs) ? a.start : b.start;
                                a.end = (ae >= be) ? a.end : b.end;
                                mn.tryCatchBlocks.remove(j);
                                merged = true;
                                break;
                            } else if (ae < bs && canMergeTryGap(mn, ae, bs, a.handler)) {
                                a.end = b.end;
                                mn.tryCatchBlocks.remove(j);
                                merged = true;
                                break;
                            } else if (be < as && canMergeTryGap(mn, be, as, a.handler)) {
                                a.start = b.start;
                                mn.tryCatchBlocks.remove(j);
                                merged = true;
                                break;
                            }
                        }
                    }
                    if (merged) break;
                }
            }

            // If a TryCatchBlockNode's end is immediately followed by a GOTO (or load+return) before its handler,
            // where the GOTO jumps back into [tcb.start, tcb.end), extend tcb.end past that GOTO so the loop back-edge stays inside the try block!
            for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
                int s = mn.instructions.indexOf(tcb.start);
                int e = mn.instructions.indexOf(tcb.end);
                int h = mn.instructions.indexOf(tcb.handler);
                AbstractInsnNode next = nextReal(tcb.end);
                if (next instanceof JumpInsnNode ji && ji.getOpcode() == Opcodes.GOTO) {
                    int nIdx = mn.instructions.indexOf(ji);
                    int tIdx = mn.instructions.indexOf(ji.label);
                    if (onlyLabelsBetween(mn, e, nIdx) && (h < e || h > nIdx) && tIdx >= s && tIdx < e) {
                        if (ji.getNext() instanceof LabelNode ln) {
                            tcb.end = ln;
                        } else {
                            LabelNode ln = new LabelNode();
                            mn.instructions.insert(ji, ln);
                            tcb.end = ln;
                        }
                    }
                }
            }
        }
    }

    private static boolean canMergeTryGap(MethodNode mn, int startIdx, int endIdx, LabelNode outerHandler) {
        if (onlyLabelsBetween(mn, startIdx, endIdx)) return true;
        int outerHIdx = mn.instructions.indexOf(outerHandler);
        if (outerHIdx >= startIdx && outerHIdx < endIdx) return false;
        // Check if every instruction in [startIdx, endIdx) is a Label, LineNumber, Frame, pure load/constant, Return, or ASTORE (start of an inner catch handler)
        for (int i = startIdx; i < endIdx; i++) {
            AbstractInsnNode in = mn.instructions.get(i);
            if (in instanceof LabelNode || in instanceof LineNumberNode || in instanceof FrameNode) continue;
            int op = in.getOpcode();
            if (op == Opcodes.ARETURN || op == Opcodes.IRETURN || op == Opcodes.LRETURN
                || op == Opcodes.FRETURN || op == Opcodes.DRETURN || op == Opcodes.RETURN
                || op == Opcodes.ASTORE || op == Opcodes.GOTO
                || isLoadOp(op) || isMovableSingleSlotPush(in)
                || op == Opcodes.LCONST_0 || op == Opcodes.LCONST_1
                || op == Opcodes.DCONST_0 || op == Opcodes.DCONST_1) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static Integer singleInsnStackDelta(AbstractInsnNode cur) {
        int op = cur.getOpcode();
        if (isStoreOp(op) || op == Opcodes.PUTSTATIC || op == Opcodes.PUTFIELD || op == Opcodes.DUP || op == Opcodes.POP) {
            return null;
        }
        if (isMovableSingleSlotPush(cur)) {
            return 1;
        } else if (op == Opcodes.LCONST_0 || op == Opcodes.LCONST_1 || op == Opcodes.DCONST_0 || op == Opcodes.DCONST_1
            || op == Opcodes.LLOAD || op == Opcodes.DLOAD
            || (cur instanceof LdcInsnNode ldc && (ldc.cst instanceof Long || ldc.cst instanceof Double))
            || (cur instanceof FieldInsnNode fi2 && fi2.getOpcode() == Opcodes.GETSTATIC && Type.getType(fi2.desc).getSize() == 2)) {
            return 2;
        } else if (op == Opcodes.CHECKCAST || op == Opcodes.ARRAYLENGTH || op == Opcodes.INEG || op == Opcodes.FNEG || op == Opcodes.I2F || op == Opcodes.F2I
            || (cur instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.GETFIELD && Type.getType(fi.desc).getSize() == 1)) {
            return 0;
        } else if (cur instanceof FieldInsnNode fi && fi.getOpcode() == Opcodes.GETFIELD && Type.getType(fi.desc).getSize() == 2) {
            return 1;
        } else if (op == Opcodes.I2L || op == Opcodes.I2D || op == Opcodes.F2L || op == Opcodes.F2D) {
            return 1;
        } else if (op == Opcodes.L2I || op == Opcodes.L2F || op == Opcodes.D2I || op == Opcodes.D2F
            || op == Opcodes.IADD || op == Opcodes.ISUB || op == Opcodes.IMUL || op == Opcodes.IDIV || op == Opcodes.IREM
            || op == Opcodes.FADD || op == Opcodes.FSUB || op == Opcodes.FMUL || op == Opcodes.FDIV
            || op == Opcodes.ISHL || op == Opcodes.ISHR || op == Opcodes.IUSHR || op == Opcodes.IAND || op == Opcodes.IOR || op == Opcodes.IXOR
            || op == Opcodes.IALOAD || op == Opcodes.FALOAD || op == Opcodes.AALOAD || op == Opcodes.BALOAD || op == Opcodes.CALOAD || op == Opcodes.SALOAD) {
            return -1;
        } else if (op == Opcodes.LADD || op == Opcodes.LSUB || op == Opcodes.LMUL || op == Opcodes.LDIV
            || op == Opcodes.DADD || op == Opcodes.DSUB || op == Opcodes.DMUL || op == Opcodes.DDIV) {
            return -2;
        } else if (op == Opcodes.LCMP || op == Opcodes.DCMPL || op == Opcodes.DCMPG) {
            return -3;
        } else if (cur instanceof InvokeDynamicInsnNode idi) {
            int retSize = Type.getReturnType(idi.desc).getSize();
            if (retSize == 0) return null;
            int argSlots = 0;
            for (Type t : Type.getArgumentTypes(idi.desc)) argSlots += t.getSize();
            return retSize - argSlots;
        } else if (cur instanceof MethodInsnNode mi) {
            if (mi.name.equals("<init>")) return null;
            int retSize = Type.getReturnType(mi.desc).getSize();
            if (retSize == 0) return null;
            int argSlots = (mi.getOpcode() == Opcodes.INVOKESTATIC) ? 0 : 1;
            for (Type t : Type.getArgumentTypes(mi.desc)) argSlots += t.getSize();
            return retSize - argSlots;
        }
        return null;
    }

    private static List<AbstractInsnNode> extractSingleValueExpressionBefore(MethodNode mn, VarInsnNode storeInsn) {
        int needed = (storeInsn.getOpcode() == Opcodes.LSTORE || storeInsn.getOpcode() == Opcodes.DSTORE) ? 2 : 1;
        List<AbstractInsnNode> rev = new ArrayList<>();
        AbstractInsnNode cur = storeInsn.getPrevious();
        while (cur != null && needed > 0) {
            if (cur instanceof LineNumberNode || cur instanceof FrameNode) {
                cur = cur.getPrevious();
                continue;
            }
            if (cur instanceof LabelNode || cur instanceof JumpInsnNode || cur instanceof LookupSwitchInsnNode || cur instanceof TableSwitchInsnNode) {
                return null;
            }
            Integer delta = singleInsnStackDelta(cur);
            if (delta == null) return null;
            rev.add(cur);
            needed -= delta;
            if (needed < 0) return null;
            cur = cur.getPrevious();
        }
        if (needed != 0) return null;
        Collections.reverse(rev);
        return rev;
    }

    private static boolean onlyLabelsBetween(MethodNode mn, int startIdx, int endIdx) {
        for (int i = startIdx; i < endIdx; i++) {
            AbstractInsnNode in = mn.instructions.get(i);
            if (!(in instanceof LabelNode || in instanceof LineNumberNode || in instanceof FrameNode)) {
                return false;
            }
        }
        return true;
    }
}
