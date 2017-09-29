/*
	Start program:
		1. Run the Bootstrap process [ Start --> Run when change is detected --> Termination]
		2. Start the DirectoryWatcher [ Start ---> Termination ] <Thread #1>
		3. Start the HTTP Server [ Start ---> Termination ]	<Thread #2>
*/
public class Runner {
	public static void main(String[] args) throws Exception
	{	
		//Create a process to run the SimpleHTTPServer python program
		final Process httpServer = new WindowsTerminal().executeProcess("python -m SimpleHTTPServer");
		//Attach a shutdown hook to intercept 'CTRL+C' event 
		//and shutdown the above process
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run()
			{
				System.out.print("Shutting down the server...");
				httpServer.destroy();
				System.out.print("Done!");
			}
		});
		//Freeze the current thread until the server is shut down
		httpServer.waitFor();
		
	}
}

------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WindowsTerminal {
	public WindowsTerminal()
	{
		//CTOR
	}
	public Process executeProcess(String cmd) 
	{
		try
		{
			final Process process = Runtime.getRuntime().exec( cmd );
			new Thread( new Runnable(){
				public void run() {
					BufferedReader terminalOutputReader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
					String terminalOutput = null;
					try {
						while( (terminalOutput = terminalOutputReader.readLine()) != null )
						{
							System.out.println( terminalOutput );
						}	
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			return process;
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
	}
}
--------------------------------

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryWatcher {
	
	private Path directoryPath;
	private boolean recursive = false;
	private final WatchService watchService;
	
	public DirectoryWatcher(String directoryPath, boolean recursive) throws IOException
	{
		this.recursive = recursive;
		this.directoryPath = Paths.get("directoryPath");
		
		this.watchService = FileSystems.getDefault().newWatchService();
		
		this.registerDirectory(this.directoryPath, this.recursive);
		
	}
	private void registerDirectory(final Path dir, boolean recursive) throws IOException
	{
		if( recursive )
			registerAll( dir );
		else
			register( dir );
	}
	private void register(Path dir) throws IOException
	{
		WatchKey key = dir.register( this.watchService, StandardWatchEventKinds.ENTRY_CREATE,
														StandardWatchEventKinds.ENTRY_DELETE,
														StandardWatchEventKinds.ENTRY_MODIFY );
		
	}
	private void registerAll( final Path startDir ) throws IOException
	{
		Files.walkFileTree( startDir, new SimpleFileVisitor<Path>(){
			@Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
		});
	}
}
