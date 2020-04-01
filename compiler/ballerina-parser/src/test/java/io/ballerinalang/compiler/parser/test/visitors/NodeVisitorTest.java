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
package io.ballerinalang.compiler.parser.test.visitors;

import io.ballerinalang.compiler.internal.parser.tree.SyntaxKind;
import io.ballerinalang.compiler.syntax.tree.AssignmentStatement;
import io.ballerinalang.compiler.syntax.tree.SyntaxNodeVisitor;
import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.syntax.tree.Token;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains test cases to test {@code SyntaxNodeVisitor} functionality.
 *
 * @since 1.3.0
 */
public class NodeVisitorTest extends AbstractVisitorTest {

    @Test
    public void testTokenTraversal() {
        SyntaxKind[] expectedKinds = {SyntaxKind.PUBLIC_KEYWORD, SyntaxKind.FUNCTION_KEYWORD,
                SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.OPEN_PAREN_TOKEN, SyntaxKind.CLOSE_PAREN_TOKEN,
                SyntaxKind.OPEN_BRACE_TOKEN, SyntaxKind.SIMPLE_TYPE, SyntaxKind.IDENTIFIER_TOKEN,
                SyntaxKind.EQUAL_TOKEN, SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.PLUS_TOKEN,
                SyntaxKind.IDENTIFIER_TOKEN, SyntaxKind.SEMICOLON_TOKEN, SyntaxKind.CLOSE_BRACE_TOKEN,
                SyntaxKind.EOF_TOKEN};

        SyntaxTree syntaxTree = parseFile("token_traversal.bal");
        TokenVisitor tokenVisitor = new TokenVisitor();
        syntaxTree.getModulePart().accept(tokenVisitor);
        SyntaxKind[] actualKinds = tokenVisitor.tokenList.toArray(new SyntaxKind[0]);

        Assert.assertEquals(actualKinds, expectedKinds);
    }

    @Test
    public void testAssignmentStmtNodeVisit() {
        SyntaxTree syntaxTree = parseFile("assignment_stmt_traversal.bal");
        AssignmentStmtVisitor visitor = new AssignmentStmtVisitor();
        syntaxTree.getModulePart().accept(visitor);
        int actualStmtCount = visitor.stmtList.size();

        Assert.assertEquals(actualStmtCount, 12);
    }

    /**
     * A simple implementation of the {@code SyntaxNodeVisitor} that collects {@code Token} instances.
     *
     * @since 1.3.0
     */
    private static class TokenVisitor extends SyntaxNodeVisitor {
        List<SyntaxKind> tokenList = new ArrayList<>();

        public void visit(Token token) {
            tokenList.add(token.getKind());
        }
    }

    /**
     * A simple implementation of the {@code SyntaxNodeVisitor} that collects {@code AssignmentStatement} nodes.
     *
     * @since 1.3.0
     */
    private static class AssignmentStmtVisitor extends SyntaxNodeVisitor {
        List<AssignmentStatement> stmtList = new ArrayList<>();

        public void visit(AssignmentStatement assignmentStatement) {
            stmtList.add(assignmentStatement);
        }
    }
}
