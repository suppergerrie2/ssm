package nl.uu.cs.ssmui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nl.uu.cs.ssm.Utils;

public class CliTestRunner extends CliRunner {

    String expectedOutput;

    public CliTestRunner(File expectedOutput, long steps) {
        super(steps);

        try {
            this.expectedOutput = Utils.readFile(expectedOutput.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        super.run();

        System.out.println("PASS: Program output matched out file!");
    }

    @Override
    public void println(String s) {
        super.println(s);

        if(machine.getMachineState().isHalted() && s.equals("machine halted")) return;

        checkDiff(s);
        checkDiff("\n");
    }

    @Override
    public void print(String s) {
        super.print(s);

        checkDiff(s);
    }

    void checkDiff(String a) {

        int l = a.length();
        for (int i = 0; i < a.length(); i++) {
            if (i >= expectedOutput.length()) {
                if(a.charAt(i) == '\n' && i == a.length()-1) {
                    l--;
                    continue;
                }

                System.out.printf("FAILURE: Expected '%s' but got EOF%n", a.charAt(i));
                System.exit(1);
            }

            if (a.charAt(i) != expectedOutput.charAt(i)) {
                System.out.printf("FAILURE: Expected '%s' but got '%s'%n", a.charAt(i),
                        expectedOutput.charAt(i));
                System.exit(1);
            }
        }

        expectedOutput = expectedOutput.substring(l);
    }
}
