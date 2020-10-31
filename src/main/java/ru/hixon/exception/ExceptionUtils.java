/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.hixon.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides utilities for manipulating and examining
 * {@code Throwable} objects.</p>
 *
 * https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/exception/ExceptionUtils.java
 *
 * @since 1.0
 */
public class ExceptionUtils {

    private static final int NOT_FOUND = -1;

    public static final Throwable[] EMPTY_THROWABLE_ARRAY = new Throwable[0];

    /**
     * <p>Returns the (zero-based) index of the first {@code Throwable}
     * that matches the specified class (exactly) in the exception chain.
     * Subclasses of the specified class do not match
     *
     * <p>A {@code null} throwable returns {@code -1}.
     * A {@code null} type returns {@code -1}.
     * No match in the chain returns {@code -1}.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param clazz  the class to search for, subclasses do not match, null returns -1
     * @return the index into the throwable chain, -1 if no match or null input
     */
    public static int indexOfThrowable(final Throwable throwable, final Class<? extends Throwable> clazz) {
        return indexOf(throwable, clazz, 0, false);
    }

    /**
     * <p>Worker method for the {@code indexOfType} methods.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @param type  the type to search for, subclasses match, null returns -1
     * @param fromIndex  the (zero-based) index of the starting position,
     *  negative treated as zero, larger than chain size returns -1
     * @param subclass if {@code true}, compares with {@link Class#isAssignableFrom(Class)}, otherwise compares
     * using references
     * @return index of the {@code type} within throwables nested within the specified {@code throwable}
     */
    private static int indexOf(final Throwable throwable, final Class<? extends Throwable> type, int fromIndex, final boolean subclass) {
        if (throwable == null || type == null) {
            return NOT_FOUND;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        final Throwable[] throwables = getThrowables(throwable);
        if (fromIndex >= throwables.length) {
            return NOT_FOUND;
        }
        if (subclass) {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.isAssignableFrom(throwables[i].getClass())) {
                    return i;
                }
            }
        } else {
            for (int i = fromIndex; i < throwables.length; i++) {
                if (type.equals(throwables[i].getClass())) {
                    return i;
                }
            }
        }
        return NOT_FOUND;
    }

    /**
     * <p>Returns the list of {@code Throwable} objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return an array containing
     * one element - the input throwable.
     * A throwable with one cause will return an array containing
     * two elements. - the input throwable and the cause throwable.
     * A {@code null} throwable will return an array of size zero.</p>
     *
     * <p>From version 2.2, this method handles recursive cause structures
     * that might otherwise cause infinite loops. The cause chain is
     * processed until the end is reached, or until the next item in the
     * chain is already in the result set.</p>
     *
     * @see #getThrowableList(Throwable)
     * @param throwable  the throwable to inspect, may be null
     * @return the array of throwables, never null
     */
    public static Throwable[] getThrowables(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.toArray(EMPTY_THROWABLE_ARRAY);
    }

    /**
     * <p>Returns the list of {@code Throwable} objects in the
     * exception chain.</p>
     *
     * <p>A throwable without cause will return a list containing
     * one element - the input throwable.
     * A throwable with one cause will return a list containing
     * two elements. - the input throwable and the cause throwable.
     * A {@code null} throwable will return a list of size zero.</p>
     *
     * <p>This method handles recursive cause structures that might
     * otherwise cause infinite loops. The cause chain is processed until
     * the end is reached, or until the next item in the chain is already
     * in the result set.</p>
     *
     * @param throwable  the throwable to inspect, may be null
     * @return the list of throwables, never null
     * @since 2.2
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

}
