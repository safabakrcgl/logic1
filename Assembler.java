import java.io.*;
import java.util.*;

public class Assembler {
    // Opcode mapping
    private static final Map<String, String> OPCODES = new HashMap<>();
    static {
        OPCODES.put("ADD", "000000");
        OPCODES.put("OR", "000001");
        OPCODES.put("NAND", "000010");
        OPCODES.put("SUB", "000011");
        OPCODES.put("SLL", "000100");
        OPCODES.put("ADDI", "000101");
        OPCODES.put("ORI", "000110");
        OPCODES.put("NANDI", "000111");
        OPCODES.put("SUBI", "001000");
        OPCODES.put("SLLI", "001001");
        OPCODES.put("LD", "001010");
        OPCODES.put("ST", "001011");
        OPCODES.put("JUMP", "001100");
        OPCODES.put("BEQ", "001101");
        OPCODES.put("BLT", "001110");
        OPCODES.put("BGT", "001111");
        OPCODES.put("BLE", "010000");
        OPCODES.put("BGE", "010001");
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Assembler <input_file> <output_file>");
            return;
        }

        String inputFileName = args[0];
        String outputFileName = args[1];

        try {
            List<String> outputLines = assemble(inputFileName);
            writeToFile(outputFileName, outputLines);
            System.out.println("Assembly completed. Output written to " + outputFileName);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static List<String> assemble(String inputFileName) throws IOException {
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue; // Skip empty lines and comments
                }
                outputLines.add(translateInstruction(line));
            }
        }
        return outputLines;
    }

    private static String translateInstruction(String line) {
        String[] parts = line.split("\\s+|,");
        String instruction = parts[0].toUpperCase();
        String opcode = OPCODES.get(instruction);

        if (opcode == null) {
            throw new IllegalArgumentException("Unknown instruction: " + instruction);
        }

        switch (instruction) {
            case "ADD":
            case "OR":
            case "NAND":
            case "SUB":
            case "SLL":
                return formatRType(opcode, parts[1], parts[2], parts[3]);
            case "ADDI":
            case "ORI":
            case "NANDI":
            case "SUBI":
            case "SLLI":
                return formatIType(opcode, parts[1], parts[2], parts[3]);
            case "LD":
            case "ST":
                return formatLoadStore(opcode, parts[1], parts[2]);
            case "JUMP":
                return formatJump(opcode, parts[1]);
            case "BEQ":
            case "BLT":
            case "BGT":
            case "BLE":
            case "BGE":
                return formatBranch(opcode, parts[1], parts[2], parts[3]);
            default:
                throw new IllegalArgumentException("Unhandled instruction: " + instruction);
        }
    }

    private static String formatRType(String opcode, String dst, String src1, String src2) {
        return opcode + registerToBinary(dst) + registerToBinary(src1) + registerToBinary(src2) + "00";
    }

    private static String formatIType(String opcode, String dst, String src1, String imm) {
        return opcode + registerToBinary(dst) + registerToBinary(src1) + immediateToBinary(imm, 6);
    }

    private static String formatLoadStore(String opcode, String reg, String addr) {
        return opcode + registerToBinary(reg) + immediateToBinary(addr, 10);
    }

    private static String formatJump(String opcode, String offset) {
        return opcode + immediateToBinary(offset, 14);
    }

    private static String formatBranch(String opcode, String op1, String op2, String offset) {
        return opcode + registerToBinary(op1) + registerToBinary(op2) + immediateToBinary(offset, 6);
    }

    private static String registerToBinary(String reg) {
        int regNumber = Integer.parseInt(reg.replace("R", ""));
        return String.format("%4s", Integer.toBinaryString(regNumber)).replace(' ', '0');
    }

    private static String immediateToBinary(String value, int bits) {
        int imm = Integer.parseInt(value);
        if (imm < 0) {
            imm = (1 << bits) + imm; // Handle negative values
        }
        return String.format("%" + bits + "s", Integer.toBinaryString(imm)).replace(' ', '0');
    }

    private static void writeToFile(String fileName, List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                writer.write(binaryToHex(line));
                writer.newLine();
            }
        }
    }

    private static String binaryToHex(String binary) {
        int value = Integer.parseInt(binary, 2);
        return String.format("%05X", value);
    }
}
