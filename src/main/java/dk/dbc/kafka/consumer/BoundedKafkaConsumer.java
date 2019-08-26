/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.md
 */

package dk.dbc.kafka.consumer;

import dk.dbc.kafka.logformat.LogEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class BoundedKafkaConsumer extends KafkaConsumer {
    private final long untilEpochMilli;

    public BoundedKafkaConsumer(String hostname, Integer port, String topicName,
                                String groupId, String offset, String clientID,
                                long untilEpochMilli) {
        super(hostname, port, topicName, groupId, offset, clientID);
        this.untilEpochMilli = untilEpochMilli;
    }

    @Override
    public Iterator<LogEvent> iterator() {
        return new Iterator<LogEvent>() {
            final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka =
                    new org.apache.kafka.clients.consumer.KafkaConsumer<>(kafkaProperties);
            final ConsumerRecordRange recordRange;
            {
                recordRange = new ConsumerRecordRange(kafka, topicName, fromEpochMilli, untilEpochMilli);
            }

            final PriorityQueue<ConsumedItem> consumedItems =
                    new PriorityQueue<>(1200, new ConsumedItemComparator());

            @Override
            public boolean hasNext() {
                if (consumedItems.isEmpty() || consumedItems.size() < 400) {
                    consumeItems();
                }
                return !consumedItems.isEmpty();
            }

            @Override
            public LogEvent next() {
                final ConsumedItem consumedItem = consumedItems.poll();
                if (consumedItem != null) {
                    return consumedItem.toLogEvent();
                }
                return null;
            }

            private void consumeItems() {
                if (recordRange.isExhausted()) {
                    // All offsets within range have already been consumed.
                    return;
                }

                do {
                    // Attempt to read more records from kafka.
                    final ConsumerRecords<String, byte[]> consumerRecords = poll();
                    for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                        if (recordRange.isInRange(record)) {
                            // Only allow records whose offsets are within range.
                            consumedItems.add(new ConsumedItem(record));
                        }
                    }
                    // Keep trying if all records were rejected by the offset filtering.
                } while (consumedItems.isEmpty());
            }

            private ConsumerRecords<String, byte[]> poll() {
                return kafka.poll(Duration.ofMillis(3000));
            }
        };
    }

    private class ConsumerRecordRange {
        private final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka;
        private final List<PartitionRange> partitionRanges;

        ConsumerRecordRange(
                final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> kafka,
                final String topicName,
                Long fromEpochMilli,
                Long untilEpochMilli) {

            if (fromEpochMilli == null) {
                // Read from the beginning of each partition.
                fromEpochMilli = 0L;
            }

            if (fromEpochMilli >= untilEpochMilli) {
                throw new IllegalArgumentException("'until' bound must be a timestamp later than 'from'");
            }

            this.kafka = kafka;

            // Get all partitions for topic.
            final List<PartitionInfo> topicPartitionInfo = kafka.partitionsFor(topicName);

            // Look up the offsets for all partitions such that the returned offset for
            // each partition is the earliest offset whose timestamp is greater than or
            // equal to the given fromEpochMilli.
            final Map<TopicPartition, Long> fromOffsets =
                    getOffsetsForTimestamp(kafka, topicName, topicPartitionInfo, fromEpochMilli);
            System.err.println("from offsets: " + fromOffsets);

            // Find upper bound offsets by looking up offsets for all partitions
            // such that the returned offset for each partition is the earliest offset
            // whose timestamp is greater than or equal to the given untilEpochMilli.
            Map<TopicPartition, Long> untilOffsets =
                    getOffsetsForTimestamp(kafka, topicName, topicPartitionInfo, untilEpochMilli);
            System.err.println("until offsets: " + untilOffsets);

            partitionRanges = getPartitionRanges(fromOffsets, untilOffsets);

            System.err.println("partition ranges: " + partitionRanges);

            if (!isExhausted()) {
                seekToOffset(partitionRanges.get(0));
            }
        }

        private List<PartitionRange> getPartitionRanges(
                Map<TopicPartition, Long> fromOffsets,
                Map<TopicPartition, Long> untilOffsets) {

            final List<PartitionRange> partitionRanges = new ArrayList<>();
            for (Map.Entry<TopicPartition, Long> entry : fromOffsets.entrySet()) {
                final TopicPartition topicPartition = entry.getKey();
                final Long fromOffset = entry.getValue();
                final Long untilOffset = untilOffsets.get(topicPartition);
                if (untilOffset != null) {
                    partitionRanges.add(new PartitionRange(topicPartition, fromOffset, untilOffset));
                } else {
                    // No untilOffsets entry exists for the partition, therefore
                    // all records from the start offset determined by fromEpochMilli
                    // and onwards should be included. Lookup and use the last
                    // committed offset as the upper bound.
                    final Long lastCommittedOffset = getLastCommittedOffset(topicPartition);
                    if (lastCommittedOffset != null) {
                        partitionRanges.add(new PartitionRange(
                                topicPartition, fromOffset, lastCommittedOffset));
                    }
                    System.err.println("getting last committed offset: " + lastCommittedOffset);
                }
            }
            return partitionRanges;
        }

        private Long getLastCommittedOffset(TopicPartition topicPartition) {
            final OffsetAndMetadata lastCommittedOffsetAndMetadata = kafka.committed(topicPartition);
            if (lastCommittedOffsetAndMetadata == null) {
                return null;
            }
            return lastCommittedOffsetAndMetadata.offset();
        }

        boolean isInRange(ConsumerRecord<String, byte[]> record) {
            final PartitionRange partitionRange = partitionRanges.get(0);
            if (partitionRange.getPartitionId() != record.partition()) {
                // Partition is no longer registered amongst the partition ranges,
                // which indicates that the partition has been exhausted.
                return false;
            }
            if (record.offset() >= partitionRange.getUntilOffset()) {
                // Upper bound is reached for the partitioner,
                // remove its partition range entry.
                partitionRanges.remove(0);
                if (!isExhausted()) {
                    // Go to next partition range.
                    seekToOffset(partitionRanges.get(0));
                }
                return false;
            }
            return true;
        }

        boolean isExhausted() {
            // This range is exhausted if no more partition ranges remain.
            return partitionRanges.isEmpty();
        }

         private void seekToOffset(final PartitionRange partitionRange) {
            kafka.assign(Collections.singletonList(partitionRange.getTopicPartition()));
            kafka.seek(partitionRange.getTopicPartition(), partitionRange.getFromOffset());
        }
    }

    private static class PartitionRange {
        final TopicPartition topicPartition;
        final long fromOffset;
        final long untilOffset;

        PartitionRange(TopicPartition topicPartition, long fromOffset, long untilOffset) {
            this.topicPartition = topicPartition;
            this.fromOffset = fromOffset;
            this.untilOffset = untilOffset;
        }

        TopicPartition getTopicPartition() {
            return topicPartition;
        }

        int getPartitionId() {
            return topicPartition.partition();
        }

        long getFromOffset() {
            return fromOffset;
        }

        long getUntilOffset() {
            return untilOffset;
        }

        @Override
        public String toString() {
            return "PartitionRange{" +
                    "partition=" + topicPartition +
                    ", fromOffset=" + fromOffset +
                    ", untilOffset=" + untilOffset +
                    '}';
        }
    }
}