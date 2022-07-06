package org.ballerinalang.langserver.codeaction.providers;

import com.google.gson.Gson;
import org.ballerinalang.langserver.codeaction.providers.createvar.CreateVariableCodeAction;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;

public class ResolvableCodeAction extends CodeAction {

    private static final Gson GSON = new Gson();

    public ResolvableCodeAction() {
    }

    public ResolvableCodeAction(String title) {
        super(title);
    }

    @Override
    public CodeActionData getData() {
        return (CodeActionData) super.getData();
    }

    public void setData(CodeActionData codeActionData) {
        super.setData(codeActionData);
    }

    public static ResolvableCodeAction from(CodeAction jsonObj) {
        ResolvableCodeAction resolvableCodeAction = new ResolvableCodeAction();
        resolvableCodeAction.setTitle(jsonObj.getTitle());
        resolvableCodeAction.setKind(jsonObj.getKind());
        resolvableCodeAction.setCommand(jsonObj.getCommand());
        String toJson = GSON.toJson(jsonObj.getData());
        CodeActionData codeActionData = GSON.fromJson(toJson, CodeActionData.class);
        resolvableCodeAction.setData(codeActionData);
        return resolvableCodeAction;
    }

    public static class CodeActionData {
        String extName;
        String codeActionName;
        String fileUri;
        Range range;
        Object actionData;

        public CodeActionData(String codeActionName, String fileUri, Range range, Object actionData) {
            this.codeActionName = codeActionName;
            this.fileUri = fileUri;
            this.range = range;
            this.actionData = actionData;
        }

        public String getExtName() {
            return extName;
        }

        public void setExtName(String extName) {
            this.extName = extName;
        }

        public String getCodeActionName() {
            return codeActionName;
        }

        public void setCodeActionName(String codeActionName) {
            this.codeActionName = codeActionName;
        }

        public Object getActionData() {
            return actionData;
        }

        public void setActionData(Object actionData) {
            this.actionData = actionData;
        }

        public String getFileUri() {
            return fileUri;
        }

        public void setFileUri(String fileUri) {
            this.fileUri = fileUri;
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
