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

import java.util.concurrent.atomic.*;

import io.reactivex.common.*;
import io.reactivex.common.disposables.CompositeDisposable;
import io.reactivex.common.exceptions.Exceptions;
import io.reactivex.common.functions.Function;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.common.internal.functions.ObjectHelper;
import io.reactivex.common.internal.utils.AtomicThrowable;
import io.reactivex.observable.*;
import io.reactivex.observable.extensions.FuseToObservable;

/**
 * Maps a sequence of values into CompletableSources and awaits their termination.
 * @param <T> the value type
 */
public final class ObservableFlatMapCompletableCompletable<T> extends Completable implements FuseToObservable<T> {

    final ObservableSource<T> source;

    final Function<? super T, ? extends CompletableSource> mapper;

    final boolean delayErrors;

    public ObservableFlatMapCompletableCompletable(ObservableSource<T> source,
            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
        this.source = source;
        this.mapper = mapper;
        this.delayErrors = delayErrors;
    }

    @Override
    protected void subscribeActual(CompletableObserver observer) {
        source.subscribe(new FlatMapCompletableMainObserver<T>(observer, mapper, delayErrors));
    }

    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaObservablePlugins.onAssembly(new ObservableFlatMapCompletable<T>(source, mapper, delayErrors));
    }

    static final class FlatMapCompletableMainObserver<T> extends AtomicInteger implements Disposable, Observer<T> {
        private static final long serialVersionUID = 8443155186132538303L;

        final CompletableObserver actual;

        final AtomicThrowable errors;

        final Function<? super T, ? extends CompletableSource> mapper;

        final boolean delayErrors;

        final CompositeDisposable set;

        Disposable d;

        volatile boolean disposed;

        FlatMapCompletableMainObserver(CompletableObserver observer, Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {
            this.actual = observer;
            this.mapper = mapper;
            this.delayErrors = delayErrors;
            this.errors = new AtomicThrowable();
            this.set = new CompositeDisposable();
            this.lazySet(1);
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.d, d)) {
                this.d = d;

                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T value) {
            CompletableSource cs;

            try {
                cs = ObjectHelper.requireNonNull(mapper.apply(value), "The mapper returned a null CompletableSource");
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                d.dispose();
                onError(ex);
                return;
            }

            getAndIncrement();

            InnerObserver inner = new InnerObserver();

            if (!disposed && set.add(inner)) {
                cs.subscribe(inner);
            }
        }

        @Override
        public void onError(Throwable e) {
            if (errors.addThrowable(e)) {
                if (delayErrors) {
                    if (decrementAndGet() == 0) {
                        Throwable ex = errors.terminate();
                        actual.onError(ex);
                    }
                } else {
                    dispose();
                    if (getAndSet(0) > 0) {
                        Throwable ex = errors.terminate();
                        actual.onError(ex);
                    }
                }
            } else {
                RxJavaCommonPlugins.onError(e);
            }
        }

        @Override
        public void onComplete() {
            if (decrementAndGet() == 0) {
                Throwable ex = errors.terminate();
                if (ex != null) {
                    actual.onError(ex);
                } else {
                    actual.onComplete();
                }
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            d.dispose();
            set.dispose();
        }

        @Override
        public boolean isDisposed() {
            return d.isDisposed();
        }

        void innerComplete(InnerObserver inner) {
            set.delete(inner);
            onComplete();
        }

        void innerError(InnerObserver inner, Throwable e) {
            set.delete(inner);
            onError(e);
        }

        final class InnerObserver extends AtomicReference<Disposable> implements CompletableObserver, Disposable {
            private static final long serialVersionUID = 8606673141535671828L;

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.setOnce(this, d);
            }

            @Override
            public void onComplete() {
                innerComplete(this);
            }

            @Override
            public void onError(Throwable e) {
                innerError(this, e);
            }

            @Override
            public void dispose() {
                DisposableHelper.dispose(this);
            }

            @Override
            public boolean isDisposed() {
                return DisposableHelper.isDisposed(get());
            }
        }
    }
}
