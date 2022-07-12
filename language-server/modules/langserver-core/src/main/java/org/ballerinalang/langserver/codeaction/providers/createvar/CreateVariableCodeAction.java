/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers.createvar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocumentChange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.codeaction.providers.ResolvableCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Code Action for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Create Variable";

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {
        return 999;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {
        return diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED) && 
                CodeActionNodeValidator.validate(context.nodeAtCursor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        List<CodeAction> actions = new ArrayList<>();
        Optional<TypeSymbol> typeSymbol = positionDetails.diagnosticProperty(
                DiagBasedPositionDetails.DIAG_PROP_VAR_ASSIGN_SYMBOL_INDEX);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() == TypeDescKind.NONE) {
            return actions;
        }

        String uri = context.fileUri();
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, typeSymbol.get(),
                context);
        List<String> types = createVarTextEdits.types;
        for (int i = 0; i < types.size(); i++) {
            String commandTitle = CommandConstants.CREATE_VARIABLE_TITLE;
            List<TextEdit> edits = new ArrayList<>();
            edits.add(createVarTextEdits.edits.get(i));
            edits.addAll(createVarTextEdits.imports);
            String type = types.get(i);
            if (createVarTextEdits.types.size() > 1) {
                // When there's multiple types; suffix code actions with `with <type>`
                boolean isTuple = type.startsWith("[") && type.endsWith("]") && !type.endsWith("[]");
                String typeLabel = isTuple && type.length() > 10 ? "Tuple" : type;
                commandTitle = String.format(CommandConstants.CREATE_VARIABLE_TITLE + " with '%s'", typeLabel);
            }

            Map<String, Object> variableData = new HashMap<>();
            variableData.put("textEdits", edits);
            variableData.put("position", context.cursorPosition());

            ResolvableCodeAction.CodeActionData data = new ResolvableCodeAction.CodeActionData(getName(), uri, range,
                    variableData);
//            actions.add(createCodeAction(commandTitle, edits, uri, CodeActionKind.QuickFix));
            actions.add(CodeActionUtil.createCodeAction(commandTitle, CodeActionKind.QuickFix, data));
        }
        return actions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    CreateVariableOut getCreateVariableTextEdits(Range range, DiagBasedPositionDetails positionDetails,
                                                 TypeSymbol typeDescriptor, CodeActionContext context) {
        Symbol matchedSymbol = positionDetails.matchedSymbol();

        Position position = PositionUtil.toPosition(positionDetails.matchedNode().lineRange().startLine());
        Set<String> allNameEntries = context.visibleSymbols(position).stream()
                .filter(s -> s.getName().isPresent())
                .map(s -> s.getName().get())
                .collect(Collectors.toSet());

        String name = NameUtil.generateVariableName(matchedSymbol, typeDescriptor, allNameEntries);

        List<TextEdit> importEdits = new ArrayList<>();
        List<TextEdit> edits = new ArrayList<>();
        List<String> types = CodeActionUtil.getPossibleTypes(typeDescriptor, importEdits, context);
        Position pos = range.getStart();
        for (String type : types) {
            Position insertPos = new Position(pos.getLine(), pos.getCharacter());
            String edit = type + " " + name + " = ";
            edits.add(new TextEdit(new Range(insertPos, insertPos), edit));
        }
        return new CreateVariableOut(name, types, edits, importEdits);
    }

    static class CreateVariableOut {

        String name;
        List<String> types;
        List<TextEdit> edits;
        List<TextEdit> imports;

        public CreateVariableOut(String name, List<String> types, List<TextEdit> edits, List<TextEdit> imports) {
            this.name = name;
            this.types = types;
            this.edits = edits;
            this.imports = imports;
        }
    }

    @Override
    public CodeAction resolve(CodeAction codeAction, CodeActionResolveContext resolveContext) {
        ResolvableCodeAction resolvableCodeAction = ResolvableCodeAction.from(codeAction);
        ResolvableCodeAction.CodeActionData data = resolvableCodeAction.getData();
        Object actionData = data.getActionData();
        CreateVariableData variableData = CreateVariableData.from(actionData);

        Optional<Path> filePath = PathUtil.getPathFromURI(data.getFileUri());
        if (filePath.isEmpty()) {
            return codeAction;
        }
        Optional<SyntaxTree> syntaxTree = resolveContext.workspace().syntaxTree(filePath.get());
        if (syntaxTree.isEmpty()) {
            return codeAction;
        }

        List<TextEdit> textEdits = variableData.getTextEdits();
        io.ballerina.tools.text.TextEdit textEdit = PositionUtil.getTextEdit(syntaxTree.get(), textEdits.get(0));
        int startOffset = textEdit.range().startOffset();
        List<io.ballerina.tools.text.TextEdit> editList = new ArrayList<>();
        editList.add(textEdit);

        if (textEdits.size() > 1) {
            int sum = 0;
            for (int i = 1; i < textEdits.size(); i++) {
                io.ballerina.tools.text.TextEdit edits = PositionUtil.getTextEdit(syntaxTree.get(), textEdits.get(i));
                int offset = edits.range().startOffset();
                if (offset < startOffset) {
                    int length = edits.text().length();
                    sum = sum + length;
                }
                editList.add(edits);
            }
            startOffset = startOffset + sum;
        }

        TextDocumentChange documentChange = TextDocumentChange.from(editList.toArray(
                new io.ballerina.tools.text.TextEdit[]{}));
        TextDocument modifiedDoc = syntaxTree.get().textDocument().apply(documentChange);
        SyntaxTree updatedTree = SyntaxTree.from(modifiedDoc);
        NonTerminalNode node = CommonUtil.findNode(data.getRange(), updatedTree);
        int length = 0;

        TypedBindingPatternNode typedBindingPatternNode = (TypedBindingPatternNode) node;
        length = typedBindingPatternNode.typeDescriptor().textRange().length() + 1;
        int startPos = startOffset + length;

        codeAction.setCommand(new Command("Rename", "ballerina.action.rename",
                List.of(data.getFileUri(), startPos)));

        codeAction.setData(null);
        codeAction.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                new TextDocumentEdit(new VersionedTextDocumentIdentifier(data.getFileUri(), null), textEdits)))));
        return codeAction;
    }

    public static class CreateVariableData {
        private static final Gson GSON = new Gson();
        List<TextEdit> textEdits;
        Position position;

        public CreateVariableData(List<TextEdit> textEdits, Position position) {
            this.textEdits = textEdits;
            this.position = position;
        }

        public List<TextEdit> getTextEdits() {
            return textEdits;
        }

        public Position getPosition() {
            return position;
        }

        public static CreateVariableData from(Object jsonObj) {
            Type gsonType = new TypeToken<HashMap>(){}.getType();
            String toJson = GSON.toJson(jsonObj, gsonType);
            return GSON.fromJson(toJson, CreateVariableData.class);
        }
    }
}
