/*
 * Copyright (c) 2023 Mariusz Bernacki <consulting@didalgo.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.didalgo.intellij.chatgpt.event;

import java.io.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * A ListenerList object can be used to manage a list of event
 * listeners of a particular type. The class provides
 * {@link #addListener(Object)} and {@link #removeListener(Object)} methods for
 * registering listeners, as well as a {@link #fire()} method for firing events
 * to the listeners.
 * </p>
 * 
 * <p>
 * To use this class, suppose you want to support ActionEvents. You would do:
 * </p>
 * 
 * <pre>
 * <code>
 * public class MyActionEventSource
 * {
 *   private ListenerList&lt;ActionListener&gt; actionListeners =
 *       EventListenerList.create(ActionListener.class);
 * 
 *   public void someMethodThatFiresAction()
 *   {
 *     ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "somethingCool");
 *     actionListeners.fire().actionPerformed(e);
 *   }
 * }
 * </code>
 * </pre>
 * 
 * <p>
 * Serializing an {@link ListenerList} instance will result in any non-
 * {@link Serializable} listeners being silently dropped.
 * </p>
 * 
 * @param <L>
 *            the type of event listener that is supported by this proxy.
 * 
 */
public class ListenerList<L> implements InvocationHandler, Serializable {
    
    @Serial
    private static final long serialVersionUID = 3593265990380473632L;
    
    /**
     * The list used to hold the registered listeners. This list is
     * intentionally a thread-safe copy-on-write-array so that traversals over
     * the list of listeners will be atomic.
     */
    private List<L> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * The proxy representing the collection of listeners. Calls to this proxy
     * object will be forwarded to all registered listeners.
     */
    private transient volatile L proxy;
    
    /**
     * Empty typed array for {@link #getListeners()}.
     */
    private transient L[] prototypeArray;
    
    /**
     * Creates an EventListenerSupport object which supports the specified
     * listener type.
     *
     * @param <T> the type of the listener interface
     * @param listenerInterface the type of listener interface that will receive
     *        events posted using this class.
     *
     * @return an EventListenerSupport object which supports the specified
     *         listener type.
     *
     * @throws NullPointerException if <code>listenerInterface</code> is
     *         <code>null</code>.
     * @throws IllegalArgumentException if <code>listenerInterface</code> is
     *         not an interface.
     */
    public static <T> ListenerList<T> of(Class<T> listenerInterface) {
        return new ListenerList<>(listenerInterface);
    }
    
    /**
     * Creates an EventListenerSupport object which supports the provided
     * listener interface.
     * 
     * @param listenerType
     *            the type of listener interface that will receive events posted
     *            using this class.
     * 
     * @throws NullPointerException
     *             if <code>listenerInterface</code> is <code>null</code>
     * @throws IllegalArgumentException
     *             if <code>listenerInterface</code> is not an interface.
     */
    public ListenerList(Class<L> listenerType) {
        if (listenerType == null)
            throw new IllegalArgumentException("Listener interface cannot be null");
        if (!listenerType.isInterface())
            throw new IllegalArgumentException("Class " + listenerType.getName() + " is not an interface");
        
        this.prototypeArray = createPrototypeArray(listenerType);
    }
    
    /**
     * Returns a proxy object which can be used to call listener methods on all
     * of the registered event listeners. All calls made to this proxy will be
     * forwarded to all registered listeners.
     * 
     * @return a proxy object which can be used to call listener methods on all
     *         of the registered event listeners
     */
    public L fire() {
        if (proxy == null)
            proxy = createProxyInstance();
        return proxy;
    }
    
    // **********************************************************************************************************************
    // Other Methods
    // **********************************************************************************************************************
    
    /**
     * Registers an event listener.
     * 
     * @param listener
     *            the event listener (may not be {@code null})
     * @throws NullPointerException
     *             if the {@code listener} is {@code null}
     */
    public Subscription addListener(L listener) {
        listeners.add(Objects.requireNonNull(listener, "Listener object cannot be null."));
        return new Subscription(listeners, listener);
    }

    /**
     * Subscription is a token for referring to added listeners, so they can
     * be {@link Unsubscriber#unsubscribe unsubscribed}.
     */
    public static final class Subscription {

        private final List<?> list;
        private final Object listener;

        Subscription(List<?> list, Object listener) {
            this.list = list;
            this.listener = listener;
        }

        public interface Unsubscriber {
            default void unsubscribe(Subscription token) {
                token.list.remove(token.listener);
            }
        }
    }

    /**
     * Unregisters an event listener.
     * 
     * @param listener
     *            the event listener (may not be {@code null})
     * @throws NullPointerException
     *             if the {@code listener} is {@code null}
     */
    public void removeListener(L listener) {
        listeners.remove(Objects.requireNonNull(listener, "Listener object cannot be null."));
    }
    
