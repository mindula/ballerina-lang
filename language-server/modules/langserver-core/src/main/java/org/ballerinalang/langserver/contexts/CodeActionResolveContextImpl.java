package org.ballerinalang.langserver.contexts;

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class CodeActionResolveContextImpl extends AbstractDocumentServiceContext implements CodeActionResolveContext {

    CodeActionResolveContextImpl(WorkspaceManager wsManager,
                                 LanguageServerContext serverContext,
                                 CancelChecker cancelChecker) {
        super(wsManager, serverContext, cancelChecker);
    }

    protected static class CodeActionResolveContextBuilder extends AbstractContextBuilder<CodeActionResolveContextBuilder>{

        public CodeActionResolveContextBuilder(LanguageServerContext serverContext) {
            super(LSContextOperation.TXT_CODE_ACTION, serverContext);
        }

        public CodeActionResolveContext build() {
            return new CodeActionResolveContextImpl(
                    this.wsManager,
                    this.serverContext,
                    this.cancelChecker);
        }

        @Override
        public CodeActionResolveContextBuilder self() {
            return this;
        }
    }
}
