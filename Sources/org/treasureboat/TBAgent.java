package org.treasureboat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

import java.lang.reflect.Method;

import java.security.ProtectionDomain;

import java.util.ArrayList;


public class TBAgent implements ClassFileTransformer {

    public static void premain(String agentArgument, Instrumentation instrumentation) {
        System.out.println("TreasureBoat Java Agent!");
        instrumentation.addTransformer(new TBAgent());
    }

    private static byte[] getClassBytes(Class clazz) {

        try {
            String resource = clazz.getName().replaceAll("\\.", "/") + ".class";

            InputStream is = clazz.getClassLoader().getResourceAsStream(resource);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();

        } catch (java.io.IOException ioe) {
            return new byte[] { };
        }
    }

    static void dumpBytes(String label, byte[] bytes) {

        System.out.println(label);

        ArrayList<String> chars = new ArrayList<String>();

        System.out.print("0000: ");

        boolean started = false;

        for (int idx = 0; idx < bytes.length; idx++) {

            int b1 = 0xff & (int)bytes[idx];

            String b2 = Integer.toHexString(b1);

            while (b2.length() < 2) { b2 = "0" + b2; }

            if (b1 >= 0x20 && b1 <= 0xd7) { chars.add(new String(new byte[] { (byte) b1 } )); } else { chars.add(new String("_")); }

            if ((idx % 16) == 0 && chars.size() > 0) {
                System.out.print("  ");
                for (String s : chars) { System.out.print(" " + s); }
                System.out.println("");
                chars.clear();
                String idxStr = ""+idx;
                while (idxStr.length() < 4) { idxStr = "0"+idxStr; }
                System.out.print(idxStr+": ");
            }

            System.out.print(" " + b2);
        }
        System.out.println("");
    }

    public byte[] transform(
             ClassLoader loader,
             String className,
             Class<?> classBeingRedefined,
             ProtectionDomain protectionDomain,
             byte[] classfileBuffer)
             throws java.lang.instrument.IllegalClassFormatException {

        byte[] buffer = null;

        //System.out.println(className);

        if (className.equals("com/webobjects/appserver/_private/WOString")) {

            System.out.println("\n\n\n");
            System.out.println("FOUND the WOString class!");
            System.out.println("loader = "+loader);
            System.out.println("className = \""+className+"\"");
            System.out.println("classBeingRedefined = "+classBeingRedefined);
            System.out.println("protectionDomain = "+protectionDomain);
            System.out.println("\n\n\n");

            dumpBytes("Original ClassfileBuffer:\n", classfileBuffer);

            Class<?> tb = null;

            byte[] tbBytes = null;

            try {
                tb = Class.forName("com.webobjects.appserver._private.TBString");
                tbBytes = getClassBytes(tb);
            } catch (Throwable t) {
                System.err.println("t: "+t+": "+t.getMessage());
                t.printStackTrace();
            }

            System.out.println("tb: "+tb);
            System.out.println("tbBytes length = "+tbBytes.length);
            System.out.println("\n\n\n");
            dumpBytes("Replacement ClassfileBuffer:\n", tbBytes);
            System.out.println("\n\n\n");

            for (int idx = 0; idx < tbBytes.length; idx++) {
                if (tbBytes[idx+0] == (byte)'T' &&
                    tbBytes[idx+1] == (byte)'B' &&
                    tbBytes[idx+2] == (byte)'S' &&
                    tbBytes[idx+3] == (byte)'t' &&
                    tbBytes[idx+4] == (byte)'r' &&
                    tbBytes[idx+5] == (byte)'i' &&
                    tbBytes[idx+6] == (byte)'n' &&
                    tbBytes[idx+7] == (byte)'g') {

                    System.out.println("\n\nFound \"TBString\" at offset = "+idx+", replacing with \"WOString\"\n");

                    tbBytes[idx+0] = (byte)'W';
                    tbBytes[idx+1] = (byte)'O';
                }
            }

            // Obviously we need to do more than this to re-define the class. -rrk
            //
            buffer = tbBytes;
        }

        return buffer;
    }
}
