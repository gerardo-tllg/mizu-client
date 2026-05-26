package meteordevelopment.starscript;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javassist.bytecode.Opcode;
import meteordevelopment.starscript.value.Value;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/Script.class */
public class Script {
    private int size;
    public byte[] code = new byte[8];
    public final List<Value> constants = new ArrayList();

    private void write(int b) {
        if (this.size >= this.code.length) {
            byte[] newCode = new byte[this.code.length * 2];
            System.arraycopy(this.code, 0, newCode, 0, this.code.length);
            this.code = newCode;
        }
        byte[] bArr = this.code;
        int i = this.size;
        this.size = i + 1;
        bArr[i] = (byte) b;
    }

    public void write(Instruction insn) {
        write(insn.ordinal());
    }

    public void write(Instruction insn, int b) {
        write(insn.ordinal());
        write(b);
    }

    public void write(Instruction insn, Value constant) {
        write(insn.ordinal());
        writeConstant(constant);
    }

    public void writeConstant(Value constant) {
        int constantI = -1;
        int i = 0;
        while (true) {
            if (i >= this.constants.size()) {
                break;
            }
            if (!this.constants.get(i).equals(constant)) {
                i++;
            } else {
                constantI = i;
                break;
            }
        }
        if (constantI == -1) {
            constantI = this.constants.size();
            this.constants.add(constant);
        }
        write(constantI);
    }

    public int writeJump(Instruction insn) {
        write(insn);
        write(0);
        write(0);
        return this.size - 2;
    }

    public void patchJump(int offset) {
        int jump = (this.size - offset) - 2;
        this.code[offset] = (byte) ((jump >> 8) & 255);
        this.code[offset + 1] = (byte) (jump & 255);
    }

    public void decompile() {
        int i = 0;
        while (i < this.size) {
            Instruction insn = Instruction.valueOf(this.code[i]);
            System.out.format("%3d %-18s", Integer.valueOf(i), insn);
            switch (AnonymousClass1.$SwitchMap$meteordevelopment$starscript$Instruction[insn.ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    i++;
                    System.out.format("%3d '%s'", Byte.valueOf(this.code[i]), this.constants.get(this.code[i]));
                    break;
                case 8:
                case 9:
                    i++;
                    PrintStream printStream = System.out;
                    Object[] objArr = new Object[2];
                    objArr[0] = Byte.valueOf(this.code[i]);
                    objArr[1] = this.code[i] == 1 ? "argument" : "arguments";
                    printStream.format("%3d %s", objArr);
                    break;
                case 10:
                case 11:
                case 12:
                    i += 2;
                    System.out.format("%3d -> %d", Integer.valueOf(i - 2), Integer.valueOf(i + 1 + (((this.code[i - 1] << 8) & 255) | (this.code[i] & 255))));
                    break;
                case Opcode.FCONST_2 /* 13 */:
                    i++;
                    System.out.format("%3d", Byte.valueOf(this.code[i]));
                    break;
                case Opcode.DCONST_0 /* 14 */:
                case 15:
                    i += 2;
                    System.out.format("%3d.%-3d '%s.%s'", Byte.valueOf(this.code[i - 1]), Byte.valueOf(this.code[i]), this.constants.get(this.code[i - 1]), this.constants.get(this.code[i]));
                    break;
            }
            System.out.println();
            i++;
        }
    }

    /* JADX INFO: renamed from: meteordevelopment.starscript.Script$1, reason: invalid class name */
    /* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/starscript/Script$1.class */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$meteordevelopment$starscript$Instruction = new int[Instruction.values().length];

        static {
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.AddConstant.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Variable.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableAppend.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Get.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.GetAppend.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Constant.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.ConstantAppend.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Call.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.CallAppend.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Jump.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.JumpIfTrue.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.JumpIfFalse.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.Section.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableGet.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$meteordevelopment$starscript$Instruction[Instruction.VariableGetAppend.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
        }
    }
}
