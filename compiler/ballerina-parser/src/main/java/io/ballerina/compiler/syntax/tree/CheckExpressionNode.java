/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.compiler.syntax.tree;

import io.ballerina.compiler.internal.parser.tree.STNode;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a generated syntax tree node.
 *
 * @since 2.0.0
 */
public class CheckExpressionNode extends ExpressionNode {

    public CheckExpressionNode(STNode internalNode, int position, NonTerminalNode parent) {
        super(internalNode, position, parent);
    }

    public Token checkKeyword() {
        return childInBucket(0);
    }

    public ExpressionNode expression() {
        return childInBucket(1);
    }

    public Optional<OnFailCheckNode> onFailClause() {
        return optionalChildInBucket(2);
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
                "checkKeyword",
                "expression",
                "onFailClause"};
    }

    public CheckExpressionNode modify(
            SyntaxKind kind,
            Token checkKeyword,
            ExpressionNode expression,
            OnFailCheckNode onFailClause) {
        if (checkForReferenceEquality(
                checkKeyword,
                expression,
                onFailClause)) {
            return this;
        }

        return NodeFactory.createCheckExpressionNode(
                kind,
                checkKeyword,
                expression,
                onFailClause);
    }

    public CheckExpressionNodeModifier modify() {
        return new CheckExpressionNodeModifier(this);
    }

    /**
     * This is a generated tree node modifier utility.
     *
     * @since 2.0.0
     */
    public static class CheckExpressionNodeModifier {
        private final CheckExpressionNode oldNode;
        private Token checkKeyword;
        private ExpressionNode expression;
        private OnFailCheckNode onFailClause;

        public CheckExpressionNodeModifier(CheckExpressionNode oldNode) {
            this.oldNode = oldNode;
            this.checkKeyword = oldNode.checkKeyword();
            this.expression = oldNode.expression();
            this.onFailClause = oldNode.onFailClause().orElse(null);
        }

        public CheckExpressionNodeModifier withCheckKeyword(
                Token checkKeyword) {
            Objects.requireNonNull(checkKeyword, "checkKeyword must not be null");
            this.checkKeyword = checkKeyword;
            return this;
        }

        public CheckExpressionNodeModifier withExpression(
                ExpressionNode expression) {
            Objects.requireNonNull(expression, "expression must not be null");
            this.expression = expression;
            return this;
        }

        public CheckExpressionNodeModifier withOnFailClause(
                OnFailCheckNode onFailClause) {
            this.onFailClause = onFailClause;
            return this;
        }

        public CheckExpressionNode apply() {
            return oldNode.modify(
                    oldNode.kind(),
                    checkKeyword,
                    expression,
                    onFailClause);
        }
    }
}
