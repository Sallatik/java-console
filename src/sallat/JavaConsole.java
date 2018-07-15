package sallat;

import java.io.*;
import static java.util.Objects.requireNonNull;

class JavaConsole{
    private static final String CLASSNAME = "TEMPCLASS";
    private static final String PREFIX = "class " + CLASSNAME + "{\n\tpublic static void main(String[] args){\n";
    private static final String SUFFIX = "\t}\n}";
    private PrintStream out;
    private InputStream in;

    private StringBuilder buffer;

    boolean read(String endOfSnippet){
	try(BufferedReader input = new BufferedReader(new InputStreamReader(in))){
	    String line;
	    while(true){
		out.print('<');
		if(!(line = input.readLine()).equals(endOfSnippet)){
		    buffer.append("\t\t" + line);
		    buffer.append('\n');
		} else
		    return true;
	    }
	} catch(IOException e){
	    System.err.println("no input");
	    return false;
	}
    }

    boolean compile(){
	File srcfile = new File(CLASSNAME + ".java");
	String snippet = buffer.toString();
	try(FileWriter file = new FileWriter(srcfile)){
	    file.write(PREFIX + snippet + SUFFIX);
	    file.close();
	    try{
	        new ProcessBuilder("javac", srcfile.getName())
	            .inheritIO()
		    .start()
		    .waitFor();
	    } catch(InterruptedException e) { } 
	    return true;
	} catch(IOException e){
	    System.err.printf("unable to create file %s in current directory%n", srcfile.getName());
	    return false;
	} finally{
	    srcfile.delete();
	}
    }

    void execute(){
	try{
	    new ProcessBuilder("java", CLASSNAME)
	        .inheritIO()
		.start()
		.waitFor();
	} catch(IOException e){
	    System.err.println("error executing class");
	} catch(InterruptedException e) { 
	} finally{
	    new File(CLASSNAME + ".class").delete();
	}
    }

    JavaConsole(PrintStream out, InputStream in){
	this.out = requireNonNull(out);
	this.in = requireNonNull(in);
	buffer = new StringBuilder();
    }

    public static void main(String [] args){
	JavaConsole console = new JavaConsole(System.out, System.in);
	if(console.read("/end") && console.compile())
	    console.execute();
    }
}

