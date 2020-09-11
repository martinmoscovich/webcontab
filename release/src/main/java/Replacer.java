import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public class Replacer {
  private static final Logger log = Logger.getLogger( Replacer.class.getName() );
  
  /** Se espera 2 minutos */
  public static final int KILL_TIMEOUT_SECONDS = 120;
  
  public static final String CURRENT_JAR_KEY = "currentJar";
  public static final String NEW_JAR_KEY = "newJar";
  public static final String CMD_KEY = "cmd";

  public static final int WRONG_NUMBER_OF_ARGS = 1;
  public static final int INVALID_CURRENT_JAR = 2;
  public static final int INVALID_NEW_JAR = 3;
  public static final int INVALID_CMD = 4;
  public static final int UNKNOWN_PARENT_PROCESS = 5;
  public static final int ERROR_PARENT_KILL = 6;
  public static final int ERROR_REPLACE = 7;
  public static final int ERROR_EXECUTING = 8;

  public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
      setupLog();
    
      Map<String, Object> params = parseParameters(args);
      if(params == null) return;
      
      ProcessHandle parent = getParent();
      if(parent == null) return;

      if(!killParent(parent)) return;

      if(!replaceFile((Path)params.get(CURRENT_JAR_KEY), (Path)params.get(NEW_JAR_KEY))) return;
      if(!startProcess(params.get(CMD_KEY).toString())) return;
      
      logInfo("Listo!");
  }
  
  private static Map<String, Object> parseParameters(String[] args) {
    if(args.length != 3) {
      logError("Debe llamarse con exactamente tres parametros [actual, nuevo, cmd]");
      System.exit(WRONG_NUMBER_OF_ARGS);
      return null;
    }
    File currentJar = new File(args[0]);
    File newJar = new File(args[1]);
    String cmd = args[2];
    if(!currentJar.exists() || !currentJar.isFile() || currentJar.length() == 0) {
      logError("El Jar actual no se encontro o no es un archivo");
      System.exit(INVALID_CURRENT_JAR);
      return null;
    }
    if(!newJar.exists() || !newJar.isFile() || newJar.length() == 0) {
      logError("El Jar nuevo no se encontro o no es un archivo");
      System.exit(INVALID_NEW_JAR);
      return null;
    }
    if(cmd == null || cmd.isEmpty()) {
      logError("Debe especificar el comando a ejecutar");
      System.exit(INVALID_CMD);
      return null;
    }
    // if(!backupDir.exists() || !backupDir.isDirectory()) {
    //   System.out.println("El directorio de backup no existe o no es un directorio");
    //   System.exit(INVALID_BACKUP_DIR);
    //   return null;
    // }

    Map<String, Object> params = new HashMap<>();
    params.put("currentJar", currentJar.toPath());
    params.put("newJar", newJar.toPath());
    params.put("cmd", cmd);
    // Parameters p = new Parameters();
    // p.currentJar = currentJar.toPath();
    // p.newJar = newJar.toPath();
    // p.cmd = cmd;
    // p.backupLocation = backupDir.toPath();
    
    return params;
  } 

  private static ProcessHandle getParent() {
    Optional<ProcessHandle> optParent = ProcessHandle.current().parent().or(Replacer::findProcess);
    if(optParent.isEmpty()) {
      logError("No se pudo determinar el proceso principal");
      System.exit(UNKNOWN_PARENT_PROCESS);
      return null;
    }

    return optParent.get();
  }

  private static Optional<ProcessHandle> findProcess() {
    return Optional.empty();
  }

  private static boolean killParent(ProcessHandle parent) {
    try {
      logInfo("Matando al proceso principal");
      if(!parent.destroy()) {
        // logWarn("Error al matar proceso principal, se prueba forzarlo");
        // if(parent.isAlive()) parent.destroyForcibly();
      }
      parent.onExit().get(KILL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch(Exception e) {
      logError("Error al matar proceso principal: " + e.getMessage(), e);
      System.exit(ERROR_PARENT_KILL);
      return false;
    }
    return true;
  }

  private static boolean replaceFile(Path currentJar, Path newJar) {
    try {
      logInfo("Reemplazando el jar " + currentJar.toString() + " por " + newJar.toString());
      Files.delete(currentJar);
      Files.copy(newJar, currentJar);
    } catch(Exception e) {
      logError("Error al reemplazar archivo " + currentJar.toString() + " con " + newJar.toString(), e);
      e.printStackTrace();
      System.exit(ERROR_REPLACE);
      return false;
    }
    return true;
  } 

  private static boolean startProcess(String cmd) {
    try {
      logInfo("Ejecutando " + cmd);
      // Process p = new ProcessBuilder(cmd).inheritIO().start();
      Process p = exec(cmd, Path.of(".").toFile());
      logInfo("Creado nuevo proceso con PID " + p.pid());
      if(!p.isAlive()) throw new RuntimeException("El nuevo proceso no comenzo");
      
    } catch(Exception e) {
      logError("Error al ejecutar " + cmd, e);
      // e.printStackTrace();
      System.exit(ERROR_EXECUTING);
      return false;
    } 
    return true;
  }

  public static Process exec(String command, File cwd) throws IOException, InterruptedException {
    List<String> env = System.getenv().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.toList());
    return Runtime.getRuntime().exec(command, env.toArray(new String[] {}), cwd);
}

  private static void logInfo(String message, Object... params) {
    log.log(Level.INFO, message, params);
  }
  // private static void logDebug(String message, Object... params) {
  //   log.log(Level.FINE, message, params);
  // }
  // private static void logWarn(String message, Object... params) {
  //   log.log(Level.WARNING, message, params);
  // }
  private static void logError(String message, Throwable t) {
    log.log(Level.SEVERE, message, t);
  }
  private static void logError(String message, Object... params) {
    log.log(Level.SEVERE, message, params);
  }
  private static void setupLog() throws IOException {
    FileHandler fileHandler = new FileHandler("Replacer.log", true);
    fileHandler.setFormatter(new SimpleFormatter());
    fileHandler.setLevel(Level.FINE);
    log.addHandler(fileHandler);
  }
}