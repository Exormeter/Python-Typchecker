import antlrImplementations.listeners.DefListener;
import antlrImplementations.listeners.RefClassFuncListener;
import antlrImplementations.listeners.RefTypeCheckListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.antlr.v4.runtime.*;

import gen.*;

class PythonLanguageServer implements LanguageServer, LanguageClientAware {

    public LanguageClient client = null;
    private long lastCall = System.currentTimeMillis();


    @SuppressWarnings("unused")
    private String workspaceRoot = null;
    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        workspaceRoot = params.getRootPath();

        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setCodeActionProvider(false);
        capabilities.setCompletionProvider(new CompletionOptions(true, null));

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
    }

    private FullTextDocumentService fullTextDocumentService = new FullTextDocumentService() {

        @Override
        public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams position) {
            CompletionItem typescriptCompletionItem = new CompletionItem();
            typescriptCompletionItem.setLabel("TypeScript");
            typescriptCompletionItem.setKind(CompletionItemKind.Text);
            typescriptCompletionItem.setData(1.0);

            CompletionItem javascriptCompletionItem = new CompletionItem();
            javascriptCompletionItem.setLabel("JavaScript");
            javascriptCompletionItem.setKind(CompletionItemKind.Text);
            javascriptCompletionItem.setData(2.0);

            List<CompletionItem> completions = new ArrayList<>();
            completions.add(typescriptCompletionItem);
            completions.add(javascriptCompletionItem);

            return CompletableFuture.completedFuture(Either.forRight(new CompletionList(false, completions)));
        }

        @Override
        public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem item) {
            if (item.getData().equals(1.0)) {
                item.setDetail("TypeScript details");
                item.setDocumentation("TypeScript documentation");
            } else if (item.getData().equals(2.0)) {
                item.setDetail("JavaScript details");
                item.setDocumentation("JavaScript documentation");
            }
            return CompletableFuture.completedFuture(item);
        }

        @Override
        public void didSave(DidSaveTextDocumentParams params) {
            TextDocumentItem document = this.documents.get(params.getTextDocument().getUri());
            //validateDocument(document);
            long currentCall = System.currentTimeMillis();
            //
            generateSynataxTree(document);

        }
    };

    @Override
    public TextDocumentService getTextDocumentService() {
        return fullTextDocumentService;
    }

//    private void validateDocument(TextDocumentItem document) {
//        List<Diagnostic> diagnostics = new ArrayList<>();
//        String[] lines = document.getText().split("\\r?\\n");
//        int problems = 0;
//        for (int i = 0; i < lines.length && problems < maxNumberOfProblems; i++) {
//            String line = lines[i];
//            int index = line.indexOf("typescript");
//            if (index >= 0) {
//                problems++;
//                Diagnostic diagnostic = new Diagnostic();
//                diagnostic.setSeverity(DiagnosticSeverity.Warning);
//                diagnostic.setRange(new Range(new Position(i, index), new Position(i, index + 10)));
//                diagnostic.setMessage(String.format("%s should be spelled TypeScript", line.substring(index, index + 10)));
//                diagnostic.setSource("ex");
//                diagnostics.add(diagnostic);
//            }
//        }
//
//        client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
//    }

    private void generateSynataxTree(TextDocumentItem document){
        Lexer lexer = new Python3Lexer(CharStreams.fromString(document.getText()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        ParseTree parseTree = parser.file_input();
        DefListener defineListener = new DefListener();
        ParseTreeWalker.DEFAULT.walk(defineListener, parseTree);
        Python3BaseListener classRefListener = new RefClassFuncListener(defineListener.getSymbolTable());
        ParseTreeWalker.DEFAULT.walk(classRefListener, parseTree);
        RefTypeCheckListener refTypeCheckListener = new RefTypeCheckListener(((RefClassFuncListener) classRefListener).getSymbolTable());
        ParseTreeWalker.DEFAULT.walk(refTypeCheckListener, parseTree);
        client.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), refTypeCheckListener.getDiagnostics()));
    }

    private int maxNumberOfProblems = 100;

    @Override
    public WorkspaceService getWorkspaceService() {

        return new WorkspaceService() {
            @Override
            public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
                return null;
            }

            @Override
            public void didChangeConfiguration(DidChangeConfigurationParams params) {
                Map<String, Object> settings = (Map<String, Object>) params.getSettings();
                Map<String, Object> languageServerExample = (Map<String, Object>) settings.get("languageServerExample");
                maxNumberOfProblems = ((Double)languageServerExample.getOrDefault("maxNumberOfProblems", 100.0)).intValue();

            }

            @Override
            public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
                client.logMessage(new MessageParams(MessageType.Log, "We received an file change event"));
            }
        };
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

}
