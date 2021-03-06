/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.drools.spi.AgendaFilter;
import org.drools.spi.AsyncExceptionHandler;
import org.drools.spi.GlobalResolver;
import org.kie.runtime.Environment;
import org.kie.runtime.process.ProcessInstance;
import org.kie.runtime.process.WorkItemManager;
import org.kie.time.SessionClock;

/**
 * A knowledge session for a <code>RuleBase</code>.
 *
 * While this object can be serialised out, it cannot be serialised in. This is because
 * the RuleBase reference is transient. Please see the RuleBase interface for serializing
 * in WorkingMemories from an InputStream.
 */
public interface WorkingMemory extends WorkingMemoryEventManager, WorkingMemoryEntryPoint {

    /**
     * Returns the Agenda for this WorkingMemory. While the WorkingMemory interface is considered public, the Agenda interface
     * is more subject to change.
     * @return
     *         the Agenda
     */
    public Agenda getAgenda();

    /**
     * Set a specific instance as a global in this working memory. Null values will return doing nothing.
     * The global identifier and its type must be declared in the drl.
     *
     * @param identifier
     *            the identifier under which to populate the data
     * @param value
     *            the global value, cannot be null
     */
    void setGlobal(String identifier,
                   Object value);

    /**
     * Retrieve a specific instance of global data by identifier
     *
     * @return application data or null if nothing is set under this identifier
     */
    Object getGlobal(String identifier);

    Environment getEnvironment();

    /**
     * Sets the GlobalResolver instance to be used when resolving globals, replaces the current GlobalResolver.
     * Typcicaly a delegating GlobalResolver is created that first gets a reference to the current GlobalResolver,
     * for delegating
     *
     * @param globalResolver
     */
    void setGlobalResolver(GlobalResolver globalResolver);

    /**
     * Returns the current GlobalResolver
     *
     * @return
     */
    GlobalResolver getGlobalResolver();

    /**
     * Retrieve the <code>RuleBase</code> for this working memory.
     *
     * @return The <code>RuleBase</code>.
     */
    RuleBase getRuleBase();

    /**
     * Fire all items on the agenda until empty.
     *
     * @throws FactException
     *             If a RuntimeException error occurs.
     */
    int fireAllRules() throws FactException;

    /**
     * Fire all items on the agenda until empty, using the given AgendaFiler
     *
     * @throws FactException
     *             If a RuntimeException error occurs.
     */
    int fireAllRules(AgendaFilter agendaFilter) throws FactException;

    /**
     * Fire all items on the agenda until empty or at most 'fireLimit' rules have fired
     *
     * @throws FactException
     *             If a RuntimeException error occurs.
     */
    int fireAllRules( int fireLimit ) throws FactException;

    /**
     * Fire all items on the agenda using the given AgendaFiler
     * until empty or at most 'fireLimit' rules have fired
     *
     * @throws FactException
     *             If a RuntimeException error occurs.
     */
    int fireAllRules(final AgendaFilter agendaFilter, int fireLimit ) throws FactException;

    /**
     * Retrieve the object associated with a <code>FactHandle</code>.
     *
     *
     * @param handle
     *            The fact handle.
     *
     * @return The associated object.
     */
    Object getObject(org.kie.runtime.rule.FactHandle handle);

    /**
     * Retrieve the <code>FactHandle</code> associated with an Object.
     *
     * @param object
     *            The object.
     *
     * @return The associated fact handle.
     */
    FactHandle getFactHandle(Object object);

    FactHandle getFactHandleByIdentity(final Object object);

    /**
     * Returns an Iterator for the Objects in the Working Memory. This Iterator is not thread safe.
     * This means that any working memory actions during iteration may invalidate the iterator.
     * @return
     *     the Iterator
     */
    Iterator<?> iterateObjects();

    /**
     *  Returns an Iterator for the Objects in the Working Memory. This Iterator will filter out
     *  any objects that the ObjectFilter does not accept. This Iterator is not thread safe.
     * This means that any working memory actions during iteration may invalidate the iterator.
     *
     * @param filter
     *
     * @return
     *     the Iterator
     */
    Iterator<?> iterateObjects(org.kie.runtime.ObjectFilter filter);

    /**
     * Returns an Iterator for the FactHandles in the Working Memory. This Iterator is not thread safe.
     * This means that any working memory actions during iteration may invalidate the iterator.
     * @return
     *     the Iterator
     */
    Iterator<?> iterateFactHandles();

    /**
     *  Returns an Iterator for the Objects in the Working Memory. This Iterator will filter out
     *  any objects that the ObjectFilter does not accept. This Iterator is not thread safe.
     * This means that any working memory actions during iteration may invalidate the iterator.
     *
     * @param filter
     *
     * @return
     *     the Iterator
     */
    Iterator<?> iterateFactHandles(org.kie.runtime.ObjectFilter filter);

    /**
     * Set the focus to the specified AgendaGroup
     * @param focus
     */
    void setFocus(String focus);

    /**
     * Retrieve the QueryResults of the specified query and arguments
     *
     * @param query
     *            The name of the query.
     *
     * @param arguments
     *            The arguments used for the query
     *
     * @return The QueryResults of the specified query.
     *         If no results match the query it is empty.
     *
     * @throws IllegalArgumentException
     *         if no query named "query" is found in the rulebase
     */
    public QueryResults getQueryResults(String query, Object... arguments);

    /**
     * Sets the AsyncExceptionHandler to handle exceptions thrown by the Agenda
     * Scheduler used for duration rules.
     *
     * @param handler
     */
    void setAsyncExceptionHandler(AsyncExceptionHandler handler);

    /**
     * Clear the Agenda. Iterates over each AgendaGroup cancalling all Activations.
     */
    void clearAgenda();

    /**
     * Clear the Agenda Group, cancelling all its Activations.
     */
    public void clearAgendaGroup(String group);

    /**
     * Clears the Activation Group, cancellings all its Activations
     * @param group
     */
    public void clearActivationGroup(String group);

    /**
     * Clears the RuleFlow group, cancelling all its Activations
     * @param group
     */
    public void clearRuleFlowGroup(String group);

    /**
     * Starts a new process instance for the process with the given id.
     */
    ProcessInstance startProcess(String processId);

    /**
     * Starts a new process instance for the process with the given id.
     */
    ProcessInstance startProcess(String processId, Map<String, Object> parameters);

    /**
     * Returns the list of process instances of this working memory.
     * This list is unmodifiable.
     * @return the list of process instances
     */
    public Collection<ProcessInstance> getProcessInstances();

    /**
     * Returns the process instance with the given id.
     * @return the process instance with the given id
     */
    public ProcessInstance getProcessInstance(long id);

    public WorkItemManager getWorkItemManager();

    /**
     * Stops rule firing after the current rule finishes executing
     */
    public void halt();

    /**
     * Returns the interface instance for a given entry point, so
     * that the application can manage entry-point-scoped facts.
     *
     * @param id the id of the entry point, as defined in the rules file
     * @return
     */
    public WorkingMemoryEntryPoint getWorkingMemoryEntryPoint( String id );
    
    /**
     * Returns the session clock instance associated with this session
     * @return
     */
    public SessionClock getSessionClock();
    
}
