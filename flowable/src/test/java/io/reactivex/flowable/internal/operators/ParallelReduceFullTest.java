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

package io.reactivex.flowable.internal.operators;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.common.functions.BiFunction;
import io.reactivex.flowable.*;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.flowable.subscribers.TestSubscriber;

public class ParallelReduceFullTest {

    @Test
    public void cancel() {
        PublishProcessor<Integer> pp = PublishProcessor.create();

        TestSubscriber<Integer> ts = pp
        .parallel()
        .reduce(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        })
        .test();

        assertTrue(pp.hasSubscribers());

        ts.cancel();

        assertFalse(pp.hasSubscribers());
    }

    @Test
    public void error() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        try {
            Flowable.<Integer>error(new TestException())
            .parallel()
            .reduce(new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            })
            .test()
            .assertFailure(TestException.class);

            assertTrue(errors.isEmpty());
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void error2() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();

        try {
            ParallelFlowable.fromArray(Flowable.<Integer>error(new IOException()), Flowable.<Integer>error(new TestException()))
            .reduce(new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a + b;
                }
            })
            .test()
            .assertFailure(IOException.class);

            TestCommonHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void empty() {
        Flowable.<Integer>empty()
        .parallel()
        .reduce(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        })
        .test()
        .assertResult();
    }

    @Test
    public void doubleError() {
        List<Throwable> errors = TestCommonHelper.trackPluginErrors();
        try {
            new ParallelInvalid()
            .reduce(new BiFunction<Object, Object, Object>() {
                @Override
                public Object apply(Object a, Object b) throws Exception {
                    return "" + a + b;
                }
            })
            .test()
            .assertFailure(TestException.class);

            assertFalse(errors.isEmpty());
            for (Throwable ex : errors) {
                assertTrue(ex.toString(), ex.getCause() instanceof TestException);
            }
        } finally {
            RxJavaCommonPlugins.reset();
        }
    }

    @Test
    public void reducerCrash() {
        Flowable.range(1, 4)
        .parallel(2)
        .reduce(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                if (b == 3) {
                    throw new TestException();
                }
                return a + b;
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void reducerCrash2() {
        Flowable.range(1, 4)
        .parallel(2)
        .reduce(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                if (a == 1 + 3) {
                    throw new TestException();
                }
                return a + b;
            }
        })
        .test()
        .assertFailure(TestException.class);
    }
}
