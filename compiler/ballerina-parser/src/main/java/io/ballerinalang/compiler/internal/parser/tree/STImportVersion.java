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
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;

/**
 * @since 1.3.0
 */
public class STImportVersion extends STNode {

    public final STNode versionKeyword;
    public final STNode semVer;

    STImportVersion(STNode versionKeyword, STNode semVer) {
        super(SyntaxKind.IMPORT_VERSION);
        this.versionKeyword = versionKeyword;
        this.semVer = semVer;

        this.bucketCount = 2;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(versionKeyword, 0);
        this.addChildNode(semVer, 1);
    }

    public NonTerminalNode createFacade(int position, NonTerminalNode parent) {
        return null;
    }
}
