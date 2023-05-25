/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.util.exceptions;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.MappingInitialValueEntry;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility class for handler error messages.
 */
public class BLangExceptionHelper {
    private static final ResourceBundle messageBundle = ResourceBundle.getBundle("MessagesBundle", Locale.getDefault());
    private static final BString ERROR_MESSAGE_FIELD = StringUtils.fromString("message");

    private BLangExceptionHelper() {}

    public static BError getRuntimeException(RuntimeErrors runtimeErrors, Object... params) {
        BString errorMsg = StringUtils
                .fromString(MessageFormat.format(messageBundle.getString(runtimeErrors.messageKey()), params));
        return ErrorCreator.createError(errorMsg);
    }

    public static BError getRuntimeException(BString reason, RuntimeErrors runtimeErrors, Object... params) {
        MappingInitialValueEntry[] initialValues = new MappingInitialValueEntry[1];
        initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD, StringUtils
                .fromString(MessageFormat.format(messageBundle.getString(runtimeErrors.messageKey()), params)));
        MapValueImpl<BString, Object> errorDetail = new MapValueImpl(PredefinedTypes.TYPE_ERROR_DETAIL, initialValues);
        return ErrorCreator.createError(reason, errorDetail);
    }

    public static BString getErrorMessage(RuntimeErrors runtimeErrors, Object... params) {
        return StringUtils.fromString(MessageFormat
                                              .format(messageBundle.getString(runtimeErrors.messageKey()), params));
    }

    public static BMap<BString, Object> getErrorDetails(RuntimeErrors runtimeErrors, Object... params) {
        MappingInitialValueEntry[] initialValues = new MappingInitialValueEntry[1];
        initialValues[0] = new MappingInitialValueEntry.KeyValueEntry(ERROR_MESSAGE_FIELD,
                StringUtils.fromString(MessageFormat.format(messageBundle.getString(
                        runtimeErrors.messageKey()), params)));
        return new MapValueImpl(PredefinedTypes.TYPE_ERROR_DETAIL, initialValues);
    }

    public static BString getErrorMessage(String messageFormat, Object... params) {
        return StringUtils.fromString(MessageFormat.format(messageBundle.getString(messageFormat), params));
    }

    /**
     * Handle any XML related exception.
     *
     * @param operation Operation that executed
     * @param e Throwable to handle
     */
    public static void handleXMLException(String operation, Throwable e) {
        // here local message of the cause is logged whenever possible, to avoid java class being logged
        // along with the error message.
        String errorDetail;
        if (isBErrorWithMessageDetail(e)) {
            errorDetail = ((BMap<BString, Object>) ((BError) e).getDetails()).get(StringUtils.fromString("message"))
                    .toString();
        } else if (e.getCause() != null) {
            errorDetail = e.getCause().getMessage();
        } else {
            errorDetail = e.getMessage();
        }
        throw ErrorCreator.createError(BallerinaErrorReasons.XML_OPERATION_ERROR,
                StringUtils.fromString("Failed to " + operation + ": " + errorDetail));
    }

    private static boolean isBErrorWithMessageDetail(Throwable e) {
        if (!(e instanceof BError)) {
            return false;
        }
        return hasMessageDetail((BError) e);
    }

    public static boolean hasMessageDetail(BError bError) {
        Object bErrorDetails = bError.getDetails();
        if (bErrorDetails == null) {
            return false;
        }
        return ((BMap<BString, Object>) bErrorDetails).get(StringUtils.fromString("message")) != null;
    }
}
