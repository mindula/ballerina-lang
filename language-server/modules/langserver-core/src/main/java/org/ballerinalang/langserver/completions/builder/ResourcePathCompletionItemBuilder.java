package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.PathRestParam;
import io.ballerina.compiler.api.symbols.resourcepath.PathSegmentList;
import io.ballerina.compiler.api.symbols.resourcepath.ResourcePath;
import io.ballerina.compiler.api.symbols.resourcepath.util.NamedPathSegment;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builder for ClientResourceAccessAction completion items.
 *
 * @since 2201.2.1
 */
public class ResourcePathCompletionItemBuilder {

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSresourceMethodSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(ResourceMethodSymbol functionSymbol, BallerinaCompletionContext context) {
        Pair<String, String> functionSignature = ResourcePathCompletionItemBuilder
                .getResourceAccessSignature(functionSymbol, context);
        CompletionItem item = build(functionSymbol, functionSignature, context);
        item.setFilterText(getFilterTextForResourceMethod(functionSymbol));
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param resourceMethodSymbol resource method symbol.
     * @param segments             path segments.
     * @param context              LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(ResourceMethodSymbol resourceMethodSymbol,
                                       List<PathSegment> segments,
                                       BallerinaCompletionContext context) {
        Pair<String, String> functionSignature = ResourcePathCompletionItemBuilder
                .getResourceAccessSignature(resourceMethodSymbol, context, segments);
        CompletionItem item = build(resourceMethodSymbol, functionSignature, context);
        item.setFilterText(getFilterTextForResourceMethod(resourceMethodSymbol, segments));
        return item;
    }

    private static CompletionItem build(ResourceMethodSymbol resourceMethodSymbol,
                                        Pair<String, String> functionSignature,
                                        BallerinaCompletionContext context) {

        CompletionItem item = new CompletionItem();
        FunctionCompletionItemBuilder.setMeta(item, resourceMethodSymbol, context);
        item.setLabel(functionSignature.getRight());
        item.setInsertText(functionSignature.getLeft());

        //Add additional text edits
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        //Check and replace preceding slash token
        Token token = null;
        if (nodeAtCursor.kind() == SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION) {
            token = ((ClientResourceAccessActionNode) nodeAtCursor).slashToken();
        } else if (nodeAtCursor.kind() == SyntaxKind.LIST) {
            ChildNodeList children = nodeAtCursor.children();
            if (children.size() > 0) {
                int size = children.size();
                Node node = children.get(size - 1);
                if (children.get(size - 1).isMissing() || children.get(size - 1).kind() == SyntaxKind.IDENTIFIER_TOKEN
                        && size >= 2) {
                    node = children.get(size - 2);
                }
                if (node.kind() == SyntaxKind.SLASH_TOKEN) {
                    token = (Token) node;
                }
            }
        }
        if (token != null) {
            TextEdit edit = new TextEdit();
            edit.setNewText("");
            edit.setRange(PositionUtil.toRange(token.lineRange()));
            item.setAdditionalTextEdits(List.of(edit));
        }
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param resourceMethodSymbol resource method symbol.
     * @param context              LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem buildMethodCallExpression(ResourceMethodSymbol resourceMethodSymbol,
                                                 BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        FunctionCompletionItemBuilder.setMeta(item, resourceMethodSymbol, context);
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        addResourceMethodCallSignature(resourceMethodSymbol, context, escapedFunctionName, signature, insertText, 1);
        item.setLabel(signature.toString());
        item.setInsertText(insertText.toString());
        item.setFilterText(resourceMethodSymbol.getName().orElse(""));
        return item;
    }

    /**
     * Get the resource access action signature.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                                   BallerinaCompletionContext ctx) {
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionName.isEmpty()) {
            return ImmutablePair.of(escapedFunctionName + "()", functionName + "()");
        }
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        int placeHolderIndex = addPathSegmentsToSignature(ctx, resourcePath, signature, insertText, 1);

        //functionName considered the resource accessor.
        addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName, signature, insertText,
                placeHolderIndex);
        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    /**
     * Get the resource access action signature.
     *
     * @param resourceMethodSymbol ballerina resource method symbol instance
     * @param ctx                  Language Server Operation context
     * @param segments             path segments.
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getResourceAccessSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                                   BallerinaCompletionContext ctx,
                                                                   List<PathSegment> segments) {
        String functionName = resourceMethodSymbol.getName().orElse("");
        String escapedFunctionName = CommonUtil.escapeEscapeCharsInIdentifier(functionName);
        if (functionName.isEmpty()) {
            return ImmutablePair.of(escapedFunctionName + "()", functionName + "()");
        }
        StringBuilder signature = new StringBuilder();
        StringBuilder insertText = new StringBuilder();
        int placeHolderIndex = 1;
        placeHolderIndex = addPathSegmentsToSignature(ctx, segments, signature, insertText, placeHolderIndex);

        //functionName considered the resource accessor.
        addResourceMethodCallSignature(resourceMethodSymbol, ctx, escapedFunctionName, signature, insertText,
                placeHolderIndex);
        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    private static void addResourceMethodCallSignature(ResourceMethodSymbol resourceMethodSymbol,
                                                       BallerinaCompletionContext ctx,
                                                       String escapedFunctionName, StringBuilder signature,
                                                       StringBuilder insertText, int placeHolderIndex) {
        if (!escapedFunctionName.equals("get")) {
            signature.append(signature.toString().isEmpty() ? "/" : "").append(".").append(escapedFunctionName);
            insertText.append(insertText.toString().isEmpty() ? "/" : "").append(".").append(escapedFunctionName);
        }

        List<String> funcArguments = CommonUtil.getFuncArguments(resourceMethodSymbol, ctx);
        if (!funcArguments.isEmpty()) {
            signature.append("(").append(String.join(", ", funcArguments)).append(")");
            insertText.append("(${" + placeHolderIndex + "})");
        }
    }

    private static int addPathSegmentsToSignature(BallerinaCompletionContext ctx,
                                                  ResourcePath resourcePath,
                                                  StringBuilder signature,
                                                  StringBuilder insertText,
                                                  int placeHolderIndex) {
        if (resourcePath.kind() == ResourcePath.Kind.PATH_SEGMENT_LIST) {
            PathSegmentList pathSegmentList = (PathSegmentList) resourcePath;
            List<PathSegment> pathSegments = pathSegmentList.list();
            for (PathSegment pathSegment : pathSegments) {
                Pair<String, String> resourceAccessPart =
                        getResourceAccessPartForSegment(pathSegment, placeHolderIndex, ctx);
                signature.append("/").append(resourceAccessPart.getLeft());
                insertText.append("/").append(resourceAccessPart.getRight());
                if (pathSegment.pathSegmentKind() != PathSegment.Kind.NAMED_SEGMENT) {
                    placeHolderIndex += 1;
                }
            }
        } else if (resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            PathRestParam pathRestParam = (PathRestParam) resourcePath;
            Pair<String, String> resourceAccessPart =
                    getResourceAccessPartForSegment(pathRestParam.parameter(), placeHolderIndex, ctx);
            signature.append("/").append(resourceAccessPart.getLeft());
            insertText.append("/").append(resourceAccessPart.getRight());
            placeHolderIndex += 1;
        }
        //DOT_RESOURCE_PATH(".") is ignored.
        return placeHolderIndex;
    }

    private static int addPathSegmentsToSignature(BallerinaCompletionContext ctx,
                                                  List<PathSegment> segments,
                                                  StringBuilder signature,
                                                  StringBuilder insertText,
                                                  int placeHolderIndex) {
        for (PathSegment pathSegment : segments) {
            Pair<String, String> resourceAccessPart =
                    getResourceAccessPartForSegment(pathSegment, placeHolderIndex, ctx);
            signature.append("/").append(resourceAccessPart.getLeft());
            insertText.append("/").append(resourceAccessPart.getRight());
            if (pathSegment.pathSegmentKind() != PathSegment.Kind.NAMED_SEGMENT) {
                placeHolderIndex += 1;
            }
        }
        return placeHolderIndex;
    }

    private static Pair<String, String> getResourceAccessPartForSegment(PathSegment segment, int placeHolderIndex,
                                                                        BallerinaCompletionContext context) {
        switch (segment.pathSegmentKind()) {
            case NAMED_SEGMENT:
                String name = ((NamedPathSegment) segment).name();
                return Pair.of(name, name);
            case PATH_PARAMETER:
                PathParameterSymbol pathParameterSymbol = (PathParameterSymbol) segment;
                Optional<String> defaultValue = DefaultValueGenerationUtil
                        .getDefaultValueForType(pathParameterSymbol.typeDescriptor());
                String paramType = FunctionCompletionItemBuilder
                        .getFunctionParameterSyntax(pathParameterSymbol, context).orElse("");
                return Pair.of("[" + paramType + "]", "[${" + placeHolderIndex + ":"
                        + defaultValue.orElse("") + "}]");
            case PATH_REST_PARAMETER:
                PathParameterSymbol pathRestParam = (PathParameterSymbol) segment;
                ArrayTypeSymbol typeSymbol = (ArrayTypeSymbol) pathRestParam.typeDescriptor();
                Optional<String> defaultVal = DefaultValueGenerationUtil
                        .getDefaultValueForType(typeSymbol.memberTypeDescriptor());
                String param = FunctionCompletionItemBuilder.getFunctionParameterSyntax(pathRestParam, context)
                        .orElse("");
                return Pair.of("[" + param + "]",
                        "[${" + placeHolderIndex + ":" + defaultVal.orElse("\"\"") + "}]");
            default:
                //ignore
        }
        return Pair.of("", "");
    }

    private static String getFilterTextForResourceMethod(ResourceMethodSymbol resourceMethodSymbol) {
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        if (resourcePath.kind() == ResourcePath.Kind.DOT_RESOURCE_PATH
                || resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            return resourceMethodSymbol.getName().orElse("");
        }
        List<PathSegment> pathSegmentList = ((PathSegmentList) resourcePath).list();
        return pathSegmentList.stream()
                .filter(pathSegment -> pathSegment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT)
                .map(pathSegment -> ((NamedPathSegment) pathSegment).name()).collect(Collectors.joining("|"))
                + "|" + resourceMethodSymbol.getName().orElse("");
    }

    private static String getFilterTextForResourceMethod(ResourceMethodSymbol resourceMethodSymbol,
                                                         List<PathSegment> segments) {
        ResourcePath resourcePath = resourceMethodSymbol.resourcePath();
        if (resourcePath.kind() == ResourcePath.Kind.DOT_RESOURCE_PATH
                || resourcePath.kind() == ResourcePath.Kind.PATH_REST_PARAM) {
            return resourceMethodSymbol.getName().orElse("");
        }
        return segments.stream()
                .filter(pathSegment -> pathSegment.pathSegmentKind() == PathSegment.Kind.NAMED_SEGMENT)
                .map(pathSegment -> ((NamedPathSegment) pathSegment).name()).collect(Collectors.joining("|"))
                + "|" + resourceMethodSymbol.getName().orElse("");
    }
}
