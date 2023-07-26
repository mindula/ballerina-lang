/*
 *  Copyright (c) 2020, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class FunctionCallExpressionNode extends ExpressionNode {

    public FunctionCallExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public NameReferenceNode functionName() {
        return childInBucket(0);
    }

    public Token openParenToken() {
        return childInBucket(1);
    }

    public SeparatedNodeList<FunctionArgumentNode> arguments() {
        return new SeparatedNodeList<>(childInBucket(2));
    }

    public Token closeParenToken() {
        return childInBucket(3);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T> T apply(NodeTransformer<T> visitor) {
        return visitor.transform(this);
    }

    @Override
    protected String[] childNames() {
        return new String[]{
                "functionName",
                "openParenToken",
                "arguments",
                "closeParenToken"};
    }

    public FunctionCallExpressionNode modify(
            NameReferenceNode functionName,
            Token openParenToken,
            SeparatedNodeList<FunctionArgumentNode> arguments,
            Token closeParenToken) {
        if (checkForReferenceEquality(
                functionName,
                openParenToken,
                arguments.underlyingListNode(),
                closeParenToken)) {
            return this;
        }

        return NodeFactory.createFunctionCallExpressionNode(
                functionName,
                openParenToken,
                arguments,
                closeParenToken);
    }

    public FunctionCallExpressionNodeModifier modify() {
        return new FunctionCallExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class FunctionCallExpressionNodeModifier {
        private final FunctionCallExpressionNode oldNode;
        private NameReferenceNode functionName;
        private Token openParenToken;
        private SeparatedNodeList<FunctionArgumentNode> arguments;
        private Token closeParenToken;

        public FunctionCallExpressionNodeModifier(FunctionCallExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.functionName = oldNode.functionName();
            this.openParenToken = oldNode.openParenToken();
            this.arguments = oldNode.arguments();
            this.closeParenToken = oldNode.closeParenToken();
        }

        public FunctionCallExpressionNodeModifier withFunctionName(
                NameReferenceNode functionName) {
            Objects.requireNonNull(functionName, "functionName must not be null");
            this.functionName = functionName;
            return this;
        }

        public FunctionCallExpressionNodeModifier withOpenParenToken(
                Token openParenToken) {
            Objects.requireNonNull(openParenToken, "openParenToken must not be null");
            this.openParenToken = openParenToken;
            return this;
        }

        public FunctionCallExpressionNodeModifier withArguments(
                SeparatedNodeList<FunctionArgumentNode> arguments) {
            Objects.requireNonNull(arguments, "arguments must not be null");
            this.arguments = arguments;
            return this;
        }

        public FunctionCallExpressionNodeModifier withCloseParenToken(
                Token closeParenToken) {
            Objects.requireNonNull(closeParenToken, "closeParenToken must not be null");
            this.closeParenToken = closeParenToken;
            return this;
        }

        public FunctionCallExpressionNode apply() {
            return oldNode.modify(
                    functionName,
                    openParenToken,
                    arguments,
                    closeParenToken);
        }
    }
}
