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

package io.reactivex.observable.internal.operators;

import org.junit.Test;

import io.reactivex.common.*;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;
import io.reactivex.observable.subjects.PublishSubject;

public class SingleCacheTest {

    @Test
    public void cancelImmediately() {
        PublishSubject<Integer> pp = PublishSubject.create();

        Single<Integer> cached = pp.single(-99).cache();

        TestObserver<Integer> ts = cached.test(true);

        pp.onNext(1);
        pp.onComplete();

        ts.assertEmpty();

        cached.test().assertResult(1);
    }

    @Test
    public void addRemoveRace() {
        for (int i = 0; i < 500; i++) {
            PublishSubject<Integer> pp = PublishSubject.create();

            final Single<Integer> cached = pp.single(-99).cache();

            final TestObserver<Integer> ts1 = cached.test();

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    ts1.cancel();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    cached.test();
                }
            };

            TestCommonHelper.race(r1, r2, Schedulers.single());
        }
    }

    @Test
    public void doubleDispose() {
        PublishSubject<Integer> pp = PublishSubject.create();

        final Single<Integer> cached = pp.single(-99).cache();

        SingleObserver<Integer> doubleDisposer = new SingleObserver<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                d.dispose();
                d.dispose();
            }

            @Override
            public void onSuccess(Integer value) {

            }

            @Override
            public void onError(Throwable e) {

            }
        };
        cached.subscribe(doubleDisposer);

        cached.test();

        cached.subscribe(doubleDisposer);
    }
}
