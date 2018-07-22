import java.io.*;
import static java.util.Objects.requireNonNull;
import java.util.Arrays;

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

	boolean executeCommand(String command){ // returns true only if the console is to be exited
		String [] tokens = command.trim().split("\\s+");
		command = tokens[0];
		String [] args = Arrays.copyOfRange(tokens, 1, tokens.length);

		switch(command){
			case "/end" :
				if(writeToSrcfile() && compile())
					execute(); // execute only if previous method calls succeed
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
		try(FileWriter file = new FileWriter(srcfile)){
			file.write(imports + PREFIX + snippet + SUFFIX);
			return true;
		} catch(IOException e){ 
			System.err.printf("unable to create file %s in current directory%n", srcfile.getName()); 
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

		new File(CLASSNAME + ".class").deleteOnExit();
    }

    public static void main(String [] args){
		JavaConsole console = new JavaConsole(System.out, System.in);
		console.run();
    }
}

