/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Base reactive classes: Flowable; base reactive consumers;
 * other common base interfaces.
 *
 * <p>A library that enables subscribing to and composing asynchronous events and
 * callbacks.</p>
 * <p>The Flowable/Subscriber interfaces and associated operators (in
 * the {@code io.reactivex.flowable.internal.operators} package) are inspired by the
 * Reactive Rx library in Microsoft .NET but designed and implemented on
 * the more advanced Reactive-Streams ( http://www.reactivestreams.org ) principles.</p>
 * <p>
 * More information can be found at <a
 * href="http://msdn.microsoft.com/en-us/data/gg577609">http://msdn.microsoft.com/en-us/data/gg577609</a>.
 * </p>
 *
 *
 * <p>Compared with the Microsoft implementation:
 * <ul>
 * <li>Disposable == IDisposable (resource/cancellation management)</li>
 * <li>Publisher = IObservable (reactive source)
 * <li>Flowable == IAsyncEnumerable (backpressure)</li>
 * <li>Subscriber == IAsyncEnumerator</li>
 * </ul>
 * <p>Services which intend on exposing data asynchronously and wish
 * to allow reactive processing and composition can implement the
 * {@link io.reactivex.flowable.Flowable} class which then allow consumers to subscribe to it
 * and receive events.</p>
 * <p>Usage examples can be found on the {@link io.reactivex.flowable.Flowable} and {@link org.reactivestreams.Subscriber} classes.</p>
 */
package io.reactivex.flowable;

