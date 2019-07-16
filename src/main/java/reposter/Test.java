package reposter;

import java.io.File;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.println;

class Test {
    public static void main(String[] args) {
        File file = new File("blog.xml");
        long size = file.length();
        double kb = (double) size / 1024;
        println("File size in KB:- " + kb);

    }
}

