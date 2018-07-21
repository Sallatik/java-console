import java.io.*;
import static java.util.Objects.requireNonNull;

class JavaConsole{
    private static final String CLASSNAME = "TEMPCLASS";
    private static final String PREFIX = "class " + CLASSNAME + "{\n"
																	+ "\tpublic static void main(String[] args)" 
																	+ " throws Exception {\n";
    private static final String SUFFIX = "\t}\n}";

    private PrintStream out;
    private InputStream in;

    private File srcfile;

    private StringBuilder buffer;

    void run(){
		try(BufferedReader input = new BufferedReader(new InputStreamReader(in))){
	    	String line;
mainloop:	while(true){
				out.print("java-console > ");
				line = input.readLine();
				switch(line){
					case "/end" :
						if(writeToSrcfile() && compile())
							execute(); // execute only if previous methods succeed
						buffer = new StringBuilder();
						break;
					case "/import":
						break;

					case "/exit" :

					case "exit" :
						out.println("Bye, have a good day!");
						break mainloop;

					default :
		    			buffer.append("\t\t" + line);
		 				buffer.append('\n');
				}
			}
		} catch(IOException e){
	    	System.err.println("no input");
		}
    }
    
    boolean writeToSrcfile(){ // returns true on success, false otherwise
		String snippet = buffer.toString();
		try(FileWriter file = new FileWriter(srcfile)){
			file.write(PREFIX + snippet + SUFFIX);
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
		buffer = new StringBuilder();

		srcfile = new File(CLASSNAME + ".java");
		srcfile.deleteOnExit();

		new File(CLASSNAME + ".class").deleteOnExit();
    }

    public static void main(String [] args){
		JavaConsole console = new JavaConsole(System.out, System.in);
		console.run();
    }
}

