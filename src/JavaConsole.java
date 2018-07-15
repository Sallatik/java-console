import java.io.*;
import static java.util.Objects.requireNonNull;

class JavaConsole{
    private static final String CLASSNAME = "TEMPCLASS";
    private static final String PREFIX = "class " + CLASSNAME + "{\n\tpublic static void main(String[] args){\n";
    private static final String SUFFIX = "\t}\n}";

    private PrintStream out;
    private InputStream in;

    private File srcfile;
    private File classfile;

    private StringBuilder buffer;

    boolean read(){
	try(BufferedReader input = new BufferedReader(new InputStreamReader(in))){
	    String line;
	    while(true){
		out.print('>');
		if(!(line = input.readLine()).equals("/end")){
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
	String snippet = buffer.toString();
	try(FileWriter file = new FileWriter(srcfile)){
	    file.write(PREFIX + snippet + SUFFIX);
	    file.close();
	    int exitValue = 1;
	    try{
		exitValue = new ProcessBuilder("javac", srcfile.getName())
	            .inheritIO()
		    .start()
		    .waitFor();
	    } catch(InterruptedException e) { } 
	    if(exitValue == 0)
	    	return true;
	    else
		return false;
	} catch(IOException e){
	    System.err.printf("unable to create file %s in current directory%n", srcfile.getName());
	    return false;
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

	srcfile = new File(CLASSNAME + ".java");
	classfile = new File(CLASSNAME + ".class");
	srcfile.deleteOnExit();
	classfile.deleteOnExit();
    }

    public static void main(String [] args){
	JavaConsole console = new JavaConsole(System.out, System.in);
	if(console.read() && console.compile())
	    console.execute();
    }
}