    /**
     * Returns the number of registered listeners.
     * 
     * @return the number of registered listeners.
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Tests is this {@code EventListenerSupport} is empty.
     * 
     * @return {@code true} if this object contains no listeners.
     */
    public boolean isEmpty() {
        return listeners.isEmpty();
    }
    
    /**
     * Returns the class representing the listener type of this {@code EventListenerSupport}
     * 
     * @return the listener type
     */
    @SuppressWarnings("unchecked")
    public Class<L> getListenerType() {
        return (Class<L>) prototypeArray.getClass().getComponentType();
    }
    
    /**
     * Get an array containing the currently registered listeners. Modification
     * to this array's elements will have no effect on the
     * {@link ListenerList} instance.
     * 
     * @return L[]
     */
    public L[] getListeners() {
        return listeners.toArray(prototypeArray);
    }
    
    /**
     * Serialize.
     * 
     * @param out
     *            the output stream
     * @throws IOException
     *             if an IO error occurs
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        List<L> serializableListeners = new ArrayList<>();
        
        // don't just rely on instanceof Serializable:
        ObjectOutputStream testOutput = new ObjectOutputStream(new ByteArrayOutputStream());
        for (L listener : listeners) {
            try {
                testOutput.writeObject(listener);
                serializableListeners.add(listener);
            } catch (IOException exception) {
                // recreate test stream in case of indeterminate state
                testOutput = new ObjectOutputStream(new ByteArrayOutputStream());
            }
        }
        /*
         * we can reconstitute everything we need from an array of our
         * listeners, which has the additional advantage of typically requiring
         * less storage than a list:
         */
        out.writeObject(serializableListeners.toArray(prototypeArray));
    }
    
    /**
     * Deserialize.
     * 
     * @param objectInputStream
     *            the input stream
     * @throws IOException
     *             if an IO error occurs
     * @throws ClassNotFoundException
     *             if the class cannot be resolved
     */
    @Serial
    private void readObject(ObjectInputStream objectInputStream)
            throws IOException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        // Will throw CCE here if not correct
        L[] srcListeners = (L[]) objectInputStream.readObject();
        this.listeners = new CopyOnWriteArrayList<>(srcListeners);
        
        @SuppressWarnings("unchecked")
        // Will throw CCE here if not correct
        Class<L> listenerType = (Class<L>) srcListeners.getClass().getComponentType();
        this.prototypeArray = createPrototypeArray(listenerType);
    }
    
    /**
     * Constructs new prototype array of the given type.
     * 
     * @param listenerType
     *            the class of the listener interface
     * @return the new empty prototype array of {@code listenerType} type
     */
    @SuppressWarnings("unchecked")
    protected L[] createPrototypeArray(Class<L> listenerType) {
        // Will throw CCE here if not correct
        L[] array = (L[]) Array.newInstance(listenerType, 0);
        return array;
    }
    
    protected L createProxyInstance() {
        Class<L> listenerType = getListenerType();
        L proxy = listenerType.cast(Proxy.newProxyInstance(listenerType.getClassLoader(),
                new Class[] {listenerType}, createInvocationHandler()));
        return proxy;
    }
    
    protected InvocationHandler createInvocationHandler() {
        return this;
    }
    
    /**
     * Propagates the method call to all registered listeners in place of the proxy
     * listener object.
     * 
     * @param unused
     *            the proxy object representing a listener on which the invocation
     *            was called; not used
     * @param method
     *            the listener method that will be called on all of the listeners.
     * @param args
     *            event arguments to propagate to the listeners.
     * @return always {@code null}
     * @throws IllegalAccessException
     *             if an error occurs during reflective method invocation
     * @throws InvocationTargetException
     *             if an error occurs during reflective method invocation
     */
    @Override
    public Object invoke(Object unused, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        for (L listener : listeners)
            method.invoke(listener, args);
        return null;
    }
    
    /**
     * Propagates the method call to all registered listeners in place of the proxy
     * listener object, intercepting any exceptions that could occur while
     * processing. All catched exceptions (instances of {@code Exception} type) are
     * logged to the application logs and aren't further propagated to the caller.
     * The instances of {@code Error}'s are immediately propagated to the caller and
     * processed in a usual way.
     * 
     * @param unused
     *            the proxy object representing a listener on which the invocation
     *            was called; not used
     * @param method
     *            the listener method that will be called on all of the listeners.
     * @param args
     *            event arguments to propagate to the listeners.
     * @return always {@code null}
     */
    public Object invokeAndCatch(Object unused, Method method, Object[] args) {
        for (L listener : listeners) {
            try {
                method.invoke(listener, args);
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Cannot notify "
                        + method.getDeclaringClass().getTypeName() + '.' + method.getName() + "() listener due to " + e,
                        e);
            }
        }
        return null;
    }
}
