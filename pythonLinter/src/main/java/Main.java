import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;

import antlrImplementations.listeners.DefListener;
import antlrImplementations.listeners.RefClassFuncListener;
import antlrImplementations.listeners.RefTypeCheckListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import gen.Python3BaseListener;
import gen.Python3Lexer;
import gen.Python3Parser;


public class Main {
    public static String port = "";

    public static void main(String[] args) throws Exception{

        if(args.length == 0){
            port = "6009";
        }
        else{
            port = args[0];
        }

        try {
            Socket socket = new Socket("localhost", Integer.parseInt(port));

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            PythonLanguageServer server = new PythonLanguageServer();
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            launcher.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//public class Main{
    //public static void main(String[] args) throws Exception{
        /*Lexer lexer = new Python3Lexer(CharStreams.fromFileName("/Users/Nils/IdeaProjects/LanguageServer/pythonLinter/res/testSuite.txt"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        ParseTree parseTree = parser.file_input();
        DefListener defineListener = new DefListener();
        ParseTreeWalker.DEFAULT.walk(defineListener, parseTree);
        Python3BaseListener classRefListener = new RefClassFuncListener(defineListener.getSymbolTable());
        ParseTreeWalker.DEFAULT.walk(classRefListener, parseTree);
        RefTypeCheckListener refTypeCheckListener = new RefTypeCheckListener(((RefClassFuncListener) classRefListener).getSymbolTable());
        ParseTreeWalker.DEFAULT.walk(refTypeCheckListener, parseTree);*/
    //}
//}
