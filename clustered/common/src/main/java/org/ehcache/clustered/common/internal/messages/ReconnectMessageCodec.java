/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ehcache.clustered.common.internal.messages;

import org.terracotta.runnel.Struct;
import org.terracotta.runnel.decoding.ArrayDecoder;
import org.terracotta.runnel.decoding.StructDecoder;
import org.terracotta.runnel.encoding.ArrayEncoder;
import org.terracotta.runnel.encoding.StructEncoder;

import java.util.HashSet;
import java.util.Set;

import static java.nio.ByteBuffer.wrap;
import static org.terracotta.runnel.StructBuilder.newStructBuilder;

public class ReconnectMessageCodec {

  private static final String HASH_INVALIDATION_IN_PROGRESS_FIELD = "hashInvalidationInProgress";
  private static final String CLEAR_IN_PROGRESS_FIELD = "clearInProgress";

  private static final Struct RECONNECT_MESSAGE_STRUCT = newStructBuilder()
    .int64s(HASH_INVALIDATION_IN_PROGRESS_FIELD, 20)
    .bool(CLEAR_IN_PROGRESS_FIELD, 30)
    .build();

  private static final Struct MANAGER_RECONNECT_MESSAGE_STRUCT = newStructBuilder()
    .build();

  public byte[] encode(ClusterTierReconnectMessage reconnectMessage) {
    StructEncoder<Void> encoder = RECONNECT_MESSAGE_STRUCT.encoder();
    ArrayEncoder<Long, StructEncoder<Void>> arrayEncoder = encoder.int64s(HASH_INVALIDATION_IN_PROGRESS_FIELD);
    for (Long hash : reconnectMessage.getInvalidationsInProgress()) {
      arrayEncoder.value(hash);
    }
    encoder.bool(CLEAR_IN_PROGRESS_FIELD, reconnectMessage.isClearInProgress());
    return encoder.encode().array();
  }

  public byte[] encode(ClusterTierManagerReconnectMessage reconnectMessage) {
    return MANAGER_RECONNECT_MESSAGE_STRUCT.encoder().encode().array();
  }

  public ClusterTierManagerReconnectMessage decodeReconnectMessage(byte[] payload) {
    return new ClusterTierManagerReconnectMessage();
  }

  public ClusterTierReconnectMessage decode(byte[] payload) {
    StructDecoder<Void> decoder = RECONNECT_MESSAGE_STRUCT.decoder(wrap(payload));
    ArrayDecoder<Long, StructDecoder<Void>> arrayDecoder = decoder.int64s(HASH_INVALIDATION_IN_PROGRESS_FIELD);
    Set<Long> hashes = new HashSet<Long>();
    if (arrayDecoder != null) {
      for (int i = 0; i < arrayDecoder.length(); i++) {
        hashes.add(arrayDecoder.value());
      }
    }
    Boolean clearInProgress = decoder.bool(CLEAR_IN_PROGRESS_FIELD);

    ClusterTierReconnectMessage message = new ClusterTierReconnectMessage();
    message.addInvalidationsInProgress(hashes);
    if (clearInProgress != null && clearInProgress) {
      message.clearInProgress();
    }
    return message;
  }
}
