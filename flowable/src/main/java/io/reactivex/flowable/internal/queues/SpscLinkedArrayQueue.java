/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.flowable.internal.queues;

import io.reactivex.common.internal.queues.AbstractSpscLinkedArrayQueue;

/*
 * The code was inspired by the similarly named JCTools class:
 * https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/queues/atomic
 */

/**
 * A single-producer single-consumer array-backed queue which can allocate new arrays in case the consumer is slower
 * than the producer.
 * @param <T> the contained value type
 */
public final class SpscLinkedArrayQueue<T> extends AbstractSpscLinkedArrayQueue<T>
implements SimplePlainQueue<T> {

    public SpscLinkedArrayQueue(final int bufferSize) {
        super(bufferSize);
    }

}
