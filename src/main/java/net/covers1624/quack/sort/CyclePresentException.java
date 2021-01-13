/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.covers1624.quack.sort;

import java.util.Set;

/**
 * Borrowed from MinecraftForge, Functionally un-modified only formatted.
 * https://github.com/MinecraftForge/MinecraftForge/pull/5319
 * <p>
 * An exception thrown for graphs with cycles as an argument for topological sort.
 */
public final class CyclePresentException extends IllegalArgumentException {

    private final Set<Set<?>> cycles;

    /**
     * Creates the exception.
     *
     * @param cycles the cycles present
     */
    CyclePresentException(Set<Set<?>> cycles) {
        this.cycles = cycles;
    }

    /**
     * Accesses the cycles present in the sorted graph.
     *
     * <p>Each element in the outer set represents a cycle; each cycle, or the inner set,
     * forms a strongly connected component with two or more elements.
     *
     * @param <T> the type of node sorted
     * @return the cycles identified
     */
    @SuppressWarnings ("unchecked")
    public <T> Set<Set<T>> getCycles() {
        return (Set<Set<T>>) (Set<?>) cycles;
    }
}
