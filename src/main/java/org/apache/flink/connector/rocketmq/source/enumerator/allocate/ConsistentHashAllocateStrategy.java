/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.connector.rocketmq.source.enumerator.allocate;

import org.apache.flink.connector.rocketmq.source.split.RocketMQSourceSplit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConsistentHashAllocateStrategy implements AllocateStrategy {

    @Override
    public String getStrategyName() {
        return AllocateStrategyFactory.STRATEGY_NAME_CONSISTENT_HASH;
    }

    /** Returns the index of the target subtask that a specific queue should be assigned to. */
    private int getSplitOwner(RocketMQSourceSplit sourceSplit, int parallelism) {
        int startIndex =
                ((sourceSplit.getMessageQueue().hashCode() * 31) & Integer.MAX_VALUE) % parallelism;
        return startIndex % parallelism;
    }

    @Override
    public Map<Integer, Set<RocketMQSourceSplit>> allocate(
            final Collection<RocketMQSourceSplit> mqAll, final int parallelism) {
        Map<Integer, Set<RocketMQSourceSplit>> result = new HashMap<>();
        for (RocketMQSourceSplit mq : mqAll) {
            int readerIndex = this.getSplitOwner(mq, parallelism);
            result.computeIfAbsent(readerIndex, k -> new HashSet<>()).add(mq);
        }
        return result;
    }

    @Override
    public Map<Integer, Set<RocketMQSourceSplit>> allocate(
            Collection<RocketMQSourceSplit> mqAll, int parallelism, int globalAssignedNumber) {
        return allocate(mqAll, parallelism);
    }
}
