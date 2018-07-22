import java.io.*;
import java.util.Arrays;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;
import static java.nio.file.StandardOpenOption.*;

class JavaConsole{
    private static final String CLASSNAME = "TEMPCLASS";
    private static final String PREFIX = "class " + CLASSNAME + "{\n"
										+ "\tpublic static void main(String[] args)" 
										+ " throws Exception {\n";
    private static final String SUFFIX = "\t}\n}";

    private PrintStream out;
    private InputStream in;

    private File srcfile;

    private StringBuilder snippetBuffer;
	private StringBuilder importBuffer;

    void run(){
		try(BufferedReader input = new BufferedReader(new InputStreamReader(in))){
			boolean exit = false;
			while(!exit){
				out.print("java-console > ");
				String line = input.readLine();
				if(isCommand(line))
					exit = executeCommand(line);
				else
					snippetBuffer.append(line + ';');
			}
		} catch(IOException e){
	    	System.err.println("no input");
		}
    }

	boolean executeCommand(String commandLine){ // returns true only if the console is to be exited
		String [] tokens = commandLine.trim().split("\\s+");
		String command = tokens[0];
		String [] args = Arrays.copyOfRange(tokens, 1, tokens.length);

		switch(command){
			case "/end" :
				if(compile())
					execute(); // execute only if call to compile() succeed
				snippetBuffer = new StringBuilder();
				break;

			case "/import":
				if(args.length >= 1)
					importBuffer.append("import " + args[0] + ";\n");
				break;

			case "/exit" :
				out.println("Bye, have a good day!");
				return true;
		}
		return false;
	}

	

	boolean isCommand(String s){
		return s.charAt(0) == '/';
	}

    boolean writeToSrcfile(){ // returns true on success, false otherwise
		String imports = importBuffer.toString();
		String snippet = snippetBuffer.toString();
		byte [] filedata = (imports + PREFIX + snippet + SUFFIX).getBytes();
		try{
			Files.write(srcfile.toPath(), filedata, CREATE, TRUNCATE_EXISTING, WRITE);
			return true;
		} catch(IOException e){
			System.err.println("error writing to file: " + e);
			return false;
		}
	}

    boolean compile(){ // returns true if the code has compiled
		if(!writeToSrcfile())
	    	return false;
	int exitValue = 1;
		try{
	    	exitValue = new ProcessBuilder("javac", srcfile.getName())
	        	.inheritIO()
				.start()
				.waitFor();
		} catch(IOException e){
	    	System.err.println("error executing compiler");
		} catch(InterruptedException e) { } // no support for interruption
	    	if(exitValue == 0) // exit value 0 means file compiled successfully
	    		return true;
	    	else
				return false;
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
		snippetBuffer = new StringBuilder();
		importBuffer = new StringBuilder();

		srcfile = new File(CLASSNAME + ".java");
		srcfile.deleteOnExit(); 

		new File(CLASSNAME + ".class").deleteOnExit(); // delete both files in the end
    }

    public static void main(String [] args){
		JavaConsole console = new JavaConsole(System.out, System.in);
		console.run();
    }
}

