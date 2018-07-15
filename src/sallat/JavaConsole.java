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

    void read(String endOfSnippet){
	try(BufferedReader input = new BufferedReader(new InputStreamReader(in))){
	    String line;
	    while(true){
		out.print('<');
		if(!(line = input.readLine()).equals(endOfSnippet)){
		    buffer.append("\t\t" + line);
		    buffer.append('\n');
		} else
		    break;
	    }
	} catch(IOException e){
	    System.err.println("no input");
	}
    }
	
    void compile(){
	String filename = CLASSNAME + ".java";
	String snippet = buffer.toString();
	try(FileWriter file = new FileWriter(filename)){
	    file.write(PREFIX + snippet + SUFFIX);
	    file.close();
	    try{
	        new ProcessBuilder("javac", filename)
	            .inheritIO()
		    .start()
		    .waitFor();
	    } catch(InterruptedException e) { } 
	} catch(IOException e){
	    System.err.printf("unable to create file %s in current directory%n", filename);
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
	} catch(InterruptedException e) { }
    }

    JavaConsole(PrintStream out, InputStream in){
	this.out = requireNonNull(out);
	this.in = requireNonNull(in);
	buffer = new StringBuilder();
    }

    public static void main(String [] args){
	JavaConsole console = new JavaConsole(System.out, System.in);
	console.read("/end");
	console.compile();
	console.execute();
    }
}

